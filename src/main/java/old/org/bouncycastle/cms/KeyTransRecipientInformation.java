package old.org.bouncycastle.cms;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.ProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import old.org.bouncycastle.asn1.cms.KeyTransRecipientInfo;
import old.org.bouncycastle.asn1.cms.RecipientIdentifier;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;


/**
 * the KeyTransRecipientInformation class for a recipient who has been sent a secret
 * key encrypted using their public key that needs to be used to
 * extract the message.
 */
public class KeyTransRecipientInformation
    extends RecipientInformation
{
    private KeyTransRecipientInfo info;

    KeyTransRecipientInformation(
        KeyTransRecipientInfo   info,
        AlgorithmIdentifier     messageAlgorithm,
        CMSSecureReadable       secureReadable,
        AuthAttributesProvider  additionalData)
    {
        super(info.getKeyEncryptionAlgorithm(), messageAlgorithm, secureReadable, additionalData);

        this.info = info;

        RecipientIdentifier r = info.getRecipientIdentifier();

        if (r.isTagged())
        {
            ASN1OctetString octs = ASN1OctetString.getInstance(r.getId());

            rid = new KeyTransRecipientId(octs.getOctets());
        }
        else
        {
            IssuerAndSerialNumber   iAnds = IssuerAndSerialNumber.getInstance(r.getId());

            rid = new KeyTransRecipientId(iAnds.getName(), iAnds.getSerialNumber().getValue());
        }
    }

    private String getExchangeEncryptionAlgorithmName(
        DERObjectIdentifier oid)
    {
        if (PKCSObjectIdentifiers.rsaEncryption.equals(oid))
        {
            return "RSA/ECB/PKCS1Padding";
        }
        
        return oid.getId();
    }

    /**
     * @deprecated
     */
    protected Key getSessionKey(Key receiverPrivateKey, Provider prov)
        throws CMSException
    {
        CMSEnvelopedHelper helper = CMSEnvelopedHelper.INSTANCE;

        String keyExchangeAlgorithm = getExchangeEncryptionAlgorithmName(keyEncAlg.getObjectId());

        try
        {
            Key sKey = null;

            Cipher keyCipher = helper.createAsymmetricCipher(keyExchangeAlgorithm, prov);

            byte[] encryptedKeyBytes = info.getEncryptedKey().getOctets();
            String contentAlgorithmName = getContentAlgorithmName();

            try
            {
                keyCipher.init(Cipher.UNWRAP_MODE, receiverPrivateKey);
                sKey = keyCipher.unwrap(encryptedKeyBytes, contentAlgorithmName, Cipher.SECRET_KEY);
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
                keyCipher.init(Cipher.DECRYPT_MODE, receiverPrivateKey);
                sKey = new SecretKeySpec(keyCipher.doFinal(encryptedKeyBytes), contentAlgorithmName);
            }

            return sKey;
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
        catch (IllegalBlockSizeException e)
        {
            throw new CMSException("illegal blocksize in message.", e);
        }
        catch (BadPaddingException e)
        {
            throw new CMSException("bad padding in message.", e);
        }
    }

    /**
     * decrypt the content and return it
     * @deprecated use getContentStream(Recipient) method
     */
    public CMSTypedStream getContentStream(
        Key key,
        String prov)
        throws CMSException, NoSuchProviderException
    {
        return getContentStream(key, CMSUtils.getProvider(prov));
    }

    /**
     * decrypt the content and return it
     * @deprecated use getContentStream(Recipient) method
     */
    public CMSTypedStream getContentStream(
        Key key,
        Provider prov)
        throws CMSException
    {
        Key sKey = getSessionKey(key, prov);

        return getContentFromSessionKey(sKey, prov);
    }

    protected RecipientOperator getRecipientOperator(Recipient recipient)
        throws CMSException
    {
        return ((KeyTransRecipient)recipient).getRecipientOperator(keyEncAlg, messageAlgorithm, info.getEncryptedKey().getOctets());
    }
}
