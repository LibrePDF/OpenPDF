/*
 * $Id: EndOfLine.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects.chunk;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Shows what happens if the end of the line is reached
 *
 * @author blowagie
 */

public class EndOfLine {

    /**
     * Demonstrates the use of the Phrase object.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("End of Line");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter.getInstance(document,
                    new FileOutputStream("EndOfLine.pdf"));

            // step 3: we open the document
            document.open();
            // step 4:
            Chunk chunk = new Chunk("quick brown fox jumps over the lazy dog ");
            for (int i = 0; i < 5; i++) {
                chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_STROKE, 0.3f,
                        new Color(i * 30, i * 30, i * 30));
                document.add(chunk);
            }
            document.newPage();
            Phrase p = new Phrase(16f);
            for (int i = 0; i < 5; i++) {
                chunk = new Chunk("quick brown fox jumps over the lazy dog ");
                chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_STROKE, 0.3f,
                        new Color(i * 30, i * 30, i * 30));
                p.add(chunk);
            }
            document.add(p);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}