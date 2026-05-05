package org.openpdf.text.pdf.security;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.pdf.PdfPKCS7;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the OID resolution logic in PdfPKCS7.setExternalDigest, which exercises the private
 * resolveSignatureAlgorithmOid method for JCA name → OID and OID → OID lookup.
 */
class SignatureAlgorithmResolutionTest {

    @BeforeAll
    static void installBc() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Test
    void rsaJcaNameIsAccepted() throws Exception {
        PdfPKCS7 pkcs7 = newPkcs7();
        pkcs7.setExternalDigest(new byte[32], null, "RSA");
    }

    @Test
    void dsaJcaNameIsAccepted() throws Exception {
        PdfPKCS7 pkcs7 = newPkcs7();
        pkcs7.setExternalDigest(new byte[32], null, "DSA");
    }

    @Test
    void ecJcaNameIsAccepted() throws Exception {
        PdfPKCS7 pkcs7 = newPkcs7();
        pkcs7.setExternalDigest(new byte[32], null, "EC");
    }

    @Test
    void ecdsaJcaNameIsAccepted() throws Exception {
        PdfPKCS7 pkcs7 = newPkcs7();
        pkcs7.setExternalDigest(new byte[32], null, "ECDSA");
    }

    @Test
    void ed25519JcaNameIsAccepted() throws Exception {
        PdfPKCS7 pkcs7 = newPkcs7();
        pkcs7.setExternalDigest(new byte[32], null, "Ed25519");
    }

    @Test
    void mlDsa65JcaNameIsAccepted() throws Exception {
        PdfPKCS7 pkcs7 = newPkcs7();
        pkcs7.setExternalDigest(new byte[32], null, "ML-DSA-65");
    }

    @Test
    void rsaOidIsAccepted() throws Exception {
        PdfPKCS7 pkcs7 = newPkcs7();
        // RSA OID passed directly
        pkcs7.setExternalDigest(new byte[32], null, SecurityIDs.ID_RSA);
    }

    @Test
    void unknownAlgorithmThrows() throws Exception {
        PdfPKCS7 pkcs7 = newPkcs7();
        assertThrows(ExceptionConverter.class,
                () -> pkcs7.setExternalDigest(new byte[32], null, "BOGUS-ALGO-XYZ"));
    }

    @Test
    void nullAlgorithmIsAccepted() throws Exception {
        PdfPKCS7 pkcs7 = newPkcs7();
        // null algorithm with null digest is a no-op
        pkcs7.setExternalDigest(null, null, null);
    }

    // ---- helpers ----

    /**
     * Creates a minimal PdfPKCS7 using the verifier constructor
     * (no private key — just enough for setExternalDigest to work).
     */
    private static PdfPKCS7 newPkcs7() throws Exception {
        // Use the public constructor that takes a signing cert chain byte[] from a CMS blob.
        // The simplest way to get an instance is through the verifier path.
        // However, since PdfPKCS7 doesn't have a no-arg constructor we use a dummy CMS blob
        // built from a real signed-PDF resource.
        //
        // Alternative: setExternalDigest only touches digestEncryptionAlgorithm which is
        // initialised in the signing constructor. Use the signing constructor with a real key.
        java.security.KeyPairGenerator gen = java.security.KeyPairGenerator.getInstance("RSA");
        gen.initialize(1024);
        java.security.KeyPair kp = gen.generateKeyPair();

        java.math.BigInteger serial = java.math.BigInteger.ONE;
        java.util.Date now = new java.util.Date();
        java.util.Date later = new java.util.Date(now.getTime() + 86400_000L);
        org.bouncycastle.asn1.x500.X500Name subject = new org.bouncycastle.asn1.x500.X500Name("CN=Test");
        org.bouncycastle.operator.ContentSigner signer =
                new org.bouncycastle.operator.jcajce.JcaContentSignerBuilder("SHA256withRSA")
                        .setProvider("BC").build(kp.getPrivate());
        java.security.cert.X509Certificate cert =
                new org.bouncycastle.cert.jcajce.JcaX509CertificateConverter().setProvider("BC")
                        .getCertificate(new org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder(
                                subject, serial, now, later, subject, kp.getPublic()).build(signer));

        return new PdfPKCS7(kp.getPrivate(),
                new java.security.cert.Certificate[]{cert},
                null, "SHA-256", null, false);
    }
}
