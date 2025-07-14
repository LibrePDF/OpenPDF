/*
 * $Id: Automatic.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent.optionalcontent;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfLayer;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Automatic layer grouping and nesting
 */
public class Automatic {

    /**
     * Automatic grouping and nesting.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Automatic grouping and nesting");
        try {
            // step 1
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("automatic.pdf"));
            writer.setPdfVersion(PdfWriter.VERSION_1_5);
            writer.setViewerPreferences(PdfWriter.PageModeUseOC);
            // step 3
            document.open();
            // step 4
            PdfContentByte cb = writer.getDirectContent();
            Phrase explanation = new Phrase("Automatic layer grouping and nesting",
                    new Font(Font.HELVETICA, 20, Font.BOLD, Color.red));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, explanation, 50, 650, 0);
            PdfLayer l12 = new PdfLayer("Layer nesting", writer);
            PdfLayer l1 = new PdfLayer("Layer 1", writer);
            PdfLayer l2 = new PdfLayer("Layer 2", writer);
            PdfLayer title = PdfLayer.createTitle("Layer grouping", writer);
            PdfLayer l3 = new PdfLayer("Layer 3", writer);
            PdfLayer l4 = new PdfLayer("Layer 4", writer);
            l12.addChild(l1);
            l12.addChild(l2);
            title.addChild(l3);
            title.addChild(l4);
            Phrase p1 = new Phrase("Text in layer 1");
            Phrase p2 = new Phrase("Text in layer 2");
            Phrase p3 = new Phrase("Text in layer 3");
            Phrase p4 = new Phrase("Text in layer 4");
            cb.beginLayer(l1);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p1, 50, 600, 0);
            cb.endLayer();
            cb.beginLayer(l2);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p2, 50, 550, 0);
            cb.endLayer();
            cb.beginLayer(l3);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p3, 50, 500, 0);
            cb.endLayer();
            cb.beginLayer(l4);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p4, 50, 450, 0);
            cb.endLayer();
            cb.sanityCheck();

            // step 5
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }


}
