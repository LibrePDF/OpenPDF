package org.openpdf.text.pdf;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openpdf.text.exceptions.InvalidPdfException;

/**
 * Sizes found in the cross-reference data of a small file must not make the reader reserve memory for hundreds of
 * millions of objects.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class PdfReaderXrefAllocationTest {

    private static final String HUGE = "900000000";

    @Test
    void validFilesAreStillRead() throws Exception {
        try (PdfReader reader = new PdfReader(classicPdf("0 4", "4", ""))) {
            assertThat(reader.isRebuilt()).isFalse();
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
        }
        try (PdfReader reader = new PdfReader(xrefStreamPdf("7", "", null, "1"))) {
            assertThat(reader.isRebuilt()).isFalse();
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
            assertThat(reader.getPdfObject(6)).isInstanceOf(PdfDictionary.class);
        }
    }

    @Test
    void hugeTrailerSizeDoesNotReserveMemory() throws Exception {
        assertRecovered(classicPdf("0 4", "1000000000", ""));
    }

    @Test
    void negativeTrailerSizeDoesNotFail() throws Exception {
        assertRecovered(classicPdf("0 4", "-1000000000", ""));
    }

    @Test
    void hugeSubsectionCountDoesNotReserveMemory() throws Exception {
        assertRecovered(classicPdf("0 " + HUGE, "4", ""));
    }

    @Test
    void subsectionEndBeyondIntegerRangeDoesNotFail() throws Exception {
        assertRecovered(classicPdf("2147483647 2147483647", "4", ""));
    }

    @Test
    void hugeObjectNumberIsIgnoredWhenRebuilding() throws Exception {
        // the missing "xref" keyword makes the reader rebuild the cross-reference table
        assertRecovered(classicPdf("", "4", HUGE + " 0 obj\n<< >>\nendobj\n"));
    }

    @Test
    void hugeXrefStreamSizeIsRejected() {
        assertRejected(xrefStreamPdf("1000000000", "", null, "1"));
    }

    @Test
    void hugeXrefStreamIndexIsRejected() {
        assertRejected(xrefStreamPdf("7", "/Index [0 " + HUGE + "]", null, "1"));
    }

    @Test
    void negativeXrefStreamIndexIsRejected() {
        assertRejected(xrefStreamPdf("7", "/Index [-5 7]", null, "1"));
        assertRejected(xrefStreamPdf("7", "/Index [0 -7]", null, "1"));
    }

    @Test
    void hugeXrefStreamLengthIsRejected() {
        assertRejected(xrefStreamPdf("7", "", "2147483647", "1"));
    }

    @Test
    void negativeXrefStreamLengthIsRejected() {
        assertRejected(xrefStreamPdf("7", "", "-1", "1"));
    }

    @Test
    void hugeObjectStreamCountIsRejected() {
        assertRejected(xrefStreamPdf("7", "", null, "2000000000"));
    }

    @Test
    void negativeObjectStreamCountIsRejected() {
        assertRejected(xrefStreamPdf("7", "", null, "-1"));
    }

    private static void assertRecovered(byte[] pdf) throws Exception {
        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(reader.isRebuilt()).isTrue();
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
            assertThat(reader.getXrefSize()).isLessThan(100);
        }
    }

    private static void assertRejected(byte[] pdf) {
        assertThatThrownBy(() -> new PdfReader(pdf)).isInstanceOf(InvalidPdfException.class);
    }

    /**
     * Builds a one-page document with a classic cross-reference table.
     *
     * @param subsection   the header of the cross-reference subsection, normally {@code "0 4"}
     * @param size         the value of /Size in the trailer
     * @param extraObjects text to add after the regular objects
     */
    private static byte[] classicPdf(String subsection, String size, String extraObjects) {
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        int[] offsets = appendObjects(pdf);
        pdf.append(extraObjects);
        int xref = pdf.length();
        if (!subsection.isEmpty()) {
            pdf.append("xref\n").append(subsection).append("\n0000000000 65535 f \n");
            for (int offset : offsets) {
                pdf.append(String.format("%010d 00000 n \n", offset));
            }
        }
        pdf.append("trailer\n<< /Size ").append(size).append(" /Root 1 0 R >>\n")
                .append("startxref\n").append(xref).append("\n%%EOF\n");
        return pdf.toString().getBytes(ISO_8859_1);
    }

    /**
     * Builds a one-page document with a cross-reference stream (object 4) and an object stream (object 5) that
     * holds object 6.
     *
     * @param size           the value of /Size of the cross-reference stream
     * @param index          an /Index entry for the cross-reference stream, or an empty string
     * @param length         the value of /Length of the cross-reference stream, or {@code null} for the real length
     * @param objStreamCount the value of /N of the object stream
     */
    private static byte[] xrefStreamPdf(String size, String index, String length, String objStreamCount) {
        StringBuilder pdf = new StringBuilder("%PDF-1.5\n");
        int[] offsets = appendObjects(pdf);
        int objectStream = pdf.length();
        String objects = "6 0 << /Title (x) >>";
        pdf.append("5 0 obj\n<< /Type /ObjStm /N ").append(objStreamCount).append(" /First 4 /Length ")
                .append(objects.length()).append(" >>\nstream\n").append(objects).append("\nendstream\nendobj\n");
        int xrefStream = pdf.length();
        StringBuilder entries = new StringBuilder();
        appendEntry(entries, 0, 0, 0);
        appendEntry(entries, 1, offsets[0], 0);
        appendEntry(entries, 1, offsets[1], 0);
        appendEntry(entries, 1, offsets[2], 0);
        appendEntry(entries, 1, xrefStream, 0);
        appendEntry(entries, 1, objectStream, 0);
        appendEntry(entries, 2, 5, 0);
        pdf.append("4 0 obj\n<< /Type /XRef /Size ").append(size).append(" /W [1 2 1] /Root 1 0 R /Info 6 0 R ").append(index)
                .append(" /Length ").append(length == null ? String.valueOf(entries.length()) : length)
                .append(" >>\nstream\n").append(entries).append("\nendstream\nendobj\n")
                .append("startxref\n").append(xrefStream).append("\n%%EOF\n");
        return pdf.toString().getBytes(ISO_8859_1);
    }

    private static void appendEntry(StringBuilder entries, int type, int field2, int field3) {
        entries.append((char) type).append((char) (field2 >> 8)).append((char) (field2 & 0xff))
                .append((char) field3);
    }

    /**
     * Appends a catalog, a page tree and one page.
     *
     * @return the offsets of the three objects
     */
    private static int[] appendObjects(StringBuilder pdf) {
        int[] offsets = new int[3];
        offsets[0] = pdf.length();
        pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        offsets[1] = pdf.length();
        pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        offsets[2] = pdf.length();
        pdf.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 10 10] >>\nendobj\n");
        return offsets;
    }
}
