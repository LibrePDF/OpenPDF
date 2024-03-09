/*
 * $Id: Annotations.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects.anchors;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfFileSpecification;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Creates a document with some PdfAnnotations.
 *
 * @author blowagie
 */

public class Annotations {

    /**
     * Creates a document with some PdfAnnotations.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("PdfAnnotations");

        // step 1: creation of a document-object
        Document document = new Document();
        try {

            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("Annotations.pdf"));
            // step 3:
            writer.setPdfVersion(PdfWriter.VERSION_1_5);
            document.open();
            // step 4:
            PdfContentByte cb = writer.getDirectContent();
            // page 1
            PdfFileSpecification fs = PdfFileSpecification.fileExtern(writer, "cards.mpg");
            writer.addAnnotation(
                    PdfAnnotation.createScreen(writer, new Rectangle(200f, 700f, 300f, 800f), "cards.mpg", fs,
                            "video/mpeg", true));
            PdfAnnotation a = new PdfAnnotation(writer, 200f, 550f, 300f, 650f,
                    PdfAction.javaScript("app.alert('Hello');\r", writer));
            document.add(new Chunk("click to trigger javascript").setAnnotation(a).setLocalDestination("top"));
            writer.addAnnotation(a);
            writer.addAnnotation(PdfAnnotation.createFileAttachment(writer, new Rectangle(100f, 650f, 150f, 700f),
                    "This is some text", "some text".getBytes(), null, "some.txt"));
            writer.addAnnotation(PdfAnnotation.createText(writer, new Rectangle(200f, 400f, 300f, 500f), "Help",
                    "This Help annotation was made with 'createText'", false, "Help"));
            writer.addAnnotation(PdfAnnotation.createText(writer, new Rectangle(200f, 250f, 300f, 350f), "Help",
                    "This Comment annotation was made with 'createText'", true, "Comment"));
            cb.rectangle(200, 700, 100, 100);
            cb.rectangle(200, 550, 100, 100);
            cb.rectangle(200, 400, 100, 100);
            cb.rectangle(200, 250, 100, 100);
            cb.stroke();
            document.newPage();
            // page 2
            writer.addAnnotation(PdfAnnotation.createLink(writer, new Rectangle(200f, 700f, 300f, 800f),
                    PdfAnnotation.HIGHLIGHT_TOGGLE, PdfAction.javaScript("app.alert('Hello');\r", writer)));
            writer.addAnnotation(PdfAnnotation.createLink(writer, new Rectangle(200f, 550f, 300f, 650f),
                    PdfAnnotation.HIGHLIGHT_OUTLINE, "top"));
            writer.addAnnotation(PdfAnnotation.createLink(writer, new Rectangle(200f, 400f, 300f, 500f),
                    PdfAnnotation.HIGHLIGHT_PUSH, 1, new PdfDestination(PdfDestination.FIT)));
            writer.addAnnotation(PdfAnnotation.createSquareCircle(writer, new Rectangle(200f, 250f, 300f, 350f),
                    "This Comment annotation was made with 'createSquareCircle'", false));
            document.newPage();
            // page 3
            PdfContentByte pcb = new PdfContentByte(writer);
            pcb.setColorFill(new Color(0xFF, 0x00, 0x00));
            writer.addAnnotation(PdfAnnotation.createFreeText(writer, new Rectangle(200f, 700f, 300f, 800f),
                    "This is some free text, blah blah blah", pcb));
            writer.addAnnotation(
                    PdfAnnotation.createLine(writer, new Rectangle(200f, 550f, 300f, 650f), "this is a line", 200, 550,
                            300, 650));
            writer.addAnnotation(
                    PdfAnnotation.createStamp(writer, new Rectangle(200f, 400f, 300f, 500f), "This is a stamp",
                            "Stamp"));
            writer.addAnnotation(
                    PdfAnnotation.createPopup(writer, new Rectangle(200f, 250f, 300f, 350f), "Hello, I'm a popup!",
                            true));
            cb.rectangle(200, 700, 100, 100);
            cb.rectangle(200, 550, 100, 100);
            cb.rectangle(200, 250, 100, 100);
            cb.stroke();

        } catch (Exception de) {
            de.printStackTrace();
        }

        // step 5: we close the document
        document.close();
    }
}