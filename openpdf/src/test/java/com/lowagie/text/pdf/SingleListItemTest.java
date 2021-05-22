package com.lowagie.text.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SingleListItemTest {

    @Test
    void testSingleParagraph() throws IOException {
        Document document = new Document();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, stream);
        document.open();

        Chunk chunk1 = new Chunk("Hier ", FontFactory.getFont(BaseFont.COURIER, 10));
        Chunk chunk2 = new Chunk("fetter", FontFactory.getFont(BaseFont.COURIER_BOLD, 10));
        Chunk chunk3 = new Chunk(" Text", FontFactory.getFont(BaseFont.COURIER, 10));

        ListItem listItem = new ListItem();
        listItem.add(chunk1);
        listItem.add(chunk2);
        listItem.add(chunk3);
        listItem.setKeepTogether(true);

        document.add(listItem);
        document.close();

        PdfReader reader = new PdfReader(stream.toByteArray());
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);
        String text = pdfTextExtractor.getTextFromPage(1);
        Assertions.assertEquals(text, "Hier fetter Text");
    }

}
