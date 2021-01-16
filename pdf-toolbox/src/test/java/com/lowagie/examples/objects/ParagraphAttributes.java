/*
 * $Id: ParagraphAttributes.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import org.librepdf.openpdf.examples.content.Constants;

/**
 * Demonstrates some Paragraph functionality.
 *
 * @author blowagie
 */

public class ParagraphAttributes {

    /**
     * Demonstrates some Paragraph functionality.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("the Paragraph object (2)");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter.getInstance(document, new FileOutputStream("ParagraphAttributes.pdf"));

            // step 3: we open the document
            document.open();
            // step 4:
            Paragraph[] p = new Paragraph[5];
            p[0] = new Paragraph(Constants.GALLIA_EST);
            p[1] = new Paragraph(Constants.EORUM_UNA);
            p[2] = new Paragraph(Constants.APUD_HELVETIOS);
            p[3] = new Paragraph(Constants.HIS_REBUS);
            p[4] = new Paragraph(Constants.EA_RES);
            for (int i = 0; i < 5; i++) {
                p[i].setAlignment(Element.ALIGN_JUSTIFIED);
                document.add(p[i]);
            }
            document.newPage();
            for (int i = 0; i < 5; i++) {
                p[i].setAlignment(Element.ALIGN_JUSTIFIED);
                p[i].setIndentationLeft(i * 15f);
                p[i].setIndentationRight((5 - i) * 15f);
                document.add(p[i]);
            }
            document.newPage();
            for (int i = 0; i < 5; i++) {
                p[i].setAlignment(Element.ALIGN_RIGHT);
                p[i].setSpacingAfter(15f);
                document.add(p[i]);
            }
            for (int i = 0; i < 5; i++) {
                p[i].setAlignment(Element.ALIGN_LEFT);
                p[i].setSpacingBefore(15f);
                document.add(p[i]);
            }
            for (int i = 0; i < 5; i++) {
                p[i].setAlignment(Element.ALIGN_CENTER);
                p[i].setSpacingAfter(15f);
                p[i].setSpacingBefore(15f);
                document.add(p[i]);
            }
            document.newPage();
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}