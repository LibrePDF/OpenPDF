package com.lowagie.text.pdf;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CrossReferenceTableEncodingTest {

    public static final String TEST_PDF = "/encodingTest.pdf";

    private static String filterPdf(final String pdf) {
        return pdf.replaceAll("<</ModDate.*?>>", "")
                .replaceAll("<</CreationDate.*?>>", "")
                .replaceAll("<</Info .*?>>", "<</Info XXXXX>>")
                .replaceAll("startxref\\n(\\d+)\\n%%EOF", "startxref\nXXXXX\n%%EOF");
    }

    // This test was once red, even it was not easy to accomplish this. The test must be run with
    // -Dfile.encoding=IBM273 from the IDE. Maven won't accept this property.
    @Disabled("This test runs ok, if it is run alone. It fails when run with other tests in the IDE. Probably it is because some 'static' state in another class.")
    @Test
    void testCrossReferenceTableEncoding() throws Exception {
        final String actualPDF = generateSimplePdf();
        final String expectedPDF = readExpectedFile();
        String actual = filterPdf(actualPDF);
        String expected = filterPdf(expectedPDF);
        assertThat(actual).isEqualTo(expected);
    }

    private String readExpectedFile() throws IOException {
        try (final InputStream expected = getClass().getResourceAsStream(TEST_PDF)) {
            assertThat(expected).isNotNull();
            return IOUtils.toString(expected, ISO_8859_1);
        }
    }

    private String generateSimplePdf() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = PdfTestBase.createPdf(out)) {
            document.open();
            document.newPage();
            document.add(new Paragraph("Hello World!"));
        }
        return out.toString(ISO_8859_1);
    }
}
