package com.lowagie.text.pdf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.io.InputStream;

/**
 * Test case for https://github.com/LibrePDF/OpenPDF/issues/1199
 */
public class ReadPDFR5Test {

    @Test
    @Disabled
    void testReadEncryptionREquals5() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("r-equals-5.pdf");
        Assertions.assertNotNull(inputStream, "PDF file not found");

        PdfReader pdfReader = null;

        try {
            pdfReader = new PdfReader(inputStream);
            pdfReader.close();


        } finally {
            if (pdfReader != null) {
                pdfReader.close();
            }

        }
    }
}
