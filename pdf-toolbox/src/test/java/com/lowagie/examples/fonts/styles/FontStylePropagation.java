/*
 * $Id: FontStylePropagation.java 3373 2008-05-12 16:21:24Z xlv $
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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Explains the mechanism of Font Style Propagation.
 *
 * @author blowagie
 */

public class FontStylePropagation {

    /**
     * Explains the mechanism of Font Style Propagation
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Font Style Propagation");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter.getInstance(document,
                    new FileOutputStream("FontStylePropagation.pdf"));

            // step 3: we open the document
            document.open();
            // step 4:
            Phrase myPhrase = new Phrase("Hello 1! ", new Font(Font.TIMES_ROMAN, 8, Font.BOLD));
            myPhrase.add(new Phrase("some other font ", new Font(Font.HELVETICA, 8)));
            myPhrase.add(new Phrase("This is the end of the sentence.\n", new Font(Font.TIMES_ROMAN, 8, Font.ITALIC)));
            document.add(myPhrase);

            myPhrase = new Phrase(12);
            myPhrase.add(new Phrase("Hello 2! ", new Font(Font.TIMES_ROMAN, 8, Font.BOLD)));
            myPhrase.add(new Phrase("This is the end of the sentence.\n", new Font(Font.TIMES_ROMAN, 8, Font.ITALIC)));
            document.add(myPhrase);

            myPhrase = new Phrase("Hello 3! ", FontFactory.getFont(FontFactory.TIMES_ROMAN, 8, Font.BOLD));
            myPhrase.add(new Phrase("some other font ", FontFactory.getFont(FontFactory.HELVETICA, 8)));
            myPhrase.add(new Phrase("This is the end of the sentence.\n",
                    FontFactory.getFont(FontFactory.TIMES_ROMAN, 8, Font.ITALIC)));
            document.add(myPhrase);

            Paragraph myParagraph = new Paragraph("Hello 1bis! ", new Font(Font.TIMES_ROMAN, 8, Font.BOLD));
            myParagraph.add(
                    new Paragraph("This is the end of the sentence.", new Font(Font.TIMES_ROMAN, 8, Font.ITALIC)));
            document.add(myParagraph);

            myParagraph = new Paragraph(12);
            myParagraph.add(new Paragraph("Hello 3bis! ", new Font(Font.TIMES_ROMAN, 8, Font.BOLD)));
            myParagraph.add(
                    new Paragraph("This is the end of the sentence.", new Font(Font.TIMES_ROMAN, 8, Font.ITALIC)));
            document.add(myParagraph);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}