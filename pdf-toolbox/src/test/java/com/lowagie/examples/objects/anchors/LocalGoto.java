/*
 * $Id: LocalGoto.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Creates a document with a Local Goto and a Local Destination.
 *
 * @author blowagie
 */

public class LocalGoto {

    /**
     * Creates a document with a Local Goto and a Local Destination.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {
        System.out.println("local goto");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2:
            PdfWriter.getInstance(document, new FileOutputStream("LocalGoto.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:

            // we make some content

            // a paragraph with a local goto
            Paragraph p1 = new Paragraph("We will do something special with this paragraph. If you click on ",
                    FontFactory.getFont(FontFactory.HELVETICA, 12));
            p1.add(new Chunk("this word",
                    FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 255))).setLocalGoto(
                    "test"));
            p1.add(" you will automatically jump to another location in this document.");

            // some paragraph
            Paragraph p2 = new Paragraph("blah, blah, blah");

            // a paragraph with a local destination
            Paragraph p3 = new Paragraph("This paragraph contains a ");
            p3.add(new Chunk("local destination", FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL,
                    new Color(0, 255, 0))).setLocalDestination("test"));

            // we add the content
            document.add(p1);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p3);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}