package old.org.bouncycastle.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Generator;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1OctetStringParser;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1SequenceParser;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.ASN1SetParser;
import old.org.bouncycastle.asn1.ASN1StreamParser;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.BERSequenceGenerator;
import old.org.bouncycastle.asn1.BERSetParser;
import old.org.bouncycastle.asn1.BERTaggedObject;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.DERTaggedObject;
import old.org.bouncycastle.asn1.DERTags;
import old.org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import old.org.bouncycastle.asn1.cms.ContentInfoParser;
import old.org.bouncycastle.asn1.cms.SignedDataParser;
import old.org.bouncycastle.asn1.cms.SignerInfo;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.AttributeCertificate;
import old.org.bouncycastle.asn1.x509.CertificateList;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.cert.X509AttributeCertificateHolder;
import old.org.bouncycastle.cert.X509CRLHolder;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import old.org.bouncycastle.operator.SignatureAlgorithmIdentifierFinder;
import old.org.bouncycastle.util.CollectionStore;
import old.org.bouncycastle.util.Store;
import old.org.bouncycastle.util.io.Streams;
import old.org.bouncycastle.x509.NoSuchStoreException;
import old.org.bouncycastle.x509.X509Store;

/**
 * Parsing class for an CMS Signed Data object from an input stream.
 * <p>
 * Note: that because we are in a streaming mode only one signer can be tried and it is important 
 * that the methods on the parser are called in the appropriate order.
 * </p>
 * <p>
 * A simple example of usage for an encapsulated signature.
 * </p>
 * <p>
 * Two notes: first, in the example below the validity of
 * the certificate isn't verified, just the fact that one of the certs 
 * matches the given signer, and, second, because we are in a streaming
 * mode the order of the operations is important.
 * </p>
 * <pre>
 *      CMSSignedDataParser     sp = new CMSSignedDataParser(encapSigData);
 *
 *      sp.getSignedContent().drain();
 *
 *      CertStore               certs = sp.getCertificatesAndCRLs("Collection", "BC");
 *      SignerInformationStore  signers = sp.getSignerInfos();
 *      
 *      Collection              c = signers.getSigners();
 *      Iterator                it = c.iterator();
 *
 *      while (it.hasNext())
 *      {
 *          SignerInformation   signer = (SignerInformation)it.next();
 *          Collection          certCollection = certStore.getMatches(signer.getSID());
 *
 *          Iterator        certIt = certCollection.iterator();
 *          X509CertificateHolder cert = (X509CertificateHolder)certIt.next();
 *
 *          System.out.println("verify returns: " + signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert)));
 *      }
 * </pre>
 *  Note also: this class does not introduce buffering - if you are processing large files you should create
 *  the parser with:
 *  <pre>
 *          CMSSignedDataParser     ep = new CMSSignedDataParser(new BufferedInputStream(encapSigData, bufSize));
 *  </pre>
 *  where bufSize is a suitably large buffer size.
 */
