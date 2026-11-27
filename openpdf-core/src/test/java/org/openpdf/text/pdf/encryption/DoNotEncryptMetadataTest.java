package org.openpdf.text.pdf.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfEncryption;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfStamper;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

/**
 * Tests for issue #1576: passing {@link PdfWriter#DO_NOT_ENCRYPT_METADATA} alone must not switch
 * the document to embedded-files-only mode. {@code EMBEDDED_FILES_ONLY} (24) contains the
 * {@code DO_NOT_ENCRYPT_METADATA} bit (8), so the embedded-files-only detection must require both
 * of its bits, not just one — otherwise stream and string crypt filters are silently set to
 * /Identity and the document content is written unencrypted.
 */
class DoNotEncryptMetadataTest {

    @Test
    void doNotEncryptMetadataAloneIsNotEmbeddedFilesOnly() {
        PdfEncryption encryption = new PdfEncryption();
        encryption.setCryptoMode(PdfWriter.ENCRYPTION_AES_128 | PdfWriter.DO_NOT_ENCRYPT_METADATA, 0);
        assertFalse(encryption.isMetadataEncrypted(), "Metadata should not be encrypted");
        assertFalse(encryption.isEmbeddedFilesOnly(),
                "DO_NOT_ENCRYPT_METADATA alone must not enable embedded-files-only mode");
    }

    @Test
    void embeddedFilesOnlyModeIsStillDetected() {
        PdfEncryption encryption = new PdfEncryption();
        encryption.setCryptoMode(PdfWriter.ENCRYPTION_AES_128 | PdfWriter.EMBEDDED_FILES_ONLY, 0);
        assertFalse(encryption.isMetadataEncrypted(), "Embedded-files-only mode implies unencrypted metadata");
        assertTrue(encryption.isEmbeddedFilesOnly(), "EMBEDDED_FILES_ONLY must enable embedded-files-only mode");
    }

    @Test
    void defaultModeEncryptsEverything() {
        PdfEncryption encryption = new PdfEncryption();
        encryption.setCryptoMode(PdfWriter.ENCRYPTION_AES_128, 0);
        assertTrue(encryption.isMetadataEncrypted(), "Metadata should be encrypted by default");
        assertFalse(encryption.isEmbeddedFilesOnly(), "Embedded-files-only mode should be off by default");
    }

    @Test
    void doNotEncryptMetadataStillEncryptsStreamsAndStrings() throws Exception {
        byte[] encrypted = encrypt(PdfWriter.ENCRYPTION_AES_128 | PdfWriter.DO_NOT_ENCRYPT_METADATA);

        // The /Encrypt dictionary is always written as a plain, uncompressed object,
        // so its entries can be asserted on the raw output bytes.
        String raw = new String(encrypted, StandardCharsets.ISO_8859_1);
        assertTrue(raw.contains("/StmF/StdCF"),
                "Streams must be encrypted with the standard crypt filter, not /Identity");
        assertTrue(raw.contains("/StrF/StdCF"),
                "Strings must be encrypted with the standard crypt filter, not /Identity");
        assertFalse(raw.contains("/StmF/Identity"), "Streams must not use the /Identity crypt filter");
        assertFalse(raw.contains("/EFF"), "No embedded-files crypt filter should be set");
        assertTrue(raw.contains("/EncryptMetadata false"), "Metadata must be declared unencrypted");
        assertTrue(raw.contains("/AuthEvent/DocOpen"),
                "Authentication must be required on document open, not only on embedded file open");

        PdfReader reader = new PdfReader(encrypted, "owner".getBytes());
        try {
            assertTrue(reader.isEncrypted(), "Document should be encrypted");
            assertFalse(reader.isMetadataEncrypted(), "Metadata should not be encrypted");
            assertEquals("Hello", new PdfTextExtractor(reader).getTextFromPage(1),
                    "Encrypted content should still be readable with the password");
        } finally {
            reader.close();
        }
    }

    @Test
    void embeddedFilesOnlyStillUsesIdentityFilters() throws Exception {
        byte[] encrypted = encrypt(PdfWriter.ENCRYPTION_AES_128 | PdfWriter.EMBEDDED_FILES_ONLY);

        String raw = new String(encrypted, StandardCharsets.ISO_8859_1);
        assertTrue(raw.contains("/StmF/Identity"), "Embedded-files-only mode must leave streams unencrypted");
        assertTrue(raw.contains("/StrF/Identity"), "Embedded-files-only mode must leave strings unencrypted");
        assertTrue(raw.contains("/EFF/StdCF"), "Embedded-files-only mode must encrypt embedded files");
        assertTrue(raw.contains("/AuthEvent/EFOpen"),
                "Embedded-files-only mode must only require authentication for embedded files");
    }

    private static byte[] encrypt(int encryptionType) throws Exception {
        ByteArrayOutputStream source = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, source);
        document.open();
        document.add(new Paragraph("Hello"));
        document.close();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(source.toByteArray());
        PdfStamper stamper = new PdfStamper(reader, out);
        stamper.setEncryption("user".getBytes(), "owner".getBytes(), PdfWriter.ALLOW_PRINTING, encryptionType);
        stamper.close();
        reader.close();
        return out.toByteArray();
    }
}
