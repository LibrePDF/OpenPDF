package old.org.bouncycastle.cert.jcajce;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.cert.X509v3CertificateBuilder;

/**
 * JCA helper class to allow JCA objects to be used in the construction of a Version 3 certificate.
 */
public class JcaX509v3CertificateBuilder
    extends X509v3CertificateBuilder
{
    /**
     * Initialise the builder using a PublicKey.
     *
     * @param issuer X500Name representing the issuer of this certificate.
     * @param serial the serial number for the certificate.
     * @param notBefore date before which the certificate is not valid.
     * @param notAfter date after which the certificate is not valid.
     * @param subject X500Name representing the subject of this certificate.
     * @param publicKey the public key to be associated with the certificate.
     */
    public JcaX509v3CertificateBuilder(X500Name issuer, BigInteger serial, Date notBefore, Date notAfter, X500Name subject, PublicKey publicKey)
    {
        super(issuer, serial, notBefore, notAfter, subject, SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }

    /**
     * Initialise the builder using X500Principal objects and a PublicKey.
     *
     * @param issuer principal representing the issuer of this certificate.
     * @param serial the serial number for the certificate.
     * @param notBefore date before which the certificate is not valid.
     * @param notAfter date after which the certificate is not valid.
     * @param subject principal representing the subject of this certificate.
     * @param publicKey the public key to be associated with the certificate.
     */
    public JcaX509v3CertificateBuilder(X500Principal issuer, BigInteger serial, Date notBefore, Date notAfter, X500Principal subject, PublicKey publicKey)
    {
        super(X500Name.getInstance(issuer.getEncoded()), serial, notBefore, notAfter, X500Name.getInstance(subject.getEncoded()), SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }

    /**
     * Initialise the builder using the subject from the passed in issuerCert as the issuer, as well as
     * passing through and converting the other objects provided.
     *
     * @param issuerCert certificate who's subject is the issuer of the certificate we are building.
     * @param serial the serial number for the certificate.
     * @param notBefore date before which the certificate is not valid.
     * @param notAfter date after which the certificate is not valid.
     * @param subject principal representing the subject of this certificate.
     * @param publicKey the public key to be associated with the certificate.
     */
    public JcaX509v3CertificateBuilder(X509Certificate issuerCert, BigInteger serial, Date notBefore, Date notAfter, X500Principal subject, PublicKey publicKey)
    {
        this(issuerCert.getSubjectX500Principal(), serial, notBefore, notAfter, subject, publicKey);
    }

    /**
     * Add a given extension field for the standard extensions tag (tag 3)
     * copying the extension value from another certificate.
     *
     * @param oid the type of the extension to be copied.
     * @param critical true if the extension is to be marked critical, false otherwise.
     * @param certificate the source of the extension to be copied.
     * @return the builder instance.
     */
    public JcaX509v3CertificateBuilder copyAndAddExtension(
        ASN1ObjectIdentifier oid,
        boolean critical,
        X509Certificate certificate)
        throws CertificateEncodingException
    {
        this.copyAndAddExtension(oid, critical, new JcaX509CertificateHolder(certificate));

        return this;
    }
}
