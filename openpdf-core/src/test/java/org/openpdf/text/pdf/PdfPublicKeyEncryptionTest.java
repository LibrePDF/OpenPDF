package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

/**
 * Documents that are encrypted for the holder of a certificate.
 */
class PdfPublicKeyEncryptionTest {

    private static final String TEXT = "Only for the recipient.";

    private static KeyPair recipientKeys;
    private static X509Certificate recipient;
    private static KeyPair strangerKeys;
    private static X509Certificate stranger;

    @BeforeAll
    static void createCertificates() throws Exception {
        recipientKeys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        recipient = selfSigned("CN=Recipient", BigInteger.ONE, recipientKeys);
        strangerKeys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        stranger = selfSigned("CN=Stranger", BigInteger.TWO, strangerKeys);
    }

    private static X509Certificate selfSigned(String subject, BigInteger serial, KeyPair keys) throws Exception {
        X500Name name = new X500Name(subject);
        Date from = new Date(System.currentTimeMillis() - 86_400_000L);
        Date to = new Date(System.currentTimeMillis() + 86_400_000L);
        return new JcaX509CertificateConverter().getCertificate(
                new JcaX509v3CertificateBuilder(name, serial, from, to, name, keys.getPublic())
                        .build(new JcaContentSignerBuilder("SHA256withRSA").build(keys.getPrivate())));
    }

    private static byte[] encryptFor(X509Certificate certificate, int encryptionType) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        writer.setEncryption(new Certificate[]{certificate}, new int[]{PdfWriter.ALLOW_PRINTING}, encryptionType);
        document.open();
        document.add(new Paragraph(TEXT));
        document.close();
        return out.toByteArray();
    }

    private static String readWith(Path file, X509Certificate certificate, KeyPair keys) throws Exception {
        try (PdfReader reader = new PdfReader(file.toString(), certificate, keys.getPrivate(), null)) {
            return new PdfTextExtractor(reader).getTextFromPage(1);
        }
    }

    @Test
    void recipientCanReadAnAes128Document(@TempDir Path directory) throws Exception {
        Path file = Files.write(directory.resolve("aes128.pdf"), encryptFor(recipient, PdfWriter.ENCRYPTION_AES_128));

        assertThat(readWith(file, recipient, recipientKeys)).contains(TEXT);
    }

    @Test
    void recipientCanReadAnRc4Document(@TempDir Path directory) throws Exception {
        Path file = Files.write(directory.resolve("rc4.pdf"),
                encryptFor(recipient, PdfWriter.STANDARD_ENCRYPTION_128));

        assertThat(readWith(file, recipient, recipientKeys)).contains(TEXT);
    }

    @Test
    void anotherCertificateCannotReadTheDocument(@TempDir Path directory) throws Exception {
        Path file = Files.write(directory.resolve("other.pdf"), encryptFor(recipient, PdfWriter.ENCRYPTION_AES_128));

        assertThatThrownBy(() -> readWith(file, stranger, strangerKeys)).isInstanceOf(Exception.class);
    }

    @Test
    void theDocumentKeyIsWrappedWithAes256() throws Exception {
        PdfPublicKeySecurityHandler handler = new PdfPublicKeySecurityHandler();
        handler.addRecipient(new PdfPublicKeyRecipient(recipient, PdfWriter.ALLOW_PRINTING));

        CMSEnvelopedData envelope = new CMSEnvelopedData(handler.getEncodedRecipient(0));

        assertThat(envelope.getContentEncryptionAlgorithm().getAlgorithm()).isEqualTo(CMSAlgorithm.AES256_CBC);
        assertThat(envelope.getRecipientInfos().size()).isEqualTo(1);
    }
}
