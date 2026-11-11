package org.openpdf.text.pdf;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import org.junit.jupiter.api.Test;
import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.exceptions.InvalidPdfException;

/**
 * A page tree that refers back to itself, or that holds fewer pages than its {@code /Count} says, must not overflow
 * the stack or keep the reader busy forever.
 */
class PdfReaderMalformedPageTreeTest {

    private static final String CATALOG = "<< /Type /Catalog /Pages 2 0 R >>";

    @Test
    void pageTreeWithOnePageIsRead() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] >>");

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
        }
        try (PdfReader reader = new PdfReader(new RandomAccessFileOrArray(pdf), null)) {
            assertThat(reader.getPageN(1).get(PdfName.TYPE)).isEqualTo(PdfName.PAGE);
        }
    }

    @Test
    void pageSharedByTwoNodesIsReadTwice() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [3 0 R 4 0 R] /Count 2 >>",
                "<< /Type /Pages /Parent 2 0 R /Kids [5 0 R] /Count 1 >>",
                "<< /Type /Pages /Parent 2 0 R /Kids [5 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 3 0 R /MediaBox [0 0 100 100] >>");

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(reader.getNumberOfPages()).isEqualTo(2);
        }
    }

    @Test
    void nodeThatIsItsOwnKidIsSkipped() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [2 0 R] /Count 1 >>");

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(reader.getNumberOfPages()).isZero();
        }
    }

    @Test
    void nodeThatIsKidOfItsOwnKidIsSkipped() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [3 0 R 4 0 R] /Count 2 >>",
                "<< /Type /Pages /Parent 2 0 R /Kids [2 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] >>");

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    void partialReadOfPageMissingFromEmptyKidsIsReported() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [] /Count 1 >>");

        assertPageNotFound(pdf);
    }

    @Test
    void partialReadOfNodeThatIsItsOwnKidIsReported() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [2 0 R] /Count 1 >>");

        assertPageNotFound(pdf);
    }

    @Test
    void partialReadOfNodeThatIsKidOfItsOwnKidIsReported() throws Exception {
        byte[] pdf = pdf(CATALOG, "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Pages /Parent 2 0 R /Kids [2 0 R] /Count 1 >>");

        assertPageNotFound(pdf);
    }

    private static void assertPageNotFound(byte[] pdf) throws Exception {
        try (PdfReader reader = new PdfReader(new RandomAccessFileOrArray(pdf), null)) {
            assertTimeoutPreemptively(ofSeconds(5), () ->
                    assertThatThrownBy(() -> reader.getPageN(1))
                            .isInstanceOfSatisfying(ExceptionConverter.class, e ->
                                    assertThat(e.getException())
                                            .isInstanceOf(InvalidPdfException.class)
                                            .hasMessage("Page not found.")));
        }
    }

    /**
     * Builds a document from the given object bodies, numbered from 1; object 1 is the catalog.
     */
    private static byte[] pdf(String... objects) {
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        StringBuilder xref = new StringBuilder("0000000000 65535 f \n");
        for (int k = 0; k < objects.length; ++k) {
            String offset = String.valueOf(pdf.length());
            xref.append("0".repeat(10 - offset.length())).append(offset).append(" 00000 n \n");
            pdf.append(k + 1).append(" 0 obj\n").append(objects[k]).append("\nendobj\n");
        }
        int startxref = pdf.length();
        pdf.append("xref\n0 ").append(objects.length + 1).append('\n').append(xref);
        pdf.append("trailer\n<< /Size ").append(objects.length + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(startxref).append("\n%%EOF\n");
        return pdf.toString().getBytes(ISO_8859_1);
    }
}
