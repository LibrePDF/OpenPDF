/*
 * $Id: FixedFontWidth.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Changing the width of font glyphs.
 */
public class FixedFontWidth {

    /**
     * Changing the width of font glyphs.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Fixed Font Width");
        // step 1
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            // step 2
            PdfWriter.getInstance(document, new FileOutputStream("fixedfontwidth.pdf"));
            // step 3
            document.open();
            // step 4
            BaseFont bf = BaseFont.createFont("Helvetica", "winansi", false, false, null, null);
            int[] widths = bf.getWidths();
            for (int k = 0; k < widths.length; ++k) {
                if (widths[k] != 0) {
                    widths[k] = 1000;
                }
            }
            bf.setForceWidthsOutput(true);
            document.add(new Paragraph("A big text to show Helvetica with fixed width.", new Font(bf)));
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step 5
        document.close();
    }
}
