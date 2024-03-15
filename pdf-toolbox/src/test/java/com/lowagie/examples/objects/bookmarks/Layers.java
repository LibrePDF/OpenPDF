/*
 * $Id: Layers.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects.bookmarks;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfLayer;
import com.lowagie.text.pdf.PdfLayerMembership;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfOCProperties;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates how layers work.
 *
 * @author blowagie
 */

public class Layers {

    /**
     * Demonstrates some Layer functionality.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("layers");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("Layers.pdf"));
            writer.setPdfVersion(PdfWriter.VERSION_1_5);
            // step 3:
            writer.setViewerPreferences(PdfWriter.PageModeUseOC);
            document.open();
            // step 4:
            PdfContentByte cb = writer.getDirectContent();
            Phrase explanation = new Phrase("Layer grouping", new Font(
                    Font.HELVETICA, 20, Font.BOLD, Color.red));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, explanation, 50,
                    650, 0);
            PdfLayer l1 = new PdfLayer("Layer 1", writer);
            PdfLayer l2 = new PdfLayer("Layer 2", writer);
            PdfLayer l3 = new PdfLayer("Layer 3", writer);
            PdfLayerMembership m1 = new PdfLayerMembership(writer);
            m1.addMember(l2);
            m1.addMember(l3);
            Phrase p1 = new Phrase("Text in layer 1");
            Phrase p2 = new Phrase("Text in layer 2 or layer 3");
            Phrase p3 = new Phrase("Text in layer 3");
            cb.beginLayer(l1);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p1, 50, 600, 0);
            cb.endLayer();
            cb.beginLayer(m1);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p2, 50, 550, 0);
            cb.endLayer();
            cb.beginLayer(l3);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p3, 50, 500, 0);
            cb.endLayer();
            PdfOCProperties p = writer.getOCProperties();
            PdfArray order = new PdfArray();
            order.add(l1.getRef());
            PdfArray group = new PdfArray();
            group.add(new PdfString("A group of two", PdfObject.TEXT_UNICODE));
            group.add(l2.getRef());
            group.add(l3.getRef());
            order.add(group);
            PdfDictionary d = new PdfDictionary();
            d.put(PdfName.ORDER, order);
            p.put(PdfName.D, d);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}