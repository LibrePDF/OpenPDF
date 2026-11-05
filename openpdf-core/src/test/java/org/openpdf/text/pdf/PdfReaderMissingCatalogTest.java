package org.openpdf.text.pdf;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.exceptions.InvalidPdfException;

/**
 * A document whose catalog cannot be read must be reported as invalid, not fail with a {@code NullPointerException}.
 */
class PdfReaderMissingCatalogTest {

    @Test
    void rootToMissingObjectIsReported() throws Exception {
        byte[] original = createPdf();
        int size;
        int startxref;
        try (PdfReader reader = new PdfReader(original)) {
            size = reader.getTrailer().getAsNumber(PdfName.SIZE).intValue();
            startxref = reader.getLastXref();
        }
        // an incremental update whose trailer points /Root at an object that is not in the file
        String trailer = String.format("<< /Size %d /Root %d 0 R /Prev %d >>", size + 1, size, startxref);
        // lines end with \n, whatever the platform
        String update = String.join("\n", "xref", "0 1", "0000000000 65535 f ", "trailer", trailer,
                "startxref", String.valueOf(original.length), "%%EOF", "");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(original);
        out.write(update.getBytes(ISO_8859_1));
        byte[] pdf = out.toByteArray();

        assertThatThrownBy(() -> new PdfReader(pdf))
                .isInstanceOf(InvalidPdfException.class)
                .hasMessage("The document has no catalog object.");
    }

    private static byte[] createPdf() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph("Hello"));
        document.close();
        return out.toByteArray();
    }
}
