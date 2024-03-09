/*
 * $Id: Literal.java 3838 2009-04-07 18:34:15Z mstorer $
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
 * Demonstrates how you can write PDF syntax directly to a document.
 */
public class Literal {


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        System.out.println("Literal PDF Syntax");
        Document.compress = false;

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("literal.pdf"));

            // step 3: we open the document
            document.open();

            // step 4: we grab the ContentByte and do some stuff with it
            PdfContentByte cb = writer.getDirectContent();
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
            cb.setLiteral(star);

            // sanityCheck doesn't check literals.
            //cb.sanityCheck();
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}
