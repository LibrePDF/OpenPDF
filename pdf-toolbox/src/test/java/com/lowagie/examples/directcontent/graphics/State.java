/*
 * $Id: State.java 3838 2009-04-07 18:34:15Z mstorer $
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
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Changing the Graphics State with saveState() and restoreState().
 */
public class State {

    /**
     * Changing the Graphics State with saveState() and restoreState().
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Changing the Graphics State with save and restore");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("state.pdf"));

            // step 3: we open the document
            document.open();

            // step 4: we grab the ContentByte and do some stuff with it
            PdfContentByte cb = writer.getDirectContent();

            cb.circle(260.0f, 500.0f, 250.0f);
            cb.fill();
            cb.saveState();
            cb.setColorFill(Color.red);
            cb.circle(260.0f, 500.0f, 200.0f);
            cb.fill();
            cb.saveState();
            cb.setColorFill(Color.blue);
            cb.circle(260.0f, 500.0f, 150.0f);
            cb.fill();
            cb.restoreState();
            cb.circle(260.0f, 500.0f, 100.0f);
            cb.fill();
            cb.restoreState();
            cb.circle(260.0f, 500.0f, 50.0f);
            cb.fill();

            cb.sanityCheck();
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}