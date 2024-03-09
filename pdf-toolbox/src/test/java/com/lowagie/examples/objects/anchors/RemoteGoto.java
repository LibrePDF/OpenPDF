/*
 * $Id: RemoteGoto.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * Creates 2 documents with links to eachother.
 *
 * @author blowagie
 */

public class RemoteGoto {

    /**
     * Creates documents with Remote Goto functionality.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Remote goto (URLs and local destinations in another document)");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2:
            PdfWriter writerA = PdfWriter.getInstance(document, new FileOutputStream("DocumentA.pdf"));
            PdfWriter writerB = PdfWriter.getInstance(document, new FileOutputStream("DocumentB.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:

            // we create some content

            // a paragraph with a link to an external url
            Paragraph p1 = new Paragraph("You can turn a Chunk into an ",
                    FontFactory.getFont(FontFactory.HELVETICA, 12));
            p1.add(new Chunk("anchor",
                    FontFactory.getFont(FontFactory.HELVETICA, 12, Font.UNDERLINE, new Color(0, 0, 255))).setAnchor(
                    new URL("https://github.com/LibrePDF/OpenPDF")));
            p1.add(", for instance to the iText site.");

            // some paragraph
            Paragraph p2 = new Paragraph("blah, blah, blah");

            // two paragraphs with a local destination
            Paragraph p3a = new Paragraph("This paragraph contains a ");
            p3a.add(new Chunk("local destination in document A",
                    FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL,
                            new Color(0, 255, 0))).setLocalDestination("test"));
            Paragraph p3b = new Paragraph("This paragraph contains a ");
            p3b.add(new Chunk("local destination in document B",
                    FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL,
                            new Color(0, 255, 0))).setLocalDestination("test"));

            // two paragraphs with a remote goto
            Paragraph p4a = new Paragraph(
                    new Chunk("Click this paragraph to go to a certain destination on document B").setRemoteGoto(
                            "DocumentB.pdf", "test"));
            Paragraph p4b = new Paragraph(
                    new Chunk("Click this paragraph to go to a certain destination on document A").setRemoteGoto(
                            "DocumentA.pdf", "test"));

            // a special remote goto
            Paragraph p5a = new Paragraph("you can also jump to a ");
            p5a.add(new Chunk("specific page on another document",
                    FontFactory.getFont(FontFactory.HELVETICA, 12, Font.ITALIC)).setRemoteGoto("DocumentB.pdf", 3));

            // we add all the content
            document.add(p1);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            // only for DocumentB.pdf:
            writerA.pause();
            document.add(p4b);
            writerA.resume();
            // only for DocumentA.pdf:
            writerB.pause();
            document.add(p4a);
            document.add(p5a);
            writerB.resume();
            // for both documents:
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            // only for DocumentB.pdf:
            writerA.pause();
            document.add(p3b);
            document.add(p2);
            document.add(p2);
            document.newPage();
            document.add(p2);
            document.add(p2);
            document.newPage();
            writerA.resume();
            // only for documentA.pdf
            writerB.pause();
            document.add(p3a);
            writerB.resume();
            // for both documents
            document.add(p2);
            document.add(p2);
        } catch (Exception ioe) {
            System.err.println(ioe.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}