package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;

@SuppressWarnings("nls")
public class CrossReferenceTableEncodingTest {

    private static final String FILE_ENCODING = "file.encoding";

    @Test
    public void testCrossReferenceTableEncoding() throws Exception {
        final String oldEncoding = System.getProperty(FILE_ENCODING);
        try {
            // Change Encoding to IBM-273
            System.setProperty(FILE_ENCODING, "IBM273");
            resetDefaultCharset();

            final String actualPDF = this.generateSimplePdf();
            final String expectedPDF = this.readExpectedFile("/encodingTest.pdf");
            assertEquals(filterPdf(expectedPDF), filterPdf(actualPDF));
        } finally {

            // Finally reset original charset
            if (oldEncoding == null) {
                System.clearProperty(FILE_ENCODING);
            } else {
                System.setProperty(FILE_ENCODING, oldEncoding);
            }
            resetDefaultCharset();
        }
    }

    /**
     * Resets the defaultCharset within Charset class, so that the Systemproperty file.encoding will be evaluated.
     */
    private static void resetDefaultCharset() throws NoSuchFieldException, IllegalAccessException {
        final Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, null);
    }

    private static String filterPdf(final String pdf) {
        return pdf.replaceAll("<<\\/ModDate.*?>>", "<</ModDate XXXXX>>")
                  .replaceAll("<</Info .*?>>", "<<\\/Info XXXXX>>")
                  .replaceAll("startxref\\n(\\d+)\\n%%EOF", "startxref\\n(XXXXX)\\n%%EOF");
    }

    private String readExpectedFile(final String resourceName) throws IOException {
        try (final InputStream expected  = this.getClass().getResourceAsStream(resourceName)) {
            assertNotNull(expected);
            return IOUtils.toString(expected, "ISO-8859-1");
        }
    }

    private String generateSimplePdf() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (Document document = PdfTestBase.createPdf(out)) {
                document.open();
                document.newPage();
                document.add(new Paragraph("Hello World!"));
            }
            final byte[] actualBytes = out.toByteArray();
            return new String(actualBytes, "ISO-8859-1");
        }
    }
}
