package old.org.bouncycastle.cms;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import old.org.bouncycastle.asn1.ASN1Null;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.cms.KEKRecipientInfo;
import old.org.bouncycastle.asn1.cms.KeyAgreeRecipientInfo;
import old.org.bouncycastle.asn1.cms.KeyTransRecipientInfo;
import old.org.bouncycastle.asn1.cms.PasswordRecipientInfo;
import old.org.bouncycastle.asn1.cms.RecipientInfo;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.operator.DigestCalculator;
import old.org.bouncycastle.util.io.TeeInputStream;

class CMSEnvelopedHelper
{
    static final CMSEnvelopedHelper INSTANCE = new CMSEnvelopedHelper();

    private static final Map KEYSIZES = new HashMap();
    private static final Map BASE_CIPHER_NAMES = new HashMap();
    private static final Map CIPHER_ALG_NAMES = new HashMap();
    private static final Map MAC_ALG_NAMES = new HashMap();

    static
    {
        KEYSIZES.put(CMSEnvelopedGenerator.DES_EDE3_CBC,  new Integer(192));
        KEYSIZES.put(CMSEnvelopedGenerator.AES128_CBC,  new Integer(128));
        KEYSIZES.put(CMSEnvelopedGenerator.AES192_CBC,  new Integer(192));
        KEYSIZES.put(CMSEnvelopedGenerator.AES256_CBC,  new Integer(256));

        BASE_CIPHER_NAMES.put(CMSEnvelopedGenerator.DES_EDE3_CBC,  "DESEDE");
        BASE_CIPHER_NAMES.put(CMSEnvelopedGenerator.AES128_CBC,  "AES");
        BASE_CIPHER_NAMES.put(CMSEnvelopedGenerator.AES192_CBC,  "AES");
        BASE_CIPHER_NAMES.put(CMSEnvelopedGenerator.AES256_CBC,  "AES");

        CIPHER_ALG_NAMES.put(CMSEnvelopedGenerator.DES_EDE3_CBC,  "DESEDE/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSEnvelopedGenerator.AES128_CBC,  "AES/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSEnvelopedGenerator.AES192_CBC,  "AES/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSEnvelopedGenerator.AES256_CBC,  "AES/CBC/PKCS5Padding");

        MAC_ALG_NAMES.put(CMSEnvelopedGenerator.DES_EDE3_CBC,  "DESEDEMac");
        MAC_ALG_NAMES.put(CMSEnvelopedGenerator.AES128_CBC,  "AESMac");
        MAC_ALG_NAMES.put(CMSEnvelopedGenerator.AES192_CBC,  "AESMac");
        MAC_ALG_NAMES.put(CMSEnvelopedGenerator.AES256_CBC,  "AESMac");
    }

    String getAsymmetricEncryptionAlgName(
        String encryptionAlgOID)
    {
        if (PKCSObjectIdentifiers.rsaEncryption.getId().equals(encryptionAlgOID))
        {
            return "RSA/ECB/PKCS1Padding";
        }
        
        return encryptionAlgOID;    
    }

    Cipher createAsymmetricCipher(
        String encryptionOid,
        String provName)
        throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException
    {
        String asymName = getAsymmetricEncryptionAlgName(encryptionOid);
        if (!asymName.equals(encryptionOid))
        {
            try
            {
                // this is reversed as the Sun policy files now allow unlimited strength RSA
                return Cipher.getInstance(asymName, provName);
            }
            catch (NoSuchAlgorithmException e)
            {
                // Ignore
            }
        }
        return Cipher.getInstance(encryptionOid, provName);
    }

    Cipher createAsymmetricCipher(
        String encryptionOid,
        Provider provider)
        throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        String asymName = getAsymmetricEncryptionAlgName(encryptionOid);
        if (!asymName.equals(encryptionOid))
        {
            try
            {
                // this is reversed as the Sun policy files now allow unlimited strength RSA
                return getCipherInstance(asymName, provider);
            }
            catch (NoSuchAlgorithmException e)
            {
                // Ignore
            }
        }
        return getCipherInstance(encryptionOid, provider);
    }

