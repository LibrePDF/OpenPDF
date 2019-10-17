/*
 * $Id: OrderedLayers.java 3838 2009-04-07 18:34:15Z mstorer $
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

import com.lowagie.examples.AbstractSample;
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
import com.lowagie.text.pdf.PdfLayerMembership;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfOCProperties;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Demonstrates how to order optional content groups.
 */
public class OrderedLayers  extends AbstractSample {

    @Override
    public String getFileName() {
        return "/ordered_layers";
    }

    public static void main(String[] args) {
        OrderedLayers templates = new OrderedLayers();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("DirectContent :: OptionalContent :: Ordering optional content groups");

        // tag::generation[]
        // step 1
        try (Document document = new Document(PageSize.A4, 50, 50, 50, 50)) {
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path + getFileName() + ".pdf"));
            writer.setPdfVersion(PdfWriter.VERSION_1_5);
            writer.setViewerPreferences(PdfWriter.PageModeUseOC);
            // step 3
            document.open();
            // step 4
            PdfContentByte cb = writer.getDirectContent();
            Phrase explanation = new Phrase("Ordered layers", new Font(
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
            cb.sanityCheck();

            PdfOCProperties p = writer.getOCProperties();
            PdfArray order = new PdfArray();
            order.add(l1.getRef());
            order.add(l2.getRef());
            order.add(l3.getRef());
            PdfDictionary d = new PdfDictionary();
            d.put(PdfName.ORDER, order);
            p.put(PdfName.D, d);
        } catch (Exception de) {
            de.printStackTrace();
        }
        // end::generation[]
    }
}