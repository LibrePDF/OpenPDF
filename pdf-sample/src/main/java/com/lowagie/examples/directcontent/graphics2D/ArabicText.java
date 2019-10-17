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
package com.lowagie.examples.directcontent.graphics2D;

import com.lowagie.examples.AbstractSample;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Font;
import java.io.FileOutputStream;

/**
 * Draws arabic text using java.awt.Graphics2D
 */
public class ArabicText  extends AbstractSample {

    @Override
    public String getFileName() {
        return "/arabic_text";
    }

    public static void main(String[] args) {
        ArabicText templates = new ArabicText();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("DirectContent :: Graphics2D :: Arabic Text.");

        // tag::generation[]
        // step 1
        try (Document document = new Document(PageSize.A4, 50, 50, 50, 50)) {
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path + getFileName() + ".pdf"));
            // step 3
            document.open();
            // step 4
            String text1 = "This text has \u0634\u0627\u062f\u062c\u0645\u0647\u0648\u0631 123,456 \u0645\u0646 (Arabic)";
            Font font = new Font("arial", Font.PLAIN, 18);
            PdfContentByte cb = writer.getDirectContent();
            java.awt.Graphics2D g2 = cb.createGraphicsShapes(PageSize.A4.getWidth(), PageSize.A4.getHeight());
            g2.setFont(font);
            g2.drawString(text1, 100, 100);
            g2.dispose();
            cb.sanityCheck();
        } catch (Exception de) {
            de.printStackTrace();
        }
        // end::generation[]
    }
}
