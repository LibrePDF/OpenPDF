/*
 * $Id: Patterns.java 3838 2009-04-07 18:34:15Z mstorer $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */
package com.lowagie.examples.directcontent.colors;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.GrayColor;
import com.lowagie.text.pdf.PatternColor;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPatternPainter;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Painting patterns
 */
public class Patterns {

    /**
     * Painting patterns.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Painting Patterns");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("patterns.pdf"));

            // step 3: we open the document
            document.open();

            // step 4: we grab the ContentByte and do some stuff with it
            PdfContentByte cb = writer.getDirectContent();

            BaseFont bf = BaseFont.createFont("Times-Roman", "winansi", false);

            // step 5: we create some PdfPatternPainter instances for drawing path, text, or placing image

            // Image instance to be placed in PdfPatternPainter canvas. Any nice one?
            Image img = Image.getInstance("pngnow.png");

            PdfPatternPainter p = cb.createPattern(60f, 60f, 60f, 60f);
            PdfPatternPainter p1 = cb.createPattern(60f, 60f, 60f, 60f);
            PdfPatternPainter p2 = cb.createPattern(img.getScaledWidth(), img.getScaledHeight(), img.getScaledWidth(),
                    img.getScaledHeight());

            // step 6: put your drawing instruction in the painter canvas

            // A star pattern taken from Adobe PDF Reference Book p.207
            String star = "0.3 g\n15.000 27.000 m\n"
                    + "7.947 5.292 l\n26.413 18.708 l\n"
                    + "3.587 18.708 l\n22.053 5.292 l\nf\n"
                    + "45.000 57.000 m\n37.947 35.292 l\n"
                    + "56.413 48.708 l\n33.587 48.708 l\n"
                    + "52.053 35.292 l\nf\n"
                    + "0.7 g\n15.000 57.000 m\n"
                    + "7.947 35.292 l\n26.413 48.708 l\n"
                    + "3.587 48.708 l\n22.053 35.292 l\nf\n"
                    + "45.000 27.000 m\n37.947 5.292 l\n"
                    + "56.413 18.708 l\n33.587 18.708 l\n"
                    + "52.053 5.292 l\nf";

            p.setLiteral(star);

            // A Pattern with some text drawing
            p1.setGrayFill(0.3f);
            p1.setFontAndSize(bf, 12);
            p1.beginText();
            p1.setTextMatrix(1f, 0f, 0f, 1f, 0f, 0f);
            p1.showText("A B C D");
            p1.endText();
            p1.moveTo(0f, 0f);
            p1.lineTo(60f, 60f);
            p1.stroke();
            p1.sanityCheck();

            // A pattern with an image and position
            p2.addImage(img, img.getScaledWidth(), 0f, 0f, img.getScaledHeight(), 0f, 0f);
            p2.setPatternMatrix(1f, 0f, 0f, 1f, 60f, 60f);
            p2.sanityCheck();

            // See if we can apply the pattern color to chunk, phrase or paragraph
            PatternColor pat = new PatternColor(p);
            PatternColor pat1 = new PatternColor(p1);
            PatternColor pat2 = new PatternColor(p2);
            String text = "Text with pattern";
            document.add(new Paragraph(text,
                    FontFactory.getFont(FontFactory.HELVETICA, 60, Font.BOLD, new GrayColor(0.3f))));
            document.add(new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 60, Font.BOLD, pat)));

            // draw a rectangle filled with star pattern
            cb.setPatternFill(p);
            cb.setGrayStroke(0.0f);
            cb.rectangle(20, 20, 284, 120);
            cb.fillStroke();

            // draw some characters filled with star.
            // Note: A gray, rgb, cmyk or spot color should be applied first
            // otherwise, you will not be able to see the character glyph
            // since the glyph path is filled by pattern
            cb.beginText();
            cb.setFontAndSize(bf, 1);
            cb.setTextMatrix(270f, 0f, 0f, 270f, 20f, 100f);
            cb.setGrayFill(0.9f);
            cb.showText("ABC");
            cb.setPatternFill(p);
            cb.moveTextWithLeading(0.0f, 0.0f);
            cb.showText("ABC");
            cb.endText();
            cb.setPatternFill(p);

            // draw a circle. Similar to rectangle
            cb.setGrayStroke(0.0f);
            cb.circle(150f, 400f, 150f);
            cb.fillStroke();

            // New Page to draw text in the pattern painter's canvas
            document.newPage();

            document.add(new Paragraph(text,
                    FontFactory.getFont(FontFactory.HELVETICA, 60, Font.BOLD, new GrayColor(0.3f))));
            document.add(new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 60, Font.BOLD, pat1)));
            // draw a rectangle
            cb.setPatternFill(p1);
            cb.setGrayStroke(0.0f);
            cb.rectangle(0, 0, 284, 120);
            cb.fillStroke();

            // draw some characters
            cb.beginText();
            cb.setFontAndSize(bf, 1);
            cb.setTextMatrix(270f, 0f, 0f, 270f, 20f, 100f);
            cb.setGrayFill(0.9f);
            cb.showText("ABC");
            cb.setPatternFill(p1);
            cb.moveTextWithLeading(0.0f, 0.0f);
            cb.showText("ABC");
            cb.endText();

            // draw a circle
            cb.setPatternFill(p1);
            cb.setGrayStroke(0.0f);
            cb.circle(150f, 400f, 150f);
            cb.fillStroke();
            cb.sanityCheck();

            // New page to place image in the pattern painter's canvas
            document.newPage();
            document.add(new Paragraph(text,
                    FontFactory.getFont(FontFactory.HELVETICA, 60, Font.BOLD, new GrayColor(0.3f))));
            document.add(new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 60, Font.BOLD, pat2)));
            // The original Image for comparison reason.
            // Note: The width and height is the same as bbox in pattern
            cb.addImage(img, img.getScaledWidth(), 0f, 0f, img.getScaledHeight(), 350f, 400f);

            // draw a rectangle
            cb.setPatternFill(p2);
            cb.setGrayStroke(0.0f);
            cb.rectangle(60, 60, 300, 120);
            cb.fillStroke();

            // draw some characters.
            // Note: if the image fills up the pattern, there's no need to draw text twice
            // since colors in image will be clipped to character glyph path
            cb.beginText();
            cb.setFontAndSize(bf, 1);
            cb.setTextMatrix(270f, 0f, 0f, 270f, 60f, 120f);
            cb.setPatternFill(p2);
            cb.showText("ABC");
            cb.endText();

            // draw a circle
            cb.setPatternFill(p2);
            cb.setGrayStroke(0.0f);
            cb.circle(150f, 400f, 150f);
            cb.fillStroke();

            cb.sanityCheck();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // finally, we close the document
        document.close();
    }
}