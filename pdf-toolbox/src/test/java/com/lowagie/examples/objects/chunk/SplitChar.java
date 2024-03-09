/*
 * $Id: SplitChar.java 3374 2008-05-12 18:42:56Z xlv $
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
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.SplitCharacter;
import com.lowagie.text.pdf.PdfChunk;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates the use of the splitcharacter.
 *
 * @author blowagie
 */

public class SplitChar implements SplitCharacter {

    /**
     * Demonstrates the use of the splitcharacter.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("SplitCharacter");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter.getInstance(document,
                    new FileOutputStream("SplitChar.pdf"));

            // step 3: we open the document
            document.open();
            // step 4:
            Chunk chunk;
            Paragraph p;
            String text = "Some.text.to.show.the.splitting.action.of.the.interface.";
            Font font = FontFactory.getFont(FontFactory.HELVETICA, 24);

            document.add(new Paragraph("Normal split."));
            chunk = new Chunk(text, font);
            p = new Paragraph(24, chunk);
            document.add(p);

            document.add(new Paragraph("The dot '.' is the split character."));
            chunk = new Chunk(text, font);
            chunk.setSplitCharacter(new SplitChar());
            p = new Paragraph(24, chunk);
            document.add(p);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }

    /**
     * @see com.lowagie.text.SplitCharacter#isSplitCharacter(int, int, int, char[], com.lowagie.text.pdf.PdfChunk[])
     */
    public boolean isSplitCharacter(int start, int current, int end, char[] cc, PdfChunk[] ck) {
        char c;
        if (ck == null) {
            c = cc[current];
        } else {
            c = (char) ck[Math.min(current, ck.length - 1)].getUnicodeEquivalent(cc[current]);
        }
        return (c == '.');
    }
}