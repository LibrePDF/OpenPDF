package org.openpdf.text.pdf;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.exceptions.InvalidPdfException;

/**
 * A {@code /Prev} cycle between earlier cross-reference sections must not make the reader loop forever or overflow
 * the stack.
 */
class PdfReaderPrevCycleTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Test
    void chainOfAppendedXrefTablesIsRead() throws Exception {
        byte[] pdf = appendXrefTables(createPdf(false), false);

        try (PdfReader reader = assertTimeoutPreemptively(TIMEOUT, () -> new PdfReader(pdf))) {
            assertThat(reader.isRebuilt()).isFalse();
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    void prevCycleBetweenEarlierXrefTablesIsRebuilt() throws Exception {
        byte[] pdf = appendXrefTables(createPdf(false), true);

        try (PdfReader reader = assertTimeoutPreemptively(TIMEOUT, () -> new PdfReader(pdf))) {
            assertThat(reader.isRebuilt()).isTrue();
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    void prevCycleBetweenEarlierXrefStreamsIsReported() throws Exception {
        byte[] pdf = appendXrefStreams(createPdf(true));

        assertTimeoutPreemptively(TIMEOUT, () -> assertThatThrownBy(() -> new PdfReader(pdf))
                .isInstanceOf(InvalidPdfException.class));
    }

    private static byte[] createPdf(boolean fullCompression) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        if (fullCompression) {
            writer.setFullCompression();
        }
        document.open();
        document.add(new Paragraph("Hello"));
        document.close();
        return out.toByteArray();
    }

    /**
     * Appends three cross-reference tables A, B and C, read from C. C points to A and A to B. B points back to A if
     * {@code cycle} is set, else to the original table.
     */
    private static byte[] appendXrefTables(byte[] original, boolean cycle) throws IOException {
        Trailer trailer = Trailer.of(original);
        int a = original.length;
        int b = a + xrefTable(trailer, 0).length();
        int c = b + xrefTable(trailer, 0).length();
        StringBuilder update = new StringBuilder()
                .append(xrefTable(trailer, b))
                .append(xrefTable(trailer, cycle ? a : trailer.startxref))
                .append(xrefTable(trailer, a))
                .append("startxref\n").append(c).append("\n%%EOF\n");
        return append(original, update);
    }

    /**
     * Appends three cross-reference streams A, B and C, read from C. C points to A, and A and B point to each other.
     */
    private static byte[] appendXrefStreams(byte[] original) throws IOException {
        Trailer trailer = Trailer.of(original);
        int a = original.length;
        int b = a + xrefStream(trailer, trailer.size, 0).length();
        int c = b + xrefStream(trailer, trailer.size + 1, 0).length();
        StringBuilder update = new StringBuilder()
                .append(xrefStream(trailer, trailer.size, b))
                .append(xrefStream(trailer, trailer.size + 1, a))
                .append(xrefStream(trailer, trailer.size + 2, a))
                .append("startxref\n").append(c).append("\n%%EOF\n");
        return append(original, update);
    }

    private static String xrefTable(Trailer trailer, int prev) {
        return String.format("xref\n0 1\n0000000000 65535 f \ntrailer\n<< /Size %d /Root %s /Prev %010d >>\n",
                trailer.size, trailer.root, prev);
    }

    private static String xrefStream(Trailer trailer, int number, int prev) {
        return String.format("%d 0 obj\n<< /Type /XRef /Size %d /Root %s /W [1 1 1] /Index [0 0] /Length 0"
                + " /Prev %010d >>\nstream\n\nendstream\nendobj\n", number, trailer.size + 3, trailer.root, prev);
    }

    private static byte[] append(byte[] original, CharSequence update) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(original);
        out.write(update.toString().getBytes(ISO_8859_1));
        return out.toByteArray();
    }

    private record Trailer(int size, String root, int startxref) {

        static Trailer of(byte[] pdf) throws IOException {
            try (PdfReader reader = new PdfReader(pdf)) {
                PdfDictionary trailer = reader.getTrailer();
                return new Trailer(trailer.getAsNumber(PdfName.SIZE).intValue(),
                        trailer.get(PdfName.ROOT).toString(), reader.getLastXref());
            }
        }
    }
}
