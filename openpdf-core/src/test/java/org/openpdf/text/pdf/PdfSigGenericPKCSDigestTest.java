package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;

/**
 * The digest algorithms of the signature filters that sign a document themselves.
 */
class PdfSigGenericPKCSDigestTest {

    private static String signAndGetHashAlgorithm(PdfName filter) throws Exception {
        KeyPair keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        X500Name name = new X500Name("CN=Test Signer");
        Date from = new Date(System.currentTimeMillis() - 86_400_000L);
        Date to = new Date(System.currentTimeMillis() + 86_400_000L);
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(
                new JcaX509v3CertificateBuilder(name, BigInteger.ONE, from, to, name, keys.getPublic())
                        .build(new JcaContentSignerBuilder("SHA256withRSA").build(keys.getPrivate())));

        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, plain);
        document.open();
        document.add(new Paragraph("The signed text."));
        document.close();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfStamper stamper = PdfStamper.createSignature(new PdfReader(plain.toByteArray()), out, (String) null);
        stamper.getSignatureAppearance().setCrypto(keys.getPrivate(), new Certificate[]{certificate}, null, filter);
        stamper.close();

        try (PdfReader reader = new PdfReader(out.toByteArray())) {
            AcroFields fields = reader.getAcroFields();
            PdfPKCS7 pkcs7 = fields.verifySignature(fields.getSignedFieldNames().get(0));
            assertTrue(pkcs7.verify());
            return pkcs7.getHashAlgorithm();
        }
    }

    @Test
    void veriSignFilterSignsWithSha256() throws Exception {
        assertEquals("SHA256", signAndGetHashAlgorithm(PdfSignatureAppearance.VERISIGN_SIGNED));
    }

    @Test
    void filterThatRequiresSha1StillUsesIt() throws Exception {
        // the sub filter adbe.pkcs7.sha1 is defined with SHA-1
        assertEquals("SHA1", signAndGetHashAlgorithm(PdfSignatureAppearance.WINCER_SIGNED));
    }
}
