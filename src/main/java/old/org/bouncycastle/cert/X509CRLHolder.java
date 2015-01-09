package old.org.bouncycastle.cert;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.CertificateList;
import old.org.bouncycastle.asn1.x509.TBSCertList;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.operator.ContentVerifier;
import old.org.bouncycastle.operator.ContentVerifierProvider;

/**
 * Holding class for an X.509 CRL structure.
 */
public class X509CRLHolder
{
    private CertificateList x509CRL;
    private X509Extensions extensions;

    private static CertificateList parseBytes(byte[] crlEncoding)
        throws IOException
    {
        try
        {
            return CertificateList.getInstance(ASN1Object.fromByteArray(crlEncoding));
        }
        catch (ClassCastException e)
        {
            throw new CertIOException("malformed data: " + e.getMessage(), e);
        }
        catch (IllegalArgumentException e)
        {
            throw new CertIOException("malformed data: " + e.getMessage(), e);
        }
    }

    /**
     * Create a X509CRLHolder from the passed in bytes.
     *
     * @param crlEncoding BER/DER encoding of the CRL
     * @throws IOException in the event of corrupted data, or an incorrect structure.
     */
    public X509CRLHolder(byte[] crlEncoding)
        throws IOException
    {
        this(parseBytes(crlEncoding));
    }

    /**
     * Create a X509CRLHolder from the passed in ASN.1 structure.
     *
     * @param x509CRL an ASN.1 CertificateList structure.
     */
    public X509CRLHolder(CertificateList x509CRL)
    {
        this.x509CRL = x509CRL;
        this.extensions = x509CRL.getTBSCertList().getExtensions();
    }

    /**
     * Return the ASN.1 encoding of this holder's CRL.
     *
     * @return a DER encoded byte array.
     * @throws IOException if an encoding cannot be generated.
     */
    public byte[] getEncoded()
        throws IOException
    {
        return x509CRL.getEncoded();
    }

    /**
     * Return the issuer of this holder's CRL.
     *
     * @return the CRL issuer.
     */
    public X500Name getIssuer()
    {
        return X500Name.getInstance(x509CRL.getIssuer());
    }

    public X509CRLEntryHolder getRevokedCertificate(BigInteger serialNumber)
    {
        for (Enumeration en = x509CRL.getRevokedCertificateEnumeration(); en.hasMoreElements();)
        {
            TBSCertList.CRLEntry entry = (TBSCertList.CRLEntry)en.nextElement();

            if (entry.getUserCertificate().getValue().equals(serialNumber))
            {
                return new X509CRLEntryHolder(entry);
            }
        }

        return null;
    }

    /**
     * Return a collection of X509CRLEntryHolder objects, giving the details of the
     * revoked certificates that appear on this CRL.
     *
     * @return the revoked certificates as a collection of X509CRLEntryHolder objects.
     */
    public Collection getRevokedCertificates()
    {
        TBSCertList.CRLEntry[] entries = x509CRL.getRevokedCertificates();
        List l = new ArrayList(entries.length);

        for (Enumeration en = x509CRL.getRevokedCertificateEnumeration(); en.hasMoreElements();)
        {
            TBSCertList.CRLEntry entry = (TBSCertList.CRLEntry)en.nextElement();


                l.add(new X509CRLEntryHolder(entry));

        }

        return l;
    }
    
    /**
     * Return whether or not the holder's CRL contains extensions.
     *
     * @return true if extension are present, false otherwise.
     */
    public boolean hasExtensions()
    {
        return extensions != null;
    }

    /**
     * Look up the extension associated with the passed in OID.
     *
     * @param oid the OID of the extension of interest.
     *
     * @return the extension if present, null otherwise.
     */
    public X509Extension getExtension(ASN1ObjectIdentifier oid)
    {
        if (extensions != null)
        {
            return extensions.getExtension(oid);
        }

        return null;
    }

    /**
     * Returns a list of ASN1ObjectIdentifier objects representing the OIDs of the
     * extensions contained in this holder's CRL.
     *
     * @return a list of extension OIDs.
     */
    public List getExtensionOIDs()
    {
        return CertUtils.getExtensionOIDs(extensions);
    }

    /**
     * Returns a set of ASN1ObjectIdentifier objects representing the OIDs of the
     * critical extensions contained in this holder's CRL.
     *
     * @return a set of critical extension OIDs.
     */
    public Set getCriticalExtensionOIDs()
    {
        return CertUtils.getCriticalExtensionOIDs(extensions);
    }

    /**
     * Returns a set of ASN1ObjectIdentifier objects representing the OIDs of the
     * non-critical extensions contained in this holder's CRL.
     *
     * @return a set of non-critical extension OIDs.
     */
    public Set getNonCriticalExtensionOIDs()
    {
        return CertUtils.getNonCriticalExtensionOIDs(extensions);
    }

    /**
     * Return the underlying ASN.1 structure for the CRL in this holder.
     *
     * @return a CertificateList object.
     */
    public CertificateList toASN1Structure()
    {
        return x509CRL;
    }

    /**
     * Validate the signature on the CRL.
     *
     * @param verifierProvider a ContentVerifierProvider that can generate a verifier for the signature.
     * @return true if the signature is valid, false otherwise.
     * @throws CertException if the signature cannot be processed or is inappropriate.
     */
    public boolean isSignatureValid(ContentVerifierProvider verifierProvider)
        throws CertException
    {
        TBSCertList tbsCRL = x509CRL.getTBSCertList();

        if (!tbsCRL.getSignature().equals(x509CRL.getSignatureAlgorithm()))
        {
            throw new CertException("signature invalid - algorithm identifier mismatch");
        }

        ContentVerifier verifier;

        try
        {
            verifier = verifierProvider.get((tbsCRL.getSignature()));

            OutputStream sOut = verifier.getOutputStream();

            sOut.write(tbsCRL.getDEREncoded());

            sOut.close();
        }
        catch (Exception e)
        {
            throw new CertException("unable to process signature: " + e.getMessage(), e);
        }

        return verifier.verify(x509CRL.getSignature().getBytes());
    }

    public boolean equals(
        Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof X509CRLHolder))
        {
            return false;
        }

        X509CRLHolder other = (X509CRLHolder)o;

        return this.x509CRL.equals(other.x509CRL);
    }

    public int hashCode()
    {
        return this.x509CRL.hashCode();
    }
}
