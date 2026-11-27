package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * {@link PdfPKCS7#isRevocationValid()} only accepts an embedded OCSP response that is about the signing certificate,
 * says that the certificate is good and is signed by the issuer.
 */
class PdfPKCS7RevocationTest {

    private static final int SIGNER_SERIAL = 42;

    private static KeyPair issuerKeys;
    private static KeyPair signerKeys;
    private static KeyPair otherKeys;
    private static X509Certificate issuer;
    private static X509Certificate signer;

    @BeforeAll
    static void createCertificates() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        issuerKeys = generator.generateKeyPair();
        signerKeys = generator.generateKeyPair();
        otherKeys = generator.generateKeyPair();
        issuer = issue("CN=Test Issuer", issuerKeys.getPublic(), "CN=Test Issuer", issuerKeys.getPrivate(), 1, true);
        signer = issue("CN=Test Signer", signerKeys.getPublic(), "CN=Test Issuer", issuerKeys.getPrivate(),
                SIGNER_SERIAL, false);
    }

    private static X509Certificate issue(String subject, PublicKey key, String issuerName, PrivateKey issuerKey,
            int serial, boolean authority) throws Exception {
        Date from = new Date(System.currentTimeMillis() - 86_400_000L);
        Date to = new Date(System.currentTimeMillis() + 86_400_000L);
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(new X500Name(issuerName),
                BigInteger.valueOf(serial), from, to, new X500Name(subject), key);
        if (authority) {
            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        }
        return new JcaX509CertificateConverter().getCertificate(
                builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(issuerKey)));
    }

    private static byte[] ocspResponse(int serial, CertificateStatus status, KeyPair responder) throws Exception {
        DigestCalculatorProvider digests = new JcaDigestCalculatorProviderBuilder().build();
        CertificateID id = new CertificateID(digests.get(CertificateID.HASH_SHA1),
                new JcaX509CertificateHolder(issuer), BigInteger.valueOf(serial));
        BasicOCSPRespBuilder builder = new BasicOCSPRespBuilder(
                SubjectPublicKeyInfo.getInstance(responder.getPublic().getEncoded()),
                digests.get(CertificateID.HASH_SHA1));
        builder.addResponse(id, status);
        return builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(responder.getPrivate()),
                new X509CertificateHolder[0], new Date()).getEncoded();
    }

    /**
     * Signs some data with an embedded OCSP response and reads the signature back.
     */
    private static PdfPKCS7 signatureWith(byte[] ocsp) throws Exception {
        PdfPKCS7 signing = new PdfPKCS7(signerKeys.getPrivate(), new Certificate[]{signer, issuer}, null, "SHA256",
                null, false);
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(new byte[]{1, 2, 3});
        Calendar now = Calendar.getInstance();
        byte[] attributes = signing.getAuthenticatedAttributeBytes(hash, now, ocsp);
        signing.update(attributes, 0, attributes.length);
        return new PdfPKCS7(signing.getEncodedPKCS7(hash, now, null, ocsp), null);
    }

    @Test
    void goodResponseSignedByTheIssuerIsValid() throws Exception {
        assertTrue(signatureWith(ocspResponse(SIGNER_SERIAL, CertificateStatus.GOOD, issuerKeys)).isRevocationValid());
    }

    @Test
    void responseSignedBySomebodyElseIsNotValid() throws Exception {
        assertFalse(signatureWith(ocspResponse(SIGNER_SERIAL, CertificateStatus.GOOD, otherKeys)).isRevocationValid());
    }

    @Test
    void responseThatSaysRevokedIsNotValid() throws Exception {
        CertificateStatus revoked = new RevokedStatus(new Date(), 0);
        assertFalse(signatureWith(ocspResponse(SIGNER_SERIAL, revoked, issuerKeys)).isRevocationValid());
    }

    @Test
    void responseAboutAnotherCertificateIsNotValid() throws Exception {
        assertFalse(signatureWith(ocspResponse(SIGNER_SERIAL + 1, CertificateStatus.GOOD, issuerKeys))
                .isRevocationValid());
    }

    @Test
    void signatureWithoutAResponseIsNotValid() throws Exception {
        assertFalse(signatureWith(null).isRevocationValid());
    }
}