    KeyGenerator createSymmetricKeyGenerator(
        String encryptionOID, 
        Provider provider)
        throws NoSuchAlgorithmException
    {
        try
        {
            return createKeyGenerator(encryptionOID, provider);
        }
        catch (NoSuchAlgorithmException e)
        {
            try
            {
                String algName = (String)BASE_CIPHER_NAMES.get(encryptionOID);
                if (algName != null)
                {
                    return createKeyGenerator(algName, provider);
                }
            }
            catch (NoSuchAlgorithmException ex)
            {
                // ignore
            }
            if (provider != null)
            {
                return createSymmetricKeyGenerator(encryptionOID, null);
            }
            throw e;
        }
    }

    AlgorithmParameters createAlgorithmParameters(
        String encryptionOID, 
        Provider provider)
        throws NoSuchAlgorithmException
    {
        try
        {
            return createAlgorithmParams(encryptionOID, provider);
        }
        catch (NoSuchAlgorithmException e)
        {
            try
            {
                String algName = (String)BASE_CIPHER_NAMES.get(encryptionOID);
                if (algName != null)
                {
                    return createAlgorithmParams(algName, provider);
                }
            }
            catch (NoSuchAlgorithmException ex)
            {
                // ignore
            }
            //
            // can't try with default provider here as parameters must be from the specified provider.
            //
            throw e;
        }
    }

    AlgorithmParameterGenerator createAlgorithmParameterGenerator(
        String encryptionOID,
        Provider provider)
        throws NoSuchAlgorithmException
    {
        try
        {
            return createAlgorithmParamsGenerator(encryptionOID, provider);
        }
        catch (NoSuchAlgorithmException e)
        {
            try
            {
                String algName = (String)BASE_CIPHER_NAMES.get(encryptionOID);
                if (algName != null)
                {
                    return createAlgorithmParamsGenerator(algName, provider);
                }
            }
            catch (NoSuchAlgorithmException ex)
            {
                // ignore
            }
            //
            // can't try with default provider here as parameters must be from the specified provider.
            //
            throw e;
        }
    }

    String getRFC3211WrapperName(String oid)
    {
        String alg = (String)BASE_CIPHER_NAMES.get(oid);

        if (alg == null)
        {
            throw new IllegalArgumentException("no name for " + oid);
        }

        return alg + "RFC3211Wrap";
    }

    int getKeySize(String oid)
    {
        Integer keySize = (Integer)KEYSIZES.get(oid);

        if (keySize == null)
        {
            throw new IllegalArgumentException("no keysize for " + oid);
        }

        return keySize.intValue();
    }

