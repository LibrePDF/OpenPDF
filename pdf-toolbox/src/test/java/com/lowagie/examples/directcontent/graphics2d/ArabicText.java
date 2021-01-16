/*
 * $Id: ArabicText.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent.graphics2d;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Font;
import java.io.FileOutputStream;

/**
 * Draws arabic text using java.awt.Graphics2D
 */
public class ArabicText {

    /**
     * Draws arabic text using java.awt.Graphics2D.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Arabic Text.");
        // step 1
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("arabictext.pdf"));
            // step 3
            document.open();
            // step 4
            String text1 = "This text has \u0634\u0627\u062f\u062c\u0645\u0647\u0648\u0631 123,456 \u0645\u0646 (Arabic)";
            java.awt.Font font = new java.awt.Font("arial", Font.PLAIN, 18);
            PdfContentByte cb = writer.getDirectContent();
            java.awt.Graphics2D g2 = cb.createGraphicsShapes(PageSize.A4.getWidth(), PageSize.A4.getHeight());
            g2.setFont(font);
            g2.drawString(text1, 100, 100);
            g2.dispose();
            cb.sanityCheck();
            // step 5
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }

}
