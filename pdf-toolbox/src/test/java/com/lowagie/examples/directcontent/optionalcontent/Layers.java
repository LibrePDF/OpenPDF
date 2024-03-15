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
import java.util.ArrayList;
import java.util.List;

/**
 * Layer radio group and zoom.
 */
public class Layers {

    /**
     * Layer radio group and zoom.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("layer radio group and zoom");
        try {
            // step 1
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("layers.pdf"));
            writer.setPdfVersion(PdfWriter.VERSION_1_5);
            writer.setViewerPreferences(PdfWriter.PageModeUseOC);
            // step 3
            document.open();
            // step 4
            PdfContentByte cb = writer.getDirectContent();
            Phrase explanation = new Phrase("Layer radio group and zoom",
                    new Font(Font.HELVETICA, 20, Font.BOLD, Color.red));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, explanation, 50, 650, 0);
            PdfLayer title = PdfLayer.createTitle("Layer radio group", writer);
            PdfLayer l1 = new PdfLayer("Layer 1", writer);
            PdfLayer l2 = new PdfLayer("Layer 2", writer);
            PdfLayer l3 = new PdfLayer("Layer 3", writer);
            PdfLayer l4 = new PdfLayer("Layer 4", writer);
            title.addChild(l1);
            title.addChild(l2);
            title.addChild(l3);
            l4.setZoom(2, -1);
            l4.setOnPanel(false);
            l4.setPrint("Print", true);
            l2.setOn(false);
            l3.setOn(false);
            List<PdfLayer> radio = new ArrayList<>();
            radio.add(l1);
            radio.add(l2);
            radio.add(l3);
            writer.addOCGRadioGroup(radio);
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
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("<< Zoom here!", new Font(Font.COURIER, 12, Font.NORMAL, Color.blue)), 150, 450, 0);
            // step 5
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}
