package old.org.bouncycastle.operator.jcajce;

import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;

import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.jcajce.DefaultJcaJceHelper;
import old.org.bouncycastle.jcajce.NamedJcaJceHelper;
import old.org.bouncycastle.jcajce.ProviderJcaJceHelper;
import old.org.bouncycastle.operator.AsymmetricKeyWrapper;
import old.org.bouncycastle.operator.GenericKey;
import old.org.bouncycastle.operator.OperatorException;

public class JceAsymmetricKeyWrapper
    extends AsymmetricKeyWrapper
{
    private OperatorHelper helper = new OperatorHelper(new DefaultJcaJceHelper());
    private PublicKey publicKey;
    private SecureRandom random;

    public JceAsymmetricKeyWrapper(PublicKey publicKey)
    {
        super(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()).getAlgorithmId());

        this.publicKey = publicKey;
    }

    public JceAsymmetricKeyWrapper(X509Certificate certificate)
    {
        this(certificate.getPublicKey());
    }

    public JceAsymmetricKeyWrapper setProvider(Provider provider)
    {
        this.helper = new OperatorHelper(new ProviderJcaJceHelper(provider));

        return this;
    }

    public JceAsymmetricKeyWrapper setProvider(String providerName)
    {
        this.helper = new OperatorHelper(new NamedJcaJceHelper(providerName));

        return this;
    }

    public JceAsymmetricKeyWrapper setSecureRandom(SecureRandom random)
    {
        this.random = random;

        return this;
    }

    public byte[] generateWrappedKey(GenericKey encryptionKey)
        throws OperatorException
    {
        Cipher keyEncryptionCipher = helper.createAsymmetricWrapper(getAlgorithmIdentifier().getAlgorithm());
        byte[] encryptedKeyBytes = null;

        try
        {
            keyEncryptionCipher.init(Cipher.WRAP_MODE, publicKey, random);
            encryptedKeyBytes = keyEncryptionCipher.wrap(OperatorUtils.getJceKey(encryptionKey));
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

        // some providers do not support WRAP (this appears to be only for asymmetric algorithms)
        if (encryptedKeyBytes == null)
        {
            try
            {
                keyEncryptionCipher.init(Cipher.ENCRYPT_MODE, publicKey, random);
                encryptedKeyBytes = keyEncryptionCipher.doFinal(OperatorUtils.getJceKey(encryptionKey).getEncoded());
            }
            catch (GeneralSecurityException e)
            {
                throw new OperatorException("unable to encrypt contents key", e);
            }
        }

        return encryptedKeyBytes;
    }
}
