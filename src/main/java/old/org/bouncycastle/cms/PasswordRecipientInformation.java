package old.org.bouncycastle.cms;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.cms.PasswordRecipientInfo;
import old.org.bouncycastle.asn1.pkcs.PBKDF2Params;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.crypto.PBEParametersGenerator;
import old.org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import old.org.bouncycastle.crypto.params.KeyParameter;

/**
 * the RecipientInfo class for a recipient who has been sent a message
 * encrypted using a password.
 */
public class PasswordRecipientInformation
    extends RecipientInformation
{
    static Map KEYSIZES = new HashMap();
    static Map BLOCKSIZES = new HashMap();

    static
    {
        BLOCKSIZES.put(CMSAlgorithm.DES_EDE3_CBC,  new Integer(8));
        BLOCKSIZES.put(CMSAlgorithm.AES128_CBC,  new Integer(16));
        BLOCKSIZES.put(CMSAlgorithm.AES192_CBC,  new Integer(16));
        BLOCKSIZES.put(CMSAlgorithm.AES256_CBC,  new Integer(16));

        KEYSIZES.put(CMSAlgorithm.DES_EDE3_CBC,  new Integer(192));
        KEYSIZES.put(CMSAlgorithm.AES128_CBC,  new Integer(128));
        KEYSIZES.put(CMSAlgorithm.AES192_CBC,  new Integer(192));
        KEYSIZES.put(CMSAlgorithm.AES256_CBC,  new Integer(256));
    }

    private PasswordRecipientInfo info;

    PasswordRecipientInformation(
        PasswordRecipientInfo   info,
        AlgorithmIdentifier     messageAlgorithm,
        CMSSecureReadable       secureReadable,
        AuthAttributesProvider  additionalData)
    {
        super(info.getKeyEncryptionAlgorithm(), messageAlgorithm, secureReadable, additionalData);

        this.info = info;
        this.rid = new PasswordRecipientId();
    }

    /**
     * return the object identifier for the key derivation algorithm, or null
     * if there is none present.
     *
     * @return OID for key derivation algorithm, if present.
     */
    public String getKeyDerivationAlgOID()
    {
        if (info.getKeyDerivationAlgorithm() != null)
        {
            return info.getKeyDerivationAlgorithm().getObjectId().getId();
        }

        return null;
    }

    /**
     * return the ASN.1 encoded key derivation algorithm parameters, or null if
     * there aren't any.
     * @return ASN.1 encoding of key derivation algorithm parameters.
     */
    public byte[] getKeyDerivationAlgParams()
    {
        try
        {
            if (info.getKeyDerivationAlgorithm() != null)
            {
                DEREncodable params = info.getKeyDerivationAlgorithm().getParameters();
                if (params != null)
                {
                    return params.getDERObject().getEncoded();
                }
            }

            return null;
        }
        catch (Exception e)
        {
            throw new RuntimeException("exception getting encryption parameters " + e);
        }
    }

    /**
     * return an AlgorithmParameters object representing the parameters to the
     * key derivation algorithm to the recipient.
     *
     * @return AlgorithmParameters object, null if there aren't any.
     */
    public AlgorithmParameters getKeyDerivationAlgParameters(String provider)
        throws NoSuchProviderException
    {
        return getKeyDerivationAlgParameters(CMSUtils.getProvider(provider));
    }
    
   /**
     * return an AlgorithmParameters object representing the parameters to the
     * key derivation algorithm to the recipient.
     *
     * @return AlgorithmParameters object, null if there aren't any.
     */
    public AlgorithmParameters getKeyDerivationAlgParameters(Provider provider)
    {
        try
        {
            if (info.getKeyDerivationAlgorithm() != null)
            {
                DEREncodable params = info.getKeyDerivationAlgorithm().getParameters();
                if (params != null)
                {
                    AlgorithmParameters algP = AlgorithmParameters.getInstance(info.getKeyDerivationAlgorithm().getObjectId().toString(), provider);

                    algP.init(params.getDERObject().getEncoded());

                    return algP;
                }
            }

            return null;
        }
        catch (Exception e)
        {
            throw new RuntimeException("exception getting encryption parameters " + e);
        }
    }

    /**
     * decrypt the content and return an input stream.
     * @deprecated use getContentStream(Recipient)
     */
    public CMSTypedStream getContentStream(
        Key key,
        String   prov)
        throws CMSException, NoSuchProviderException
    {
        return getContentStream(key, CMSUtils.getProvider(prov));
    }

    /**
     * decrypt the content and return an input stream.
     * @deprecated use getContentStream(Recipient)
     */
    public CMSTypedStream getContentStream(
        Key key,
        Provider prov)
        throws CMSException
    {
        try
        {
            CMSEnvelopedHelper  helper = CMSEnvelopedHelper.INSTANCE;
            AlgorithmIdentifier kekAlg = AlgorithmIdentifier.getInstance(info.getKeyEncryptionAlgorithm());
            ASN1Sequence        kekAlgParams = (ASN1Sequence)kekAlg.getParameters();
            String              kekAlgName = DERObjectIdentifier.getInstance(kekAlgParams.getObjectAt(0)).getId();
            String              wrapAlgName = helper.getRFC3211WrapperName(kekAlgName);

            Cipher keyCipher = helper.createSymmetricCipher(wrapAlgName, prov);
            IvParameterSpec ivSpec = new IvParameterSpec(ASN1OctetString.getInstance(kekAlgParams.getObjectAt(1)).getOctets());
            keyCipher.init(Cipher.UNWRAP_MODE, new SecretKeySpec(((CMSPBEKey)key).getEncoded(kekAlgName), kekAlgName), ivSpec);

            Key sKey = keyCipher.unwrap(info.getEncryptedKey().getOctets(), getContentAlgorithmName(),
                Cipher.SECRET_KEY);

            return getContentFromSessionKey(sKey, prov);
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
            throw new CMSException("invalid iv.", e);
        }
    }

    protected RecipientOperator getRecipientOperator(Recipient recipient)
        throws CMSException, IOException
    {
        PasswordRecipient pbeRecipient = (PasswordRecipient)recipient;
        AlgorithmIdentifier kekAlg = AlgorithmIdentifier.getInstance(info.getKeyEncryptionAlgorithm());
        ASN1Sequence        kekAlgParams = (ASN1Sequence)kekAlg.getParameters();
        DERObjectIdentifier kekAlgName = DERObjectIdentifier.getInstance(kekAlgParams.getObjectAt(0));
        PBKDF2Params        params = PBKDF2Params.getInstance(info.getKeyDerivationAlgorithm().getParameters());

        byte[]              derivedKey;
        int keySize = ((Integer)KEYSIZES.get(kekAlgName)).intValue();

        if (pbeRecipient.getPasswordConversionScheme() == PasswordRecipient.PKCS5_SCHEME2)
        {
            PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator();

            gen.init(PBEParametersGenerator.PKCS5PasswordToBytes(pbeRecipient.getPassword()), params.getSalt(), params.getIterationCount().intValue());

            derivedKey = ((KeyParameter)gen.generateDerivedParameters(keySize)).getKey();
        }
        else
        {
            PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator();

            gen.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(pbeRecipient.getPassword()), params.getSalt(), params.getIterationCount().intValue());

            derivedKey = ((KeyParameter)gen.generateDerivedParameters(keySize)).getKey();
        }
        
        return pbeRecipient.getRecipientOperator(AlgorithmIdentifier.getInstance(kekAlg.getParameters()), messageAlgorithm, derivedKey, info.getEncryptedKey().getOctets());
    }
}
