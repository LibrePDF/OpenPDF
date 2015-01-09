package old.org.bouncycastle.cert.crmf.jcajce;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.cert.crmf.CRMFException;
import old.org.bouncycastle.cert.crmf.ValueDecryptorGenerator;
import old.org.bouncycastle.jcajce.DefaultJcaJceHelper;
import old.org.bouncycastle.jcajce.NamedJcaJceHelper;
import old.org.bouncycastle.jcajce.ProviderJcaJceHelper;
import old.org.bouncycastle.operator.InputDecryptor;

public class JceAsymmetricValueDecryptorGenerator
    implements ValueDecryptorGenerator
{
    private PrivateKey recipientKey;
    private CRMFHelper helper = new CRMFHelper(new DefaultJcaJceHelper());

    public JceAsymmetricValueDecryptorGenerator(PrivateKey recipientKey)
    {
        this.recipientKey = recipientKey;
    }

    public JceAsymmetricValueDecryptorGenerator setProvider(Provider provider)
    {
        this.helper = new CRMFHelper(new ProviderJcaJceHelper(provider));

        return this;
    }

    public JceAsymmetricValueDecryptorGenerator setProvider(String providerName)
    {
        this.helper = new CRMFHelper(new NamedJcaJceHelper(providerName));

        return this;
    }

    private Key extractSecretKey(AlgorithmIdentifier keyEncryptionAlgorithm, AlgorithmIdentifier contentEncryptionAlgorithm, byte[] encryptedContentEncryptionKey)
        throws CRMFException
    {
        try
        {
            Key sKey = null;

            Cipher keyCipher = helper.createCipher(keyEncryptionAlgorithm.getAlgorithm());

            try
            {
                keyCipher.init(Cipher.UNWRAP_MODE, recipientKey);
                sKey = keyCipher.unwrap(encryptedContentEncryptionKey, contentEncryptionAlgorithm.getAlgorithm().getId(), Cipher.SECRET_KEY);
            }
            catch (GeneralSecurityException e)
            {
            }
            catch (IllegalStateException e)
            {
            }
            catch (UnsupportedOperationException e)
            {
            }
            catch (ProviderException e)
            {
            }

            // some providers do not support UNWRAP (this appears to be only for asymmetric algorithms)
            if (sKey == null)
            {
                keyCipher.init(Cipher.DECRYPT_MODE, recipientKey);
                sKey = new SecretKeySpec(keyCipher.doFinal(encryptedContentEncryptionKey), contentEncryptionAlgorithm.getAlgorithm().getId());
            }

            return sKey;
        }
        catch (InvalidKeyException e)
        {
            throw new CRMFException("key invalid in message.", e);
        }
        catch (IllegalBlockSizeException e)
        {
            throw new CRMFException("illegal blocksize in message.", e);
        }
        catch (BadPaddingException e)
        {
            throw new CRMFException("bad padding in message.", e);
        }
    }

    public InputDecryptor getValueDecryptor(AlgorithmIdentifier keyEncryptionAlgorithm, final AlgorithmIdentifier contentEncryptionAlgorithm, byte[] encryptedContentEncryptionKey)
        throws CRMFException
    {
        Key secretKey = extractSecretKey(keyEncryptionAlgorithm, contentEncryptionAlgorithm, encryptedContentEncryptionKey);

        final Cipher dataCipher = helper.createContentCipher(secretKey, contentEncryptionAlgorithm);

        return new InputDecryptor()
        {
            public AlgorithmIdentifier getAlgorithmIdentifier()
            {
                return contentEncryptionAlgorithm;
            }

            public InputStream getInputStream(InputStream dataIn)
            {
                return new CipherInputStream(dataIn, dataCipher);
            }
        };
    }
}
