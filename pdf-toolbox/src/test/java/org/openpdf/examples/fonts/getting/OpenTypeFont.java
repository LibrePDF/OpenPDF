/*
 * $Id: OpenTypeFont.java 3373 2008-05-12 16:21:24Z xlv $
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
package org.openpdf.examples.fonts.getting;

import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Using an Open Type Font (CFF only).
 */
public class OpenTypeFont {

    /**
     * Using oth
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        // step 1
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            // step 2
            PdfWriter.getInstance(document, new FileOutputStream("opentypefont.pdf"));
            // step 3
            document.open();
            // step 4
            BaseFont bf = BaseFont.createFont("liz.otf", BaseFont.CP1252, true);
            String text = "Some text with the otf font LIZ.";
            document.add(new Paragraph(text, new Font(bf, 14)));
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step 5
        document.close();
    }
}
