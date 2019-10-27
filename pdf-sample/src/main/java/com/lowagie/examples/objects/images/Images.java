/*
 * $Id: Images.java 3373 2008-05-12 16:21:24Z xlv $
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
 * General Images example.
 */
public class Images {

    /**
     * General Images example
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Objects :: Images :: Images");

        // step 1: creation of a document-object
        Document document = new Document();

        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter.getInstance(document, new FileOutputStream(args[0] + "/Images.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            document.add(new Paragraph("A picture of my dog: otsoe.jpg"));
            Image jpg = Image.getInstance(Images.class.getClassLoader().getResource("otsoe.jpg"));
            document.add(jpg);
            document.add(new Paragraph("getacro.gif"));
            Image gif = Image.getInstance(Images.class.getClassLoader().getResource("getacro.gif"));
            document.add(gif);
            document.add(new Paragraph("pngnow.png"));
            Image png = Image.getInstance(Images.class.getClassLoader().getResource("pngnow.png"));
            document.add(png);
            document.add(new Paragraph("iText.bmp"));
            Image bmp = Image.getInstance(Images.class.getClassLoader().getResource("iText.bmp"));
            document.add(bmp);
            document.add(new Paragraph("iText.wmf"));
            Image wmf = Image.getInstance(Images.class.getClassLoader().getResource("iText.wmf"));
//            document.add(wmf);
//            document.add(new Paragraph("iText.tif"));
//            Image tif = Image.getInstance(Images.class.getClassLoader().getResource("iText.tif"));
//            document.add(tif);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}