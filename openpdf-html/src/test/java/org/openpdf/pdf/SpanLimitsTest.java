package org.openpdf.pdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class SpanLimitsTest {

    @Test
    @Timeout(120)
    void hugeRowspanIsLimited() throws IOException {
        String html = """
                <html><body><table>
                <tr><td rowspan="2000000000">first</td><td>second</td></tr>
                <tr><td>third</td></tr>
                </table></body></html>""";

        assertThat(textOf(render(html))).contains("first", "second", "third");
    }

    @Test
    @Timeout(120)
    void hugeColspanIsLimited() throws IOException {
        String html = """
                <html><body><table>
                <tr><td colspan="2000000000">first</td></tr>
                <tr><td>second</td><td>third</td></tr>
                </table></body></html>""";

        assertThat(textOf(render(html))).contains("first", "second", "third");
    }

    @Test
    @Timeout(120)
    void hugeColumnSpanIsLimited() throws IOException {
        String html = """
                <html><body><table>
                <col span="2000000000"/>
                <tr><td>first</td><td>second</td></tr>
                </table></body></html>""";

        assertThat(textOf(render(html))).contains("first", "second");
    }

    @Test
    @Timeout(120)
    void hugeTabSizeIsLimited() throws IOException {
        String html = "<html><body><pre style=\"tab-size:1000000000\">first\tsecond\tthird</pre></body></html>";

        assertThat(textOf(render(html))).contains("first", "second", "third");
    }

    @Test
    void ordinarySpansAndTabSizeAreKept() throws IOException {
        String html = """
                <html><body><table>
                <tr><td rowspan="2">first</td><td colspan="2">second</td></tr>
                <tr><td>third</td><td>fourth</td></tr>
                </table>
                <pre style="tab-size:4">a\tb</pre></body></html>""";

        assertThat(textOf(render(html))).contains("first", "second", "third", "fourth", "a    b");
    }

    private static byte[] render(String html) {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(pdf);
        return pdf.toByteArray();
    }

    private static String textOf(byte[] pdf) throws IOException {
        PdfReader reader = new PdfReader(pdf);
        try {
            return new PdfTextExtractor(reader).getTextFromPage(1);
        } finally {
            reader.close();
        }
    }
}
