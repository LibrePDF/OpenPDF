package com.lowagie.text.pdf;

import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TextExtractTest {
    @Test
    public void textExtractTest1() throws IOException {
        PdfReader reader = new PdfReader(TextExtractTest.class.getResourceAsStream("/identity-h.pdf"));
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);
        Assertions.assertEquals("Hello World", pdfTextExtractor.getTextFromPage(1));
    }

    @Test
    public void textExtractTest2() throws IOException {
        PdfReader reader = new PdfReader(TextExtractTest.class.getResourceAsStream("/HelloWorldMeta.pdf"));
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);
        Assertions.assertEquals("Hello World", pdfTextExtractor.getTextFromPage(1));
    }
}
