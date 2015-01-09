package old.org.bouncycastle.operator.jcajce;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.Provider;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import old.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import old.org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.jcajce.DefaultJcaJceHelper;
import old.org.bouncycastle.jcajce.NamedJcaJceHelper;
import old.org.bouncycastle.jcajce.ProviderJcaJceHelper;
import old.org.bouncycastle.operator.GenericKey;
import old.org.bouncycastle.operator.OperatorException;
import old.org.bouncycastle.operator.SymmetricKeyWrapper;

public class JceSymmetricKeyWrapper
    extends SymmetricKeyWrapper
{
    private OperatorHelper helper = new OperatorHelper(new DefaultJcaJceHelper());
    private SecureRandom random;
    private SecretKey wrappingKey;

    public JceSymmetricKeyWrapper(SecretKey wrappingKey)
    {
        super(determineKeyEncAlg(wrappingKey));

        this.wrappingKey = wrappingKey;
    }

    public JceSymmetricKeyWrapper setProvider(Provider provider)
    {
        this.helper = new OperatorHelper(new ProviderJcaJceHelper(provider));

        return this;
    }

    public JceSymmetricKeyWrapper setProvider(String providerName)
    {
        this.helper = new OperatorHelper(new NamedJcaJceHelper(providerName));

        return this;
    }

    public JceSymmetricKeyWrapper setSecureRandom(SecureRandom random)
    {
        this.random = random;

        return this;
    }

    public byte[] generateWrappedKey(GenericKey encryptionKey)
        throws OperatorException
    {
        Key contentEncryptionKeySpec = OperatorUtils.getJceKey(encryptionKey);

        Cipher keyEncryptionCipher = helper.createSymmetricWrapper(this.getAlgorithmIdentifier().getAlgorithm());

        try
        {
            keyEncryptionCipher.init(Cipher.WRAP_MODE, wrappingKey, random);

            return keyEncryptionCipher.wrap(contentEncryptionKeySpec);
        }
        catch (GeneralSecurityException e)
        {
            throw new OperatorException("cannot wrap key: " + e.getMessage(), e);
        }
    }

    private static AlgorithmIdentifier determineKeyEncAlg(SecretKey key)
    {
        String algorithm = key.getAlgorithm();

        if (algorithm.startsWith("DES"))
        {
            return new AlgorithmIdentifier(new DERObjectIdentifier(
                    "1.2.840.113549.1.9.16.3.6"), new DERNull());
        }
        else if (algorithm.startsWith("RC2"))
        {
            return new AlgorithmIdentifier(new DERObjectIdentifier(
                    "1.2.840.113549.1.9.16.3.7"), new DERInteger(58));
        }
        else if (algorithm.startsWith("AES"))
        {
            int length = key.getEncoded().length * 8;
            DERObjectIdentifier wrapOid;

            if (length == 128)
            {
                wrapOid = NISTObjectIdentifiers.id_aes128_wrap;
            }
            else if (length == 192)
            {
                wrapOid = NISTObjectIdentifiers.id_aes192_wrap;
            }
            else if (length == 256)
            {
                wrapOid = NISTObjectIdentifiers.id_aes256_wrap;
            }
            else
            {
                throw new IllegalArgumentException("illegal keysize in AES");
            }

            return new AlgorithmIdentifier(wrapOid); // parameters absent
        }
        else if (algorithm.startsWith("SEED"))
        {
            // parameters absent
            return new AlgorithmIdentifier(
                    KISAObjectIdentifiers.id_npki_app_cmsSeed_wrap);
        }
        else if (algorithm.startsWith("Camellia"))
        {
            int length = key.getEncoded().length * 8;
            DERObjectIdentifier wrapOid;

            if (length == 128)
            {
                wrapOid = NTTObjectIdentifiers.id_camellia128_wrap;
            }
            else if (length == 192)
            {
                wrapOid = NTTObjectIdentifiers.id_camellia192_wrap;
            }
            else if (length == 256)
            {
                wrapOid = NTTObjectIdentifiers.id_camellia256_wrap;
            }
            else
            {
                throw new IllegalArgumentException(
                        "illegal keysize in Camellia");
            }

            return new AlgorithmIdentifier(wrapOid); // parameters must be
                                                     // absent
        }
        else
        {
            throw new IllegalArgumentException("unknown algorithm");
        }
    }
}
