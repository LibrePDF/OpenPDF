/*
 * $Id: OptionalContent.java 3838 2009-04-07 18:34:15Z mstorer $
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

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfBorderDictionary;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfLayer;
import com.lowagie.text.pdf.PdfLayerMembership;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.TextField;
import java.awt.Color;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates the use of layers.
 */
public class OptionalContent {

    /**
     * Demonstrates the use of layers.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        try {
            System.out.println("Optional content");
            // step 1: creation of a document-object
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("optionalcontent.pdf"));
            writer.setPdfVersion(PdfWriter.VERSION_1_5);
            writer.setViewerPreferences(PdfWriter.PageModeUseOC);
            // step 3: opening the document
            document.open();
            // step 4: content
            PdfContentByte cb = writer.getDirectContent();
            Phrase explanation = new Phrase("Automatic layers, form fields, images, templates and actions",
                    new Font(Font.HELVETICA, 18, Font.BOLD, Color.red));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, explanation, 50, 650, 0);
            PdfLayer l1 = new PdfLayer("Layer 1", writer);
            PdfLayer l2 = new PdfLayer("Layer 2", writer);
            PdfLayer l3 = new PdfLayer("Layer 3", writer);
            PdfLayer l4 = new PdfLayer("Form and XObject Layer", writer);
            PdfLayerMembership m1 = new PdfLayerMembership(writer);
            m1.addMember(l2);
            m1.addMember(l3);
            Phrase p1 = new Phrase("Text in layer 1");
            Phrase p2 = new Phrase("Text in layer 2 or layer 3");
            Phrase p3 = new Phrase("Text in layer 3");
            cb.beginLayer(l1);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p1, 50, 600, 0f);
            cb.endLayer();
            cb.beginLayer(m1);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p2, 50, 550, 0);
            cb.endLayer();
            cb.beginLayer(l3);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p3, 50, 500, 0);
            cb.endLayer();
            TextField ff = new TextField(writer, new Rectangle(200, 600, 300, 620), "field1");
            ff.setBorderColor(Color.blue);
            ff.setBorderStyle(PdfBorderDictionary.STYLE_SOLID);
            ff.setBorderWidth(TextField.BORDER_WIDTH_THIN);
            ff.setText("I'm a form field");
            PdfFormField form = ff.getTextField();
            form.setLayer(l4);
            writer.addAnnotation(form);
            Image img = Image.getInstance("pngnow.png");
            img.setLayer(l4);
            img.setAbsolutePosition(200, 550);
            cb.addImage(img);
            PdfTemplate tp = cb.createTemplate(100, 20);
            Phrase pt = new Phrase("I'm a template", new Font(Font.HELVETICA, 12, Font.NORMAL, Color.magenta));
            ColumnText.showTextAligned(tp, Element.ALIGN_LEFT, pt, 0, 0, 0);
            tp.setLayer(l4);
            tp.setBoundingBox(new Rectangle(0, -10, 100, 20));
            cb.addTemplate(tp, 200, 500);
            List<Object> state = new ArrayList<>();
            state.add("toggle");
            state.add(l1);
            state.add(l2);
            state.add(l3);
            state.add(l4);
            PdfAction action = PdfAction.setOCGstate(state, true);
            Chunk ck = new Chunk("Click here to toggle the layers",
                    new Font(Font.HELVETICA, 18, Font.NORMAL, Color.yellow)).setBackground(Color.blue)
                    .setAction(action);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(ck), 250, 400, 0);
            cb.sanityCheck();

            // step 5: closing the document
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}