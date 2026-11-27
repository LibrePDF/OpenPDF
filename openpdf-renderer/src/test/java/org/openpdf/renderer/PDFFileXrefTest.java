package org.openpdf.renderer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for reading the cross-reference tables and streams of damaged files.
 */
@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PDFFileXrefTest {

    private static final String ROOT = "/Root 1 0 R";

    @Test
    void fileWithOneTableOpens() throws IOException {
        Pdf pdf = new Pdf();
        int xref = pdf.table("0 4", 3);
        pdf.trailer("/Size 4 " + ROOT);

        assertThat(pdf.open(xref).getNumPages()).isEqualTo(1);
    }

    @Test
    void fileWithTwoLinkedTablesOpens() throws IOException {
        Pdf pdf = new Pdf();
        int first = pdf.table("0 4", 3);
        pdf.trailer("/Size 4 " + ROOT);
        int second = pdf.table("0 1", 0);
        pdf.trailer("/Size 4 " + ROOT + " /Prev " + first);

        assertThat(pdf.open(second).getNumPages()).isEqualTo(1);
    }

    @Test
    void tableLinkedToItselfIsRejected() {
        Pdf pdf = new Pdf();
        int xref = pdf.table("0 4", 3);
        pdf.trailer("/Size 4 " + ROOT + " /Prev " + xref);

        assertThatThrownBy(() -> pdf.open(xref)).isInstanceOf(PDFParseException.class);
    }

    @Test
    void twoTablesLinkedToEachOtherAreRejected() {
        Pdf pdf = new Pdf();
        int first = pdf.table("0 4", 3);
        // the second table starts after the trailer of the first one, which has a fixed length
        String firstTrailer = "/Size 4 " + ROOT + " /Prev %010d";
        int second = first + pdf.tableLength(3) + pdf.trailerLength(String.format(firstTrailer, 0));
        pdf.trailer(String.format(firstTrailer, second));
        assertThat(pdf.table("0 1", 0)).isEqualTo(second);
        pdf.trailer("/Size 4 " + ROOT + " /Prev " + first);

        assertThatThrownBy(() -> pdf.open(second)).isInstanceOf(PDFParseException.class);
    }

    @Test
    void previousTableOutsideTheFileIsRejected() {
        Pdf pdf = new Pdf();
        int xref = pdf.table("0 4", 3);
        pdf.trailer("/Size 4 " + ROOT + " /Prev 99999999");

        assertThatThrownBy(() -> pdf.open(xref)).isInstanceOf(PDFParseException.class);
    }

    @Test
    void startOfTableOutsideTheFileIsRejected() {
        Pdf pdf = new Pdf();
        pdf.table("0 4", 3);
        pdf.trailer("/Size 4 " + ROOT);

        assertThatThrownBy(() -> pdf.open(99999999)).isInstanceOf(PDFParseException.class);
    }

    @Test
    void subsectionLargerThanTheFileIsRejected() {
        Pdf pdf = new Pdf();
        int xref = pdf.table("0 1900000000", 3);
        pdf.trailer("/Size 4 " + ROOT);

        assertThatThrownBy(() -> pdf.open(xref)).isInstanceOf(PDFParseException.class);
    }

    @Test
    void subsectionStartingBeyondTheFileIsRejected() {
        Pdf pdf = new Pdf();
        int xref = pdf.table("1900000000 4", 3);
        pdf.trailer("/Size 4 " + ROOT);

        assertThatThrownBy(() -> pdf.open(xref)).isInstanceOf(PDFParseException.class);
    }

    @Test
    void subsectionWithNegativeCountIsRejected() {
        Pdf pdf = new Pdf();
        int xref = pdf.table("0 -4", 3);
        pdf.trailer("/Size 4 " + ROOT);

        assertThatThrownBy(() -> pdf.open(xref)).isInstanceOf(PDFParseException.class);
    }

    @Test
    void fileWithCrossReferenceStreamOpens() throws IOException {
        Pdf pdf = new Pdf();
        int xref = pdf.stream("/Size 5", 5);

        assertThat(pdf.open(xref).getNumPages()).isEqualTo(1);
    }

    @Test
    void streamLinkedToItselfIsReadOnce() throws IOException {
        Pdf pdf = new Pdf();
        int xref = pdf.length();
        pdf.stream("/Size 5 /Prev " + xref, 5);

        assertThat(pdf.open(xref).getNumPages()).isEqualTo(1);
    }

    @Test
    void streamWithMoreEntriesThanDataIsRejected() {
        Pdf pdf = new Pdf();
        int xref = pdf.stream("/Size 1900000000", 5);

        assertThatThrownBy(() -> pdf.open(xref)).isInstanceOf(PDFParseException.class);
    }

    @Test
    void streamWithNegativeIndexIsRejected() {
        Pdf pdf = new Pdf();
        int xref = pdf.stream("/Size 5 /Index [0 -5]", 5);

        assertThatThrownBy(() -> pdf.open(xref)).isInstanceOf(PDFParseException.class);
    }

    /**
     * A file with a catalog, a page tree and one page, to which cross-reference sections are added by the tests.
     */
    private static final class Pdf {

        private final StringBuilder content = new StringBuilder("%PDF-1.5\n");
        private final List<Integer> offsets = new ArrayList<>();

        Pdf() {
            object("<< /Type /Catalog /Pages 2 0 R >>");
            object("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
            object("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] >>");
        }

        private void object(String body) {
            offsets.add(content.length());
            content.append(offsets.size()).append(" 0 obj\n").append(body).append("\nendobj\n");
        }

        int length() {
            return content.length();
        }

        /**
         * Appends a cross-reference table with the given subsection header, followed by the free entry and entries for
         * the first objects of the file.
         *
         * @return the offset of the table
         */
        int table(String subsection, int objects) {
            int start = content.length();
            content.append("xref\n").append(subsection).append("\n0000000000 65535 f \n");
            for (int i = 0; i < objects; i++) {
                content.append(String.format("%010d 00000 n \n", offsets.get(i)));
            }
            return start;
        }

        int tableLength(int objects) {
            return "xref\n0 4\n".length() + 20 * (objects + 1);
        }

        void trailer(String entries) {
            content.append("trailer\n<< ").append(entries).append(" >>\n");
        }

        int trailerLength(String entries) {
            return ("trailer\n<< " + entries + " >>\n").length();
        }

        /**
         * Appends an uncompressed cross-reference stream as object 4, with entries for objects 0 to 4.
         *
         * @return the offset of the stream object
         */
        int stream(String entries, int count) {
            int start = content.length();
            StringBuilder data = new StringBuilder();
            data.append((char) 0).append((char) 0).append((char) 0).append((char) 0);
            for (int i = 0; i < count - 1; i++) {
                int offset = i < offsets.size() ? offsets.get(i) : start;
                data.append((char) 1).append((char) (offset >> 8)).append((char) (offset & 0xff)).append((char) 0);
            }
            content.append("4 0 obj\n<< /Type /XRef /W [1 2 1] ").append(ROOT).append(' ').append(entries)
                    .append(" /Length ").append(data.length()).append(" >>\nstream\n").append(data)
                    .append("\nendstream\nendobj\n");
            return start;
        }

        PDFFile open(int startxref) throws IOException {
            String file = content + "startxref\n" + startxref + "\n%%EOF\n";
            return new PDFFile(ByteBuffer.wrap(file.getBytes(StandardCharsets.ISO_8859_1)));
        }
    }
}
