package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import com.lowagie.text.Annotation;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

public class SimplePdfTest {

    private static final String FILE_ENCODING = "file.encoding";

    @Test
    void testSimplePdf() throws Exception {
        // create document
        Document document = PdfTestBase.createTempPdf("testSimplePdf.pdf");
        try {
            // new page with a rectangle
            document.open();
            document.newPage();
            Annotation ann = new Annotation("Title", "Text");
            Rectangle rect = new Rectangle(100, 100);
            document.add(ann);
            document.add(rect);
        } finally {
            // close document
            if (document != null)
                document.close();
        }

    }

    @Test
    void testTryWithResources_with_os_before_doc() throws Exception {
        try (PdfReader reader = new PdfReader("./src/test/resources/HelloWorldMeta.pdf");
            FileOutputStream os = new FileOutputStream(File.createTempFile("temp-file-name", ".pdf"));
             Document document = new Document()
        ) {
            PdfWriter writer = PdfWriter.getInstance(document, os);
            document.open();
            final PdfContentByte cb = writer.getDirectContent();

            document.newPage();
            PdfImportedPage page = writer.getImportedPage(reader, 1);
            cb.addTemplate(page, 1, 0, 0, 1, 0, 0);
        }
    }

    @Test
    void testTryWithResources_with_unknown_os() throws Exception {
        try (PdfReader reader = new PdfReader("./src/test/resources/HelloWorldMeta.pdf");
             Document document = new Document()
        ) {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(File.createTempFile("temp-file-name", ".pdf")));
            document.open();
            final PdfContentByte cb = writer.getDirectContent();

            document.newPage();
            PdfImportedPage page = writer.getImportedPage(reader, 1);
            cb.addTemplate(page, 1, 0, 0, 1, 0, 0);
        }
    }
    
    @Test
    void testCrossReferenceTableEncoding() throws Exception {
        final String oldEncoding = System.getProperty(FILE_ENCODING);
        try {
            // Change Encoding to IBM-273
            System.setProperty(FILE_ENCODING, "IBM273");
            resetDefaultCharset();
            
            String actualPDF = generateSimplePdf();
            String expectedPDF = readExpectedFile("/encodingTest.pdf");
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
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, null);
    }
    
    private static String filterPdf(String pdf) {
        return pdf.replaceAll("<<\\/ModDate.*?>>", "<</ModDate XXXXX>>")
        .replaceAll("<</Info .*?>>", "<<\\/Info XXXXX>>");
    }
    
    private String readExpectedFile(String resourceName) throws IOException {
        try (final InputStream expected  = getClass().getResourceAsStream(resourceName)) {
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
            byte[] actualBytes = out.toByteArray();
            return new String(actualBytes, "ISO-8859-1");
        }
    }
}
