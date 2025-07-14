/*
 * $Id: Logo.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent.text;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Draws the iText logo.
 */
public class Logo {

    /**
     * Draws the iText logo.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("iText logo");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("logo.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate template = cb.createTemplate(500, 200);
            template.setLineWidth(2f);
            template.rectangle(2.5f, 2.5f, 495f, 195f);
            template.stroke();
            template.setLineWidth(12f);
            template.arc(40f - (float) Math.sqrt(12800), 120f + (float) Math.sqrt(12800),
                    200f - (float) Math.sqrt(12800), -40f + (float) Math.sqrt(12800), 281.25f, 33.75f);
            template.arc(40f, 120f, 200f, -40f, 90f, 45f);
            template.stroke();
            template.setLineCap(1);
            template.setLineWidth(12f);
            template.arc(80f, 40f, 160f, 120f, 90f, 180f);
            template.arc(115f, 75f, 125f, 85f, 0f, 360f);
            template.stroke();
            template.beginText();
            template.setFontAndSize(bf, 180);
            template.setRGBColorFill(0xFF, 0x00, 0x00);
            template.showTextAligned(PdfContentByte.ALIGN_LEFT, "T", 125f, 35f, 0f);
            template.resetRGBColorFill();
            template.showTextAligned(PdfContentByte.ALIGN_LEFT, "ext", 220f, 35f, 0f);
            template.endText();
            template.sanityCheck();

            cb.addTemplate(template, 0, 1, -1, 0, 500, 200);
            cb.addTemplate(template, .5f, 0, 0, .5f, 100, 400);
            cb.addTemplate(template, 0.25f, 0, 0, 0.25f, 100, 100);
            cb.sanityCheck();
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}