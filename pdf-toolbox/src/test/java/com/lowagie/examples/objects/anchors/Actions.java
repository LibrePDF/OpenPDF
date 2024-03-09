/*
 * $Id: Actions.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Creates a document with some goto actions.
 *
 * @author blowagie
 */

public class Actions {

    /**
     * Creates a document with some goto actions.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Actions");

        // step 1: creation of a document-object
        Document document = new Document();
        Document remote = new Document();
        try {

            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("Actions.pdf"));
            PdfWriter.getInstance(remote, new FileOutputStream("remote.pdf"));
            // step 3:
            document.open();
            remote.open();
            // step 4: we add some content
            PdfAction action = PdfAction.gotoLocalPage(2, new PdfDestination(
                    PdfDestination.XYZ, -1, 10000, 0), writer);
            writer.setOpenAction(action);
            document.add(new Paragraph("Page 1"));
            document.newPage();
            document.add(new Paragraph("Page 2"));
            document.add(new Chunk("goto page 1").setAction(PdfAction
                    .gotoLocalPage(1, new PdfDestination(PdfDestination.FITH,
                            500), writer)));
            document.add(Chunk.NEWLINE);
            document.add(new Chunk("goto another document").setAction(PdfAction
                    .gotoRemotePage("remote.pdf", "test", false, true)));
            remote.add(new Paragraph("Some remote document"));
            remote.newPage();
            Paragraph p = new Paragraph("This paragraph contains a ");
            p.add(new Chunk("local destination").setLocalDestination("test"));
            remote.add(p);
        } catch (Exception de) {
            de.printStackTrace();
        }

        // step 5: we close the document
        document.close();
        remote.close();
    }
}