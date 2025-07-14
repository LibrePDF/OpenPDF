/*
 * $Id: Groups.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent.colors;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfTransparencyGroup;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Demonstrates transparency and images.
 */
public class Groups {

    /**
     * Prints a square and fills half of it with a gray rectangle.
     *
     * @param x  the x-coordinate of the rectangle
     * @param y  the y-coordinate of the rectangle
     * @param cb the PdfContentByte
     */
    public static void pictureBackdrop(float x, float y, PdfContentByte cb) {
        cb.setColorStroke(Color.black);
        cb.setColorFill(Color.red);
        cb.rectangle(x, y, 100, 200);
        cb.fill();
        cb.setLineWidth(2);
        cb.rectangle(x, y, 200, 200);
        cb.stroke();
    }

    /**
     * Prints 3 circles in different colors that intersect with eachother.
     *
     * @param x  the x-coordinate of the intersection
     * @param y  the y-coordinate of the intersection
     * @param cb the PdfContentByte
     */
    public static void pictureCircles(float x, float y, PdfContentByte cb) {
        PdfGState gs = new PdfGState();
        gs.setBlendMode(PdfGState.BM_SOFTLIGHT);
        gs.setFillOpacity(0.7f);
        cb.setGState(gs);
        cb.setColorFill(Color.gray);
        cb.circle(x + 70, y + 70, 50);
        cb.fill();
        cb.circle(x + 100, y + 130, 50);
        cb.fill();
        cb.circle(x + 130, y + 70, 50);
        cb.fill();
    }

    /**
     * Demonstrates the Transparency functionality.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Transparency Groups");
        // step 1: creation of a document-object
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            // step 2: creation of a writer 
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("groups.pdf"));
            // step 3: we open the document
            document.open();
            // step 4: content
            PdfContentByte cb = writer.getDirectContent();
            float gap = (document.getPageSize().getWidth() - 400) / 3;

            pictureBackdrop(gap, 500, cb);
            pictureBackdrop(200 + 2 * gap, 500, cb);
            pictureBackdrop(gap, 500 - 200 - gap, cb);
            pictureBackdrop(200 + 2 * gap, 500 - 200 - gap, cb);

            PdfTemplate tp;
            PdfTransparencyGroup group;

            tp = cb.createTemplate(200, 200);
            pictureCircles(0, 0, tp);
            group = new PdfTransparencyGroup();
            group.setIsolated(true);
            group.setKnockout(true);
            tp.setGroup(group);
            tp.sanityCheck();
            cb.addTemplate(tp, gap, 500);

            tp = cb.createTemplate(200, 200);
            pictureCircles(0, 0, tp);
            group = new PdfTransparencyGroup();
            group.setIsolated(true);
            group.setKnockout(false);
            tp.setGroup(group);
            tp.sanityCheck();
            cb.addTemplate(tp, 200 + 2 * gap, 500);

            tp = cb.createTemplate(200, 200);
            pictureCircles(0, 0, tp);
            group = new PdfTransparencyGroup();
            group.setIsolated(false);
            group.setKnockout(true);
            tp.setGroup(group);
            tp.sanityCheck();
            cb.addTemplate(tp, gap, 500 - 200 - gap);

            tp = cb.createTemplate(200, 200);
            pictureCircles(0, 0, tp);
            group = new PdfTransparencyGroup();
            group.setIsolated(false);
            group.setKnockout(false);
            tp.setGroup(group);
            tp.sanityCheck();
            cb.addTemplate(tp, 200 + 2 * gap, 500 - 200 - gap);

            cb.sanityCheck();
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step 5: we close the document
        document.close();
    }
}
