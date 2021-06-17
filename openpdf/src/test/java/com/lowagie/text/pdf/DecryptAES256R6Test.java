package com.lowagie.text.pdf;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.lowagie.text.pdf.parser.PdfTextExtractor;

/**
 * This class tests the OpenPDF decryption feature for
 * AES256 encrypted files according to ISO 32000-2, i.e.
 * for <code>R = 6</code>.
 * <p>
 * See also <a href="https://github.com/LibrePDF/OpenPDF/issues/375">OpenPDF issue 375</a>
 * 
 * @author mkl
 */
class DecryptAES256R6Test {
    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * <a href="https://github.com/LibrePDF/OpenPDF/files/4700100/pwProtectedAES256_openPDFiss375.pdf">
     * pwProtectedAES256_openPDFiss375.pdf
     * </a>
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt
     * a file which is AES256 encrypted according to ISO 32000-2.
     */
    @Test
    void testReadPwProtectedAES256_openPDFiss375() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/issue375/pwProtectedAES256_openPDFiss375.pdf")  ) {
            PdfReader pdfReader = new PdfReader(resource);
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(), "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("TEST", new PdfTextExtractor(pdfReader).getTextFromPage(1), "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * Demo1_encrypted_.pdf provided by TvT
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt
     * a file which is AES256 encrypted according to ISO 32000-2.
     */
    @Test
    void testReadDemo1Encrypted() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/issue375/Demo1_encrypted_.pdf")  ) {
            PdfReader pdfReader = new PdfReader(resource);
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(), "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Demo   Name   Signature   Date  Elizabeth Schultz (Apr 24, 2018) Elizabeth Schultz Apr 24, 2018 Elizabeth Schultz Sue Northrop (Apr 24, 2018) Apr 24, 2018 Sue Northrop", new PdfTextExtractor(pdfReader).getTextFromPage(1), "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }
}
