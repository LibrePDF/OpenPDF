package old.org.bouncycastle.cms.jcajce;

import java.io.IOException;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;

import old.org.bouncycastle.asn1.ASN1Null;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.RC2CBCParameter;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.cms.CMSAlgorithm;
import old.org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.jcajce.JcaJceHelper;
import old.org.bouncycastle.operator.AsymmetricKeyUnwrapper;
import old.org.bouncycastle.operator.SymmetricKeyUnwrapper;

class EnvelopedDataHelper
{
    protected static final Map BASE_CIPHER_NAMES = new HashMap();
    protected static final Map CIPHER_ALG_NAMES = new HashMap();
    protected static final Map MAC_ALG_NAMES = new HashMap();

    static
    {
        BASE_CIPHER_NAMES.put(CMSAlgorithm.DES_EDE3_CBC,  "DESEDE");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.AES128_CBC,  "AES");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.AES192_CBC,  "AES");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.AES256_CBC,  "AES");

        CIPHER_ALG_NAMES.put(CMSAlgorithm.DES_EDE3_CBC,  "DESEDE/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.AES128_CBC,  "AES/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.AES192_CBC,  "AES/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.AES256_CBC,  "AES/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(new ASN1ObjectIdentifier(PKCSObjectIdentifiers.rsaEncryption.getId()), "RSA/ECB/PKCS1Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.CAST5_CBC, "CAST5/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.CAMELLIA128_CBC, "Camellia/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.CAMELLIA192_CBC, "Camellia/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.CAMELLIA256_CBC, "Camellia/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.SEED_CBC, "SEED/CBC/PKCS5Padding");

        MAC_ALG_NAMES.put(CMSAlgorithm.DES_EDE3_CBC,  "DESEDEMac");
        MAC_ALG_NAMES.put(CMSAlgorithm.AES128_CBC,  "AESMac");
        MAC_ALG_NAMES.put(CMSAlgorithm.AES192_CBC,  "AESMac");
        MAC_ALG_NAMES.put(CMSAlgorithm.AES256_CBC,  "AESMac");
        MAC_ALG_NAMES.put(CMSAlgorithm.RC2_CBC,  "RC2Mac");
    }

    private static final short[] rc2Table = {
        0xbd, 0x56, 0xea, 0xf2, 0xa2, 0xf1, 0xac, 0x2a, 0xb0, 0x93, 0xd1, 0x9c, 0x1b, 0x33, 0xfd, 0xd0,
        0x30, 0x04, 0xb6, 0xdc, 0x7d, 0xdf, 0x32, 0x4b, 0xf7, 0xcb, 0x45, 0x9b, 0x31, 0xbb, 0x21, 0x5a,
        0x41, 0x9f, 0xe1, 0xd9, 0x4a, 0x4d, 0x9e, 0xda, 0xa0, 0x68, 0x2c, 0xc3, 0x27, 0x5f, 0x80, 0x36,
        0x3e, 0xee, 0xfb, 0x95, 0x1a, 0xfe, 0xce, 0xa8, 0x34, 0xa9, 0x13, 0xf0, 0xa6, 0x3f, 0xd8, 0x0c,
        0x78, 0x24, 0xaf, 0x23, 0x52, 0xc1, 0x67, 0x17, 0xf5, 0x66, 0x90, 0xe7, 0xe8, 0x07, 0xb8, 0x60,
        0x48, 0xe6, 0x1e, 0x53, 0xf3, 0x92, 0xa4, 0x72, 0x8c, 0x08, 0x15, 0x6e, 0x86, 0x00, 0x84, 0xfa,
        0xf4, 0x7f, 0x8a, 0x42, 0x19, 0xf6, 0xdb, 0xcd, 0x14, 0x8d, 0x50, 0x12, 0xba, 0x3c, 0x06, 0x4e,
        0xec, 0xb3, 0x35, 0x11, 0xa1, 0x88, 0x8e, 0x2b, 0x94, 0x99, 0xb7, 0x71, 0x74, 0xd3, 0xe4, 0xbf,
        0x3a, 0xde, 0x96, 0x0e, 0xbc, 0x0a, 0xed, 0x77, 0xfc, 0x37, 0x6b, 0x03, 0x79, 0x89, 0x62, 0xc6,
        0xd7, 0xc0, 0xd2, 0x7c, 0x6a, 0x8b, 0x22, 0xa3, 0x5b, 0x05, 0x5d, 0x02, 0x75, 0xd5, 0x61, 0xe3,
        0x18, 0x8f, 0x55, 0x51, 0xad, 0x1f, 0x0b, 0x5e, 0x85, 0xe5, 0xc2, 0x57, 0x63, 0xca, 0x3d, 0x6c,
        0xb4, 0xc5, 0xcc, 0x70, 0xb2, 0x91, 0x59, 0x0d, 0x47, 0x20, 0xc8, 0x4f, 0x58, 0xe0, 0x01, 0xe2,
        0x16, 0x38, 0xc4, 0x6f, 0x3b, 0x0f, 0x65, 0x46, 0xbe, 0x7e, 0x2d, 0x7b, 0x82, 0xf9, 0x40, 0xb5,
        0x1d, 0x73, 0xf8, 0xeb, 0x26, 0xc7, 0x87, 0x97, 0x25, 0x54, 0xb1, 0x28, 0xaa, 0x98, 0x9d, 0xa5,
        0x64, 0x6d, 0x7a, 0xd4, 0x10, 0x81, 0x44, 0xef, 0x49, 0xd6, 0xae, 0x2e, 0xdd, 0x76, 0x5c, 0x2f,
        0xa7, 0x1c, 0xc9, 0x09, 0x69, 0x9a, 0x83, 0xcf, 0x29, 0x39, 0xb9, 0xe9, 0x4c, 0xff, 0x43, 0xab
    };

    private static final short[] rc2Ekb = {
        0x5d, 0xbe, 0x9b, 0x8b, 0x11, 0x99, 0x6e, 0x4d, 0x59, 0xf3, 0x85, 0xa6, 0x3f, 0xb7, 0x83, 0xc5,
        0xe4, 0x73, 0x6b, 0x3a, 0x68, 0x5a, 0xc0, 0x47, 0xa0, 0x64, 0x34, 0x0c, 0xf1, 0xd0, 0x52, 0xa5,
        0xb9, 0x1e, 0x96, 0x43, 0x41, 0xd8, 0xd4, 0x2c, 0xdb, 0xf8, 0x07, 0x77, 0x2a, 0xca, 0xeb, 0xef,
        0x10, 0x1c, 0x16, 0x0d, 0x38, 0x72, 0x2f, 0x89, 0xc1, 0xf9, 0x80, 0xc4, 0x6d, 0xae, 0x30, 0x3d,
        0xce, 0x20, 0x63, 0xfe, 0xe6, 0x1a, 0xc7, 0xb8, 0x50, 0xe8, 0x24, 0x17, 0xfc, 0x25, 0x6f, 0xbb,
        0x6a, 0xa3, 0x44, 0x53, 0xd9, 0xa2, 0x01, 0xab, 0xbc, 0xb6, 0x1f, 0x98, 0xee, 0x9a, 0xa7, 0x2d,
        0x4f, 0x9e, 0x8e, 0xac, 0xe0, 0xc6, 0x49, 0x46, 0x29, 0xf4, 0x94, 0x8a, 0xaf, 0xe1, 0x5b, 0xc3,
        0xb3, 0x7b, 0x57, 0xd1, 0x7c, 0x9c, 0xed, 0x87, 0x40, 0x8c, 0xe2, 0xcb, 0x93, 0x14, 0xc9, 0x61,
        0x2e, 0xe5, 0xcc, 0xf6, 0x5e, 0xa8, 0x5c, 0xd6, 0x75, 0x8d, 0x62, 0x95, 0x58, 0x69, 0x76, 0xa1,
        0x4a, 0xb5, 0x55, 0x09, 0x78, 0x33, 0x82, 0xd7, 0xdd, 0x79, 0xf5, 0x1b, 0x0b, 0xde, 0x26, 0x21,
        0x28, 0x74, 0x04, 0x97, 0x56, 0xdf, 0x3c, 0xf0, 0x37, 0x39, 0xdc, 0xff, 0x06, 0xa4, 0xea, 0x42,
        0x08, 0xda, 0xb4, 0x71, 0xb0, 0xcf, 0x12, 0x7a, 0x4e, 0xfa, 0x6c, 0x1d, 0x84, 0x00, 0xc8, 0x7f,
        0x91, 0x45, 0xaa, 0x2b, 0xc2, 0xb1, 0x8f, 0xd5, 0xba, 0xf2, 0xad, 0x19, 0xb2, 0x67, 0x36, 0xf7,
        0x0f, 0x0a, 0x92, 0x7d, 0xe3, 0x9d, 0xe9, 0x90, 0x3e, 0x23, 0x27, 0x66, 0x13, 0xec, 0x81, 0x15,
        0xbd, 0x22, 0xbf, 0x9f, 0x7e, 0xa9, 0x51, 0x4b, 0x4c, 0xfb, 0x02, 0xd3, 0x70, 0x86, 0x31, 0xe7,
        0x3b, 0x05, 0x03, 0x54, 0x60, 0x48, 0x65, 0x18, 0xd2, 0xcd, 0x5f, 0x32, 0x88, 0x0e, 0x35, 0xfd
    };

    private JcaJceHelper        helper;

    EnvelopedDataHelper(JcaJceHelper helper)
    {
        this.helper = helper;
    }

    String getBaseCipherName(ASN1ObjectIdentifier algorithm)
    {
        String name = (String)BASE_CIPHER_NAMES.get(algorithm);

        if (name == null)
        {
            return algorithm.getId();
        }

        return name;
    }
    
    Cipher createCipher(ASN1ObjectIdentifier algorithm)
        throws CMSException
    {
        try
        {
            String cipherName = (String)CIPHER_ALG_NAMES.get(algorithm);

            if (cipherName != null)
            {
                try
                {
                    // this is reversed as the Sun policy files now allow unlimited strength RSA
                    return helper.createCipher(cipherName);
                }
                catch (NoSuchAlgorithmException e)
                {
                    // Ignore
                }
            }
            return helper.createCipher(algorithm.getId());
        }
        catch (GeneralSecurityException e)
        {
            throw new CMSException("cannot create cipher: " + e.getMessage(), e);
        }
    }

    Mac createMac(ASN1ObjectIdentifier algorithm)
        throws CMSException
    {
        try
        {
            String macName = (String)MAC_ALG_NAMES.get(algorithm);

            if (macName != null)
            {
                try
                {
                    // this is reversed as the Sun policy files now allow unlimited strength RSA
                    return helper.createMac(macName);
                }
                catch (NoSuchAlgorithmException e)
                {
                    // Ignore
                }
            }
            return helper.createMac(algorithm.getId());
        }
        catch (GeneralSecurityException e)
        {
            throw new CMSException("cannot create mac: " + e.getMessage(), e);
        }
    }

    Cipher createRFC3211Wrapper(ASN1ObjectIdentifier algorithm)
        throws CMSException
    {
        String cipherName = (String)BASE_CIPHER_NAMES.get(algorithm);

        if (cipherName == null)
        {
            throw new CMSException("no name for " + algorithm);
        }

        cipherName += "RFC3211Wrap";

        try
        {
             return helper.createCipher(cipherName);
        }
        catch (GeneralSecurityException e)
        {
            throw new CMSException("cannot create cipher: " + e.getMessage(), e);
        }
    }

    KeyAgreement createKeyAgreement(ASN1ObjectIdentifier algorithm)
        throws CMSException
    {
        try
        {
            String agreementName = (String)BASE_CIPHER_NAMES.get(algorithm);

            if (agreementName != null)
            {
                try
                {
                    // this is reversed as the Sun policy files now allow unlimited strength RSA
                    return helper.createKeyAgreement(agreementName);
                }
                catch (NoSuchAlgorithmException e)
                {
                    // Ignore
                }
            }
            return helper.createKeyAgreement(algorithm.getId());
        }
        catch (GeneralSecurityException e)
        {
            throw new CMSException("cannot create key pair generator: " + e.getMessage(), e);
        }
    }

    AlgorithmParameterGenerator createAlgorithmParameterGenerator(ASN1ObjectIdentifier algorithm)
        throws GeneralSecurityException
    {
        String algorithmName = (String)BASE_CIPHER_NAMES.get(algorithm);

        if (algorithmName != null)
        {
            try
            {
                // this is reversed as the Sun policy files now allow unlimited strength RSA
                return helper.createAlgorithmParameterGenerator(algorithmName);
            }
            catch (NoSuchAlgorithmException e)
            {
                // Ignore
            }
        }
        return helper.createAlgorithmParameterGenerator(algorithm.getId());
    }

    Cipher createContentCipher(final Key sKey, final AlgorithmIdentifier encryptionAlgID)
        throws CMSException
    {
        return (Cipher)execute(new JCECallback()
        {
            public Object doInJCE()
                throws CMSException, InvalidAlgorithmParameterException,
                InvalidKeyException, InvalidParameterSpecException, NoSuchAlgorithmException,
                NoSuchPaddingException, NoSuchProviderException
            {
                Cipher cipher = createCipher(encryptionAlgID.getAlgorithm());
                ASN1Object sParams = (ASN1Object)encryptionAlgID.getParameters().getDERObject();
                String encAlg = encryptionAlgID.getAlgorithm().getId();

                if (sParams != null && !(sParams instanceof ASN1Null))
                {
                    try
                    {
                        AlgorithmParameters params = createAlgorithmParameters(encryptionAlgID.getAlgorithm());

                        try
                        {
                            params.init(sParams.getEncoded(), "ASN.1");
                        }
                        catch (IOException e)
                        {
                            throw new CMSException("error decoding algorithm parameters.", e);
                        }

                        cipher.init(Cipher.DECRYPT_MODE, sKey, params);
                    }
                    catch (NoSuchAlgorithmException e)
                    {
                        if (encAlg.equals(CMSEnvelopedDataGenerator.DES_EDE3_CBC)
                            || encAlg.equals(CMSEnvelopedDataGenerator.IDEA_CBC)
                            || encAlg.equals(CMSEnvelopedDataGenerator.AES128_CBC)
                            || encAlg.equals(CMSEnvelopedDataGenerator.AES192_CBC)
                            || encAlg.equals(CMSEnvelopedDataGenerator.AES256_CBC))
                        {
                            cipher.init(Cipher.DECRYPT_MODE, sKey, new IvParameterSpec(
                                ASN1OctetString.getInstance(sParams).getOctets()));
                        }
                        else
                        {
                            throw e;
                        }
                    }
                }
                else
                {
                    if (encAlg.equals(CMSEnvelopedDataGenerator.DES_EDE3_CBC)
                        || encAlg.equals(CMSEnvelopedDataGenerator.IDEA_CBC)
                        || encAlg.equals(CMSEnvelopedDataGenerator.CAST5_CBC))
                    {
                        cipher.init(Cipher.DECRYPT_MODE, sKey, new IvParameterSpec(new byte[8]));
                    }
                    else
                    {
                        cipher.init(Cipher.DECRYPT_MODE, sKey);
                    }
                }

                return cipher;
            }
        });
    }

    Mac createContentMac(final Key sKey, final AlgorithmIdentifier macAlgId)
        throws CMSException
    {
        return (Mac)execute(new JCECallback()
        {
            public Object doInJCE()
                throws CMSException, InvalidAlgorithmParameterException,
                InvalidKeyException, InvalidParameterSpecException, NoSuchAlgorithmException,
                NoSuchPaddingException, NoSuchProviderException
            {
                Mac mac = createMac(macAlgId.getAlgorithm());
                ASN1Object sParams = (ASN1Object)macAlgId.getParameters().getDERObject();
                String macAlg = macAlgId.getAlgorithm().getId();

                if (sParams != null && !(sParams instanceof ASN1Null))
                {
                    try
                    {
                        AlgorithmParameters params = createAlgorithmParameters(macAlgId.getAlgorithm());

                        try
                        {
                            params.init(sParams.getEncoded(), "ASN.1");
                        }
                        catch (IOException e)
                        {
                            throw new CMSException("error decoding algorithm parameters.", e);
                        }

                        mac.init(sKey, params.getParameterSpec(IvParameterSpec.class));
                    }
                    catch (NoSuchAlgorithmException e)
                    {
                        throw e;
                    }
                }
                else
                {
                    mac.init(sKey);
                }

                return mac;
            }
        });
    }

    AlgorithmParameters createAlgorithmParameters(ASN1ObjectIdentifier algorithm)
        throws NoSuchAlgorithmException, NoSuchProviderException
    {
        String algorithmName = (String)BASE_CIPHER_NAMES.get(algorithm);

        if (algorithmName != null)
        {
            try
            {
                // this is reversed as the Sun policy files now allow unlimited strength RSA
                return helper.createAlgorithmParameters(algorithmName);
            }
            catch (NoSuchAlgorithmException e)
            {
                // Ignore
            }
        }
        return helper.createAlgorithmParameters(algorithm.getId());
    }


    KeyPairGenerator createKeyPairGenerator(DERObjectIdentifier algorithm)
        throws CMSException
    {
        try
        {
            String cipherName = (String)BASE_CIPHER_NAMES.get(algorithm);

            if (cipherName != null)
            {
                try
                {
                    // this is reversed as the Sun policy files now allow unlimited strength RSA
                    return helper.createKeyPairGenerator(cipherName);
                }
                catch (NoSuchAlgorithmException e)
                {
                    // Ignore
                }
            }
            return helper.createKeyPairGenerator(algorithm.getId());
        }
        catch (GeneralSecurityException e)
        {
            throw new CMSException("cannot create key pair generator: " + e.getMessage(), e);
        }
    }

    public KeyGenerator createKeyGenerator(ASN1ObjectIdentifier algorithm)
        throws CMSException
    {
        try
        {
            String cipherName = (String)BASE_CIPHER_NAMES.get(algorithm);

            if (cipherName != null)
            {
                try
                {
                    // this is reversed as the Sun policy files now allow unlimited strength RSA
                    return helper.createKeyGenerator(cipherName);
                }
                catch (NoSuchAlgorithmException e)
                {
                    // Ignore
                }
            }
            return helper.createKeyGenerator(algorithm.getId());
        }
        catch (GeneralSecurityException e)
        {
            throw new CMSException("cannot create key generator: " + e.getMessage(), e);
        }
    }

    AlgorithmParameters generateParameters(ASN1ObjectIdentifier encryptionOID, SecretKey encKey, SecureRandom rand)
        throws CMSException
    {
        try
        {
            AlgorithmParameterGenerator pGen = createAlgorithmParameterGenerator(encryptionOID);

            if (encryptionOID.equals(CMSEnvelopedDataGenerator.RC2_CBC))
            {
                byte[]  iv = new byte[8];

                rand.nextBytes(iv);

                try
                {
                    pGen.init(new RC2ParameterSpec(encKey.getEncoded().length * 8, iv), rand);
                }
                catch (InvalidAlgorithmParameterException e)
                {
                    throw new CMSException("parameters generation error: " + e, e);
                }
            }

            return pGen.generateParameters();
        }
        catch (NoSuchAlgorithmException e)
        {
            return null;
        }
        catch (GeneralSecurityException e)
        {
            throw new CMSException("exception creating algorithm parameter generator: " + e, e);
        }
    }

    AlgorithmIdentifier getAlgorithmIdentifier(ASN1ObjectIdentifier encryptionOID, AlgorithmParameters params)
        throws CMSException
    {
        DEREncodable asn1Params;
        if (params != null)
        {
            try
            {
                asn1Params = ASN1Object.fromByteArray(params.getEncoded("ASN.1"));
            }
            catch (IOException e)
            {
                throw new CMSException("cannot encode parameters: " + e.getMessage(), e);
            }
        }
        else
        {
            asn1Params = DERNull.INSTANCE;
        }

        return new AlgorithmIdentifier(
            encryptionOID,
            asn1Params);
    }

    static Object execute(JCECallback callback) throws CMSException
    {
        try
        {
            return callback.doInJCE();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new CMSException("can't find algorithm.", e);
        }
        catch (InvalidKeyException e)
        {
            throw new CMSException("key invalid in message.", e);
        }
        catch (NoSuchProviderException e)
        {
            throw new CMSException("can't find provider.", e);
        }
        catch (NoSuchPaddingException e)
        {
            throw new CMSException("required padding not supported.", e);
        }
        catch (InvalidAlgorithmParameterException e)
        {
            throw new CMSException("algorithm parameters invalid.", e);
        }
        catch (InvalidParameterSpecException e)
        {
            throw new CMSException("MAC algorithm parameter spec invalid.", e);
        }
    }

    public KeyFactory createKeyFactory(ASN1ObjectIdentifier algorithm)
        throws CMSException
    {
        try
        {
            String cipherName = (String)BASE_CIPHER_NAMES.get(algorithm);

            if (cipherName != null)
            {
                try
                {
                    // this is reversed as the Sun policy files now allow unlimited strength RSA
                    return helper.createKeyFactory(cipherName);
                }
                catch (NoSuchAlgorithmException e)
                {
                    // Ignore
                }
            }
            return helper.createKeyFactory(algorithm.getId());
        }
        catch (GeneralSecurityException e)
        {
            throw new CMSException("cannot create key factory: " + e.getMessage(), e);
        }
    }

    public AsymmetricKeyUnwrapper createAsymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, PrivateKey keyEncryptionKey)
    {
        return helper.createAsymmetricUnwrapper(keyEncryptionAlgorithm, keyEncryptionKey);
    }

    public SymmetricKeyUnwrapper createSymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, SecretKey keyEncryptionKey)
    {
        return helper.createSymmetricUnwrapper(keyEncryptionAlgorithm, keyEncryptionKey);
    }

    public AlgorithmIdentifier getAlgorithmIdentifier(ASN1ObjectIdentifier macOID, AlgorithmParameterSpec paramSpec)
    {
        if (paramSpec instanceof IvParameterSpec)
        {
            return new AlgorithmIdentifier(macOID, new DEROctetString(((IvParameterSpec)paramSpec).getIV()));
        }
        if (paramSpec instanceof RC2ParameterSpec)
        {
            RC2ParameterSpec rc2Spec = (RC2ParameterSpec)paramSpec;

            int effKeyBits = ((RC2ParameterSpec)paramSpec).getEffectiveKeyBits();

            if (effKeyBits != -1)
            {
                int parameterVersion;
                            
                if (effKeyBits < 256)
                {
                    parameterVersion = rc2Table[effKeyBits];
                }
                else
                {
                    parameterVersion = effKeyBits;
                }

                return new AlgorithmIdentifier(macOID, new RC2CBCParameter(parameterVersion, rc2Spec.getIV()));
            }

            return new AlgorithmIdentifier(macOID, new RC2CBCParameter(rc2Spec.getIV()));
        }

        throw new IllegalStateException("unknown parameter spec");
    }

    static interface JCECallback
    {
        Object doInJCE()
            throws CMSException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidParameterSpecException,
            NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException;
    }
}
