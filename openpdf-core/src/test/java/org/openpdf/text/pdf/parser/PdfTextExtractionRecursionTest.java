package org.openpdf.text.pdf.parser;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfReader;

/**
 * Form XObjects that draw themselves and content arrays that contain themselves must not make text extraction
 * recurse without end.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class PdfTextExtractionRecursionTest {

    private static final String CATALOG = "<< /Type /Catalog /Pages 2 0 R >>";
    private static final String PAGES = "<< /Type /Pages /Kids [3 0 R] /Count 1 >>";
    private static final String FONT = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>";
    private static final String PAGE = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] /Contents 5 0 R"
            + " /Resources << /Font << /F1 4 0 R >> /XObject << /Fm 6 0 R >> >> >>";

    @Test
    void formThatDrawsItselfIsProcessedOnce() throws Exception {
        byte[] pdf = pdf(CATALOG, PAGES, PAGE, FONT,
                stream("", "/Fm Do"),
                form("/Fm 6 0 R", "BT /F1 12 Tf (Hello) Tj ET /Fm Do"));

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(new PdfTextExtractor(reader).getTextFromPage(1)).containsOnlyOnce("Hello");
        }
    }

    @Test
    void formsThatDrawEachOtherAreProcessedOnce() throws Exception {
        byte[] pdf = pdf(CATALOG, PAGES, PAGE, FONT,
                stream("", "/Fm Do"),
                form("/Other 7 0 R", "BT /F1 12 Tf (Hello) Tj ET /Other Do"),
                form("/Fm 6 0 R", "BT /F1 12 Tf (World) Tj ET /Fm Do"));

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(new PdfTextExtractor(reader).getTextFromPage(1))
                    .containsOnlyOnce("Hello").containsOnlyOnce("World");
        }
    }

    @Test
    void formUsedTwiceIsProcessedTwice() throws Exception {
        byte[] pdf = pdf(CATALOG, PAGES, PAGE, FONT,
                stream("", "/Fm Do /Fm Do"),
                form("/Other 7 0 R", "/Other Do"),
                form("", "BT /F1 12 Tf (Hello) Tj ET"));

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(new PdfTextExtractor(reader).getTextFromPage(1)).matches("(?s).*Hello.*Hello.*");
        }
    }

    @Test
    void longChainOfNestedFormsIsCutOff() throws Exception {
        List<String> objects = new ArrayList<>(List.of(CATALOG, PAGES, PAGE, FONT, stream("", "/Fm Do")));
        int forms = 5_000;
        for (int i = 0; i < forms; i++) {
            int next = objects.size() + 2;
            objects.add(form("/Next " + next + " 0 R", "BT /F1 12 Tf (Hello) Tj ET /Next Do"));
        }
        objects.add(form("", ""));

        try (PdfReader reader = new PdfReader(pdf(objects.toArray(new String[0])))) {
            String text = new PdfTextExtractor(reader).getTextFromPage(1);

            assertThat(text.split("Hello", -1).length - 1).isBetween(1, 100);
        }
    }

    @Test
    void contentArrayThatContainsItselfIsReadOnce() throws Exception {
        byte[] pdf = pdf(CATALOG, PAGES, PAGE, FONT,
                "[7 0 R 5 0 R 7 0 R]",
                form("", ""),
                stream("", "BT /F1 12 Tf (Hello) Tj ET\n"));

        try (PdfReader reader = new PdfReader(pdf)) {
            // the stream is listed twice, which is allowed
            assertThat(new PdfTextExtractor(reader).getTextFromPage(1)).matches("(?s).*Hello.*Hello.*");
            byte[] content = PdfContentStreamHandler.getContentBytesFromPdfObjectStatic(
                    reader.getPageN(1).get(PdfName.CONTENTS));
            assertThat(new String(content, ISO_8859_1))
                    .isEqualTo("BT /F1 12 Tf (Hello) Tj ET\nBT /F1 12 Tf (Hello) Tj ET\n");
        }
    }

    private static String form(String xObjects, String content) {
        return stream("/Type /XObject /Subtype /Form /BBox [0 0 200 200]"
                + " /Resources << /Font << /F1 4 0 R >> /XObject << " + xObjects + " >> >>", content);
    }

    private static String stream(String entries, String content) {
        return "<< " + entries + " /Length " + content.length() + " >>\nstream\n" + content + "\nendstream";
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
