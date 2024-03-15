/*
 * $Id: FontSelection.java 3373 2008-05-12 16:21:24Z xlv $
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

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.FontSelector;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Selects the appropriate fonts that contain the glyphs needed to render text correctly.
 */
public class FontSelection {

    /**
     * Using FontSelector.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        try {
            // step 1
            Document document = new Document();
            // step 2
            PdfWriter.getInstance(document, new FileOutputStream("fontselection.pdf"));
            // step 3
            document.open();
            // step 4
            String text = "This text is the first verse of \u275dThe Iliad\u275e. It's not polytonic as it should be "
                    + "with \u2798 and \u279a entoation variants but that's all we have for now.\n\n"
                    + "\u2766\u00a0\u00a0\u039c\u03b7\u03bd\u03b9\u03bd \u03b1\u03b5\u03b9\u03b4\u03b5, "
                    + "\u03b8\u03b5\u03b1, \u03a0\u03b7\u03bb\u03b7\u03b9\u03b1\u03b4\u03b5\u03c9 "
                    + "\u0391\u03c7\u03b9\u03bb\u03b7\u03bf\u03c2";
            FontSelector sel = new FontSelector();
            sel.addFont(new Font(Font.TIMES_ROMAN, 12));
            sel.addFont(new Font(Font.ZAPFDINGBATS, 12));
            sel.addFont(new Font(Font.SYMBOL, 12));
            Phrase ph = sel.process(text);
            document.add(new Paragraph(ph));
            // step 5
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}