    private Cipher getCipherInstance(
        String algName,
        Provider provider)
        throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        if (provider != null)
        {
            return Cipher.getInstance(algName, provider);
        }
        else
        {
            return Cipher.getInstance(algName);
        }
    }

    private AlgorithmParameters createAlgorithmParams(
        String algName,
        Provider provider)
        throws NoSuchAlgorithmException
    {
        if (provider != null)
        {
            return AlgorithmParameters.getInstance(algName, provider);
        }
        else
        {
            return AlgorithmParameters.getInstance(algName);
        }
    }

    private AlgorithmParameterGenerator createAlgorithmParamsGenerator(
        String algName,
        Provider provider)
        throws NoSuchAlgorithmException
    {
        if (provider != null)
        {
            return AlgorithmParameterGenerator.getInstance(algName, provider);
        }
        else
        {
            return AlgorithmParameterGenerator.getInstance(algName);
        }
    }

    private KeyGenerator createKeyGenerator(
        String algName,
        Provider provider)
        throws NoSuchAlgorithmException
    {
        if (provider != null)
        {
            return KeyGenerator.getInstance(algName, provider);
        }
        else
        {
            return KeyGenerator.getInstance(algName);
        }
    }

    Cipher createSymmetricCipher(String encryptionOID, Provider provider)
        throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        try
        {
            return getCipherInstance(encryptionOID, provider);
        }
        catch (NoSuchAlgorithmException e)
        {
            String alternate = (String)CIPHER_ALG_NAMES.get(encryptionOID);

            try
            {
                return getCipherInstance(alternate, provider);
            }
            catch (NoSuchAlgorithmException ex)
            {
                if (provider != null)
                {
                    return createSymmetricCipher(encryptionOID, null); // roll back to default
                }
                throw e;
            }
        }
    }

    private Mac createMac(
        String algName,
        Provider provider)
        throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        if (provider != null)
        {
            return Mac.getInstance(algName, provider);
        }
        else
        {
            return Mac.getInstance(algName);
        }
    }

    Mac getMac(String macOID, Provider provider)
        throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        try
        {
            return createMac(macOID, provider);
        }
        catch (NoSuchAlgorithmException e)
        {
            String alternate = (String)MAC_ALG_NAMES.get(macOID);

            try
            {
                return createMac(alternate, provider);
            }
            catch (NoSuchAlgorithmException ex)
            {
                if (provider != null)
                {
                    return getMac(macOID, null); // roll back to default
                }
                throw e;
            }
        }
    }

    AlgorithmParameters getEncryptionAlgorithmParameters(
        String encOID,
        byte[] encParams,
        Provider provider)
        throws CMSException
    {
        if (encParams == null)
        {
            return null;
        }

        try
        {
            AlgorithmParameters params = createAlgorithmParameters(encOID, provider);

            params.init(encParams, "ASN.1");

            return params;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new CMSException("can't find parameters for algorithm", e);
        }
        catch (IOException e)
        {
            throw new CMSException("can't find parse parameters", e);
        }
    }

    String getSymmetricCipherName(String oid)
    {
        String algName = (String)BASE_CIPHER_NAMES.get(oid);
        if (algName != null)
        {
            return algName;
        }
        return oid;
    }

    static RecipientInformationStore buildRecipientInformationStore(
        ASN1Set recipientInfos, AlgorithmIdentifier messageAlgorithm, CMSSecureReadable secureReadable)
    {
        return buildRecipientInformationStore(recipientInfos, messageAlgorithm, secureReadable, null);
    }

    static RecipientInformationStore buildRecipientInformationStore(
        ASN1Set recipientInfos, AlgorithmIdentifier messageAlgorithm, CMSSecureReadable secureReadable, AuthAttributesProvider additionalData)
    {
        List infos = new ArrayList();
        for (int i = 0; i != recipientInfos.size(); i++)
        {
            RecipientInfo info = RecipientInfo.getInstance(recipientInfos.getObjectAt(i));

            readRecipientInfo(infos, info, messageAlgorithm, secureReadable, additionalData);
        }
        return new RecipientInformationStore(infos);
    }

    private static void readRecipientInfo(
        List infos, RecipientInfo info, AlgorithmIdentifier messageAlgorithm, CMSSecureReadable secureReadable, AuthAttributesProvider additionalData)
    {
        DEREncodable recipInfo = info.getInfo();
        if (recipInfo instanceof KeyTransRecipientInfo)
        {
            infos.add(new KeyTransRecipientInformation(
                (KeyTransRecipientInfo)recipInfo, messageAlgorithm, secureReadable, additionalData));
        }
        else if (recipInfo instanceof KEKRecipientInfo)
        {
            infos.add(new KEKRecipientInformation(
                (KEKRecipientInfo)recipInfo, messageAlgorithm, secureReadable, additionalData));
        }
        else if (recipInfo instanceof KeyAgreeRecipientInfo)
        {
            KeyAgreeRecipientInformation.readRecipientInfo(infos,
                (KeyAgreeRecipientInfo)recipInfo, messageAlgorithm, secureReadable, additionalData);
        }
        else if (recipInfo instanceof PasswordRecipientInfo)
        {
            infos.add(new PasswordRecipientInformation(
                (PasswordRecipientInfo)recipInfo, messageAlgorithm, secureReadable, additionalData));
        }
    }

    static class CMSDigestAuthenticatedSecureReadable
        implements CMSSecureReadable
    {
        private DigestCalculator digestCalculator;
        private CMSReadable readable;

        public CMSDigestAuthenticatedSecureReadable(DigestCalculator digestCalculator, CMSReadable readable)
        {
            this.digestCalculator = digestCalculator;
            this.readable = readable;
        }

        public AlgorithmIdentifier getAlgorithm()
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Object getCryptoObject()
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public CMSReadable getReadable(SecretKey key, Provider provider)
            throws CMSException
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public InputStream getInputStream()
            throws IOException, CMSException
        {
            return new FilterInputStream(readable.getInputStream())
            {
                public int read()
                    throws IOException
                {
                    int b = in.read();

                    if (b >= 0)
                    {
                        digestCalculator.getOutputStream().write(b);
                    }

                    return b;
                }

                public int read(byte[] inBuf, int inOff, int inLen)
                    throws IOException
                {
                    int n = in.read(inBuf, inOff, inLen);
                    
                    if (n >= 0)
                    {
                        digestCalculator.getOutputStream().write(inBuf, inOff, n);
                    }

                    return n;
                }
            };
        }

        public byte[] getDigest()
        {
            return digestCalculator.getDigest();
        }
    }

    static class CMSAuthenticatedSecureReadable implements CMSSecureReadable
    {
        private AlgorithmIdentifier algorithm;
        private Mac mac;
        private CMSReadable readable;

        CMSAuthenticatedSecureReadable(AlgorithmIdentifier algorithm, CMSReadable readable)
        {
            this.algorithm = algorithm;
            this.readable = readable;
        }

        public AlgorithmIdentifier getAlgorithm()
        {
            return this.algorithm;
        }

        public Object getCryptoObject()
        {
            return this.mac;
        }

        public InputStream getInputStream()
            throws IOException, CMSException
        {
            return readable.getInputStream();
        }

        public CMSReadable getReadable(final SecretKey sKey, final Provider provider)
            throws CMSException
        {
            final String macAlg = this.algorithm.getObjectId().getId();
            final ASN1Object sParams = (ASN1Object)this.algorithm.getParameters();

            this.mac = (Mac)execute(new JCECallback()
            {
                public Object doInJCE() throws CMSException, InvalidAlgorithmParameterException,
                    InvalidKeyException, InvalidParameterSpecException, NoSuchAlgorithmException,
                    NoSuchPaddingException
                {
                    Mac mac = CMSEnvelopedHelper.INSTANCE.getMac(macAlg, provider);

                    if (sParams != null && !(sParams instanceof ASN1Null))
                    {
                        AlgorithmParameters params = CMSEnvelopedHelper.INSTANCE.createAlgorithmParameters(
                            macAlg, provider);

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
                    else
                    {
                        mac.init(sKey);
                    }

                    return mac;
                }
            });

            try
            {
                return new CMSProcessableInputStream(
                    new TeeInputStream(readable.getInputStream(), new MacOutputStream(this.mac)));
            }
            catch (IOException e)
            {
                throw new CMSException("error reading content.", e);
            }
        }
    }

    static class CMSEnvelopedSecureReadable implements CMSSecureReadable
    {
        private AlgorithmIdentifier algorithm;
        private Cipher cipher;
        private CMSReadable readable;

        CMSEnvelopedSecureReadable(AlgorithmIdentifier algorithm, CMSReadable readable)
        {
            this.algorithm = algorithm;
            this.readable = readable;
        }

        public AlgorithmIdentifier getAlgorithm()
        {
            return this.algorithm;
        }
        public InputStream getInputStream()
            throws IOException, CMSException
        {
            return readable.getInputStream();
        }
        public Object getCryptoObject()
        {
            return this.cipher;
        }

        public CMSReadable getReadable(final SecretKey sKey, final Provider provider)
            throws CMSException
        {
            final String encAlg = this.algorithm.getObjectId().getId();
            final ASN1Object sParams = (ASN1Object)this.algorithm.getParameters();

            this.cipher = (Cipher)execute(new JCECallback()
            {
                public Object doInJCE() throws CMSException, InvalidAlgorithmParameterException,
                    InvalidKeyException, InvalidParameterSpecException, NoSuchAlgorithmException,
                    NoSuchPaddingException
                {
                    Cipher cipher = CMSEnvelopedHelper.INSTANCE.createSymmetricCipher(encAlg, provider);

                    if (sParams != null && !(sParams instanceof ASN1Null))
                    {
                        try
                        {
                            AlgorithmParameters params = CMSEnvelopedHelper.INSTANCE.createAlgorithmParameters(
                                encAlg, cipher.getProvider());

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

            try
            {
                return new CMSProcessableInputStream(new CipherInputStream(readable.getInputStream(), cipher));
            }
            catch (IOException e)
            {
                throw new CMSException("error reading content.", e);
            }
        }
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

    static interface JCECallback
    {
        Object doInJCE()
            throws CMSException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidParameterSpecException,
                NoSuchAlgorithmException, NoSuchPaddingException;
    }
}
