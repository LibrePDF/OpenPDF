/*
 * $Id: SpotColors.java 4079 2009-10-21 17:04:41Z blowagie $
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
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.GrayColor;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfSpotColor;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SpotColor;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Demonstrates the use of spotcolors.
 */
public class SpotColors {

    /**
     * Demonstrates the use of spotcolors.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Pantone example : Spot Color");

        // step 1: creation of a document-object
        Document document = new Document();
        try {

            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("spotcolor.pdf"));
            BaseFont bf = BaseFont.createFont("Helvetica", "winansi", BaseFont.NOT_EMBEDDED);

            // step 3: we open the document
            document.open();

            // step 4: we grab the ContentByte and do some stuff with it
            PdfContentByte cb = writer.getDirectContent();

            // step 5: we instantiate PdfSpotColor

            // Note: I made up these names unless someone give me a PANTONE swatch as gift (phillip@formstar.com)
            PdfSpotColor spc_cmyk = new PdfSpotColor("PANTONE 280 CV", new CMYKColor(0.9f, .2f, .3f, .1f));
            PdfSpotColor spc_rgb = new PdfSpotColor("PANTONE 147", new Color(114, 94, 38));
            PdfSpotColor spc_g = new PdfSpotColor("PANTONE 100 CV", new GrayColor(0.9f));

            // Stroke a rectangle with CMYK alternate
            cb.setColorStroke(spc_cmyk, .5f);
            cb.setLineWidth(10f);
            // draw a rectangle
            cb.rectangle(100, 700, 100, 100);
            // add the diagonal
            cb.moveTo(100, 700);
            cb.lineTo(200, 800);
            // stroke the lines
            cb.stroke();

            // Fill a rectangle with CMYK alternate
            cb.setColorFill(spc_cmyk, 0.25f);
            cb.rectangle(250, 700, 100, 100);
            cb.fill();

            // Stroke a circle with RGB alternate
            cb.setColorStroke(spc_rgb, 0.9f);
            cb.setLineWidth(5f);
            cb.circle(150f, 500f, 100f);
            cb.stroke();

            // Fill the circle with RGB alternate
            cb.setColorFill(spc_rgb, 0.9f);
            cb.circle(150f, 500f, 50f);
            cb.fill();

            // example with colorfill
            cb.setColorFill(spc_g, 0.5f);
            cb.moveTo(100f, 200f);
            cb.lineTo(200f, 250f);
            cb.lineTo(400f, 150f);
            cb.fill();
            // cb.sanityCheck is called during newPage().
            document.newPage();
            String text = "Some text to show";
            document.add(
                    new Paragraph(text, new Font(Font.HELVETICA, 24, Font.NORMAL, new SpotColor(spc_cmyk, 0.25f))));
            document.add(new Paragraph(text, new Font(Font.HELVETICA, 24, Font.NORMAL, new SpotColor(spc_cmyk, 0.5f))));

            // example with template
            PdfTemplate t = cb.createTemplate(500f, 500f);
            // Stroke a rectangle with CMYK alternate
            t.setColorStroke(new SpotColor(spc_cmyk, .5f));
            t.setLineWidth(10f);
            // draw a rectangle
            t.rectangle(100, 10, 100, 100);
            // add the diagonal
            t.moveTo(100, 10);
            t.lineTo(200, 100);
            // stroke the lines
            t.stroke();

            // Fill a rectangle with CMYK alternate
            t.setColorFill(spc_g, 0.5f);
            t.rectangle(100, 125, 100, 100);
            t.fill();
            t.beginText();
            t.setFontAndSize(bf, 20f);
            t.setTextMatrix(1f, 0f, 0f, 1f, 10f, 10f);
            t.showText("Template text upside down");
            t.endText();
            t.rectangle(0, 0, 499, 499);
            t.stroke();
            t.sanityCheck();
            cb.addTemplate(t, -1.0f, 0.00f, 0.00f, -1.0f, 550f, 550f);

            cb.sanityCheck();
        } catch (Exception de) {
            de.printStackTrace();
        }

        // step 5: we close the document
        document.close();
    }


}
