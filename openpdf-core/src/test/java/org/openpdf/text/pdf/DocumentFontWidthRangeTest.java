package org.openpdf.text.pdf;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * The ranges in the /W array of a Type0 font are limited to the CIDs that exist.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class DocumentFontWidthRangeTest {

    @Test
    void rangeUpToLargestIntegerIsLimitedToCids() throws Exception {
        try (PdfReader reader = new PdfReader(pdfWithWidths("[0 2147483647 500]"))) {
            DocumentFont font = new DocumentFont(new PRIndirectReference(reader, 4));

            assertThat(font.getWidthOfCode(0)).isEqualTo(500);
            assertThat(font.getWidthOfCode(65)).isEqualTo(500);
            assertThat(font.getWidthOfCode(65535)).isEqualTo(500);
            assertThat(font.getWidthOfCode(65536)).isEqualTo(1000);
        }
    }

    @Test
    void negativeStartOfRangeIsLimitedToCids() throws Exception {
        try (PdfReader reader = new PdfReader(pdfWithWidths("[-2147483648 3 500]"))) {
            DocumentFont font = new DocumentFont(new PRIndirectReference(reader, 4));

            assertThat(font.getWidthOfCode(0)).isEqualTo(500);
            assertThat(font.getWidthOfCode(3)).isEqualTo(500);
            assertThat(font.getWidthOfCode(4)).isEqualTo(1000);
        }
    }

    @Test
    void regularRangesAreReadAndReversedRangeIsIgnored() throws Exception {
        try (PdfReader reader = new PdfReader(pdfWithWidths("[10 5 500 20 22 600 30 [700 800]]"))) {
            DocumentFont font = new DocumentFont(new PRIndirectReference(reader, 4));

            assertThat(font.getWidthOfCode(5)).isEqualTo(1000);
            assertThat(font.getWidthOfCode(10)).isEqualTo(1000);
            assertThat(font.getWidthOfCode(19)).isEqualTo(1000);
            assertThat(font.getWidthOfCode(20)).isEqualTo(600);
            assertThat(font.getWidthOfCode(22)).isEqualTo(600);
            assertThat(font.getWidthOfCode(23)).isEqualTo(1000);
            assertThat(font.getWidthOfCode(30)).isEqualTo(700);
            assertThat(font.getWidthOfCode(31)).isEqualTo(800);
        }
    }

    private static byte[] pdfWithWidths(String widths) {
        String[] objects = {
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 10 10] /Resources << /Font << /F1 4 0 R >> >> >>",
                "<< /Type /Font /Subtype /Type0 /BaseFont /Test /Encoding /Identity-H /DescendantFonts [5 0 R] >>",
                "<< /Type /Font /Subtype /CIDFontType2 /BaseFont /Test /DW 1000 /W " + widths + " >>"};
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
