package org.openpdf.text.pdf;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openpdf.text.exceptions.InvalidPdfException;

/**
 * A chain of cross-reference sections linked by {@code /Prev} that loops back on itself must not keep the reader
 * busy forever.
 */
class PdfReaderXrefPrevCycleTest {

    private static final String FREE_ENTRY = "0000000000 65535 f \n";

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void classicSectionWithPrevPointingAtItselfIsRecovered() throws Exception {
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        appendObjects(pdf);
        int selfReferencing = pdf.length();
        pdf.append("xref\n0 1\n").append(FREE_ENTRY)
                .append("trailer\n<< /Size 4 /Prev ").append(selfReferencing).append(" >>\n");
        int last = pdf.length();
        pdf.append("xref\n0 1\n").append(FREE_ENTRY)
                .append("trailer\n<< /Size 4 /Root 1 0 R /Prev ").append(selfReferencing).append(" >>\n")
                .append("startxref\n").append(last).append("\n%%EOF\n");

        try (PdfReader reader = new PdfReader(pdf.toString().getBytes(ISO_8859_1))) {
            assertThat(reader.isRebuilt()).isTrue();
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void classicSectionsPointingAtEachOtherAreRecovered() throws Exception {
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        appendObjects(pdf);
        int second = pdf.length();
        // the two older sections have the same length, so the offset of the third one is known here
        String olderSection = "xref\n0 1\n" + FREE_ENTRY + "trailer\n<< /Size 4 /Prev %010d >>\n";
        int third = second + String.format(olderSection, 0).length();
        pdf.append(String.format(olderSection, third));
        pdf.append(String.format(olderSection, second));
        int last = pdf.length();
        pdf.append("xref\n0 1\n").append(FREE_ENTRY)
                .append("trailer\n<< /Size 4 /Root 1 0 R /Prev ").append(second).append(" >>\n")
                .append("startxref\n").append(last).append("\n%%EOF\n");

        try (PdfReader reader = new PdfReader(pdf.toString().getBytes(ISO_8859_1))) {
            assertThat(reader.isRebuilt()).isTrue();
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void xrefStreamWithPrevPointingAtItselfIsRejected() {
        StringBuilder pdf = new StringBuilder("%PDF-1.5\n");
        int[] offsets = appendObjects(pdf);
        int xrefStream = pdf.length();
        StringBuilder entries = new StringBuilder();
        entries.append((char) 0).append((char) 0).append((char) 0).append((char) 0);
        for (int offset : new int[]{offsets[0], offsets[1], offsets[2], xrefStream}) {
            entries.append((char) 1).append((char) (offset >> 8)).append((char) (offset & 0xff)).append((char) 0);
        }
        pdf.append("4 0 obj\n<< /Type /XRef /Size 5 /W [1 2 1] /Root 1 0 R /Prev ").append(xrefStream)
                .append(" /Length ").append(entries.length()).append(" >>\nstream\n")
                .append(entries).append("\nendstream\nendobj\n")
                .append("startxref\n").append(xrefStream).append("\n%%EOF\n");
        byte[] bytes = pdf.toString().getBytes(ISO_8859_1);

        assertThatThrownBy(() -> new PdfReader(bytes)).isInstanceOf(InvalidPdfException.class);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void xrefStreamPrevCycleIsReportedByTheXrefStreamReader() throws Exception {
        String object = "1 0 obj\n<< /Type /XRef /Size 2 /W [1 2 1] /Prev 0 /Length 8 >>\nstream\n"
                + "\0\0\0\0\1\0\0\0\nendstream\nendobj\n";
        PdfReader reader = new PdfReader();
        reader.tokens = new PRTokeniser(object.getBytes(ISO_8859_1));

        assertThatThrownBy(() -> reader.readXRefStream(0))
                .isInstanceOf(InvalidPdfException.class)
                .hasMessage("An infinite loop was detected in the xref table.");
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
