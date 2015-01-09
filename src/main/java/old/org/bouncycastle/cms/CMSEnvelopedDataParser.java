package old.org.bouncycastle.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.NoSuchProviderException;
import java.security.Provider;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1OctetStringParser;
import old.org.bouncycastle.asn1.ASN1SequenceParser;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.ASN1SetParser;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.DERTags;
import old.org.bouncycastle.asn1.cms.AttributeTable;
import old.org.bouncycastle.asn1.cms.EncryptedContentInfoParser;
import old.org.bouncycastle.asn1.cms.EnvelopedDataParser;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * Parsing class for an CMS Enveloped Data object from an input stream.
 * <p>
 * Note: that because we are in a streaming mode only one recipient can be tried and it is important 
 * that the methods on the parser are called in the appropriate order.
 * </p>
 * <p>
 * Example of use - assuming the first recipient matches the private key we have.
 * <pre>
 *      CMSEnvelopedDataParser     ep = new CMSEnvelopedDataParser(inputStream);
 *
 *      RecipientInformationStore  recipients = ep.getRecipientInfos();
 *
 *      Collection  c = recipients.getRecipients();
 *      Iterator    it = c.iterator();
 *      
 *      if (it.hasNext())
 *      {
 *          RecipientInformation   recipient = (RecipientInformation)it.next();
 *
 *          CMSTypedStream recData = recipient.getContentStream(new JceKeyTransEnvelopedRecipient(privateKey).setProvider("BC"));
 *          
 *          processDataStream(recData.getContentStream());
 *      }
 *  </pre>
 *  Note: this class does not introduce buffering - if you are processing large files you should create
 *  the parser with:
 *  <pre>
 *          CMSEnvelopedDataParser     ep = new CMSEnvelopedDataParser(new BufferedInputStream(inputStream, bufSize));
 *  </pre>
 *  where bufSize is a suitably large buffer size.
 */
public class CMSEnvelopedDataParser
    extends CMSContentInfoParser
{
    RecipientInformationStore   _recipientInfoStore;
    EnvelopedDataParser         _envelopedData;
    
    private AlgorithmIdentifier _encAlg;
    private AttributeTable      _unprotectedAttributes;
    private boolean             _attrNotRead;

    public CMSEnvelopedDataParser(
        byte[]    envelopedData) 
        throws CMSException, IOException
    {
        this(new ByteArrayInputStream(envelopedData));
    }

    public CMSEnvelopedDataParser(
        InputStream    envelopedData) 
        throws CMSException, IOException
    {
        super(envelopedData);

        this._attrNotRead = true;
        this._envelopedData = new EnvelopedDataParser((ASN1SequenceParser)_contentInfo.getContent(DERTags.SEQUENCE));

        // TODO Validate version?
        //DERInteger version = this._envelopedData.getVersion();

        //
        // read the recipients
        //
        ASN1Set recipientInfos = ASN1Set.getInstance(_envelopedData.getRecipientInfos().getDERObject());

        //
        // read the encrypted content info
        //
        EncryptedContentInfoParser encInfo = _envelopedData.getEncryptedContentInfo();
        this._encAlg = encInfo.getContentEncryptionAlgorithm();
        CMSReadable readable = new CMSProcessableInputStream(
            ((ASN1OctetStringParser)encInfo.getEncryptedContent(DERTags.OCTET_STRING)).getOctetStream());
        CMSSecureReadable secureReadable = new CMSEnvelopedHelper.CMSEnvelopedSecureReadable(
            this._encAlg, readable);

        //
        // build the RecipientInformationStore
        //
        this._recipientInfoStore = CMSEnvelopedHelper.buildRecipientInformationStore(
            recipientInfos, this._encAlg, secureReadable);
    }

    /**
     * return the object identifier for the content encryption algorithm.
     */
    public String getEncryptionAlgOID()
    {
        return _encAlg.getObjectId().toString();
    }

    /**
     * return the ASN.1 encoded encryption algorithm parameters, or null if
     * there aren't any.
     */
    public byte[] getEncryptionAlgParams()
    {
        try
        {
            return encodeObj(_encAlg.getParameters());
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
     * @param provider the name of the provider to generate the parameters for.
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
        return _recipientInfoStore;
    }

    /**
     * return a table of the unprotected attributes indexed by
     * the OID of the attribute.
     * @exception IOException 
     */
    public AttributeTable getUnprotectedAttributes() 
        throws IOException
    {
        if (_unprotectedAttributes == null && _attrNotRead)
        {
            ASN1SetParser             set = _envelopedData.getUnprotectedAttrs();
            
            _attrNotRead = false;
            
            if (set != null)
            {
                ASN1EncodableVector v = new ASN1EncodableVector();
                DEREncodable        o;
                
                while ((o = set.readObject()) != null)
                {
                    ASN1SequenceParser    seq = (ASN1SequenceParser)o;
                    
                    v.add(seq.getDERObject());
                }
                
                _unprotectedAttributes = new AttributeTable(new DERSet(v));
            }
        }

        return _unprotectedAttributes;
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
}
