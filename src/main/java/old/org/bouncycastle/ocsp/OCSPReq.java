package old.org.bouncycastle.ocsp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1OutputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ocsp.OCSPRequest;
import old.org.bouncycastle.asn1.ocsp.Request;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;

/**
 * <pre>
 * OCSPRequest     ::=     SEQUENCE {
 *       tbsRequest                  TBSRequest,
 *       optionalSignature   [0]     EXPLICIT Signature OPTIONAL }
 *
 *   TBSRequest      ::=     SEQUENCE {
 *       version             [0]     EXPLICIT Version DEFAULT v1,
 *       requestorName       [1]     EXPLICIT GeneralName OPTIONAL,
 *       requestList                 SEQUENCE OF Request,
 *       requestExtensions   [2]     EXPLICIT Extensions OPTIONAL }
 *
 *   Signature       ::=     SEQUENCE {
 *       signatureAlgorithm      AlgorithmIdentifier,
 *       signature               BIT STRING,
 *       certs               [0] EXPLICIT SEQUENCE OF Certificate OPTIONAL}
 *
 *   Version         ::=             INTEGER  {  v1(0) }
 *
 *   Request         ::=     SEQUENCE {
 *       reqCert                     CertID,
 *       singleRequestExtensions     [0] EXPLICIT Extensions OPTIONAL }
 *
 *   CertID          ::=     SEQUENCE {
 *       hashAlgorithm       AlgorithmIdentifier,
 *       issuerNameHash      OCTET STRING, -- Hash of Issuer's DN
 *       issuerKeyHash       OCTET STRING, -- Hash of Issuers public key
 *       serialNumber        CertificateSerialNumber }
 * </pre>
 */
public class OCSPReq
    implements java.security.cert.X509Extension
{
    private OCSPRequest       req;

    public OCSPReq(
        OCSPRequest req)
    {
        this.req = req;
    }
    
    public OCSPReq(
        byte[]          req)
        throws IOException
    {
        this(new ASN1InputStream(req));
    }

    public OCSPReq(
        InputStream     in)
        throws IOException
    {
        this(new ASN1InputStream(in));
    }

    private OCSPReq(
        ASN1InputStream aIn) 
        throws IOException
    {
        try
        {
            this.req = OCSPRequest.getInstance(aIn.readObject());
        }
        catch (IllegalArgumentException e)
        {
            throw new IOException("malformed request: " + e.getMessage());
        }
        catch (ClassCastException e)
        {
            throw new IOException("malformed request: " + e.getMessage());
        }
    }

    /**
     * Return the DER encoding of the tbsRequest field.
     * @return DER encoding of tbsRequest
     * @throws OCSPException in the event of an encoding error.
     */
    public byte[] getTBSRequest()
        throws OCSPException
    {
        try
        {
            return req.getTbsRequest().getEncoded();
        }
        catch (IOException e)
        {
            throw new OCSPException("problem encoding tbsRequest", e);
        }
    }
    
    public int getVersion()
    {
        return req.getTbsRequest().getVersion().getValue().intValue() + 1;
    }
    
    public GeneralName getRequestorName()
    {
        return GeneralName.getInstance(req.getTbsRequest().getRequestorName());
    }

    public Req[] getRequestList()
    {
        ASN1Sequence    seq = req.getTbsRequest().getRequestList();
        Req[]           requests = new Req[seq.size()];

        for (int i = 0; i != requests.length; i++)
        {
            requests[i] = new Req(Request.getInstance(seq.getObjectAt(i)));
        }

        return requests;
    }

    public X509Extensions getRequestExtensions()
    {
        return X509Extensions.getInstance(req.getTbsRequest().getRequestExtensions());
    }

    /**
     * return the object identifier representing the signature algorithm
     */
    public String getSignatureAlgOID()
    {
        if (!this.isSigned())
        {
            return null;
        }

        return req.getOptionalSignature().getSignatureAlgorithm().getObjectId().getId();
    }

    public byte[] getSignature()
    {
        if (!this.isSigned())
        {
            return null;
        }

        return req.getOptionalSignature().getSignature().getBytes();
    }
    
    private List getCertList(
        String provider) 
        throws OCSPException, NoSuchProviderException
    {
        List                  certs = new ArrayList();
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ASN1OutputStream      aOut = new ASN1OutputStream(bOut);
        CertificateFactory    cf;

        try
        {
            cf = OCSPUtil.createX509CertificateFactory(provider);
        }
        catch (CertificateException ex)
        {
            throw new OCSPException("can't get certificate factory.", ex);
        }

        //
        // load the certificates if we have any
        //
        ASN1Sequence s = req.getOptionalSignature().getCerts();

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
        if (!this.isSigned())
        {
            return null;
        }
    
        List         certs = this.getCertList(provider);
        
        return (X509Certificate[])certs.toArray(new X509Certificate[certs.size()]);
    }
    
    /**
     * If the request is signed return a possibly empty CertStore containing the certificates in the
     * request. If the request is not signed the method returns null.
     * 
     * @param type type of CertStore to return
     * @param provider provider to use
     * @return null if not signed, a CertStore otherwise
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws OCSPException
     */
    public CertStore getCertificates(
        String type,
        String provider) 
        throws NoSuchAlgorithmException, NoSuchProviderException, OCSPException
    {
        if (!this.isSigned())
        {
            return null;
        }
        
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
     * Return whether or not this request is signed.
     * 
     * @return true if signed false otherwise.
     */
    public boolean isSigned()
    {
        return req.getOptionalSignature() != null;
    }

    /**
     * verify the signature against the TBSRequest object we contain.
     */
    public boolean verify(
        PublicKey   key,
        String      sigProvider)
        throws OCSPException, NoSuchProviderException
    {
        if (!this.isSigned())
        {
            throw new OCSPException("attempt to verify signature on unsigned object");
        }

        try
        {
            Signature signature = OCSPUtil.createSignatureInstance(this.getSignatureAlgOID(), sigProvider);

            signature.initVerify(key);

            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            ASN1OutputStream        aOut = new ASN1OutputStream(bOut);

            aOut.writeObject(req.getTbsRequest());

            signature.update(bOut.toByteArray());

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
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        ASN1OutputStream        aOut = new ASN1OutputStream(bOut);

        aOut.writeObject(req);

        return bOut.toByteArray();
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
        X509Extensions  extensions = this.getRequestExtensions();
        
        if (extensions != null)
        {
            Enumeration     e = extensions.oids();
    
            while (e.hasMoreElements())
            {
                ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier)e.nextElement();
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
        X509Extensions exts = this.getRequestExtensions();

        if (exts != null)
        {
            X509Extension   ext = exts.getExtension(new ASN1ObjectIdentifier(oid));

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
}
