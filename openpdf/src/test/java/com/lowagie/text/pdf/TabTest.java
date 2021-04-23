package com.lowagie.text.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TabTest {
    @Test
    public void TabTest1() throws IOException {
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        Document.compress = false;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document,
                    stream);
            document.open();
            Chunk a = new Chunk("data\ttable");
            document.add(a);
        } catch (Exception de) {
            de.printStackTrace();
        }
        document.close();
        PdfReader rd = new PdfReader(stream.toByteArray());
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(rd);
        Assertions.assertEquals(pdfTextExtractor.getTextFromPage(1), "data\ttable");
    }
}