public class CMSSignedDataParser
    extends CMSContentInfoParser
{
    private static final CMSSignedHelper HELPER = CMSSignedHelper.INSTANCE;

    private SignedDataParser        _signedData;
    private ASN1ObjectIdentifier    _signedContentType;
    private CMSTypedStream          _signedContent;
    private Map                     _digests;
    
    private CertStore               _certStore;
    private SignerInformationStore  _signerInfoStore;
    private X509Store               _attributeStore;
    private ASN1Set                 _certSet, _crlSet;
    private boolean                 _isCertCrlParsed;
    private X509Store               _certificateStore;
    private X509Store               _crlStore;

    public CMSSignedDataParser(
        byte[]      sigBlock)
        throws CMSException
    {
        this(new ByteArrayInputStream(sigBlock));
    }

    public CMSSignedDataParser(
        CMSTypedStream  signedContent,
        byte[]          sigBlock)
        throws CMSException
    {
        this(signedContent, new ByteArrayInputStream(sigBlock));
    }

    /**
     * base constructor - with encapsulated content
     */
    public CMSSignedDataParser(
        InputStream sigData)
        throws CMSException
    {
        this(null, sigData);
    }

    /**
     * base constructor
     *
     * @param signedContent the content that was signed.
     * @param sigData the signature object stream.
     */
    public CMSSignedDataParser(
        CMSTypedStream  signedContent,
        InputStream     sigData) 
        throws CMSException
    {
        super(sigData);
        
        try
        {
            _signedContent = signedContent;
            _signedData = SignedDataParser.getInstance(_contentInfo.getContent(DERTags.SEQUENCE));
            _digests = new HashMap();
            
            ASN1SetParser digAlgs = _signedData.getDigestAlgorithms();
            DEREncodable  o;
            
            while ((o = digAlgs.readObject()) != null)
            {
                AlgorithmIdentifier id = AlgorithmIdentifier.getInstance(o.getDERObject());
                try
                {
                    String        digestName = HELPER.getDigestAlgName(id.getObjectId().toString());
                    MessageDigest dig = HELPER.getDigestInstance(digestName, null);

                    this._digests.put(digestName, dig);
                }
                catch (NoSuchAlgorithmException e)
                {
                     //  ignore
                }
            }

            //
            // If the message is simply a certificate chain message getContent() may return null.
            //
            ContentInfoParser     cont = _signedData.getEncapContentInfo();
            ASN1OctetStringParser octs = (ASN1OctetStringParser)
                cont.getContent(DERTags.OCTET_STRING);

            if (octs != null)
            {
                CMSTypedStream ctStr = new CMSTypedStream(
                    cont.getContentType().getId(), octs.getOctetStream());

                if (_signedContent == null)
                {
                    _signedContent = ctStr; 
                }
                else
                {
                    //
                    // content passed in, need to read past empty encapsulated content info object if present
                    //
                    ctStr.drain();
                }
            }

            if (signedContent == null)
            {
                _signedContentType = cont.getContentType();
            }
            else
            {
                _signedContentType = _signedContent.getContentType();
            }
        }
        catch (IOException e)
        {
            throw new CMSException("io exception: " + e.getMessage(), e);
        }
        
        if (_digests.isEmpty())
        {
            throw new CMSException("no digests could be created for message.");
        }
    }

    /**
     * Return the version number for the SignedData object
     *
     * @return the version number
     */
    public int getVersion()
    {
        return _signedData.getVersion().getValue().intValue();
    }

    /**
     * return the collection of signers that are associated with the
     * signatures for the message.
     * @throws CMSException 
     */
    public SignerInformationStore getSignerInfos() 
        throws CMSException
    {
        if (_signerInfoStore == null)
        {
            populateCertCrlSets();
            
            List      signerInfos = new ArrayList();
            Map       hashes = new HashMap();
            
            Iterator  it = _digests.keySet().iterator();
            while (it.hasNext())
            {
                Object digestKey = it.next();
                
                hashes.put(digestKey, ((MessageDigest)_digests.get(digestKey)).digest());
            }
            
            try
            {
                ASN1SetParser     s = _signedData.getSignerInfos();
                DEREncodable      o;
                SignatureAlgorithmIdentifierFinder sigAlgFinder = new DefaultSignatureAlgorithmIdentifierFinder();
                
                while ((o = s.readObject()) != null)
                {
                    SignerInfo info = SignerInfo.getInstance(o.getDERObject());
                    String     digestName = HELPER.getDigestAlgName(info.getDigestAlgorithm().getAlgorithm().getId());
                    
                    byte[] hash = (byte[])hashes.get(digestName);
                    
                    signerInfos.add(new SignerInformation(info, _signedContentType, null, new BaseDigestCalculator(hash), sigAlgFinder));
                }
            }
            catch (IOException e)
            {
                throw new CMSException("io exception: " + e.getMessage(), e);
            }

            _signerInfoStore = new SignerInformationStore(signerInfos);
        }

        return _signerInfoStore;
    }

    /**
     * return a X509Store containing the attribute certificates, if any, contained
     * in this message.
     *
     * @param type type of store to create
     * @param provider name of provider to use
     * @return a store of attribute certificates
     * @exception NoSuchProviderException if the provider requested isn't available.
     * @exception old.org.bouncycastle.x509.NoSuchStoreException if the store type isn't available.
     * @exception CMSException if a general exception prevents creation of the X509Store
     */
    public X509Store getAttributeCertificates(
        String type,
        String provider)
        throws NoSuchStoreException, NoSuchProviderException, CMSException
    {
        return getAttributeCertificates(type, CMSUtils.getProvider(provider));
    }

    /**
     * return a X509Store containing the attribute certificates, if any, contained
     * in this message.
     *
     * @param type type of store to create
     * @param provider provider to use
     * @return a store of attribute certificates
     * @exception old.org.bouncycastle.x509.NoSuchStoreException if the store type isn't available.
     * @exception CMSException if a general exception prevents creation of the X509Store
     */
    public X509Store getAttributeCertificates(
        String type,
        Provider provider)
        throws NoSuchStoreException, CMSException
    {
        if (_attributeStore == null)
        {
            populateCertCrlSets();

            _attributeStore = HELPER.createAttributeStore(type, provider, _certSet);
        }

        return _attributeStore;
    }

    /**
     * return a X509Store containing the public key certificates, if any, contained
     * in this message.
     *
     * @param type type of store to create
     * @param provider provider to use
     * @return a store of public key certificates
     * @exception NoSuchProviderException if the provider requested isn't available.
     * @exception NoSuchStoreException if the store type isn't available.
     * @exception CMSException if a general exception prevents creation of the X509Store
     * @deprecated use getCertificates()
     */
    public X509Store getCertificates(
        String type,
        String provider)
        throws NoSuchStoreException, NoSuchProviderException, CMSException
    {
        return getCertificates(type, CMSUtils.getProvider(provider));
    }

    /**
     * return a X509Store containing the public key certificates, if any, contained
     * in this message.
     *
     * @param type type of store to create
     * @param provider provider to use
     * @return a store of public key certificates
     * @exception NoSuchStoreException if the store type isn't available.
     * @exception CMSException if a general exception prevents creation of the X509Store
     * @deprecated use getCertificates()
     */
    public X509Store getCertificates(
        String type,
        Provider provider)
        throws NoSuchStoreException, CMSException
    {
        if (_certificateStore == null)
        {
            populateCertCrlSets();

            _certificateStore = HELPER.createCertificateStore(type, provider, _certSet);
        }

        return _certificateStore;
    }

    /**
     * return a X509Store containing CRLs, if any, contained
     * in this message.
     *
     * @param type type of store to create
     * @param provider name of provider to use
     * @return a store of CRLs
     * @exception NoSuchProviderException if the provider requested isn't available.
     * @exception NoSuchStoreException if the store type isn't available.
     * @exception CMSException if a general exception prevents creation of the X509Store
     * @deprecated use getCRLs()
     */
    public X509Store getCRLs(
        String type,
        String provider)
        throws NoSuchStoreException, NoSuchProviderException, CMSException
    {
        return getCRLs(type, CMSUtils.getProvider(provider));
    }

    /**
     * return a X509Store containing CRLs, if any, contained
     * in this message.
     *
     * @param type type of store to create
     * @param provider provider to use
     * @return a store of CRLs
     * @exception NoSuchStoreException if the store type isn't available.
     * @exception CMSException if a general exception prevents creation of the X509Store
     * @deprecated use getCRLs()
     */
    public X509Store getCRLs(
        String type,
        Provider provider)
        throws NoSuchStoreException, CMSException
    {
        if (_crlStore == null)
        {
            populateCertCrlSets();

            _crlStore = HELPER.createCRLsStore(type, provider, _crlSet);
        }

        return _crlStore;
    }

    /**
     * return a CertStore containing the certificates and CRLs associated with
     * this message.
     *
     * @exception NoSuchProviderException if the provider requested isn't available.
     * @exception NoSuchAlgorithmException if the cert store isn't available.
     * @exception CMSException if a general exception prevents creation of the CertStore
     * @deprecated use getCertificates()
     */
    public CertStore getCertificatesAndCRLs(
        String  type,
        String  provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, CMSException
    {
        return getCertificatesAndCRLs(type, CMSUtils.getProvider(provider));
    }

    /**
     * return a CertStore containing the certificates and CRLs associated with
     * this message.
     *
     * @exception NoSuchProviderException if the provider requested isn't available.
     * @exception NoSuchAlgorithmException if the cert store isn't available.
     * @exception CMSException if a general exception prevents creation of the CertStore
     * @deprecated use getCertificates()
     */
    public CertStore getCertificatesAndCRLs(
        String  type,
        Provider  provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, CMSException
    {
        populateCertCrlSets();

        return HELPER.createCertStore(type, provider, _certSet, _crlSet);
    }

    public Store getCertificates()
        throws CMSException
    {
        populateCertCrlSets();

        ASN1Set certSet = _certSet;

        if (certSet != null)
        {
            List    certList = new ArrayList(certSet.size());

            for (Enumeration en = certSet.getObjects(); en.hasMoreElements();)
            {
                DERObject obj = ((DEREncodable)en.nextElement()).getDERObject();

                if (obj instanceof ASN1Sequence)
                {
                    certList.add(new X509CertificateHolder(X509CertificateStructure.getInstance(obj)));
                }
            }

            return new CollectionStore(certList);
        }

        return new CollectionStore(new ArrayList());
    }

    public Store getCRLs()
        throws CMSException
    {
        populateCertCrlSets();

        ASN1Set crlSet = _crlSet;

        if (crlSet != null)
        {
            List    crlList = new ArrayList(crlSet.size());

            for (Enumeration en = crlSet.getObjects(); en.hasMoreElements();)
            {
                DERObject obj = ((DEREncodable)en.nextElement()).getDERObject();

                if (obj instanceof ASN1Sequence)
                {
                    crlList.add(new X509CRLHolder(CertificateList.getInstance(obj)));
                }
            }

            return new CollectionStore(crlList);
        }

        return new CollectionStore(new ArrayList());
    }

    public Store getAttributeCertificates()
        throws CMSException
    {
        populateCertCrlSets();

        ASN1Set certSet = _certSet;

        if (certSet != null)
        {
            List    certList = new ArrayList(certSet.size());

            for (Enumeration en = certSet.getObjects(); en.hasMoreElements();)
            {
                DERObject obj = ((DEREncodable)en.nextElement()).getDERObject();

                if (obj instanceof ASN1TaggedObject)
                {
                    ASN1TaggedObject tagged = (ASN1TaggedObject)obj;

                    if (tagged.getTagNo() == 2)
                    {
                        certList.add(new X509AttributeCertificateHolder(AttributeCertificate.getInstance(ASN1Sequence.getInstance(tagged, false))));
                    }
                }
            }

            return new CollectionStore(certList);
        }

        return new CollectionStore(new ArrayList());
    }

    private void populateCertCrlSets()
        throws CMSException
    {
        if (_isCertCrlParsed)
        {
            return;
        }

        _isCertCrlParsed = true;

        try
        {
            // care! Streaming - these must be done in exactly this order.
            _certSet = getASN1Set(_signedData.getCertificates());
            _crlSet = getASN1Set(_signedData.getCrls());
        }
        catch (IOException e)
        {
            throw new CMSException("problem parsing cert/crl sets", e);
        }
    }

    /**
     * Return the a string representation of the OID associated with the
     * encapsulated content info structure carried in the signed data.
     * 
     * @return the OID for the content type.
     */
    public String getSignedContentTypeOID()
    {
        return _signedContentType.getId();
    }

    public CMSTypedStream getSignedContent()
    {
        if (_signedContent == null)
        {
            return null;
        }

        InputStream digStream = CMSUtils.attachDigestsToInputStream(
            _digests.values(), _signedContent.getContentStream());

        return new CMSTypedStream(_signedContent.getContentType(), digStream);
    }

    /**
     * Replace the signerinformation store associated with the passed
     * in message contained in the stream original with the new one passed in.
     * You would probably only want to do this if you wanted to change the unsigned
     * attributes associated with a signer, or perhaps delete one.
     * <p>
     * The output stream is returned unclosed.
     * </p>
     * @param original the signed data stream to be used as a base.
     * @param signerInformationStore the new signer information store to use.
     * @param out the stream to write the new signed data object to.
     * @return out.
     */
    public static OutputStream replaceSigners(
        InputStream             original,
        SignerInformationStore  signerInformationStore,
        OutputStream            out)
        throws CMSException, IOException
    {
        ASN1StreamParser in = new ASN1StreamParser(original, CMSUtils.getMaximumMemory());
        ContentInfoParser contentInfo = new ContentInfoParser((ASN1SequenceParser)in.readObject());
        SignedDataParser signedData = SignedDataParser.getInstance(contentInfo.getContent(DERTags.SEQUENCE));

        BERSequenceGenerator sGen = new BERSequenceGenerator(out);

        sGen.addObject(CMSObjectIdentifiers.signedData);

        BERSequenceGenerator sigGen = new BERSequenceGenerator(sGen.getRawOutputStream(), 0, true);

        // version number
        sigGen.addObject(signedData.getVersion());

        // digests
        signedData.getDigestAlgorithms().getDERObject();  // skip old ones

        ASN1EncodableVector digestAlgs = new ASN1EncodableVector();

        for (Iterator it = signerInformationStore.getSigners().iterator(); it.hasNext();)
        {
            SignerInformation signer = (SignerInformation)it.next();
            digestAlgs.add(CMSSignedHelper.INSTANCE.fixAlgID(signer.getDigestAlgorithmID()));
        }

        sigGen.getRawOutputStream().write(new DERSet(digestAlgs).getEncoded());

        // encap content info
        ContentInfoParser encapContentInfo = signedData.getEncapContentInfo();

        BERSequenceGenerator eiGen = new BERSequenceGenerator(sigGen.getRawOutputStream());

        eiGen.addObject(encapContentInfo.getContentType());

        pipeEncapsulatedOctetString(encapContentInfo, eiGen.getRawOutputStream());

        eiGen.close();


        writeSetToGeneratorTagged(sigGen, signedData.getCertificates(), 0);
        writeSetToGeneratorTagged(sigGen, signedData.getCrls(), 1);


        ASN1EncodableVector signerInfos = new ASN1EncodableVector();
        for (Iterator it = signerInformationStore.getSigners().iterator(); it.hasNext();)
        {
            SignerInformation        signer = (SignerInformation)it.next();

            signerInfos.add(signer.toSignerInfo());
        }

        sigGen.getRawOutputStream().write(new DERSet(signerInfos).getEncoded());

        sigGen.close();

        sGen.close();

        return out;
    }

    /**
     * Replace the certificate and CRL information associated with this
     * CMSSignedData object with the new one passed in.
     * <p>
     * The output stream is returned unclosed.
     * </p>
     * @param original the signed data stream to be used as a base.
     * @param certsAndCrls the new certificates and CRLs to be used.
     * @param out the stream to write the new signed data object to.
     * @return out.
     * @exception CMSException if there is an error processing the CertStore
     * @deprecated use method that takes Store objects.
     */
    public static OutputStream replaceCertificatesAndCRLs(
        InputStream   original,
        CertStore     certsAndCrls,
        OutputStream  out)
        throws CMSException, IOException
    {
        ASN1StreamParser in = new ASN1StreamParser(original, CMSUtils.getMaximumMemory());
        ContentInfoParser contentInfo = new ContentInfoParser((ASN1SequenceParser)in.readObject());
        SignedDataParser signedData = SignedDataParser.getInstance(contentInfo.getContent(DERTags.SEQUENCE));

        BERSequenceGenerator sGen = new BERSequenceGenerator(out);

        sGen.addObject(CMSObjectIdentifiers.signedData);

        BERSequenceGenerator sigGen = new BERSequenceGenerator(sGen.getRawOutputStream(), 0, true);

        // version number
        sigGen.addObject(signedData.getVersion());

        // digests
        sigGen.getRawOutputStream().write(signedData.getDigestAlgorithms().getDERObject().getEncoded());

        // encap content info
        ContentInfoParser encapContentInfo = signedData.getEncapContentInfo();

        BERSequenceGenerator eiGen = new BERSequenceGenerator(sigGen.getRawOutputStream());

        eiGen.addObject(encapContentInfo.getContentType());

        pipeEncapsulatedOctetString(encapContentInfo, eiGen.getRawOutputStream());

        eiGen.close();

        //
        // skip existing certs and CRLs
        //
        getASN1Set(signedData.getCertificates());
        getASN1Set(signedData.getCrls());

        //
        // replace the certs and crls in the SignedData object
        //
        ASN1Set certs;

        try
        {
            certs = CMSUtils.createBerSetFromList(CMSUtils.getCertificatesFromStore(certsAndCrls));
        }
        catch (CertStoreException e)
        {
            throw new CMSException("error getting certs from certStore", e);
        }

        if (certs.size() > 0)
        {
            sigGen.getRawOutputStream().write(new DERTaggedObject(false, 0, certs).getEncoded());
        }

        ASN1Set crls;

        try
        {
            crls = CMSUtils.createBerSetFromList(CMSUtils.getCRLsFromStore(certsAndCrls));
        }
        catch (CertStoreException e)
        {
            throw new CMSException("error getting crls from certStore", e);
        }

        if (crls.size() > 0)
        {
            sigGen.getRawOutputStream().write(new DERTaggedObject(false, 1, crls).getEncoded());
        }

        sigGen.getRawOutputStream().write(signedData.getSignerInfos().getDERObject().getEncoded());

        sigGen.close();

        sGen.close();

        return out;
    }

    /**
     * Replace the certificate and CRL information associated with this
     * CMSSignedData object with the new one passed in.
     * <p>
     * The output stream is returned unclosed.
     * </p>
     * @param original the signed data stream to be used as a base.
     * @param certs new certificates to be used, if any.
     * @param crls new CRLs to be used, if any.
     * @param attrCerts new attribute certificates to be used, if any.
     * @param out the stream to write the new signed data object to.
     * @return out.
     * @exception CMSException if there is an error processing the CertStore
     */
    public static OutputStream replaceCertificatesAndCRLs(
        InputStream   original,
        Store         certs,
        Store         crls,
        Store         attrCerts,
        OutputStream  out)
        throws CMSException, IOException
    {
        ASN1StreamParser in = new ASN1StreamParser(original, CMSUtils.getMaximumMemory());
        ContentInfoParser contentInfo = new ContentInfoParser((ASN1SequenceParser)in.readObject());
        SignedDataParser signedData = SignedDataParser.getInstance(contentInfo.getContent(DERTags.SEQUENCE));

        BERSequenceGenerator sGen = new BERSequenceGenerator(out);

        sGen.addObject(CMSObjectIdentifiers.signedData);

        BERSequenceGenerator sigGen = new BERSequenceGenerator(sGen.getRawOutputStream(), 0, true);

        // version number
        sigGen.addObject(signedData.getVersion());

        // digests
        sigGen.getRawOutputStream().write(signedData.getDigestAlgorithms().getDERObject().getEncoded());

        // encap content info
        ContentInfoParser encapContentInfo = signedData.getEncapContentInfo();

        BERSequenceGenerator eiGen = new BERSequenceGenerator(sigGen.getRawOutputStream());

        eiGen.addObject(encapContentInfo.getContentType());

        pipeEncapsulatedOctetString(encapContentInfo, eiGen.getRawOutputStream());

        eiGen.close();

        //
        // skip existing certs and CRLs
        //
        getASN1Set(signedData.getCertificates());
        getASN1Set(signedData.getCrls());

        //
        // replace the certs and crls in the SignedData object
        //
        if (certs != null || attrCerts != null)
        {
            List certificates = new ArrayList();

            if (certs != null)
            {
                certificates.addAll(CMSUtils.getCertificatesFromStore(certs));
            }
            if (attrCerts != null)
            {
                certificates.addAll(CMSUtils.getAttributeCertificatesFromStore(attrCerts));
            }

            ASN1Set asn1Certs = CMSUtils.createBerSetFromList(certificates);

            if (asn1Certs.size() > 0)
            {
                sigGen.getRawOutputStream().write(new DERTaggedObject(false, 0, asn1Certs).getEncoded());
            }
        }

        if (crls != null)
        {
            ASN1Set asn1Crls = CMSUtils.createBerSetFromList(CMSUtils.getCRLsFromStore(crls));

            if (asn1Crls.size() > 0)
            {
                sigGen.getRawOutputStream().write(new DERTaggedObject(false, 1, asn1Crls).getEncoded());
            }
        }

        sigGen.getRawOutputStream().write(signedData.getSignerInfos().getDERObject().getEncoded());

        sigGen.close();

        sGen.close();

        return out;
    }

    private static void writeSetToGeneratorTagged(
        ASN1Generator asn1Gen,
        ASN1SetParser asn1SetParser,
        int           tagNo)
        throws IOException
    {
        ASN1Set asn1Set = getASN1Set(asn1SetParser);

        if (asn1Set != null)
        {
            ASN1TaggedObject taggedObj = (asn1SetParser instanceof BERSetParser)
                ?   new BERTaggedObject(false, tagNo, asn1Set)
                :   new DERTaggedObject(false, tagNo, asn1Set);

            asn1Gen.getRawOutputStream().write(taggedObj.getEncoded());                
        }
    }

    private static ASN1Set getASN1Set(
        ASN1SetParser asn1SetParser)
    {
        return asn1SetParser == null
            ?   null
            :   ASN1Set.getInstance(asn1SetParser.getDERObject());
    }

    private static void pipeEncapsulatedOctetString(ContentInfoParser encapContentInfo,
        OutputStream rawOutputStream) throws IOException
    {
        ASN1OctetStringParser octs = (ASN1OctetStringParser)
            encapContentInfo.getContent(DERTags.OCTET_STRING);

        if (octs != null)
        {
            pipeOctetString(octs, rawOutputStream);
        }

//        BERTaggedObjectParser contentObject = (BERTaggedObjectParser)encapContentInfo.getContentObject();
//        if (contentObject != null)
//        {
//            // Handle IndefiniteLengthInputStream safely
//            InputStream input = ASN1StreamParser.getSafeRawInputStream(contentObject.getContentStream(true));
//
//            // TODO BerTaggedObjectGenerator?
//            BEROutputStream berOut = new BEROutputStream(rawOutputStream);
//            berOut.write(DERTags.CONSTRUCTED | DERTags.TAGGED | 0);
//            berOut.write(0x80);
//
//            pipeRawOctetString(input, rawOutputStream);
//
//            berOut.write(0x00);
//            berOut.write(0x00);
//
//            input.close();
//        }
    }

    private static void pipeOctetString(
        ASN1OctetStringParser octs,
        OutputStream          output)
        throws IOException
    {
        // TODO Allow specification of a specific fragment size?
        OutputStream outOctets = CMSUtils.createBEROctetOutputStream(
            output, 0, true, 0);
        Streams.pipeAll(octs.getOctetStream(), outOctets);
        outOctets.close();
    }

//    private static void pipeRawOctetString(
//        InputStream     rawInput,
//        OutputStream    rawOutput)
//        throws IOException
//    {
//        InputStream tee = new TeeInputStream(rawInput, rawOutput);
//        ASN1StreamParser sp = new ASN1StreamParser(tee);
//        ASN1OctetStringParser octs = (ASN1OctetStringParser)sp.readObject();
//        Streams.drain(octs.getOctetStream());
//    }
}
