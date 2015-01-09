package old.org.bouncycastle.cms.jcajce;

import java.io.OutputStream;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.jcajce.DefaultJcaJceHelper;
import old.org.bouncycastle.jcajce.NamedJcaJceHelper;
import old.org.bouncycastle.jcajce.ProviderJcaJceHelper;
import old.org.bouncycastle.operator.GenericKey;
import old.org.bouncycastle.operator.OutputEncryptor;

public class JceCMSContentEncryptorBuilder
{
    private final ASN1ObjectIdentifier encryptionOID;
    private final int                  keySize;

    private EnvelopedDataHelper helper = new EnvelopedDataHelper(new DefaultJcaJceHelper());
    private SecureRandom random;

    public JceCMSContentEncryptorBuilder(ASN1ObjectIdentifier encryptionOID)
    {
        this(encryptionOID, -1);
    }

    public JceCMSContentEncryptorBuilder(ASN1ObjectIdentifier encryptionOID, int keySize)
    {
        this.encryptionOID = encryptionOID;
        this.keySize = keySize;
    }

    public JceCMSContentEncryptorBuilder setProvider(Provider provider)
    {
        this.helper = new EnvelopedDataHelper(new ProviderJcaJceHelper(provider));

        return this;
    }

    public JceCMSContentEncryptorBuilder setProvider(String providerName)
    {
        this.helper = new EnvelopedDataHelper(new NamedJcaJceHelper(providerName));

        return this;
    }

    public JceCMSContentEncryptorBuilder setSecureRandom(SecureRandom random)
    {
        this.random = random;

        return this;
    }

    public OutputEncryptor build()
        throws CMSException
    {
        return new CMSOutputEncryptor(encryptionOID, keySize, random);
    }

    private class CMSOutputEncryptor
        implements OutputEncryptor
    {
        private SecretKey encKey;
        private AlgorithmIdentifier algorithmIdentifier;
        private Cipher              cipher;

        CMSOutputEncryptor(ASN1ObjectIdentifier encryptionOID, int keySize, SecureRandom random)
            throws CMSException
        {
            KeyGenerator keyGen = helper.createKeyGenerator(encryptionOID);

            if (random == null)
            {
                random = new SecureRandom();
            }

            if (keySize < 0)
            {
                keyGen.init(random);
            }
            else
            {
                keyGen.init(keySize, random);
            }

            cipher = helper.createCipher(encryptionOID);
            encKey = keyGen.generateKey();
            AlgorithmParameters params = helper.generateParameters(encryptionOID, encKey, random);

            try
            {
                cipher.init(Cipher.ENCRYPT_MODE, encKey, params, random);
            }
            catch (GeneralSecurityException e)
            {
                throw new CMSException("unable to initialize cipher: " + e.getMessage(), e);
            }

            //
            // If params are null we try and second guess on them as some providers don't provide
            // algorithm parameter generation explicity but instead generate them under the hood.
            //
            if (params == null)
            {
                params = cipher.getParameters();
            }

            algorithmIdentifier = helper.getAlgorithmIdentifier(encryptionOID, params);
        }

        public AlgorithmIdentifier getAlgorithmIdentifier()
        {
            return algorithmIdentifier;
        }

        public OutputStream getOutputStream(OutputStream dOut)
        {
            return new CipherOutputStream(dOut, cipher);
        }

        public GenericKey getKey()
        {
            return new GenericKey(encKey);
        }
    }
}
