/*
 * $Id: ImageSequence.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.images;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Generates 2 documents: one that respects the order of Images added, another that has the default behaviour: only show
 * the images if they fit on the page, if they don't fit, wait until the next page.
 */
public class ImageSequence {

    /**
     * Generates 2 documents: one that respects the order of Images added, another that has the default behaviour: only
     * show the images if they fit on the page, if they don't fit, wait until the next page.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("ImageSequence");

        // step 1: creation of a document-object
        Document document = new Document();

        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter.getInstance(document, new FileOutputStream("notInSequence.pdf"));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("inSequence.pdf"));
            writer.setStrictImageSequence(true);

            // step 3: we open the document
            document.open();

            // step 4:
            document.add(new Paragraph("1st image"));
            Image jpg = Image.getInstance("otsoe.jpg");
            document.add(jpg);
            document.add(new Paragraph("2nd image"));
            Image gif = Image.getInstance("getacro.gif");
            document.add(gif);
            document.add(new Paragraph("3rd image"));
            document.add(jpg);
            document.add(new Paragraph("4th image"));
            document.add(gif);
            document.add(new Paragraph("5th image"));
            document.add(jpg);
            document.add(new Paragraph("6th image"));
            document.add(gif);
            document.add(new Paragraph("7th image"));
            document.add(jpg);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}
