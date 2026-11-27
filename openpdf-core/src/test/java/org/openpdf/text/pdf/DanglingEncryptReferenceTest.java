package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Tests for issue #1588: a malformed PDF whose trailer {@code /Encrypt} entry is an indirect
 * reference to an object without a usable xref entry made {@code PdfReader.readDecryptedDocObj()}
 * dereference a null encryption dictionary. The partial-read constructor surfaced a bare
 * {@code NullPointerException}; the byte-array constructor wrapped the same NPE in an
 * {@code InvalidPdfException} after a failed rebuild. Other readers (poppler, qpdf) ignore the
 * dangling reference and open such documents as unencrypted.
 */
class DanglingEncryptReferenceTest {

    @Test
    void partialReadOpensDocumentWithDanglingEncryptAsUnencrypted() {
        byte[] pdf = buildPdfWithDanglingEncrypt();
        assertDoesNotThrow(() -> {
            PdfReader reader = new PdfReader(new RandomAccessFileOrArray(pdf), new byte[0]);
            try {
                assertFalse(reader.isEncrypted(), "A dangling /Encrypt reference must be treated as unencrypted");
                assertEquals(1, reader.getNumberOfPages());
            } finally {
                reader.close();
            }
        });
    }

    @Test
    void fullReadOpensDocumentWithDanglingEncryptAsUnencrypted() {
        byte[] pdf = buildPdfWithDanglingEncrypt();
        assertDoesNotThrow(() -> {
            PdfReader reader = new PdfReader(pdf);
            try {
                assertFalse(reader.isEncrypted(), "A dangling /Encrypt reference must be treated as unencrypted");
                assertEquals(1, reader.getNumberOfPages());
            } finally {
                reader.close();
            }
        });
    }

    /**
     * Builds the minimal malformed PDF from the issue report: {@code /Size 5} with
     * {@code /Encrypt 4 0 R} in the trailer, but object 4 only has a free ("f") xref entry, so
     * resolving the reference yields no dictionary.
     */
    private static byte[] buildPdfWithDanglingEncrypt() {
        String header = "%PDF-1.4\n";
        String obj1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";
        String obj2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n";
        String obj3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] >>\nendobj\n";
        long off1 = header.length();
        long off2 = off1 + obj1.length();
        long off3 = off2 + obj2.length();
        long xrefPos = off3 + obj3.length();
        String pdf = header + obj1 + obj2 + obj3
                + "xref\n0 5\n"
                + "0000000000 65535 f \n"
                + xrefEntry(off1, 'n')
                + xrefEntry(off2, 'n')
                + xrefEntry(off3, 'n')
                + xrefEntry(0, 'f')
                + "trailer\n<< /Size 5 /Root 1 0 R /Encrypt 4 0 R >>\nstartxref\n" + xrefPos + "\n%%EOF";
        return pdf.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String xrefEntry(long offset, char type) {
        String digits = Long.toString(offset);
        return "0".repeat(10 - digits.length()) + digits + " 00000 " + type + " \n";
    }
}
