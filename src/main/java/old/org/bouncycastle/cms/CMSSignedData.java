package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.BERSequence;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.cms.ContentInfo;
import old.org.bouncycastle.asn1.cms.SignedData;
import old.org.bouncycastle.asn1.cms.SignerInfo;
import old.org.bouncycastle.asn1.x509.AttributeCertificate;
import old.org.bouncycastle.asn1.x509.CertificateList;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.cert.X509AttributeCertificateHolder;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import old.org.bouncycastle.operator.SignatureAlgorithmIdentifierFinder;
import old.org.bouncycastle.util.CollectionStore;
import old.org.bouncycastle.util.Store;
import old.org.bouncycastle.x509.NoSuchStoreException;
import old.org.bouncycastle.x509.X509Store;

/**
 * general class for handling a pkcs7-signature message.
 *
 * A simple example of usage - note, in the example below the validity of
 * the certificate isn't verified, just the fact that one of the certs 
 * matches the given signer...
 *
 * <pre>
 *  CertStore               certs = s.getCertificatesAndCRLs("Collection", "BC");
 *  SignerInformationStore  signers = s.getSignerInfos();
 *  Collection              c = signers.getSigners();
 *  Iterator                it = c.iterator();
 *  
 *  while (it.hasNext())
 *  {
 *      SignerInformation   signer = (SignerInformation)it.next();
 *      Collection          certCollection = certStore.getMatches(signer.getSID());
 *
 *      Iterator        certIt = certCollection.iterator();
 *      X509CertificateHolder cert = (X509CertificateHolder)certIt.next();
 *  
 *      if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert)))
 *      {
 *          verified++;
 *      }   
 *  }
 * </pre>
 */
public class CMSSignedData
{
    private static final CMSSignedHelper HELPER = CMSSignedHelper.INSTANCE;
    
    SignedData              signedData;
    ContentInfo             contentInfo;
    CMSProcessable          signedContent;
    SignerInformationStore  signerInfoStore;
    X509Store               attributeStore;
    X509Store               certificateStore;
    X509Store               crlStore;
    private Map             hashes;

    private CMSSignedData(
        CMSSignedData   c)
    {
        this.signedData = c.signedData;
        this.contentInfo = c.contentInfo;
        this.signedContent = c.signedContent;
        this.signerInfoStore = c.signerInfoStore;
    }

    public CMSSignedData(
        byte[]      sigBlock)
        throws CMSException
    {
        this(CMSUtils.readContentInfo(sigBlock));
    }

    public CMSSignedData(
        CMSProcessable  signedContent,
        byte[]          sigBlock)
        throws CMSException
    {
        this(signedContent, CMSUtils.readContentInfo(sigBlock));
    }

    /**
     * Content with detached signature, digests precomputed
     *
     * @param hashes a map of precomputed digests for content indexed by name of hash.
     * @param sigBlock the signature object.
     */
    public CMSSignedData(
        Map     hashes,
        byte[]  sigBlock)
        throws CMSException
    {
        this(hashes, CMSUtils.readContentInfo(sigBlock));
    }

    /**
     * base constructor - content with detached signature.
     *
     * @param signedContent the content that was signed.
     * @param sigData the signature object.
     */
    public CMSSignedData(
        CMSProcessable  signedContent,
        InputStream     sigData)
        throws CMSException
    {
        this(signedContent, CMSUtils.readContentInfo(new ASN1InputStream(sigData)));
    }

    /**
     * base constructor - with encapsulated content
     */
    public CMSSignedData(
        InputStream sigData)
        throws CMSException
    {
        this(CMSUtils.readContentInfo(sigData));
    }

    public CMSSignedData(
        CMSProcessable  signedContent,
        ContentInfo     sigData)
    {
        this.signedContent = signedContent;
        this.contentInfo = sigData;
        this.signedData = SignedData.getInstance(contentInfo.getContent());
    }

    public CMSSignedData(
        Map             hashes,
        ContentInfo     sigData)
    {
        this.hashes = hashes;
        this.contentInfo = sigData;
        this.signedData = SignedData.getInstance(contentInfo.getContent());
    }

