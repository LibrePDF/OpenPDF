/*
 * $Id: Lines.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates how to add lines to underline Chunks, strike through, etc...
 *
 * @author blowagie
 */

public class Lines {

    /**
     * Underline or strike through text.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Underline, Strike through,...");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter.getInstance(document,
                    new FileOutputStream("Lines.pdf"));

            // step 3: we open the document
            document.open();
            // step 4:
            Chunk underlined = new Chunk("underlined");
            underlined.setUnderline(0.2f, -2f);
            Paragraph p = new Paragraph("The following chunk is ");
            p.add(underlined);
            document.add(p);
            Chunk strikethru = new Chunk("strike through example");
            strikethru.setUnderline(0.5f, 3f);
            document.add(strikethru);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            Chunk c;
            c = new Chunk("Multiple lines");
            c.setUnderline(new Color(0xFF, 0x00, 0x00), 0.0f, 0.3f, 0.0f, 0.4f, PdfContentByte.LINE_CAP_ROUND);
            c.setUnderline(new Color(0x00, 0xFF, 0x00), 5.0f, 0.0f, 0.0f, -0.5f,
                    PdfContentByte.LINE_CAP_PROJECTING_SQUARE);
            c.setUnderline(new Color(0x00, 0x00, 0xFF), 0.0f, 0.2f, 15.0f, 0.0f, PdfContentByte.LINE_CAP_BUTT);
            document.add(c);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            c = new Chunk("Multiple lines", FontFactory.getFont(FontFactory.HELVETICA, 24));
            c.setUnderline(new Color(0xFF, 0x00, 0x00), 0.0f, 0.3f, 0.0f, 0.4f, PdfContentByte.LINE_CAP_ROUND);
            c.setUnderline(new Color(0x00, 0xFF, 0x00), 5.0f, 0.0f, 0.0f, -0.5f,
                    PdfContentByte.LINE_CAP_PROJECTING_SQUARE);
            c.setUnderline(new Color(0x00, 0x00, 0xFF), 0.0f, 0.2f, 15.0f, 0.0f, PdfContentByte.LINE_CAP_BUTT);
            document.add(c);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}