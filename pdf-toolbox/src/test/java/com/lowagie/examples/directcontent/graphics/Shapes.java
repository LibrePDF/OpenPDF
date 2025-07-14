/*
 * $Id: Shapes.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent.graphics;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Drawing some shapes.
 */
public class Shapes {

    /**
     * Draws some shapes.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Drawing some shapes");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("shapes.pdf"));

            // step 3: we open the document
            document.open();

            // step 4: we grab the ContentByte and do some stuff with it
            PdfContentByte cb = writer.getDirectContent();

            // an example of a rectangle with a diagonal in very thick lines
            cb.setLineWidth(10f);
            // draw a rectangle
            cb.rectangle(100, 700, 100, 100);
            // add the diagonal
            cb.moveTo(100, 700);
            cb.lineTo(200, 800);
            // stroke the lines
            cb.stroke();

            // an example of some circles
            cb.setLineDash(3, 3, 0);
            cb.setRGBColorStrokeF(0f, 255f, 0f);
            cb.circle(150f, 500f, 100f);
            cb.stroke();

            cb.setLineWidth(5f);
            cb.resetRGBColorStroke();
            cb.circle(150f, 500f, 50f);
            cb.stroke();

            // example with colorfill
            cb.setRGBColorFillF(0f, 255f, 0f);
            cb.moveTo(100f, 200f);
            cb.lineTo(200f, 250f);
            cb.lineTo(400f, 150f);
            // because we change the fill color BEFORE we stroke the triangle
            // the color of the triangle will be red instead of green
            cb.setRGBColorFillF(255f, 0f, 0f);
            cb.closePathFillStroke();

            cb.sanityCheck();
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }

}
