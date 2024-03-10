/*
 * $Id: ComplexText.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Adding text at an absolute position.
 */
public class ComplexText {

    /**
     * Adding text at absolute positions.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Text at absolute positions");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("complextext.pdf"));

            // step 3: we open the document
            document.open();

            // step 4: we grab the ContentByte and do some stuff with it
            PdfContentByte cb = writer.getDirectContent();

            // first we draw some lines to be able to visualize the text alignment functions
            cb.setLineWidth(0f);
            cb.moveTo(250, 500);
            cb.lineTo(250, 800);
            cb.moveTo(50, 700);
            cb.lineTo(400, 700);
            cb.moveTo(50, 650);
            cb.lineTo(400, 650);
            cb.moveTo(50, 600);
            cb.lineTo(400, 600);
            cb.stroke();

            // we construct a font
            BaseFont bf = BaseFont.createFont("c:\\windows\\fonts\\arialuni.ttf", BaseFont.IDENTITY_H, true);
            Font ft = new Font(bf, 12);
            // This is the text:
            String text = "\u0623\u0648\u0631\u0648\u0628\u0627, \u0628\u0631\u0645\u062c\u064a\u0627\u062a"
                    + " \u0627\u0644\u062d\u0627\u0633\u0648\u0628 + \u0627\u0646\u062a\u0631\u0646\u064a\u062a :";
            Phrase center = new Phrase(text + " Center", ft);
            ColumnText.showTextAligned(cb, PdfContentByte.ALIGN_CENTER, center, 250, 700, 0,
                    PdfWriter.RUN_DIRECTION_RTL, 0);
            ColumnText.showTextAligned(cb, PdfContentByte.ALIGN_RIGHT, new Phrase(text + " Right", ft), 250, 650, 20,
                    PdfWriter.RUN_DIRECTION_RTL, 0);
            ColumnText.showTextAligned(cb, PdfContentByte.ALIGN_LEFT, new Phrase("Some text Left aligned", ft), 250,
                    600, 20);
            float size = ColumnText.getWidth(center, PdfWriter.RUN_DIRECTION_RTL, 0);
            cb.setRGBColorStroke(255, 0, 0);
            cb.rectangle(250 - size / 2, 690, size, 30);
            cb.stroke();
            writer.setRunDirection(PdfWriter.RUN_DIRECTION_DEFAULT);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}
