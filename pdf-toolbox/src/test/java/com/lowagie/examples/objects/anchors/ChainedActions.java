/*
 * $Id: ChainedActions.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Creates a documents with chained actions.
 *
 * @author blowagie
 */

public class ChainedActions {

    /**
     * Creates a document with chained Actions.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Chained actions");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(
                    "ChainedActions.pdf"));
            // step 3: we add Javascript as Metadata and we open the document        
            document.open();
            // step 4: we add some content
            PdfAction action = PdfAction.javaScript("app.alert('Welcome at my site');\r", writer);
            action.next(new PdfAction("https://github.com/LibrePDF/OpenPDF"));
            Paragraph p = new Paragraph(new Chunk("Click to go to Bruno's site")
                    .setAction(action));
            document.add(p);
        } catch (Exception de) {
            de.printStackTrace();
        }

        // step 5: we close the document
        document.close();

    }
}