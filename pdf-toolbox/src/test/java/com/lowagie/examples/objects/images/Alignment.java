/*
 * $Id: Alignment.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates the alignment method and parameters.
 */
public class Alignment {

    /**
     * Demonstrates the alignment method.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Alignment of images");
        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2: creation of a writer
            PdfWriter.getInstance(document, new FileOutputStream("alignment.pdf"));

            // step 3: we open the document
            document.open();

            Image gif = Image.getInstance("vonnegut.gif");
            gif.setAlignment(Image.RIGHT);
            Image jpeg = Image.getInstance("otsoe.jpg");
            jpeg.setAlignment(Image.MIDDLE);
            Image png = Image.getInstance("hitchcock.png");
            png.setAlignment(Image.LEFT);

            document.add(gif);
            document.add(jpeg);
            document.add(png);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        // step 5: we close the document
        document.close();
    }
}
