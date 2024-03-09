package com.lowagie.text;

import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FooterImageTest {

    @Test
    public void onlyTextFooterUpperBoundTest() {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new ByteArrayOutputStream());

        Paragraph footerParagraph = new Paragraph();
        String test = "This is a test line.";
        String footerstr = "footer";
        footerParagraph.add(footerstr);
        HeaderFooter footer = new HeaderFooter(footerParagraph, false);
        document.setFooter(footer);

        document.open();
        document.add(new Paragraph(test));
        document.close();
        Assertions.assertEquals(52.0, footer.getTop());
    }

    @Test
    public void imageLeftAlignmentPositionTest() throws IOException {
        Document document = new Document(PageSize.A4);
        Image jpg = Image.getInstance(
                Objects.requireNonNull(getClass().getClassLoader().getResource("GitHub-Mark-32px.png")));
        PdfWriter.getInstance(document, new ByteArrayOutputStream());

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

        float footerTop = footer.getTop();
        float imageBottom = footerTop - jpg.getRelativeTop() - jpg.getScaledHeight();
        float imageIndentLeft = document.left() - jpg.matrix()[4];
        Assertions.assertEquals(92.0, footerTop);
        Assertions.assertEquals(60.0, imageBottom);
        Assertions.assertEquals(36.0, imageIndentLeft);
    }

    @Test
    public void imageRightAlignmentPositionTest() throws IOException {
        Document document = new Document(PageSize.A4);
        Image jpg = Image.getInstance(
                Objects.requireNonNull(getClass().getClassLoader().getResource("GitHub-Mark-32px.png")));
        PdfWriter.getInstance(document, new ByteArrayOutputStream());
        jpg.setAlignment(Element.ALIGN_RIGHT);

        Paragraph footerParagraph = new Paragraph();
        String test = "This is a test line.";
        footerParagraph.add(jpg);
        HeaderFooter footer = new HeaderFooter(footerParagraph, false);
        document.setFooter(footer);

        document.open();
        document.add(new Paragraph(test));
        document.close();

        float footerTop = footer.getTop();
        float imageBottom = footerTop - jpg.getRelativeTop() - jpg.getScaledHeight();
        float imageIndentRight = document.right() - jpg.getScaledWidth() - jpg.matrix()[4];
        imageIndentRight -= jpg.getIndentationRight();
        Assertions.assertEquals(76.0, footerTop);
        Assertions.assertEquals(44.0, imageBottom);
        Assertions.assertEquals(527.0, imageIndentRight);
    }

    @Test
    public void centerUnderlyingPositionTest() throws IOException {
        Document document = new Document(PageSize.A4);
        Image jpg = Image.getInstance(
                Objects.requireNonNull(getClass().getClassLoader().getResource("GitHub-Mark-32px.png")));
        jpg.setAlignment(Image.UNDERLYING);

        PdfWriter.getInstance(document, new ByteArrayOutputStream());

        Paragraph footerParagraph = new Paragraph();
        String test = "This is a test line.";
        footerParagraph.add(jpg);
        HeaderFooter footer = new HeaderFooter(footerParagraph, true);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.setFooter(footer);

        document.open();
        document.add(new Paragraph(test));
        document.close();

        float footerTop = footer.getTop();
        float imageBottom = footerTop - jpg.getRelativeTop() - jpg.getScaledHeight();
        float imageIndentLeft = document.left() + (document.right() - document.left() - jpg.getScaledWidth()) / 2;
        imageIndentLeft -= jpg.getIndentationRight() + jpg.matrix()[4];
        Assertions.assertEquals(76.0, footerTop);
        Assertions.assertEquals(44.0, imageBottom);
        Assertions.assertEquals(281.5, imageIndentLeft);
    }

    @Test
    public void multiplePageWithImageAndNumberTest() throws IOException {
        Document document = new Document(PageSize.A4);
        Image jpg = Image.getInstance("src/test/resources/GitHub-Mark-32px.png");
        jpg.setAlignment(Image.UNDERLYING);

        PdfWriter.getInstance(document, new ByteArrayOutputStream());

        Paragraph footerParagraph = new Paragraph();
        String test = "Github manual.";
        footerParagraph.add(jpg);
        HeaderFooter footer = new HeaderFooter(footerParagraph, true);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.setFooter(footer);

        document.open();
        for (int i = 0; i < 100; i++) {
            document.add(new Paragraph(test));
        }
        document.close();

        float footerTop = footer.getTop();
        float imageBottom = footerTop - jpg.getRelativeTop() - jpg.getScaledHeight();
        float imageIndentLeft = document.left() + (document.right() - document.left() - jpg.getScaledWidth()) / 2;
        imageIndentLeft -= jpg.getIndentationRight() + jpg.matrix()[4];
        Assertions.assertEquals(76.0, footerTop);
        Assertions.assertEquals(44.0, imageBottom);
        Assertions.assertEquals(281.5, imageIndentLeft);

    }
}
