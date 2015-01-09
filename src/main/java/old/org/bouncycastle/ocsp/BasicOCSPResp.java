package old.org.bouncycastle.ocsp;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1OutputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import old.org.bouncycastle.asn1.ocsp.ResponseData;
import old.org.bouncycastle.asn1.ocsp.SingleResponse;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <pre>
 * BasicOCSPResponse       ::= SEQUENCE {
 *    tbsResponseData      ResponseData,
 *    signatureAlgorithm   AlgorithmIdentifier,
 *    signature            BIT STRING,
 *    certs                [0] EXPLICIT SEQUENCE OF Certificate OPTIONAL }
 * </pre>
 */
public class BasicOCSPResp
    implements java.security.cert.X509Extension
{
    BasicOCSPResponse   resp;
    ResponseData        data;
    X509Certificate[]   chain = null;

    public BasicOCSPResp(
        BasicOCSPResponse   resp)
    {
        this.resp = resp;
        this.data = resp.getTbsResponseData();
    }

    /**
     * Return the DER encoding of the tbsResponseData field.
     * @return DER encoding of tbsResponseData
     * @throws OCSPException in the event of an encoding error.
     */
    public byte[] getTBSResponseData()
        throws OCSPException
    {
        try
        {
            return resp.getTbsResponseData().getEncoded();
        }
        catch (IOException e)
        {
            throw new OCSPException("problem encoding tbsResponseData", e);
        }
    }
    
    public int getVersion()
    {
        return data.getVersion().getValue().intValue() + 1;
    }

    public RespID getResponderId()
    {
        return new RespID(data.getResponderID());
    }

    public Date getProducedAt()
    {
        try
        {
            return data.getProducedAt().getDate();
        }
        catch (ParseException e)
        {
            throw new IllegalStateException("ParseException:" + e.getMessage());
        }
    }

    public SingleResp[] getResponses()
    {
        ASN1Sequence    s = data.getResponses();
        SingleResp[]    rs = new SingleResp[s.size()];

        for (int i = 0; i != rs.length; i++)
        {
            rs[i] = new SingleResp(SingleResponse.getInstance(s.getObjectAt(i)));
        }

        return rs;
    }

    public X509Extensions getResponseExtensions()
    {
        return data.getResponseExtensions();
    }
    
    /**
     * RFC 2650 doesn't specify any critical extensions so we return true
     * if any are encountered.
     * 
     * @return true if any critical extensions are present.
     */
    public boolean hasUnsupportedCriticalExtension()
    {
        Set extns = getCriticalExtensionOIDs();
        if (extns != null && !extns.isEmpty())
        {
            return true;
        }

        return false;
    }

    private Set getExtensionOIDs(boolean critical)
    {
        Set             set = new HashSet();
        X509Extensions  extensions = this.getResponseExtensions();
        
        if (extensions != null)
        {
            Enumeration     e = extensions.oids();
    
            while (e.hasMoreElements())
            {
                DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                X509Extension       ext = extensions.getExtension(oid);
    
                if (critical == ext.isCritical())
                {
                    set.add(oid.getId());
                }
            }
        }

        return set;
    }

    public Set getCriticalExtensionOIDs()
    {
        return getExtensionOIDs(true);
    }

    public Set getNonCriticalExtensionOIDs()
    {
        return getExtensionOIDs(false);
    }

    public byte[] getExtensionValue(String oid)
    {
        X509Extensions exts = this.getResponseExtensions();

        if (exts != null)
        {
            X509Extension   ext = exts.getExtension(new DERObjectIdentifier(oid));

            if (ext != null)
            {
                try
                {
                    return ext.getValue().getEncoded(ASN1Encodable.DER);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("error encoding " + e.toString());
                }
            }
        }

        return null;
    }

    public String getSignatureAlgName()
    {
        return OCSPUtil.getAlgorithmName(resp.getSignatureAlgorithm().getObjectId());
    }

    public String getSignatureAlgOID()
    {
        return resp.getSignatureAlgorithm().getObjectId().getId();
    }

    /**
     * @deprecated RespData class is no longer required as all functionality is
     * available on this class.
     * @return the RespData object
     */
    public RespData getResponseData()
    {
        return new RespData(resp.getTbsResponseData());
    }

    public byte[] getSignature()
    {
        return resp.getSignature().getBytes();
    }

    private List getCertList(
        String provider) 
        throws OCSPException, NoSuchProviderException
    {
        List                    certs = new ArrayList();
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        ASN1OutputStream        aOut = new ASN1OutputStream(bOut);
        CertificateFactory      cf;

        try
        {
            cf = OCSPUtil.createX509CertificateFactory(provider);
        }
        catch (CertificateException ex)
        {
            throw new OCSPException("can't get certificate factory.", ex);
        }

        //
        // load the certificates and revocation lists if we have any
        //
        ASN1Sequence s = resp.getCerts();

        if (s != null)
        {
            Enumeration e = s.getObjects();

            while (e.hasMoreElements())
            {
                try
                {
                    aOut.writeObject(e.nextElement());

                    certs.add(cf.generateCertificate(
                        new ByteArrayInputStream(bOut.toByteArray())));
                }
                catch (IOException ex)
                {
                    throw new OCSPException(
                            "can't re-encode certificate!", ex);
                }
                catch (CertificateException ex)
                {
                    throw new OCSPException(
                            "can't re-encode certificate!", ex);
                }

                bOut.reset();
            }
        }
        
        return certs;
    }
    
    public X509Certificate[] getCerts(
        String  provider)
        throws OCSPException, NoSuchProviderException
    {
        List                    certs = getCertList(provider);
            
        return (X509Certificate[])certs.toArray(new X509Certificate[certs.size()]);
    }

    /**
     * Return the certificates, if any associated with the response.
     * @param type type of CertStore to create
     * @param provider provider to use
     * @return a CertStore, possibly empty
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws OCSPException
     */
    public CertStore getCertificates(
        String type,
        String provider) 
        throws NoSuchAlgorithmException, NoSuchProviderException, OCSPException
    {
        try
        {
            CertStoreParameters params = new CollectionCertStoreParameters(this.getCertList(provider));
            return OCSPUtil.createCertStoreInstance(type, params, provider);
        }
        catch (InvalidAlgorithmParameterException e)
        {
            throw new OCSPException("can't setup the CertStore", e);
        }
    }
    
    /**
     * verify the signature against the tbsResponseData object we contain.
     */
    public boolean verify(
        PublicKey   key,
        String      sigProvider)
        throws OCSPException, NoSuchProviderException
    {
        try
        {
            Signature signature = OCSPUtil.createSignatureInstance(this.getSignatureAlgName(), sigProvider);

            signature.initVerify(key);

            signature.update(resp.getTbsResponseData().getEncoded(ASN1Encodable.DER));

            return signature.verify(this.getSignature());
        }
        catch (NoSuchProviderException e)
        {
            // TODO Why this special case?
            throw e;
        }
        catch (Exception e)
        {
            throw new OCSPException("exception processing sig: " + e, e);
        }
    }

    /**
     * return the ASN.1 encoded representation of this object.
     */
    public byte[] getEncoded()
        throws IOException
    {
    	return resp.getEncoded();
    }
    
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        
        if (!(o instanceof BasicOCSPResp))
        {
            return false;
        }
        
        BasicOCSPResp r = (BasicOCSPResp)o;
        
        return resp.equals(r.resp);
    }
    
    public int hashCode()
    {
        return resp.hashCode();
    }
}
