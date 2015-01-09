package old.org.bouncycastle.cms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.BERConstructedOctetString;
import old.org.bouncycastle.asn1.BERSet;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.cms.AttributeTable;
import old.org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import old.org.bouncycastle.asn1.cms.ContentInfo;
import old.org.bouncycastle.asn1.cms.EncryptedContentInfo;
import old.org.bouncycastle.asn1.cms.EnvelopedData;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.operator.GenericKey;
import old.org.bouncycastle.operator.OutputEncryptor;

/**
 * General class for generating a CMS enveloped-data message.
 *
 * A simple example of usage.
 *
 * <pre>
 *       CMSTypedData msg     = new CMSProcessableByteArray("Hello World!".getBytes());
 *
 *       CMSEnvelopedDataGenerator edGen = new CMSEnvelopedDataGenerator();
 *
 *       edGen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(recipientCert).setProvider("BC"));
 *
 *       CMSEnvelopedData ed = edGen.generate(
 *                                       msg,
 *                                       new JceCMSContentEncryptorBuilder(CMSAlgorithm.DES_EDE3_CBC)
 *                                              .setProvider("BC").build());
 *
 * </pre>
 */
public class CMSEnvelopedDataGenerator
    extends CMSEnvelopedGenerator
{
    /**
     * base constructor
     */
    public CMSEnvelopedDataGenerator()
    {
    }

    /**
     * constructor allowing specific source of randomness
     * @param rand instance of SecureRandom to use
     */
    public CMSEnvelopedDataGenerator(
        SecureRandom rand)
    {
        super(rand);
    }

    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given provider and the passed in key generator.
     */
    private CMSEnvelopedData generate(
        CMSProcessable  content,
        String          encryptionOID,
        KeyGenerator    keyGen,
        Provider        provider)
        throws NoSuchAlgorithmException, CMSException
    {
        Provider                encProvider = keyGen.getProvider();
        ASN1EncodableVector     recipientInfos = new ASN1EncodableVector();
        AlgorithmIdentifier     encAlgId;
        SecretKey               encKey;
        ASN1OctetString         encContent;

        try
        {
            Cipher cipher = CMSEnvelopedHelper.INSTANCE.createSymmetricCipher(encryptionOID, encProvider);

            AlgorithmParameters params;
            
            encKey = keyGen.generateKey();
            params = generateParameters(encryptionOID, encKey, encProvider);

            cipher.init(Cipher.ENCRYPT_MODE, encKey, params, rand);

            //
            // If params are null we try and second guess on them as some providers don't provide
            // algorithm parameter generation explicity but instead generate them under the hood.
            //
            if (params == null)
            {
                params = cipher.getParameters();
            }
            
            encAlgId = getAlgorithmIdentifier(encryptionOID, params);

            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            CipherOutputStream      cOut = new CipherOutputStream(bOut, cipher);

            content.write(cOut);

            cOut.close();

            encContent = new BERConstructedOctetString(bOut.toByteArray());
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
        catch (IOException e)
        {
            throw new CMSException("exception decoding algorithm parameters.", e);
        }

        for (Iterator it = oldRecipientInfoGenerators.iterator(); it.hasNext();)
        {
            IntRecipientInfoGenerator recipient = (IntRecipientInfoGenerator)it.next();

            try
            {
                recipientInfos.add(recipient.generate(encKey, rand, provider));
            }
            catch (InvalidKeyException e)
            {
                throw new CMSException("key inappropriate for algorithm.", e);
            }
            catch (GeneralSecurityException e)
            {
                throw new CMSException("error making encrypted content.", e);
            }
        }

        for (Iterator it = recipientInfoGenerators.iterator(); it.hasNext();)
        {
            RecipientInfoGenerator recipient = (RecipientInfoGenerator)it.next();

            recipientInfos.add(recipient.generate(new GenericKey(encKey)));
        }

        EncryptedContentInfo  eci;

        if (content instanceof CMSTypedData)
        {
            eci = new EncryptedContentInfo(
                        ((CMSTypedData)content).getContentType(),
                        encAlgId,
                        encContent);
        }
        else
        {
            eci = new EncryptedContentInfo(
                        CMSObjectIdentifiers.data,
                        encAlgId,
                        encContent);
        }

        ASN1Set unprotectedAttrSet = null;
        if (unprotectedAttributeGenerator != null)
        {
            AttributeTable attrTable = unprotectedAttributeGenerator.getAttributes(new HashMap());

            unprotectedAttrSet = new BERSet(attrTable.toASN1EncodableVector());
        }

        ContentInfo contentInfo = new ContentInfo(
                CMSObjectIdentifiers.envelopedData,
                new EnvelopedData(null, new DERSet(recipientInfos), eci, unprotectedAttrSet));

        return new CMSEnvelopedData(contentInfo);
    }

    private CMSEnvelopedData doGenerate(
        CMSTypedData content,
        OutputEncryptor contentEncryptor)
        throws CMSException
    {
        if (!oldRecipientInfoGenerators.isEmpty())
        {
            throw new IllegalStateException("can only use addRecipientGenerator() with this method");
        }

        ASN1EncodableVector     recipientInfos = new ASN1EncodableVector();
        AlgorithmIdentifier     encAlgId;
        ASN1OctetString         encContent;

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        try
        {
            OutputStream cOut = contentEncryptor.getOutputStream(bOut);

            content.write(cOut);

            cOut.close();
        }
        catch (IOException e)
        {
            throw new CMSException("");
        }

        byte[] encryptedContent = bOut.toByteArray();

        encAlgId = contentEncryptor.getAlgorithmIdentifier();

        encContent = new BERConstructedOctetString(encryptedContent);

        GenericKey encKey = contentEncryptor.getKey();

        for (Iterator it = recipientInfoGenerators.iterator(); it.hasNext();)
        {
            RecipientInfoGenerator recipient = (RecipientInfoGenerator)it.next();

            recipientInfos.add(recipient.generate(encKey));
        }

        EncryptedContentInfo  eci = new EncryptedContentInfo(
                        content.getContentType(),
                        encAlgId,
                        encContent);

        ASN1Set unprotectedAttrSet = null;
        if (unprotectedAttributeGenerator != null)
        {
            AttributeTable attrTable = unprotectedAttributeGenerator.getAttributes(new HashMap());

            unprotectedAttrSet = new BERSet(attrTable.toASN1EncodableVector());
        }

        ContentInfo contentInfo = new ContentInfo(
                CMSObjectIdentifiers.envelopedData,
                new EnvelopedData(null, new DERSet(recipientInfos), eci, unprotectedAttrSet));

        return new CMSEnvelopedData(contentInfo);
    }

    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given provider.
     * @deprecated use OutputEncryptor method.
     */
    public CMSEnvelopedData generate(
        CMSProcessable  content,
        String          encryptionOID,
        String          provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, CMSException
    {
        return generate(content, encryptionOID, CMSUtils.getProvider(provider));
    }

    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given provider.
     * @deprecated use OutputEncryptor method.
     */
    public CMSEnvelopedData generate(
        CMSProcessable  content,
        String          encryptionOID,
        Provider        provider)
        throws NoSuchAlgorithmException, CMSException
    {
        KeyGenerator keyGen = CMSEnvelopedHelper.INSTANCE.createSymmetricKeyGenerator(encryptionOID, provider);

        keyGen.init(rand);

        return generate(content, encryptionOID, keyGen, provider);
    }

    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given provider.
     * @deprecated use OutputEncryptor method.
     */
    public CMSEnvelopedData generate(
        CMSProcessable  content,
        String          encryptionOID,
        int             keySize,
        String          provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, CMSException
    {
        return generate(content, encryptionOID, keySize, CMSUtils.getProvider(provider));
    }

    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given provider.
     * @deprecated use OutputEncryptor method.
     */
    public CMSEnvelopedData generate(
        CMSProcessable  content,
        String          encryptionOID,
        int             keySize,
        Provider        provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, CMSException
    {
        KeyGenerator keyGen = CMSEnvelopedHelper.INSTANCE.createSymmetricKeyGenerator(encryptionOID, provider);

        keyGen.init(keySize, rand);

        return generate(content, encryptionOID, keyGen, provider);
    }

    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given provider.
     *
     * @param content the content to be encrypted
     * @param contentEncryptor the symmetric key based encryptor to encrypt the content with.
     */
    public CMSEnvelopedData generate(
        CMSTypedData content,
        OutputEncryptor contentEncryptor)
        throws CMSException
    {
        return doGenerate(content, contentEncryptor);
    }
}
