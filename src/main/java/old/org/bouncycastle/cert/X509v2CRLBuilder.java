package old.org.bouncycastle.cert;

import java.math.BigInteger;
import java.util.Date;
import java.util.Enumeration;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERGeneralizedTime;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.TBSCertList;
import old.org.bouncycastle.asn1.x509.Time;
import old.org.bouncycastle.asn1.x509.V2TBSCertListGenerator;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.asn1.x509.X509ExtensionsGenerator;
import old.org.bouncycastle.operator.ContentSigner;

/**
 * class to produce an X.509 Version 2 CRL.
 */
public class X509v2CRLBuilder
{
    private V2TBSCertListGenerator      tbsGen;
    private X509ExtensionsGenerator     extGenerator;

    /**
     * Basic constructor.
     *
     * @param issuer the issuer this CRL is associated with.
     * @param thisUpdate  the date of this update.
     */
    public X509v2CRLBuilder(
        X500Name issuer,
        Date     thisUpdate)
    {
        tbsGen = new V2TBSCertListGenerator();
        extGenerator = new X509ExtensionsGenerator();

        tbsGen.setIssuer(issuer);
        tbsGen.setThisUpdate(new Time(thisUpdate));
    }

    /**
     * Set the date by which the next CRL will become available.
     *
     * @param date  date of next CRL update.
     * @return the current builder.
     */
    public X509v2CRLBuilder setNextUpdate(
        Date    date)
    {
        tbsGen.setNextUpdate(new Time(date));

        return this;
    }

    /**
     * Add a CRL entry with the just reasonCode extension.
     *
     * @param userCertificateSerial serial number of revoked certificate.
     * @param revocationDate date of certificate revocation.
     * @param reason the reason code, as indicated in CRLReason, i.e CRLReason.keyCompromise, or 0 if not to be used.
     * @return the current builder.
     */
    public X509v2CRLBuilder addCRLEntry(BigInteger userCertificateSerial, Date revocationDate, int reason)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificateSerial), new Time(revocationDate), reason);

        return this;
    }

    /**
     * Add a CRL entry with an invalidityDate extension as well as a reasonCode extension. This is used
     * where the date of revocation might be after issues with the certificate may have occurred.
     *
     * @param userCertificateSerial serial number of revoked certificate.
     * @param revocationDate date of certificate revocation.
     * @param reason the reason code, as indicated in CRLReason, i.e CRLReason.keyCompromise, or 0 if not to be used.
     * @param invalidityDate the date on which the private key for the certificate became compromised or the certificate otherwise became invalid.
     * @return the current builder.
     */
    public X509v2CRLBuilder addCRLEntry(BigInteger userCertificateSerial, Date revocationDate, int reason, Date invalidityDate)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificateSerial), new Time(revocationDate), reason, new DERGeneralizedTime(invalidityDate));

        return this;
    }
   
    /**
     * Add a CRL entry with extensions.
     *
     * @param userCertificateSerial serial number of revoked certificate.
     * @param revocationDate date of certificate revocation.
     * @param extensions extension set to be associated with this CRLEntry.
     * @return the current builder.
     */
    public X509v2CRLBuilder addCRLEntry(BigInteger userCertificateSerial, Date revocationDate, X509Extensions extensions)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificateSerial), new Time(revocationDate), extensions);

        return this;
    }
    
    /**
     * Add the CRLEntry objects contained in a previous CRL.
     * 
     * @param other the X509CRLHolder to source the other entries from.
     * @return the current builder.
     */
    public X509v2CRLBuilder addCRL(X509CRLHolder other)
    {
        TBSCertList revocations = other.toASN1Structure().getTBSCertList();

        if (revocations != null)
        {
            for (Enumeration en = revocations.getRevokedCertificateEnumeration(); en.hasMoreElements();)
            {
                    tbsGen.addCRLEntry(ASN1Sequence.getInstance(((ASN1Encodable)en.nextElement()).getDERObject()));
            }
        }

        return this;
    }

    /**
     * Add a given extension field for the standard extensions tag (tag 3)
     *
     * @param oid the OID defining the extension type.
     * @param isCritical true if the extension is critical, false otherwise.
     * @param value the ASN.1 structure that forms the extension's value.
     * @return this builder object.
     */
    public X509v2CRLBuilder addExtension(
        ASN1ObjectIdentifier oid,
        boolean isCritical,
        ASN1Encodable value)
    {
        extGenerator.addExtension(oid, isCritical, value);

        return this;
    }

    /**
     * Generate an X.509 CRL, based on the current issuer and subject
     * using the passed in signer.
     *
     * @param signer the content signer to be used to generate the signature validating the certificate.
     * @return a holder containing the resulting signed certificate.
     */
    public X509CRLHolder build(
        ContentSigner signer)
    {
        tbsGen.setSignature(signer.getAlgorithmIdentifier());

        if (!extGenerator.isEmpty())
        {
            tbsGen.setExtensions(extGenerator.generate());
        }

        return CertUtils.generateFullCRL(signer, tbsGen.generateTBSCertList());
    }
}
