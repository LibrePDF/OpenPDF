package old.org.bouncycastle.cert.jcajce;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.cert.X509v1CertificateBuilder;

/**
 * JCA helper class to allow JCA objects to be used in the construction of a Version 1 certificate.
 */
public class JcaX509v1CertificateBuilder
    extends X509v1CertificateBuilder
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
    public JcaX509v1CertificateBuilder(X500Name issuer, BigInteger serial, Date notBefore, Date notAfter, X500Name subject, PublicKey publicKey)
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
    public JcaX509v1CertificateBuilder(X500Principal issuer, BigInteger serial, Date notBefore, Date notAfter, X500Principal subject, PublicKey publicKey)
    {
        super(X500Name.getInstance(issuer.getEncoded()), serial, notBefore, notAfter, X500Name.getInstance(subject.getEncoded()), SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }
}
