/*
 * $Id: Destinations.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Creates a document with some goto actions.
 *
 * @author blowagie
 */

public class Destinations {

    /**
     * Creates a document with some goto actions.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Destinations");

        // step 1: creation of a document-object
        Document document = new Document();
        try {

            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("Destinations.pdf"));
            // step 3:       
            writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
            document.open();
            // step 4: we grab the ContentByte and do some stuff with it
            PdfContentByte cb = writer.getDirectContent();

            // we create a PdfTemplate
            PdfTemplate template = cb.createTemplate(25, 25);

            // we add some crosses to visualize the destinations
            template.moveTo(13, 0);
            template.lineTo(13, 25);
            template.moveTo(0, 13);
            template.lineTo(50, 13);
            template.stroke();

            // we add the template on different positions
            cb.addTemplate(template, 287, 787);
            cb.addTemplate(template, 187, 487);
            cb.addTemplate(template, 487, 287);
            cb.addTemplate(template, 87, 87);

            // we define the destinations
            PdfDestination d1 = new PdfDestination(PdfDestination.XYZ, 300, 800, 0);
            PdfDestination d2 = new PdfDestination(PdfDestination.FITH, 500);
            PdfDestination d3 = new PdfDestination(PdfDestination.FITR, 200, 300, 400, 500);
            PdfDestination d4 = new PdfDestination(PdfDestination.FITBV, 100);
            PdfDestination d5 = new PdfDestination(PdfDestination.FIT);

            // we define the outlines
            PdfOutline out1 = new PdfOutline(cb.getRootOutline(), d1, "root");
            PdfOutline out2 = new PdfOutline(out1, d2, "sub 1");
            new PdfOutline(out1, d3, "sub 2");
            new PdfOutline(out2, d4, "sub 2.1");
            new PdfOutline(out2, d5, "sub 2.2");
        } catch (Exception de) {
            de.printStackTrace();
        }

        // step 5: we close the document
        document.close();
    }
}