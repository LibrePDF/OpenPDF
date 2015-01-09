package old.org.bouncycastle.cert;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x509.TBSCertificateStructure;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.operator.ContentVerifier;
import old.org.bouncycastle.operator.ContentVerifierProvider;

/**
 * Holding class for an X.509 Certificate structure.
 */
public class X509CertificateHolder
{
    private X509CertificateStructure x509Certificate;
    private X509Extensions extensions;

    private static X509CertificateStructure parseBytes(byte[] certEncoding)
        throws IOException
    {
        try
        {
            return X509CertificateStructure.getInstance(ASN1Object.fromByteArray(certEncoding));
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
     * Create a X509CertificateHolder from the passed in bytes.
     *
     * @param certEncoding BER/DER encoding of the certificate.
     * @throws IOException in the event of corrupted data, or an incorrect structure.
     */
    public X509CertificateHolder(byte[] certEncoding)
        throws IOException
    {
        this(parseBytes(certEncoding));
    }

    /**
     * Create a X509CertificateHolder from the passed in ASN.1 structure.
     *
     * @param x509Certificate an ASN.1 Certificate structure.
     */
    public X509CertificateHolder(X509CertificateStructure x509Certificate)
    {
        this.x509Certificate = x509Certificate;
        this.extensions = x509Certificate.getTBSCertificate().getExtensions();
    }

    public int getVersion()
    {
        return x509Certificate.getVersion();
    }

    /**
     * Return whether or not the holder's certificate contains extensions.
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
     * extensions contained in this holder's certificate.
     *
     * @return a list of extension OIDs.
     */
    public List getExtensionOIDs()
    {
        return CertUtils.getExtensionOIDs(extensions);
    }

    /**
     * Returns a set of ASN1ObjectIdentifier objects representing the OIDs of the
     * critical extensions contained in this holder's certificate.
     *
     * @return a set of critical extension OIDs.
     */
    public Set getCriticalExtensionOIDs()
    {
        return CertUtils.getCriticalExtensionOIDs(extensions);
    }

    /**
     * Returns a set of ASN1ObjectIdentifier objects representing the OIDs of the
     * non-critical extensions contained in this holder's certificate.
     *
     * @return a set of non-critical extension OIDs.
     */
    public Set getNonCriticalExtensionOIDs()
    {
        return CertUtils.getNonCriticalExtensionOIDs(extensions);
    }

    public IssuerAndSerialNumber getIssuerAndSerialNumber()
    {
        return new IssuerAndSerialNumber(x509Certificate.getIssuer(), x509Certificate.getSerialNumber());
    }

    /**
     * Return the serial number of this attribute certificate.
     *
     * @return the serial number.
     */
    public BigInteger getSerialNumber()
    {
        return x509Certificate.getSerialNumber().getValue();
    }

    /**
     * Return the issuer of this certificate.
     *
     * @return the certificate issuer.
     */
    public X500Name getIssuer()
    {
        return X500Name.getInstance(x509Certificate.getIssuer());
    }

    /**
     * Return the subject this certificate is for.
     *
     * @return the subject for the certificate.
     */
    public X500Name getSubject()
    {
        return X500Name.getInstance(x509Certificate.getSubject());
    }

    /**
     * Return the date before which this certificate is not valid.
     *
     * @return the start time for the certificate's validity period.
     */
    public Date getNotBefore()
    {
        return x509Certificate.getStartDate().getDate();
    }

    /**
     * Return the date after which this certificate is not valid.
     *
     * @return the final time for the certificate's validity period.
     */
    public Date getNotAfter()
    {
        return x509Certificate.getEndDate().getDate();
    }

    /**
     * Return the SubjectPublicKeyInfo describing the public key this certificate is carrying.
     *
     * @return the public key ASN.1 structure contained in the certificate.
     */
    public SubjectPublicKeyInfo getSubjectPublicKeyInfo()
    {
        return x509Certificate.getSubjectPublicKeyInfo();
    }

    /**
     * Return the underlying ASN.1 structure for the certificate in this holder.
     *
     * @return a X509CertificateStructure object.
     */
    public X509CertificateStructure toASN1Structure()
    {
        return x509Certificate;
    }

    /**
     * Return the details of the signature algorithm used to create this attribute certificate.
     *
     * @return the AlgorithmIdentifier describing the signature algorithm used to create this attribute certificate.
     */
    public AlgorithmIdentifier getSignatureAlgorithm()
    {
        return x509Certificate.getSignatureAlgorithm();
    }

    /**
     * Return the bytes making up the signature associated with this attribute certificate.
     *
     * @return the attribute certificate signature bytes.
     */
    public byte[] getSignature()
    {
        return x509Certificate.getSignature().getBytes();
    }

    /**
     * Return whether or not this certificate is valid on a particular date.
     *
     * @param date the date of interest.
     * @return true if the certificate is valid, false otherwise.
     */
    public boolean isValidOn(Date date)
    {
        return !date.before(x509Certificate.getStartDate().getDate()) && !date.after(x509Certificate.getEndDate().getDate());
    }

    /**
     * Validate the signature on the certificate in this holder.
     *
     * @param verifierProvider a ContentVerifierProvider that can generate a verifier for the signature.
     * @return true if the signature is valid, false otherwise.
     * @throws CertException if the signature cannot be processed or is inappropriate.
     */
    public boolean isSignatureValid(ContentVerifierProvider verifierProvider)
        throws CertException
    {
        TBSCertificateStructure tbsCert = x509Certificate.getTBSCertificate();

        if (!tbsCert.getSignature().equals(x509Certificate.getSignatureAlgorithm()))
        {
            throw new CertException("signature invalid - algorithm identifier mismatch");
        }

        ContentVerifier verifier;

        try
        {
            verifier = verifierProvider.get((tbsCert.getSignature()));

            OutputStream sOut = verifier.getOutputStream();

            sOut.write(tbsCert.getDEREncoded());

            sOut.close();
        }
        catch (Exception e)
        {
            throw new CertException("unable to process signature: " + e.getMessage(), e);
        }

        return verifier.verify(x509Certificate.getSignature().getBytes());
    }

    public boolean equals(
        Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof X509CertificateHolder))
        {
            return false;
        }

        X509CertificateHolder other = (X509CertificateHolder)o;

        return this.x509Certificate.equals(other.x509Certificate);
    }

    public int hashCode()
    {
        return this.x509Certificate.hashCode();
    }

    /**
     * Return the ASN.1 encoding of this holder's certificate.
     *
     * @return a DER encoded byte array.
     * @throws IOException if an encoding cannot be generated.
     */
    public byte[] getEncoded()
        throws IOException
    {
        return x509Certificate.getEncoded();
    }
}
