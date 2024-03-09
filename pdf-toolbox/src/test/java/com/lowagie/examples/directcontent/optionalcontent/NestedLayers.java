/*
 * $Id: NestedLayers.java 3838 2009-04-07 18:34:15Z mstorer $
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
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfLayer;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfOCProperties;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Demonstrates the use of nested layers.
 */
public class NestedLayers {

    /**
     * Demonstrates the use of nested layers
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("nested layers");
        try {
            // step 1
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("nestedlayers.pdf"));
            writer.setPdfVersion(PdfWriter.VERSION_1_5);
            writer.setViewerPreferences(PdfWriter.PageModeUseOC);
            // step 3
            document.open();
            // step 4
            PdfContentByte cb = writer.getDirectContent();
            Phrase explanation = new Phrase("Layer nesting", new Font(Font.HELVETICA, 20, Font.BOLD, Color.red));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, explanation, 50, 650, 0);
            PdfLayer l1 = new PdfLayer("Layer 1", writer);
            PdfLayer l23 = new PdfLayer("Top Layer 2 3", writer);
            PdfLayer l2 = new PdfLayer("Layer 2", writer);
            PdfLayer l3 = new PdfLayer("Layer 3", writer);
            Phrase p1 = new Phrase("Text in layer 1");
            Phrase p2 = new Phrase("Text in layer 2");
            Phrase p3 = new Phrase("Text in layer 3");
            cb.beginLayer(l1);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p1, 50, 600, 0);
            cb.endLayer();
            cb.beginLayer(l23);
            cb.beginLayer(l2);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p2, 50, 550, 0);
            cb.endLayer();
            cb.beginLayer(l3);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p3, 50, 500, 0);
            cb.endLayer();
            cb.endLayer();
            cb.sanityCheck();

            PdfOCProperties p = writer.getOCProperties();
            PdfArray order = new PdfArray();
            order.add(l1.getRef());
            order.add(l23.getRef());
            PdfArray group = new PdfArray();
            group.add(l2.getRef());
            group.add(l3.getRef());
            order.add(group);
            PdfDictionary d = new PdfDictionary();
            d.put(PdfName.ORDER, order);
            p.put(PdfName.D, d);
            // step 5
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }


}
