/*
 * $Id: Phrases.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * How to use class Phrase.
 *
 * @author blowagie
 */

public class Phrases {

    /**
     * Demonstrates how the class Phrase works.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Phrases");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter
                    .getInstance(document, new FileOutputStream("Phrases.pdf"));

            // step 3: we open the document
            document.open();
            // step 4:
            Phrase phrase1 = new Phrase("(1) this is a phrase\n");
            // In this example the leading is passed as a parameter
            Phrase phrase2 = new Phrase(
                    24,
                    "(2) this is a phrase with leading 24. You can only see the difference if the line is long"
                            + " enough. Do you see it? There is more space between this line and the previous one.\n");
            // When a Font is passed (explicitely or embedded in a chunk),
            // the default leading = 1.5 * size of the font
            Phrase phrase3 = new Phrase(
                    "(3) this is a phrase with a red, normal font Courier, size 20. As you can see the leading is automatically changed.\n",
                    FontFactory.getFont(FontFactory.COURIER, 20, Font.NORMAL,
                            new Color(255, 0, 0)));
            Phrase phrase4 = new Phrase(new Chunk("(4) this is a phrase\n"));
            Phrase phrase5 = new Phrase(
                    18,
                    new Chunk(
                            "(5) this is a phrase in Helvetica, bold, red and size 16 with a given leading of 18 points.\n",
                            FontFactory.getFont(FontFactory.HELVETICA, 16,
                                    Font.BOLD, new Color(255, 0, 0))));
            // A Phrase can contains several chunks with different fonts
            Phrase phrase6 = new Phrase("(6)");
            Chunk chunk = new Chunk(" This is a font: ");
            phrase6.add(chunk);
            phrase6.add(new Chunk("Helvetica", FontFactory.getFont(
                    FontFactory.HELVETICA, 12)));
            phrase6.add(chunk);
            phrase6.add(new Chunk("Times New Roman", FontFactory.getFont(
                    FontFactory.TIMES_ROMAN, 12)));
            phrase6.add(chunk);
            phrase6.add(new Chunk("Courier", FontFactory.getFont(
                    FontFactory.COURIER, 12)));
            phrase6.add(chunk);
            phrase6.add(new Chunk("Symbol", FontFactory.getFont(
                    FontFactory.SYMBOL, 12)));
            phrase6.add(chunk);
            phrase6.add(new Chunk("ZapfDingBats", FontFactory.getFont(
                    FontFactory.ZAPFDINGBATS, 12)));
            Phrase phrase7 = new Phrase(
                    "(7) if you don't add a newline yourself, all phrases are glued to eachother!");

            document.add(phrase1);
            document.add(phrase2);
            document.add(phrase3);
            document.add(phrase4);
            document.add(phrase5);
            document.add(phrase6);
            document.add(phrase7);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}