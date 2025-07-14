/*
 * $Id: Chunks.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates some Chunk functionality.
 *
 * @author blowagie
 */

public class Chunks {

    /**
     * Demonstrates some Chunk functionality.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("the Chunk object");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter.getInstance(document,
                    new FileOutputStream("Chunks.pdf"));

            // step 3: we open the document
            document.open();
            // step 4:
            Chunk fox = new Chunk("quick brown fox");
            float superscript = 8.0f;
            fox.setTextRise(superscript);
            fox.setBackground(new Color(0xFF, 0xDE, 0xAD));
            Chunk jumps = new Chunk(" jumps over ");
            Chunk dog = new Chunk("the lazy dog");
            float subscript = -8.0f;
            dog.setTextRise(subscript);
            dog.setUnderline(new Color(0xFF, 0x00, 0x00), 3.0f, 0.0f, -5.0f + subscript, 0.0f,
                    PdfContentByte.LINE_CAP_ROUND);
            document.add(fox);
            document.add(jumps);
            document.add(dog);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}