package old.org.bouncycastle.cms;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import old.org.bouncycastle.asn1.cms.KeyAgreeRecipientIdentifier;
import old.org.bouncycastle.asn1.cms.KeyAgreeRecipientInfo;
import old.org.bouncycastle.asn1.cms.OriginatorIdentifierOrKey;
import old.org.bouncycastle.asn1.cms.OriginatorPublicKey;
import old.org.bouncycastle.asn1.cms.RecipientEncryptedKey;
import old.org.bouncycastle.asn1.cms.RecipientKeyIdentifier;
import old.org.bouncycastle.asn1.cms.ecc.MQVuserKeyingMaterial;
import old.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.jce.spec.MQVPrivateKeySpec;
import old.org.bouncycastle.jce.spec.MQVPublicKeySpec;

/**
 * the RecipientInfo class for a recipient who has been sent a message
 * encrypted using key agreement.
 */
public class KeyAgreeRecipientInformation
    extends RecipientInformation
{
    private KeyAgreeRecipientInfo info;
    private ASN1OctetString       encryptedKey;

    static void readRecipientInfo(List infos, KeyAgreeRecipientInfo info,
        AlgorithmIdentifier messageAlgorithm, CMSSecureReadable secureReadable, AuthAttributesProvider additionalData)
    {
        ASN1Sequence s = info.getRecipientEncryptedKeys();

        for (int i = 0; i < s.size(); ++i)
        {
            RecipientEncryptedKey id = RecipientEncryptedKey.getInstance(
                s.getObjectAt(i));

            RecipientId rid;

            KeyAgreeRecipientIdentifier karid = id.getIdentifier();
            IssuerAndSerialNumber iAndSN = karid.getIssuerAndSerialNumber();

            if (iAndSN != null)
            {
                rid = new KeyAgreeRecipientId(iAndSN.getName(), iAndSN.getSerialNumber().getValue());
            }
            else
            {
                RecipientKeyIdentifier rKeyID = karid.getRKeyID();

                // Note: 'date' and 'other' fields of RecipientKeyIdentifier appear to be only informational

                rid = new KeyAgreeRecipientId(rKeyID.getSubjectKeyIdentifier().getOctets());
            }

            infos.add(new KeyAgreeRecipientInformation(info, rid, id.getEncryptedKey(), messageAlgorithm,
                secureReadable, additionalData));
        }
    }

    KeyAgreeRecipientInformation(
        KeyAgreeRecipientInfo   info,
        RecipientId             rid,
        ASN1OctetString         encryptedKey,
        AlgorithmIdentifier     messageAlgorithm,
        CMSSecureReadable       secureReadable,
        AuthAttributesProvider  additionalData)
    {
        super(info.getKeyEncryptionAlgorithm(), messageAlgorithm, secureReadable, additionalData);

        this.info = info;
        this.rid = rid;
        this.encryptedKey = encryptedKey;
    }

    private SubjectPublicKeyInfo getSenderPublicKeyInfo(AlgorithmIdentifier recKeyAlgId,
        OriginatorIdentifierOrKey originator)
        throws CMSException, IOException
    {
        OriginatorPublicKey opk = originator.getOriginatorKey();
        if (opk != null)
        {
            return getPublicKeyInfoFromOriginatorPublicKey(recKeyAlgId, opk);
        }

        OriginatorId origID = new OriginatorId();

        IssuerAndSerialNumber iAndSN = originator.getIssuerAndSerialNumber();
        if (iAndSN != null)
        {
            origID.setIssuer(iAndSN.getName().getEncoded());
            origID.setSerialNumber(iAndSN.getSerialNumber().getValue());
        }
        else
        {
            SubjectKeyIdentifier ski = originator.getSubjectKeyIdentifier();

            origID.setSubjectKeyIdentifier(ski.getKeyIdentifier());
        }

        return getPublicKeyInfoFromOriginatorId(origID);
    }

    private SubjectPublicKeyInfo getPublicKeyInfoFromOriginatorPublicKey(AlgorithmIdentifier recKeyAlgId,
            OriginatorPublicKey originatorPublicKey)
    {
        SubjectPublicKeyInfo pubInfo = new SubjectPublicKeyInfo(
            recKeyAlgId,
            originatorPublicKey.getPublicKey().getBytes());

        return pubInfo;
    }

    private SubjectPublicKeyInfo getPublicKeyInfoFromOriginatorId(OriginatorId origID)
            throws CMSException
    {
        // TODO Support all alternatives for OriginatorIdentifierOrKey
        // see RFC 3852 6.2.2
        throw new CMSException("No support for 'originator' as IssuerAndSerialNumber or SubjectKeyIdentifier");
    }

    private PublicKey getSenderPublicKey(Key receiverPrivateKey,
        OriginatorIdentifierOrKey originator, Provider prov)
        throws CMSException, GeneralSecurityException, IOException
    {
        SubjectPublicKeyInfo pubInfo = getSenderPublicKeyInfo(PrivateKeyInfo.getInstance(receiverPrivateKey.getEncoded()).getAlgorithmId(), originator);

        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubInfo.getEncoded());
        KeyFactory fact = KeyFactory.getInstance(keyEncAlg.getAlgorithm().getId(), prov);
        return fact.generatePublic(pubSpec);
    }

    private PublicKey getPublicKeyFromOriginatorPublicKey(Key receiverPrivateKey,
            OriginatorPublicKey originatorPublicKey, Provider prov)
            throws CMSException, GeneralSecurityException, IOException
    {
        SubjectPublicKeyInfo pubInfo = getPublicKeyInfoFromOriginatorPublicKey(PrivateKeyInfo.getInstance(receiverPrivateKey.getEncoded()).getAlgorithmId(), originatorPublicKey);

        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubInfo.getEncoded());
        KeyFactory fact = KeyFactory.getInstance(keyEncAlg.getAlgorithm().getId(), prov);
        return fact.generatePublic(pubSpec);
    }

    private SecretKey calculateAgreedWrapKey(String wrapAlg,
        PublicKey senderPublicKey, PrivateKey receiverPrivateKey, Provider prov)
        throws CMSException, GeneralSecurityException, IOException
    {
        String agreeAlg = keyEncAlg.getAlgorithm().getId();

        if (agreeAlg.equals(CMSEnvelopedGenerator.ECMQV_SHA1KDF))
        {
            byte[] ukmEncoding = info.getUserKeyingMaterial().getOctets();
            MQVuserKeyingMaterial ukm = MQVuserKeyingMaterial.getInstance(
                ASN1Object.fromByteArray(ukmEncoding));

            PublicKey ephemeralKey = getPublicKeyFromOriginatorPublicKey(receiverPrivateKey,
                ukm.getEphemeralPublicKey(), prov);

            senderPublicKey = new MQVPublicKeySpec(senderPublicKey, ephemeralKey);
            receiverPrivateKey = new MQVPrivateKeySpec(receiverPrivateKey, receiverPrivateKey);
        }

        KeyAgreement agreement = KeyAgreement.getInstance(agreeAlg, prov);
        agreement.init(receiverPrivateKey);
        agreement.doPhase(senderPublicKey, true);
        return agreement.generateSecret(wrapAlg);
    }

    private Key unwrapSessionKey(String wrapAlg, SecretKey agreedKey,
        Provider prov)
        throws GeneralSecurityException
    {
        Cipher keyCipher = CMSEnvelopedHelper.INSTANCE.createSymmetricCipher(wrapAlg, prov);
        keyCipher.init(Cipher.UNWRAP_MODE, agreedKey);
        return keyCipher.unwrap(encryptedKey.getOctets(), getContentAlgorithmName(), Cipher.SECRET_KEY);
    }

    protected Key getSessionKey(Key receiverPrivateKey, Provider prov)
        throws CMSException
    {
        try
        {
            String wrapAlg = 
                AlgorithmIdentifier.getInstance(keyEncAlg.getParameters()).getAlgorithm().getId();

            PublicKey senderPublicKey = getSenderPublicKey(receiverPrivateKey,
                info.getOriginator(), prov);

            SecretKey agreedWrapKey = calculateAgreedWrapKey(wrapAlg,
                senderPublicKey, (PrivateKey)receiverPrivateKey, prov);

            return unwrapSessionKey(wrapAlg, agreedWrapKey, prov);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new CMSException("can't find algorithm.", e);
        }
        catch (InvalidKeyException e)
        {
            throw new CMSException("key invalid in message.", e);
        }
        catch (InvalidKeySpecException e)
        {
            throw new CMSException("originator key spec invalid.", e);
        }
        catch (NoSuchPaddingException e)
        {
            throw new CMSException("required padding not supported.", e);
        }
        catch (Exception e)
        {
            throw new CMSException("originator key invalid.", e);
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
        throws CMSException, IOException
    {
        KeyAgreeRecipient agreeRecipient = (KeyAgreeRecipient)recipient;
                AlgorithmIdentifier    recKeyAlgId = agreeRecipient.getPrivateKeyAlgorithmIdentifier();

        return ((KeyAgreeRecipient)recipient).getRecipientOperator(keyEncAlg, messageAlgorithm, getSenderPublicKeyInfo(recKeyAlgId,
                        info.getOriginator()), info.getUserKeyingMaterial(), encryptedKey.getOctets());
    }
}
