package org.openpdf.text.pdf.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Round-trip tests for PDF 2.0 public-key encryption (V=5 / R=6, AES-256-CBC)
 * as specified in ISO 32000-2 §7.6.5.
 */
class PubSecV5Test {

    @TempDir
    File tempDir;

    @BeforeAll
    static void installBc() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Test
    void pubSecV5EncryptAndDecryptRoundTrip() throws Exception {
        KeyPair kp = generateRsaKeyPair();
        X509Certificate cert = selfSignedCert(kp);

        byte[] pdfBytes = createEncryptedPdf(cert);

        File tmp = new File(tempDir, "pubsec_v5.pdf");
        Files.write(tmp.toPath(), pdfBytes);

        try (PdfReader reader = new PdfReader(tmp.getAbsolutePath(), cert, kp.getPrivate(), "BC")) {
            assertTrue(reader.isEncrypted(), "Document should be marked encrypted");
            assertEquals(
                    "PDF 2.0 PUBSEC test",
                    new PdfTextExtractor(reader).getTextFromPage(1),
                    "Decrypted text must match what was written");
        }
    }

    @Test
    void pubSecV5EncryptionDictionaryHasCorrectVersion() throws Exception {
        KeyPair kp = generateRsaKeyPair();
        X509Certificate cert = selfSignedCert(kp);
        byte[] pdfBytes = createEncryptedPdf(cert);

        // Open without decrypting to inspect the encryption dictionary
        File tmp = new File(tempDir, "pubsec_v5_dict.pdf");
        Files.write(tmp.toPath(), pdfBytes);

        try (PdfReader reader = new PdfReader(tmp.getAbsolutePath(), cert, kp.getPrivate(), "BC")) {
            assertTrue(reader.isEncrypted());
            // V=5 / R=6 maps to crypto mode 4 (ENCRYPTION_AES_256_V3)
            int mode = reader.getCryptoMode() & 0x0F;
            assertEquals(PdfWriter.ENCRYPTION_AES_256_V3, mode,
                    "Crypto mode should be AES_256_V3 (V=5)");
        }
    }

    // ---- helpers ----

    private static byte[] createEncryptedPdf(X509Certificate cert) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setEncryption(
                new java.security.cert.Certificate[]{cert},
                new int[]{PdfWriter.ALLOW_PRINTING},
                PdfWriter.ENCRYPTION_AES_256_V3);
        doc.open();
        doc.add(new Paragraph("PDF 2.0 PUBSEC test"));
        doc.close();
        return baos.toByteArray();
    }

    private static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

    private static X509Certificate selfSignedCert(KeyPair kp)
            throws Exception {
        X500Name subject = new X500Name("CN=OpenPDF Test, O=Test, C=NO");
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + 365L * 24 * 60 * 60 * 1000);

        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                subject, serial, notBefore, notAfter, subject, kp.getPublic());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build(kp.getPrivate());

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(builder.build(signer));
    }
}