    public CMSSignedData(
        ContentInfo sigData)
    {
        this.contentInfo = sigData;
        this.signedData = SignedData.getInstance(contentInfo.getContent());

        //
        // this can happen if the signed message is sent simply to send a
        // certificate chain.
        //
        if (signedData.getEncapContentInfo().getContent() != null)
        {
            this.signedContent = new CMSProcessableByteArray(
                    ((ASN1OctetString)(signedData.getEncapContentInfo()
                                                .getContent())).getOctets());
        }
        else
        {
            this.signedContent = null;
        }
    }

    /**
     * Return the version number for this object
     */
    public int getVersion()
    {
        return signedData.getVersion().getValue().intValue();
    }

    /**
     * return the collection of signers that are associated with the
     * signatures for the message.
     */
    public SignerInformationStore getSignerInfos()
    {
        if (signerInfoStore == null)
        {
            ASN1Set         s = signedData.getSignerInfos();
            List            signerInfos = new ArrayList();
            SignatureAlgorithmIdentifierFinder sigAlgFinder = new DefaultSignatureAlgorithmIdentifierFinder();

            for (int i = 0; i != s.size(); i++)
            {
                SignerInfo info = SignerInfo.getInstance(s.getObjectAt(i));
                ASN1ObjectIdentifier contentType = signedData.getEncapContentInfo().getContentType();

                if (hashes == null)
                {
                    signerInfos.add(new SignerInformation(info, contentType, signedContent, null, sigAlgFinder));
                }
                else
                {

                    byte[] hash = (byte[])hashes.get(info.getDigestAlgorithm().getAlgorithm().getId());

                    signerInfos.add(new SignerInformation(info, contentType, null, new BaseDigestCalculator(hash), sigAlgFinder));
                }
            }

            signerInfoStore = new SignerInformationStore(signerInfos);
        }

        return signerInfoStore;
    }

    /**
     * return a X509Store containing the attribute certificates, if any, contained
     * in this message.
     *
     * @param type type of store to create
     * @param provider name of provider to use
     * @return a store of attribute certificates
     * @exception NoSuchProviderException if the provider requested isn't available.
     * @exception NoSuchStoreException if the store type isn't available.
     * @exception CMSException if a general exception prevents creation of the X509Store
     * @deprecated use base Store returning method
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
     * @exception NoSuchStoreException if the store type isn't available.
     * @exception CMSException if a general exception prevents creation of the X509Store
     * @deprecated use base Store returning method
     */
    public X509Store getAttributeCertificates(
        String type,
        Provider provider)
        throws NoSuchStoreException, CMSException
    {
        if (attributeStore == null)
        {
            attributeStore = HELPER.createAttributeStore(type, provider, signedData.getCertificates());
        }

        return attributeStore;
    }

