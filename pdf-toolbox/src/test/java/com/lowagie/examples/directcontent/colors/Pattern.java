/*
 * $Id: Pattern.java 4079 2009-10-21 17:04:41Z blowagie $
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
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.GrayColor;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPatternPainter;
import com.lowagie.text.pdf.PdfSpotColor;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SpotColor;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Painting Patterns.
 */
public class Pattern {

    /**
     * Painting Patterns.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Stencil");

        // step 1: creation of a document-object
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {

            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("pattern.pdf"));

            // step 3: we open the document
            document.open();

            // step 4: we add some content
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(400, 300);
            PdfPatternPainter pat = cb.createPattern(15, 15, null);
            pat.rectangle(5, 5, 5, 5);
            pat.fill();
            pat.sanityCheck();

            PdfSpotColor spc_cmyk = new PdfSpotColor("PANTONE 280 CV", new CMYKColor(0.9f, .2f, .3f, .1f));
            SpotColor spot = new SpotColor(spc_cmyk, 0.25f);
            tp.setPatternFill(pat, spot, .9f);
            tp.rectangle(0, 0, 400, 300);
            tp.fill();
            tp.sanityCheck();

            cb.addTemplate(tp, 50, 50);
            PdfPatternPainter pat2 = cb.createPattern(10, 10, null);
            pat2.setLineWidth(2);
            pat2.moveTo(-5, 0);
            pat2.lineTo(10, 15);
            pat2.stroke();
            pat2.moveTo(0, -5);
            pat2.lineTo(15, 10);
            pat2.stroke();
            cb.setLineWidth(1);
            cb.setColorStroke(Color.black);
            cb.setPatternFill(pat2, Color.red);
            cb.rectangle(100, 400, 30, 210);
            cb.fillStroke();
            cb.setPatternFill(pat2, Color.green);
            cb.rectangle(150, 400, 30, 100);
            cb.fillStroke();
            cb.setPatternFill(pat2, Color.blue);
            cb.rectangle(200, 400, 30, 130);
            cb.fillStroke();
            cb.setPatternFill(pat2, new GrayColor(0.5f));
            cb.rectangle(250, 400, 30, 80);
            cb.fillStroke();
            cb.setPatternFill(pat2, new GrayColor(0.7f));
            cb.rectangle(300, 400, 30, 170);
            cb.fillStroke();
            cb.setPatternFill(pat2, new GrayColor(0.9f));
            cb.rectangle(350, 400, 30, 40);
            cb.fillStroke();

            cb.sanityCheck();
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step 5: we close the document
        document.close();
    }
}
