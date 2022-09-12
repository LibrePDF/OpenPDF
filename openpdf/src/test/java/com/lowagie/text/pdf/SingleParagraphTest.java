package com.lowagie.text.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SingleParagraphTest {

    @Test
    void testSingleParagraph() throws IOException {
        Document document = new Document();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, stream);
        document.open();

        Chunk chunk1 = new Chunk("Hier ", FontFactory.getFont(BaseFont.COURIER, 10));
        Chunk chunk2 = new Chunk("fetter", FontFactory.getFont(BaseFont.COURIER_BOLD, 10));
        Chunk chunk3 = new Chunk(" Text", FontFactory.getFont(BaseFont.COURIER, 10));

        Paragraph paragraph = new Paragraph();
        paragraph.add(chunk1);
        paragraph.add(chunk2);
        paragraph.add(chunk3);
        paragraph.setKeepTogether(true);

        document.add(paragraph);
        document.close();

        PdfReader reader = new PdfReader(stream.toByteArray());
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);
        String text = pdfTextExtractor.getTextFromPage(1);
        Assertions.assertEquals(text, "Hier fetter Text");
    }

}