    /**
     * return a X509Store containing the public key certificates, if any, contained
     * in this message.
     *
     * @param type type of store to create
     * @param provider name of provider to use
     * @return a store of public key certificates
     * @exception NoSuchProviderException if the provider requested isn't available.
     * @exception NoSuchStoreException if the store type isn't available.
     * @exception CMSException if a general exception prevents creation of the X509Store
     * @deprecated use base Store returning method
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
     * @deprecated use base Store returning method
     */
    public X509Store getCertificates(
        String type,
        Provider provider)
        throws NoSuchStoreException, CMSException
    {
        if (certificateStore == null)
        {
            certificateStore = HELPER.createCertificateStore(type, provider, signedData.getCertificates());
        }

        return certificateStore;
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
     * @deprecated use base Store returning method
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
     * @deprecated use base Store returning method
     */
    public X509Store getCRLs(
        String type,
        Provider provider)
        throws NoSuchStoreException, CMSException
    {
        if (crlStore == null)
        {
            crlStore = HELPER.createCRLsStore(type, provider, signedData.getCRLs());
        }

        return crlStore;
    }
  
    /**
     * return a CertStore containing the certificates and CRLs associated with
     * this message.
     *
     * @exception NoSuchProviderException if the provider requested isn't available.
     * @exception NoSuchAlgorithmException if the cert store isn't available.
     * @exception CMSException if a general exception prevents creation of the CertStore
     * @deprecated use base Store returning method
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
     * @exception NoSuchAlgorithmException if the cert store isn't available.
     * @exception CMSException if a general exception prevents creation of the CertStore
     * @deprecated use base Store returning method
     */
    public CertStore getCertificatesAndCRLs(
        String  type,
        Provider  provider)
        throws NoSuchAlgorithmException, CMSException
    {
        ASN1Set certSet = signedData.getCertificates();
        ASN1Set crlSet = signedData.getCRLs();

        return HELPER.createCertStore(type, provider, certSet, crlSet);
    }

    public Store getCertificates()
    {
        ASN1Set certSet = signedData.getCertificates();

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
    {
        ASN1Set crlSet = signedData.getCRLs();

        if (crlSet != null)
        {
            List    crlList = new ArrayList(crlSet.size());

            for (Enumeration en = crlSet.getObjects(); en.hasMoreElements();)
            {
                DERObject obj = ((DEREncodable)en.nextElement()).getDERObject();

                if (obj instanceof ASN1Sequence)
                {
                    crlList.add(CertificateList.getInstance(obj));
                }
            }

            return new CollectionStore(crlList);
        }

        return new CollectionStore(new ArrayList());
    }

    public Store getAttributeCertificates()
    {
        ASN1Set certSet = signedData.getCertificates();

        if (certSet != null)
        {
            List    certList = new ArrayList(certSet.size());

            for (Enumeration en = certSet.getObjects(); en.hasMoreElements();)
            {
                DERObject obj = ((DEREncodable)en.nextElement()).getDERObject();

                if (obj instanceof ASN1TaggedObject)
                {
                    certList.add(new X509AttributeCertificateHolder(AttributeCertificate.getInstance(((ASN1TaggedObject)obj).getObject())));
                }
            }

            return new CollectionStore(certList);
        }

        return new CollectionStore(new ArrayList());
    }

    /**
     * Return the a string representation of the OID associated with the
     * encapsulated content info structure carried in the signed data.
     * 
     * @return the OID for the content type.
     */
    public String getSignedContentTypeOID()
    {
        return signedData.getEncapContentInfo().getContentType().getId();
    }
    
    public CMSProcessable getSignedContent()
    {
        return signedContent;
    }

    /**
     * return the ContentInfo 
     */
    public ContentInfo getContentInfo()
    {
        return contentInfo;
    }

    /**
     * return the ASN.1 encoded representation of this object.
     */
    public byte[] getEncoded()
        throws IOException
    {
        return contentInfo.getEncoded();
    }
    
    /**
     * Replace the signerinformation store associated with this
     * CMSSignedData object with the new one passed in. You would
     * probably only want to do this if you wanted to change the unsigned 
     * attributes associated with a signer, or perhaps delete one.
     * 
     * @param signedData the signed data object to be used as a base.
     * @param signerInformationStore the new signer information store to use.
     * @return a new signed data object.
     */
    public static CMSSignedData replaceSigners(
        CMSSignedData           signedData,
        SignerInformationStore  signerInformationStore)
    {
        //
        // copy
        //
        CMSSignedData   cms = new CMSSignedData(signedData);
        
        //
        // replace the store
        //
        cms.signerInfoStore = signerInformationStore;

        //
        // replace the signers in the SignedData object
        //
        ASN1EncodableVector digestAlgs = new ASN1EncodableVector();
        ASN1EncodableVector vec = new ASN1EncodableVector();
        
        Iterator    it = signerInformationStore.getSigners().iterator();
        while (it.hasNext())
        {
            SignerInformation signer = (SignerInformation)it.next();
            digestAlgs.add(CMSSignedHelper.INSTANCE.fixAlgID(signer.getDigestAlgorithmID()));
            vec.add(signer.toSignerInfo());
        }

        ASN1Set             digests = new DERSet(digestAlgs);
        ASN1Set             signers = new DERSet(vec);
        ASN1Sequence        sD = (ASN1Sequence)signedData.signedData.getDERObject();

        vec = new ASN1EncodableVector();
        
        //
        // signers are the last item in the sequence.
        //
        vec.add(sD.getObjectAt(0)); // version
        vec.add(digests);

        for (int i = 2; i != sD.size() - 1; i++)
        {
            vec.add(sD.getObjectAt(i));
        }
        
        vec.add(signers);
        
        cms.signedData = SignedData.getInstance(new BERSequence(vec));
        
        //
        // replace the contentInfo with the new one
        //
        cms.contentInfo = new ContentInfo(cms.contentInfo.getContentType(), cms.signedData);
        
        return cms;
    }

    /**
     * Replace the certificate and CRL information associated with this
     * CMSSignedData object with the new one passed in.
     * 
     * @param signedData the signed data object to be used as a base.
     * @param certsAndCrls the new certificates and CRLs to be used.
     * @return a new signed data object.
     * @exception CMSException if there is an error processing the CertStore
     */
    public static CMSSignedData replaceCertificatesAndCRLs(
        CMSSignedData   signedData,
        CertStore       certsAndCrls)
        throws CMSException
    {
        //
        // copy
        //
        CMSSignedData   cms = new CMSSignedData(signedData);
        
        //
        // replace the certs and crls in the SignedData object
        //
        ASN1Set             certs = null;
        ASN1Set             crls = null;

        try
        {
            ASN1Set set = CMSUtils.createBerSetFromList(CMSUtils.getCertificatesFromStore(certsAndCrls));

            if (set.size() != 0)
            {
                certs = set;
            }
        }
        catch (CertStoreException e)
        {
            throw new CMSException("error getting certs from certStore", e);
        }

        try
        {
            ASN1Set set = CMSUtils.createBerSetFromList(CMSUtils.getCRLsFromStore(certsAndCrls));

            if (set.size() != 0)
            {
                crls = set;
            }
        }
        catch (CertStoreException e)
        {
            throw new CMSException("error getting crls from certStore", e);
        }
        
        //
        // replace the CMS structure.
        //
        cms.signedData = new SignedData(signedData.signedData.getDigestAlgorithms(), 
                                   signedData.signedData.getEncapContentInfo(),
                                   certs,
                                   crls,
                                   signedData.signedData.getSignerInfos());
        
        //
        // replace the contentInfo with the new one
        //
        cms.contentInfo = new ContentInfo(cms.contentInfo.getContentType(), cms.signedData);
        
        return cms;
    }

    /**
     * Replace the certificate and CRL information associated with this
     * CMSSignedData object with the new one passed in.
     *
     * @param signedData the signed data object to be used as a base.
     * @param certificates the new certificates to be used.
     * @param attrCerts the new attribute certificates to be used.
     * @param crls the new CRLs to be used.
     * @return a new signed data object.
     * @exception CMSException if there is an error processing the CertStore
     */
    public static CMSSignedData replaceCertificatesAndCRLs(
        CMSSignedData   signedData,
        Store           certificates,
        Store           attrCerts,
        Store           crls)
        throws CMSException
    {
        //
        // copy
        //
        CMSSignedData   cms = new CMSSignedData(signedData);

        //
        // replace the certs and crls in the SignedData object
        //
        ASN1Set certSet = null;
        ASN1Set crlSet = null;

        if (certificates != null || attrCerts != null)
        {
            List certs = new ArrayList();

            if (certificates != null)
            {
                certs.addAll(CMSUtils.getCertificatesFromStore(certificates));
            }
            if (attrCerts != null)
            {
                certs.addAll(CMSUtils.getAttributeCertificatesFromStore(attrCerts));   
            }

            ASN1Set set = CMSUtils.createBerSetFromList(certs);

            if (set.size() != 0)
            {
                certSet = set;
            }
        }

        if (crls != null)
        {
            ASN1Set set = CMSUtils.createBerSetFromList(CMSUtils.getCRLsFromStore(crls));

            if (set.size() != 0)
            {
                crlSet = set;
            }
        }

        //
        // replace the CMS structure.
        //
        cms.signedData = new SignedData(signedData.signedData.getDigestAlgorithms(),
                                   signedData.signedData.getEncapContentInfo(),
                                   certSet,
                                   crlSet,
                                   signedData.signedData.getSignerInfos());

        //
        // replace the contentInfo with the new one
        //
        cms.contentInfo = new ContentInfo(cms.contentInfo.getContentType(), cms.signedData);

        return cms;
    }
}
