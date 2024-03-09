/*
 * $Id: FontFactoryType1Fonts.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.fonts;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Generates a PDF with the 14 Standard Type 1 Fonts (using FontFactory).
 *
 * @author blowagie
 */

public class FontFactoryType1Fonts {

    /**
     * Generates a PDF file with the 14 standard Type 1 Fonts (using FontFactory)
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Standard Type 1 fonts: FontFactory");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter.getInstance(document,
                    new FileOutputStream("FontFactoryType1Fonts.pdf"));

            // step 3: we open the document
            document.open();
            // step 4:

            // the 14 standard fonts in PDF
            Font[] fonts = new Font[15];
            fonts[0] = FontFactory.getFont(FontFactory.COURIER, Font.DEFAULTSIZE, Font.NORMAL);
            fonts[1] = FontFactory.getFont(FontFactory.COURIER, Font.DEFAULTSIZE, Font.ITALIC);
            fonts[2] = FontFactory.getFont(FontFactory.COURIER, Font.DEFAULTSIZE, Font.BOLD);
            fonts[3] = FontFactory.getFont(FontFactory.COURIER, Font.DEFAULTSIZE, Font.BOLD | Font.ITALIC);
            fonts[4] = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.NORMAL);
            fonts[5] = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.ITALIC);
            fonts[6] = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.BOLD);
            fonts[7] = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.BOLDITALIC);
            fonts[8] = FontFactory.getFont(FontFactory.TIMES_ROMAN, Font.DEFAULTSIZE, Font.NORMAL);
            fonts[9] = FontFactory.getFont(FontFactory.TIMES_ROMAN, Font.DEFAULTSIZE, Font.ITALIC);
            fonts[10] = FontFactory.getFont(FontFactory.TIMES_ROMAN, Font.DEFAULTSIZE, Font.BOLD);
            fonts[11] = FontFactory.getFont(FontFactory.TIMES_ROMAN, Font.DEFAULTSIZE, Font.BOLDITALIC);
            fonts[12] = FontFactory.getFont(FontFactory.SYMBOL, Font.DEFAULTSIZE, Font.NORMAL);
            fonts[13] = FontFactory.getFont(FontFactory.ZAPFDINGBATS, Font.DEFAULTSIZE, Font.NORMAL);
            fonts[14] = FontFactory.getFont(FontFactory.COURIER, Font.DEFAULTSIZE, Font.BOLD | Font.UNDERLINE);
            // add the content
            for (int i = 0; i < fonts.length; i++) {
                document.add(new Paragraph("quick brown fox jumps over the lazy dog", fonts[i]));
            }
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}