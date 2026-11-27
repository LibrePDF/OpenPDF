package org.openpdf.text.pdf;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openpdf.text.ExceptionConverter;

/**
 * A page tree that leads back to itself, or that does not hold the pages it announces, must not make the reader
 * loop or recurse without end.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class PdfReaderPageTreeCycleTest {

    private static final String CATALOG = "<< /Type /Catalog /Pages 2 0 R >>";
    private static final String PAGE = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 10 10] >>";

    @Test
    void kidsPointingBackToAnAncestorAreSkipped() throws Exception {
        byte[] pdf = pdf(CATALOG,
                "<< /Type /Pages /Kids [3 0 R 4 0 R] /Count 2 >>",
                PAGE,
                "<< /Type /Pages /Parent 2 0 R /Kids [2 0 R 4 0 R 5 0 R] /Count 1 >>",
                PAGE);

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(reader.getNumberOfPages()).isEqualTo(2);
            assertThat(reader.getPageOrigRef(1).getNumber()).isEqualTo(3);
            assertThat(reader.getPageOrigRef(2).getNumber()).isEqualTo(5);
        }
    }

    @Test
    void branchListedManyTimesIsReadOnce() throws Exception {
        // 30 levels that each list the next level twice: 2^30 paths lead to the single page
        List<String> objects = new ArrayList<>();
        objects.add(CATALOG);
        int levels = 30;
        for (int level = 0; level < levels; level++) {
            int kid = level + 3;
            objects.add("<< /Type /Pages /Kids [" + kid + " 0 R " + kid + " 0 R] /Count 2 >>");
        }
        objects.add(PAGE);

        try (PdfReader reader = new PdfReader(pdf(objects.toArray(new String[0])))) {
            // the last level lists the page twice, and a page may be listed more than once
            assertThat(reader.getNumberOfPages()).isEqualTo(2);
        }
    }

    @Test
    void samePageListedTwiceIsStillTwoPages() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [3 0 R 3 0 R] /Count 2 >>", PAGE);

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(reader.getNumberOfPages()).isEqualTo(2);
        }
    }

    @Test
    void partialReaderRejectsPageThatIsNotInTheTree() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [] /Count 5 >>", PAGE);

        try (PdfReader reader = new PdfReader(new RandomAccessFileOrArray(pdf), null)) {
            assertThat(reader.getNumberOfPages()).isEqualTo(5);
            assertThatThrownBy(() -> reader.getPageN(1))
                    .isInstanceOf(ExceptionConverter.class)
                    .hasMessageContaining("The page tree is invalid.");
        }
    }

    @Test
    void partialReaderRejectsCountThatOverstatesThePages() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [3 0 R] /Count 5 >>", PAGE);

        try (PdfReader reader = new PdfReader(new RandomAccessFileOrArray(pdf), null)) {
            assertThat(reader.getPageN(1)).isNotNull();
            assertThatThrownBy(() -> reader.getPageN(3))
                    .isInstanceOf(ExceptionConverter.class)
                    .hasMessageContaining("The page tree is invalid.");
        }
    }

    @Test
    void partialReaderRejectsKidPointingBackToItsParent() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [2 0 R] /Count 2 >>", PAGE);

        try (PdfReader reader = new PdfReader(new RandomAccessFileOrArray(pdf), null)) {
            assertThatThrownBy(() -> reader.getPageN(1))
                    .isInstanceOf(ExceptionConverter.class)
                    .hasMessageContaining("The page tree is invalid.");
        }
    }

    @Test
    void partialReaderStillFindsPagesInNestedTree() throws Exception {
        byte[] pdf = pdf(CATALOG,
                "<< /Type /Pages /Kids [3 0 R 4 0 R] /Count 3 >>",
                PAGE,
                "<< /Type /Pages /Parent 2 0 R /Kids [5 0 R 6 0 R] /Count 2 >>",
                PAGE,
                PAGE);

        try (PdfReader reader = new PdfReader(new RandomAccessFileOrArray(pdf), null)) {
            assertThat(reader.getNumberOfPages()).isEqualTo(3);
            assertThat(reader.getPageOrigRef(1).getNumber()).isEqualTo(3);
            assertThat(reader.getPageOrigRef(2).getNumber()).isEqualTo(5);
            assertThat(reader.getPageOrigRef(3).getNumber()).isEqualTo(6);
        }
    }

    @Test
    void partialReaderReadsStreamWhoseLengthRefersToItself() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 10 10] /Contents 4 0 R >>",
                "<< /Length 4 0 R >>\nstream\n0 0 m\nendstream");

        try (PdfReader reader = new PdfReader(new RandomAccessFileOrArray(pdf), null)) {
            PdfObject contents = reader.getPdfObject(4);

            assertThat(contents).isInstanceOf(PRStream.class);
            assertThat(new String(PdfReader.getStreamBytes((PRStream) contents), ISO_8859_1)).startsWith("0 0 m");
            assertThat(new String(reader.getPageContent(1), ISO_8859_1)).startsWith("0 0 m");
        }
    }

    @Test
    void partialReaderReadsStreamsWhoseLengthsReferToEachOther() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 10 10] /Contents 4 0 R >>",
                "<< /Length 5 0 R >>\nstream\n0 0 m\nendstream",
                "<< /Length 4 0 R >>\nstream\n1 1 l\nendstream");

        try (PdfReader reader = new PdfReader(new RandomAccessFileOrArray(pdf), null)) {
            assertThat(new String(reader.getPageContent(1), ISO_8859_1)).startsWith("0 0 m");
        }
    }

    /**
     * Builds a document from the given objects, which get the numbers 1, 2, 3 and so on.
     */
    private static byte[] pdf(String... objects) {
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        int[] offsets = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            offsets[i] = pdf.length();
            pdf.append(i + 1).append(" 0 obj\n").append(objects[i]).append("\nendobj\n");
        }
        int xref = pdf.length();
        pdf.append("xref\n0 ").append(objects.length + 1).append("\n0000000000 65535 f \n");
        for (int offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size ").append(objects.length + 1).append(" /Root 1 0 R >>\n")
                .append("startxref\n").append(xref).append("\n%%EOF\n");
        return pdf.toString().getBytes(ISO_8859_1);
    }
}
