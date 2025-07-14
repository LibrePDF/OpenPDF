/*
 * $Id: Measurements.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.general.faq;


import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates the measurement system.
 *
 * @author blowagie
 */
public class Measurements {

    /**
     * Creates a PDF document explaining the measurement system.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Measurements");
        // step 1: creation of a document-object
        Rectangle pageSize = new Rectangle(288, 720);
        Document document = new Document(pageSize, 36, 18, 72, 72);

        try {

            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file

            PdfWriter.getInstance(document, new FileOutputStream("Measurements.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            document.add(new Paragraph("The size of this page is 288x720 points."));
            document.add(new Paragraph("288pt / 72 points per inch = 4 inch"));
            document.add(new Paragraph("720pt / 72 points per inch = 10 inch"));
            document.add(new Paragraph("The size of this page is 4x10 inch."));
            document.add(new Paragraph("4 inch x 2.54 = 10.16 cm"));
            document.add(new Paragraph("10 inch x 2.54 = 25.4 cm"));
            document.add(new Paragraph("The size of this page is 10.16x25.4 cm."));
            document.add(new Paragraph("The left border is 36pt or 0.5 inch or 1.27 cm"));
            document.add(new Paragraph("The right border is 18pt or 0.25 inch or 0.63 cm."));
            document.add(new Paragraph("The top and bottom border are 72pt or 1 inch or 2.54 cm."));
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}