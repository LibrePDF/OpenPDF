/*
 * $Id: FontColor.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.fonts.styles;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * How to change the color of a font.
 *
 * @author blowagie
 */

public class FontColor {

    /**
     * Changing Font colors
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("ChunkColor");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("FontColor.pdf"));

            // step 3: we open the document
            document.open();
            // step 4:
            Font red = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.BOLD,
                    new Color(0xFF, 0x00, 0x00));
            Font blue = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.ITALIC,
                    new Color(0x00, 0x00, 0xFF));
            Paragraph p;
            p = new Paragraph("Roses are ");
            p.add(new Chunk("red", red));
            document.add(p);
            p = new Paragraph("Violets are ");
            p.add(new Chunk("blue", blue));
            document.add(p);
            BaseFont bf = FontFactory.getFont(FontFactory.COURIER).getCalculatedBaseFont(false);
            PdfContentByte cb = writer.getDirectContent();
            cb.beginText();
            cb.setColorFill(new Color(0x00, 0xFF, 0x00));
            cb.setFontAndSize(bf, 12);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "Grass is green", 250, 700, 0);
            cb.endText();
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}