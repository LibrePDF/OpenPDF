package com.lowagie.text.pdf;

import com.lowagie.text.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FooterImageTest {
    @Test
    public void testSimple() throws IOException {
        Document document = new Document(PageSize.A4);
        Image jpg = Image.getInstance("src/test/resources/GitHub-Mark-32px.png");

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("testSimple.pdf"));

        Paragraph footerParagraph = new Paragraph();
        String test = "This is a test line.";
        String footerstr = "footer";
        footerParagraph.add(jpg);
        footerParagraph.add(footerstr);
        HeaderFooter footer = new HeaderFooter(footerParagraph, false);
        document.setFooter(footer);

        document.open();
        document.add(new Paragraph(test));
        document.close();
    }
    @Test
    public void testCenterUnderlyingPosition() throws IOException {
        Document document = new Document(PageSize.A4);
        Image jpg = Image.getInstance("src/test/resources/GitHub-Mark-32px.png");
        jpg.setAlignment(Image.UNDERLYING);

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("testCenterPosition.pdf"));

        Paragraph footerParagraph = new Paragraph();
        String test = "This is a test line.";
        footerParagraph.add(jpg);
        HeaderFooter footer = new HeaderFooter(footerParagraph, true);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.setFooter(footer);

        document.open();
        document.add(new Paragraph(test));
        document.close();
    }


}
