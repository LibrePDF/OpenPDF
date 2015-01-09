package old.org.bouncycastle.cert.ocsp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1OutputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ocsp.OCSPRequest;
import old.org.bouncycastle.asn1.ocsp.Request;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.cert.CertIOException;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.operator.ContentVerifier;
import old.org.bouncycastle.operator.ContentVerifierProvider;

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
{
    private static final X509CertificateHolder[] EMPTY_CERTS = new X509CertificateHolder[0];

    private OCSPRequest    req;
    private X509Extensions extensions;

    public OCSPReq(
        OCSPRequest req)
    {
        this.req = req;
        this.extensions = req.getTbsRequest().getRequestExtensions();
    }
    
    public OCSPReq(
        byte[]          req)
        throws IOException
    {
        this(new ASN1InputStream(req));
    }

    private OCSPReq(
        ASN1InputStream aIn)
        throws IOException
    {
        try
        {
            this.req = OCSPRequest.getInstance(aIn.readObject());
            this.extensions = req.getTbsRequest().getRequestExtensions();
        }
        catch (IllegalArgumentException e)
        {
            throw new CertIOException("malformed request: " + e.getMessage(), e);
        }
        catch (ClassCastException e)
        {
            throw new CertIOException("malformed request: " + e.getMessage(), e);
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

    public boolean hasExtensions()
    {
        return extensions != null;
    }

    public X509Extension getExtension(ASN1ObjectIdentifier oid)
    {
        if (extensions != null)
        {
            return extensions.getExtension(oid);
        }

        return null;
    }

    public List getExtensionOIDs()
    {
        return OCSPUtils.getExtensionOIDs(extensions);
    }

    public Set getCriticalExtensionOIDs()
    {
        return OCSPUtils.getCriticalExtensionOIDs(extensions);
    }

    public Set getNonCriticalExtensionOIDs()
    {
        return OCSPUtils.getNonCriticalExtensionOIDs(extensions);
    }

    /**
     * return the object identifier representing the signature algorithm
     */
    public ASN1ObjectIdentifier getSignatureAlgOID()
    {
        if (!this.isSigned())
        {
            return null;
        }

        return req.getOptionalSignature().getSignatureAlgorithm().getAlgorithm();
    }

    public byte[] getSignature()
    {
        if (!this.isSigned())
        {
            return null;
        }

        return req.getOptionalSignature().getSignature().getBytes();
    }

    public X509CertificateHolder[] getCerts()
    {
        //
        // load the certificates if we have any
        //
        if (req.getOptionalSignature() != null)
        {
            ASN1Sequence s = req.getOptionalSignature().getCerts();

            if (s != null)
            {
                X509CertificateHolder[] certs = new X509CertificateHolder[s.size()];

                for (int i = 0; i != certs.length; i++)
                {
                    certs[i] = new X509CertificateHolder(X509CertificateStructure.getInstance(s.getObjectAt(i)));
                }

                return certs;
            }

            return EMPTY_CERTS;
        }
        else
        {
            return EMPTY_CERTS;
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
    public boolean isSignatureValid(
        ContentVerifierProvider verifierProvider)
        throws OCSPException
    {
        if (!this.isSigned())
        {
            throw new OCSPException("attempt to verify signature on unsigned object");
        }

        try
        {
            ContentVerifier verifier = verifierProvider.get(req.getOptionalSignature().getSignatureAlgorithm());
            OutputStream sOut = verifier.getOutputStream();

            sOut.write(req.getTbsRequest().getDEREncoded());

            return verifier.verify(this.getSignature());
        }
        catch (Exception e)
        {
            throw new OCSPException("exception processing signature: " + e, e);
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
}
