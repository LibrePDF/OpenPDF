package com.lowagie.text.pdf.parser;

import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PdfTextExtractorTest {

    @Test
    void testPageExceeded() throws Exception {
        assertThat(getString("HelloWorldMeta.pdf", 5), is(emptyString()));
    }

    @Test
    void testInvalidPageNumber() throws Exception {
        assertThat(getString("HelloWorldMeta.pdf", 0), is(emptyString()));
    }


    @Test
    void testConcatenateWatermark() throws Exception {
        String result = getString("merge-acroforms.pdf", 5);
        assertNotNull(result);
        // html??
        result = result.replaceAll("<.*?>", "");
        // Multiple spaces between words??
        assertTrue(result.contains("2.  This  is  chapter  2"));
        assertTrue(result.contains("watermark-concatenate"));
    }


    private String getString(String fileName, int pageNumber) throws Exception {
        URL resource = getClass().getResource("/" + fileName);
        return getString(new File(resource.toURI()), pageNumber);
    }

    private String getString(File file, int pageNumber) throws Exception {
        byte[] pdfBytes = readDocument(file);
        final PdfReader pdfReader = new PdfReader(pdfBytes);

        return new PdfTextExtractor(pdfReader).getTextFromPage(pageNumber);
    }

    protected static byte[] readDocument(final File file) throws IOException {

        try (ByteArrayOutputStream fileBytes = new ByteArrayOutputStream();
             InputStream inputStream = new FileInputStream(file)) {
            final byte[] buffer = new byte[8192];
            while (true) {
                final int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                fileBytes.write(buffer, 0, bytesRead);
            }
            return fileBytes.toByteArray();
        }

    }
}
