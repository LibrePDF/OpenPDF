package old.org.bouncycastle.cert;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.AttCertValidityPeriod;
import old.org.bouncycastle.asn1.x509.Attribute;
import old.org.bouncycastle.asn1.x509.AttributeCertificate;
import old.org.bouncycastle.asn1.x509.AttributeCertificateInfo;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.operator.ContentVerifier;
import old.org.bouncycastle.operator.ContentVerifierProvider;

/**
 * Holding class for an X.509 AttributeCertificate structure.
 */
public class X509AttributeCertificateHolder
{
    private static Attribute[] EMPTY_ARRAY = new Attribute[0];
    
    private AttributeCertificate attrCert;
    private X509Extensions extensions;

    private static AttributeCertificate parseBytes(byte[] certEncoding)
        throws IOException
    {
        try
        {
            return AttributeCertificate.getInstance(ASN1Object.fromByteArray(certEncoding));
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
     * Create a X509AttributeCertificateHolder from the passed in bytes.
     *
     * @param certEncoding BER/DER encoding of the certificate.
     * @throws IOException in the event of corrupted data, or an incorrect structure.
     */
    public X509AttributeCertificateHolder(byte[] certEncoding)
        throws IOException
    {
        this(parseBytes(certEncoding));
    }

    /**
     * Create a X509AttributeCertificateHolder from the passed in ASN.1 structure.
     *
     * @param attrCert an ASN.1 AttributeCertificate structure.
     */
    public X509AttributeCertificateHolder(AttributeCertificate attrCert)
    {
        this.attrCert = attrCert;
        this.extensions = attrCert.getAcinfo().getExtensions();
    }

    /**
     * Return the ASN.1 encoding of this holder's attribute certificate.
     *
     * @return a DER encoded byte array.
     * @throws IOException if an encoding cannot be generated.
     */
    public byte[] getEncoded()
        throws IOException
    {
        return attrCert.getEncoded();
    }

    public int getVersion()
    {
        return attrCert.getAcinfo().getVersion().getValue().intValue() + 1;
    }

    /**
     * Return the serial number of this attribute certificate.
     *
     * @return the serial number.
     */
    public BigInteger getSerialNumber()
    {
        return attrCert.getAcinfo().getSerialNumber().getValue();
    }

    /**
     * Return the holder details for this attribute certificate.
     *
     * @return this attribute certificate's holder structure.
     */
    public AttributeCertificateHolder getHolder()
    {
        return new AttributeCertificateHolder((ASN1Sequence)attrCert.getAcinfo().getHolder().toASN1Object());
    }

    /**
     * Return the issuer details for this attribute certificate.
     *
     * @return this attribute certificate's issuer structure,
     */
    public AttributeCertificateIssuer getIssuer()
    {
        return new AttributeCertificateIssuer(attrCert.getAcinfo().getIssuer());
    }

    /**
     * Return the date before which this attribute certificate is not valid.
     *
     * @return the start date for the attribute certificate's validity period.
     */
    public Date getNotBefore()
    {
        return CertUtils.recoverDate(attrCert.getAcinfo().getAttrCertValidityPeriod().getNotBeforeTime());
    }

    /**
     * Return the date after which this attribute certificate is not valid.
     *
     * @return the final date for the attribute certificate's validity period.
     */
    public Date getNotAfter()
    {
        return CertUtils.recoverDate(attrCert.getAcinfo().getAttrCertValidityPeriod().getNotAfterTime());
    }

    /**
     * Return the attributes, if any associated with this request.
     *
     * @return an array of Attribute, zero length if none present.
     */
    public Attribute[] getAttributes()
    {
        ASN1Sequence seq = attrCert.getAcinfo().getAttributes();
        Attribute[] attrs = new Attribute[seq.size()];

        for (int i = 0; i != seq.size(); i++)
        {
            attrs[i] = Attribute.getInstance(seq.getObjectAt(i));
        }

        return attrs;
    }

    /**
     * Return an  array of attributes matching the passed in type OID.
     *
     * @param type the type of the attribute being looked for.
     * @return an array of Attribute of the requested type, zero length if none present.
     */
    public Attribute[] getAttributes(ASN1ObjectIdentifier type)
    {
        ASN1Sequence    seq = attrCert.getAcinfo().getAttributes();
        List            list = new ArrayList();

        for (int i = 0; i != seq.size(); i++)
        {
            Attribute attr = Attribute.getInstance(seq.getObjectAt(i));
            if (attr.getAttrType().equals(type))
            {
                list.add(attr);
            }
        }

        if (list.size() == 0)
        {
            return EMPTY_ARRAY;
        }

        return (Attribute[])list.toArray(new Attribute[list.size()]);
    }

    /**
     * Return whether or not the holder's attribute certificate contains extensions.
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
     * extensions contained in this holder's attribute certificate.
     *
     * @return a list of extension OIDs.
     */
    public List getExtensionOIDs()
    {
        return CertUtils.getExtensionOIDs(extensions);
    }

    /**
     * Returns a set of ASN1ObjectIdentifier objects representing the OIDs of the
     * critical extensions contained in this holder's attribute certificate.
     *
     * @return a set of critical extension OIDs.
     */
    public Set getCriticalExtensionOIDs()
    {
        return CertUtils.getCriticalExtensionOIDs(extensions);
    }

    /**
     * Returns a set of ASN1ObjectIdentifier objects representing the OIDs of the
     * non-critical extensions contained in this holder's attribute certificate.
     *
     * @return a set of non-critical extension OIDs.
     */
    public Set getNonCriticalExtensionOIDs()
    {
        return CertUtils.getNonCriticalExtensionOIDs(extensions);
    }

    public boolean[] getIssuerUniqueID()
    {
        return CertUtils.bitStringToBoolean(attrCert.getAcinfo().getIssuerUniqueID());
    }

    /**
     * Return the details of the signature algorithm used to create this attribute certificate.
     *
     * @return the AlgorithmIdentifier describing the signature algorithm used to create this attribute certificate.
     */
    public AlgorithmIdentifier getSignatureAlgorithm()
    {
        return attrCert.getSignatureAlgorithm();
    }

    /**
     * Return the bytes making up the signature associated with this attribute certificate.
     *
     * @return the attribute certificate signature bytes.
     */
    public byte[] getSignature()
    {
        return attrCert.getSignatureValue().getBytes();
    }

    /**
     * Return the underlying ASN.1 structure for the attribute certificate in this holder.
     *
     * @return a AttributeCertificate object.
     */
    public AttributeCertificate toASN1Structure()
    {
        return attrCert;
    }

    /**
     * Return whether or not this attribute certificate is valid on a particular date.
     *
     * @param date the date of interest.
     * @return true if the attribute certificate is valid, false otherwise.
     */
    public boolean isValidOn(Date date)
    {
        AttCertValidityPeriod certValidityPeriod = attrCert.getAcinfo().getAttrCertValidityPeriod();

        return !date.before(CertUtils.recoverDate(certValidityPeriod.getNotBeforeTime())) && !date.after(CertUtils.recoverDate(certValidityPeriod.getNotAfterTime()));
    }

    /**
     * Validate the signature on the attribute certificate in this holder.
     *
     * @param verifierProvider a ContentVerifierProvider that can generate a verifier for the signature.
     * @return true if the signature is valid, false otherwise.
     * @throws CertException if the signature cannot be processed or is inappropriate.
     */
    public boolean isSignatureValid(ContentVerifierProvider verifierProvider)
        throws CertException
    {
        AttributeCertificateInfo acinfo = attrCert.getAcinfo();

        if (!acinfo.getSignature().equals(attrCert.getSignatureAlgorithm()))
        {
            throw new CertException("signature invalid - algorithm identifier mismatch");
        }

        ContentVerifier verifier;

        try
        {
            verifier = verifierProvider.get((acinfo.getSignature()));

            OutputStream sOut = verifier.getOutputStream();

            sOut.write(acinfo.getDEREncoded());

            sOut.close();
        }
        catch (Exception e)
        {
            throw new CertException("unable to process signature: " + e.getMessage(), e);
        }

        return verifier.verify(attrCert.getSignatureValue().getBytes());
    }

    public boolean equals(
        Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof X509AttributeCertificateHolder))
        {
            return false;
        }

        X509AttributeCertificateHolder other = (X509AttributeCertificateHolder)o;

        return this.attrCert.equals(other.attrCert);
    }

    public int hashCode()
    {
        return this.attrCert.hashCode();
    }
}
