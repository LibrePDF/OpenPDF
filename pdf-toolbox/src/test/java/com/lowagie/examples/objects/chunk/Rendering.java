/*
 * $Id: Rendering.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Special rendering of Chunks.
 *
 * @author blowagie
 */

public class Rendering {

    /**
     * Special rendering of Chunks.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Rendering");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter.getInstance(document,
                    new FileOutputStream("Rendering.pdf"));

            // step 3: we open the document
            document.open();
            // step 4:            
            Paragraph p = new Paragraph("Text Rendering:");
            document.add(p);
            Chunk chunk = new Chunk("rendering test");
            chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL, 100f, new Color(0xFF, 0x00, 0x00));
            document.add(chunk);
            document.add(Chunk.NEWLINE);
            chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.3f, new Color(0xFF, 0x00, 0x00));
            document.add(chunk);
            document.add(Chunk.NEWLINE);
            chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_INVISIBLE, 100f, new Color(0x00, 0xFF, 0x00));
            document.add(chunk);
            document.add(Chunk.NEWLINE);
            chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_STROKE, 0.3f, new Color(0x00, 0x00, 0xFF));
            document.add(chunk);
            document.add(Chunk.NEWLINE);
            Chunk bold = new Chunk("This looks like Font.BOLD");
            bold.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, 0.5f, new Color(0x00, 0x00, 0x00));
            document.add(bold);

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}