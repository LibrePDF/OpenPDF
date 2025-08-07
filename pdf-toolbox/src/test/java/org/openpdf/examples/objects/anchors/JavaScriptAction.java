/*
 * $Id: JavaScriptAction.java 3373 2008-05-12 16:21:24Z xlv $
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

package org.openpdf.examples.objects.anchors;

import java.io.FileOutputStream;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfAction;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Creates a documents with a javascript action.
 *
 * @author blowagie
 */

public class JavaScriptAction {

    /**
     * Creates a document with a javascript action.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("JavaScript");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(
                    "JavaScriptAction.pdf"));
            // step 3: we add Javascript as Metadata and we open the document        
            document.open();
            // step 4: we add some content
            Paragraph p = new Paragraph(new Chunk("Click to say Hello")
                    .setAction(PdfAction.javaScript("app.alert('Hello');\r", writer)));
            document.add(p);
        } catch (Exception de) {
            de.printStackTrace();
        }

        // step 5: we close the document
        document.close();

    }
}