package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class FontSelectorTest {
    @Test
    public void testDefaultFont() throws IOException {
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, stream);
        document.open();

        FontSelector selector = new FontSelector();
        selector.addFont(new Font(Font.HELVETICA));
        String text = "Greek = χαίρετε";
        Phrase phrase = selector.process(text);
        for (Element chunk : phrase.getChunks()) {
            System.out.format("%s %n", chunk.toString());
        }
        document.add(phrase);
        document.close();

        PdfReader rd = new PdfReader(stream.toByteArray());
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(rd);
        assertThat(pdfTextExtractor.getTextFromPage(1)).isEqualTo(text);
    }
}