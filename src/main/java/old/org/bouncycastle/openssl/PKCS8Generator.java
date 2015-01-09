package old.org.bouncycastle.openssl;

import java.io.IOException;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import old.org.bouncycastle.asn1.pkcs.EncryptionScheme;
import old.org.bouncycastle.asn1.pkcs.KeyDerivationFunc;
import old.org.bouncycastle.asn1.pkcs.PBES2Parameters;
import old.org.bouncycastle.asn1.pkcs.PBKDF2Params;
import old.org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.util.io.pem.PemGenerationException;
import old.org.bouncycastle.util.io.pem.PemObject;
import old.org.bouncycastle.util.io.pem.PemObjectGenerator;

public class PKCS8Generator
    implements PemObjectGenerator
{
    public static final String AES_128_CBC = NISTObjectIdentifiers.id_aes128_CBC.getId();
    public static final String AES_192_CBC = NISTObjectIdentifiers.id_aes192_CBC.getId();
    public static final String AES_256_CBC = NISTObjectIdentifiers.id_aes256_CBC.getId();

    public static final String DES3_CBC = PKCSObjectIdentifiers.des_EDE3_CBC.getId();

    public static final String PBE_SHA1_RC4_128 = PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC4.getId();
    public static final String PBE_SHA1_RC4_40 = PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC4.getId();
    public static final String PBE_SHA1_3DES = PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC.getId();
    public static final String PBE_SHA1_2DES = PKCSObjectIdentifiers.pbeWithSHAAnd2_KeyTripleDES_CBC.getId();
    public static final String PBE_SHA1_RC2_128 = PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC2_CBC.getId();
    public static final String PBE_SHA1_RC2_40 = PKCSObjectIdentifiers.pbewithSHAAnd40BitRC2_CBC.getId();

    private char[] password;
    private String algorithm;
    private int iterationCount;
    private PrivateKey key;
    private Cipher cipher;
    private SecureRandom random;
    private AlgorithmParameterGenerator paramGen;
    private SecretKeyFactory secKeyFact;

    /**
     * Constructor for an unencrypted private key PEM object.
     *
     * @param key private key to be encoded.
     */
    public PKCS8Generator(PrivateKey key)
    {
        this.key = key;
    }

    /**
     * Constructor for an encrypted private key PEM object.
     *
     * @param key       private key to be encoded
     * @param algorithm encryption algorithm to use
     * @param provider  name of provider to use
     * @throws NoSuchProviderException  if provider cannot be found
     * @throws NoSuchAlgorithmException if algorithm/mode cannot be found
     */
    public PKCS8Generator(PrivateKey key, String algorithm, String provider)
        throws NoSuchProviderException, NoSuchAlgorithmException
    {
        Provider prov = Security.getProvider(provider);

        if (prov == null)
        {
            throw new NoSuchProviderException("cannot find provider: " + provider);
        }

        init(key, algorithm, prov);
    }

    /**
     * Constructor for an encrypted private key PEM object.
     *
     * @param key       private key to be encoded
     * @param algorithm encryption algorithm to use
     * @param provider  provider to use
     * @throws NoSuchAlgorithmException if algorithm/mode cannot be found
     */
    public PKCS8Generator(PrivateKey key, String algorithm, Provider provider)
        throws NoSuchAlgorithmException
    {
        init(key, algorithm, provider);
    }

    private void init(PrivateKey key, String algorithm, Provider provider)
        throws NoSuchAlgorithmException
    {
        this.key = key;
        this.algorithm = algorithm;
        this.iterationCount = 2048;

        try
        {
            this.cipher = Cipher.getInstance(algorithm, provider);
        }
        catch (NoSuchPaddingException e)
        {
            throw new NoSuchAlgorithmException(algorithm + " found, but padding not available: " + e.getMessage());
        }
        DERObjectIdentifier algOID = new DERObjectIdentifier(algorithm);

        if (PEMUtilities.isPKCS5Scheme2(algOID))
        {
            this.paramGen = AlgorithmParameterGenerator.getInstance(algorithm, provider);
        }
        else
        {
            this.secKeyFact = SecretKeyFactory.getInstance(algorithm, provider);
        }
    }

    public PKCS8Generator setSecureRandom(SecureRandom random)
    {
        this.random = random;

        return this;
    }

    public PKCS8Generator setPassword(char[] password)
    {
        this.password = password;

        return this;
    }

    public PKCS8Generator setIterationCount(int iterationCount)
    {
        this.iterationCount = iterationCount;

        return this;
    }

    public PemObject generate()
        throws PemGenerationException
    {
        byte[] keyData = key.getEncoded();

        if (algorithm == null)
        {
            return new PemObject("PRIVATE KEY", keyData);
        }

        DERObjectIdentifier algOID = new DERObjectIdentifier(algorithm);

        if (PEMUtilities.isPKCS5Scheme2(algOID))
        {
            byte[] salt = new byte[20];

            if (random == null)
            {
                random = new SecureRandom();
            }

            random.nextBytes(salt);

            SecretKey key = PEMUtilities.generateSecretKeyForPKCS5Scheme2(algorithm, password, salt, iterationCount);

            AlgorithmParameters params = paramGen.generateParameters();

            try
            {
                cipher.init(Cipher.ENCRYPT_MODE, key, params);

                EncryptionScheme scheme = new EncryptionScheme(new DERObjectIdentifier(algorithm), ASN1Object.fromByteArray(params.getEncoded()));
                KeyDerivationFunc func = new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(salt, iterationCount));

                ASN1EncodableVector v = new ASN1EncodableVector();

                v.add(func);
                v.add(scheme);

                EncryptedPrivateKeyInfo info = new EncryptedPrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, new PBES2Parameters(new DERSequence(v))), cipher.doFinal(keyData));

                return new PemObject("ENCRYPTED PRIVATE KEY", info.getEncoded());
            }
            catch (IOException e)
            {
                throw new PemGenerationException(e.getMessage(), e);
            }
            catch (GeneralSecurityException e)
            {
                throw new PemGenerationException(e.getMessage(), e);
            }
        }
        else if (PEMUtilities.isPKCS12(algOID))
        {
            byte[] salt = new byte[20];

            if (random == null)
            {
                random = new SecureRandom();
            }

            random.nextBytes(salt);

            try
            {
                PBEKeySpec pbeSpec = new PBEKeySpec(password);
                PBEParameterSpec defParams = new PBEParameterSpec(salt, iterationCount);

                cipher.init(Cipher.ENCRYPT_MODE, secKeyFact.generateSecret(pbeSpec), defParams);

                ASN1EncodableVector v = new ASN1EncodableVector();

                v.add(new DEROctetString(salt));
                v.add(new DERInteger(iterationCount));

                EncryptedPrivateKeyInfo info = new EncryptedPrivateKeyInfo(new AlgorithmIdentifier(algOID, new PKCS12PBEParams(new DERSequence(v))), cipher.doFinal(keyData));

                return new PemObject("ENCRYPTED PRIVATE KEY", info.getEncoded());
            }
            catch (IOException e)
            {
                throw new PemGenerationException(e.getMessage(), e);
            }
            catch (GeneralSecurityException e)
            {
                throw new PemGenerationException(e.getMessage(), e);
            }
        }
        else
        {
            throw new PemGenerationException("unknown algorithm: " + algorithm);
        }
    }
}
