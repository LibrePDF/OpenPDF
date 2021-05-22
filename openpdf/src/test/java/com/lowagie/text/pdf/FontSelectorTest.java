package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.parser.PdfTextExtractor;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FontSelectorTest {
    @Test
    public void testDefaultFont() throws IOException {
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, stream);
        document.open();

        FontSelector selector = new FontSelector();
        selector.addFont(new Font(Font.HELVETICA));
        document.add(selector.process("ΧαίρετεGreek -"));
        document.close();

        PdfReader rd = new PdfReader(stream.toByteArray());
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(rd);
        Assertions.assertEquals(pdfTextExtractor.getTextFromPage(1), "ΧαίρετεGreek -");
    }
}