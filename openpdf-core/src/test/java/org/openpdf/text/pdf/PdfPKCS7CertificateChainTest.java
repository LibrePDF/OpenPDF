package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;

/**
 * {@link PdfPKCS7#verifyCertificates} verifies its first certificate, and only accepts a certification path whose
 * issuing certificates are certification authorities.
 */
class PdfPKCS7CertificateChainTest {

    private static final String ROOT = "CN=Test Root";
    private static final String INTERMEDIATE = "CN=Test Intermediate";
    private static final String SIGNER = "CN=Test Signer with a longer name, O=Test Organisation, C=NO";
    private static final String OTHER = "CN=Somebody Else";

    private static int serial = 1;
    private static KeyStore trusted;
    private static KeyPair signerKeys;
    private static KeyPair otherKeys;
    private static X509Certificate root;
    private static X509Certificate intermediate;
    private static X509Certificate signer;
    private static X509Certificate other;

    @BeforeAll
    static void createCertificates() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        KeyPair rootKeys = generator.generateKeyPair();
        KeyPair intermediateKeys = generator.generateKeyPair();
        signerKeys = generator.generateKeyPair();
        otherKeys = generator.generateKeyPair();
        root = issue(ROOT, rootKeys.getPublic(), ROOT, rootKeys.getPrivate(), true, 0);
        intermediate = issue(INTERMEDIATE, intermediateKeys.getPublic(), ROOT, rootKeys.getPrivate(), true, 0);
        signer = issue(SIGNER, signerKeys.getPublic(), INTERMEDIATE, intermediateKeys.getPrivate(), false, 0);
        other = issue(OTHER, otherKeys.getPublic(), ROOT, rootKeys.getPrivate(), false, 0);
        trusted = KeyStore.getInstance(KeyStore.getDefaultType());
        trusted.load(null, null);
        trusted.setCertificateEntry("root", root);
    }

    private static X509Certificate issue(String subject, PublicKey key, String issuer, PrivateKey issuerKey,
            boolean authority, int validityOffsetDays) throws Exception {
        long day = 86_400_000L;
        Date from = new Date(System.currentTimeMillis() - day + validityOffsetDays * day);
        Date to = new Date(System.currentTimeMillis() + day + validityOffsetDays * day);
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(new X500Name(issuer),
                BigInteger.valueOf(serial++), from, to, new X500Name(subject), key);
        if (authority) {
            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        }
        return new JcaX509CertificateConverter().getCertificate(
                builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(issuerKey)));
    }

    private static Object[] verify(Certificate... certs) {
        return PdfPKCS7.verifyCertificates(certs, trusted, null, null);
    }

    @Test
    void chainToATrustedRootIsAccepted() {
        assertNull(verify(signer, intermediate));
        assertNull(verify(signer, intermediate, root));
    }

    @Test
    void orderOfTheOtherCertificatesDoesNotMatter() {
        assertNull(verify(signer, other, root, intermediate));
    }

    @Test
    void trustedCertificateItselfIsAccepted() {
        assertNull(verify(root));
    }

    @Test
    void certificateWithoutAPathIsRejected() {
        Object[] result = verify(signer);
        assertNotNull(result);
        assertEquals(signer, result[0]);
    }

    @Test
    void selfSignedCertificateIsRejectedAlsoWhenATrustedCertificateFollows() throws Exception {
        KeyPair keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        X509Certificate selfSigned = issue(SIGNER, keys.getPublic(), SIGNER, keys.getPrivate(), false, 0);
        assertNotNull(verify(selfSigned));
        assertNotNull(verify(selfSigned, other, intermediate, root));
    }

    @Test
    void certificateIssuedByACertificateThatIsNotAnAuthorityIsRejected() throws Exception {
        KeyPair keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        X509Certificate issuedByEndEntity = issue(SIGNER, keys.getPublic(), OTHER, otherKeys.getPrivate(), false, 0);
        Object[] result = verify(issuedByEndEntity, other);
        assertNotNull(result);
        assertEquals(issuedByEndEntity, result[0]);
    }

    @Test
    void expiredCertificateIsRejected() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        KeyPair keys = generator.generateKeyPair();
        KeyPair untrustedRootKeys = generator.generateKeyPair();
        X509Certificate expired = issue(SIGNER, keys.getPublic(), ROOT, untrustedRootKeys.getPrivate(), false, -10);
        assertNotNull(verify(expired));
        // valid at the date it is checked for, but not issued by a trusted certificate
        Calendar tenDaysAgo = new GregorianCalendar();
        tenDaysAgo.add(Calendar.DAY_OF_YEAR, -10);
        assertNotNull(PdfPKCS7.verifyCertificates(new Certificate[]{expired}, trusted, null, tenDaysAgo));
    }

    @Test
    void noCertificateIsRejected() {
        assertNotNull(verify());
    }

    @Test
    void signingCertificateIsTheFirstOfTheCertificatesOfASignature() throws Exception {
        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, plain);
        document.open();
        document.add(new Paragraph("The signed text."));
        document.close();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfStamper stamper = PdfStamper.createSignature(new PdfReader(plain.toByteArray()), out, (String) null);
        // the certificates are passed with the signing certificate first; in the file they are a sorted set
        stamper.getSignatureAppearance().setCrypto(signerKeys.getPrivate(),
                new Certificate[]{signer, intermediate, root}, null, PdfSignatureAppearance.WINCER_SIGNED);
        stamper.close();

        try (PdfReader reader = new PdfReader(out.toByteArray())) {
            AcroFields fields = reader.getAcroFields();
            PdfPKCS7 pkcs7 = fields.verifySignature(fields.getSignedFieldNames().get(0));
            assertTrue(pkcs7.verify());
            Certificate[] certificates = pkcs7.getCertificates();
            assertEquals(3, certificates.length);
            assertEquals(signer, certificates[0]);
            assertEquals(pkcs7.getSigningCertificate(), certificates[0]);
            assertNull(PdfPKCS7.verifyCertificates(certificates, trusted, null, null));
        }
    }
}
