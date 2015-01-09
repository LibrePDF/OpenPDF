package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.NoSuchProviderException;
import java.security.Provider;

import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.cms.AttributeTable;
import old.org.bouncycastle.asn1.cms.ContentInfo;
import old.org.bouncycastle.asn1.cms.EncryptedContentInfo;
import old.org.bouncycastle.asn1.cms.EnvelopedData;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * containing class for an CMS Enveloped Data object
 * <p>
 * Example of use - assuming the first recipient matches the private key we have.
 * <pre>
 *      CMSEnvelopedData     ed = new CMSEnvelopedData(inputStream);
 *
 *      RecipientInformationStore  recipients = ed.getRecipientInfos();
 *
 *      Collection  c = recipients.getRecipients();
 *      Iterator    it = c.iterator();
 *
 *      if (it.hasNext())
 *      {
 *          RecipientInformation   recipient = (RecipientInformation)it.next();
 *
 *          byte[] recData = recipient.getContent(new JceKeyTransEnvelopedRecipient(privateKey).setProvider("BC"));
 *
 *          processData(recData);
 *      }
 *  </pre>
 */
public class CMSEnvelopedData
{
    RecipientInformationStore   recipientInfoStore;
    ContentInfo                 contentInfo;
    
    private AlgorithmIdentifier    encAlg;
    private ASN1Set                unprotectedAttributes;

    public CMSEnvelopedData(
        byte[]    envelopedData) 
        throws CMSException
    {
        this(CMSUtils.readContentInfo(envelopedData));
    }

    public CMSEnvelopedData(
        InputStream    envelopedData) 
        throws CMSException
    {
        this(CMSUtils.readContentInfo(envelopedData));
    }

    public CMSEnvelopedData(
        ContentInfo contentInfo)
    {
        this.contentInfo = contentInfo;

        EnvelopedData  envData = EnvelopedData.getInstance(contentInfo.getContent());

        //
        // read the recipients
        //
        ASN1Set recipientInfos = envData.getRecipientInfos();

        //
        // read the encrypted content info
        //
        EncryptedContentInfo encInfo = envData.getEncryptedContentInfo();
        this.encAlg = encInfo.getContentEncryptionAlgorithm();
        CMSReadable readable = new CMSProcessableByteArray(encInfo.getEncryptedContent().getOctets());
        CMSSecureReadable secureReadable = new CMSEnvelopedHelper.CMSEnvelopedSecureReadable(
            this.encAlg, readable);

        //
        // build the RecipientInformationStore
        //
        this.recipientInfoStore = CMSEnvelopedHelper.buildRecipientInformationStore(
            recipientInfos, this.encAlg, secureReadable);

        this.unprotectedAttributes = envData.getUnprotectedAttrs();
    }

    private byte[] encodeObj(
        DEREncodable    obj)
        throws IOException
    {
        if (obj != null)
        {
            return obj.getDERObject().getEncoded();
        }

        return null;
    }
    
    /**
     * return the object identifier for the content encryption algorithm.
     */
    public String getEncryptionAlgOID()
    {
        return encAlg.getObjectId().getId();
    }

    /**
     * return the ASN.1 encoded encryption algorithm parameters, or null if
     * there aren't any.
     */
    public byte[] getEncryptionAlgParams()
    {
        try
        {
            return encodeObj(encAlg.getParameters());
        }
        catch (Exception e)
        {
            throw new RuntimeException("exception getting encryption parameters " + e);
        }
    }
    
    /**
     * Return an AlgorithmParameters object giving the encryption parameters
     * used to encrypt the message content.
     * 
     * @param provider the provider to generate the parameters for.
     * @return the parameters object, null if there is not one.
     * @throws CMSException if the algorithm cannot be found, or the parameters can't be parsed.
     * @throws NoSuchProviderException if the provider cannot be found.
     */
    public AlgorithmParameters getEncryptionAlgorithmParameters(
        String  provider) 
    throws CMSException, NoSuchProviderException    
    {
        return getEncryptionAlgorithmParameters(CMSUtils.getProvider(provider));
    }

    /**
     * Return an AlgorithmParameters object giving the encryption parameters
     * used to encrypt the message content.
     *
     * @param provider the provider to generate the parameters for.
     * @return the parameters object, null if there is not one.
     * @throws CMSException if the algorithm cannot be found, or the parameters can't be parsed.
     */
    public AlgorithmParameters getEncryptionAlgorithmParameters(
        Provider provider)
    throws CMSException
    {
        return CMSEnvelopedHelper.INSTANCE.getEncryptionAlgorithmParameters(getEncryptionAlgOID(), getEncryptionAlgParams(), provider);
    }

    /**
     * return a store of the intended recipients for this message
     */
    public RecipientInformationStore getRecipientInfos()
    {
        return recipientInfoStore;
    }

    /**
     * return the ContentInfo 
     */
    public ContentInfo getContentInfo()
    {
        return contentInfo;
    }

    /**
     * return a table of the unprotected attributes indexed by
     * the OID of the attribute.
     */
    public AttributeTable getUnprotectedAttributes()
    {
        if (unprotectedAttributes == null)
        {
            return null;
        }

        return new AttributeTable(unprotectedAttributes);
    }
    
    /**
     * return the ASN.1 encoded representation of this object.
     */
    public byte[] getEncoded()
        throws IOException
    {
        return contentInfo.getEncoded();
    }
}
