package old.org.bouncycastle.cert;

import java.math.BigInteger;
import java.util.Date;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.DERGeneralizedTime;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.x509.AttCertIssuer;
import old.org.bouncycastle.asn1.x509.Attribute;
import old.org.bouncycastle.asn1.x509.V2AttributeCertificateInfoGenerator;
import old.org.bouncycastle.asn1.x509.X509ExtensionsGenerator;
import old.org.bouncycastle.operator.ContentSigner;

/**
 * class to produce an X.509 Version 2 AttributeCertificate.
 */
public class X509v2AttributeCertificateBuilder
{
    private V2AttributeCertificateInfoGenerator   acInfoGen;
    private X509ExtensionsGenerator               extGenerator;

    public X509v2AttributeCertificateBuilder(AttributeCertificateHolder     holder, AttributeCertificateIssuer  issuer, BigInteger      serialNumber, Date notBefore, Date notAfter)
    {
        acInfoGen = new V2AttributeCertificateInfoGenerator();
        extGenerator = new X509ExtensionsGenerator();

        acInfoGen.setHolder(holder.holder);
        acInfoGen.setIssuer(AttCertIssuer.getInstance(issuer.form));
        acInfoGen.setSerialNumber(new DERInteger(serialNumber));
        acInfoGen.setStartDate(new DERGeneralizedTime(notBefore));
        acInfoGen.setEndDate(new DERGeneralizedTime(notAfter));
    }

    /**
     * Add an attribute to the certification request we are building.
     *
     * @param attrType the OID giving the type of the attribute.
     * @param attrValue the ASN.1 structure that forms the value of the attribute.
     * @return this builder object.
     */
    public X509v2AttributeCertificateBuilder addAttribute(ASN1ObjectIdentifier attrType, ASN1Encodable attrValue)
    {
        acInfoGen.addAttribute(new Attribute(attrType, new DERSet(attrValue)));

        return this;
    }

    /**
     * Add an attribute with multiple values to the certification request we are building.
     *
     * @param attrType the OID giving the type of the attribute.
     * @param attrValues an array of ASN.1 structures that form the value of the attribute.
     * @return this builder object.
     */
    public X509v2AttributeCertificateBuilder addAttribute(ASN1ObjectIdentifier attrType, ASN1Encodable[] attrValues)
    {
        acInfoGen.addAttribute(new Attribute(attrType, new DERSet(attrValues)));

        return this;
    }

    public void setIssuerUniqueId(
        boolean[] iui)
    {
        acInfoGen.setIssuerUniqueID(CertUtils.booleanToBitString(iui));
    }

    /**
     * Add a given extension field for the standard extensions tag
     *
     * @param oid the OID defining the extension type.
     * @param isCritical true if the extension is critical, false otherwise.
     * @param value the ASN.1 structure that forms the extension's value.
     * @return this builder object.
     */
    public X509v2AttributeCertificateBuilder addExtension(
        ASN1ObjectIdentifier oid,
        boolean isCritical,
        ASN1Encodable value)
    {
        extGenerator.addExtension(oid, isCritical, value);

        return this;
    }

   /**
     * Generate an X509 certificate, based on the current issuer and subject
     * using the passed in signer.
     *
     * @param signer the content signer to be used to generate the signature validating the certificate.
     * @return a holder containing the resulting signed certificate.
     */
    public X509AttributeCertificateHolder build(
        ContentSigner signer)
    {
        acInfoGen.setSignature(signer.getAlgorithmIdentifier());

        if (!extGenerator.isEmpty())
        {
            acInfoGen.setExtensions(extGenerator.generate());
        }

        return CertUtils.generateFullAttrCert(signer, acInfoGen.generateAttributeCertificateInfo());
    }
}
