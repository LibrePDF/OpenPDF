/*
 * $Id: Margins.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.general;


import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates the use of margins.
 *
 * @author blowagie
 */
public class Margins {

    /**
     * Creates a PDF document with different pages that have different margins.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Document margins");
        // step 1: creation of a document-object
        Document document = new Document(PageSize.A5, 36, 72, 108, 180);

        try {

            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file

            PdfWriter.getInstance(document, new FileOutputStream("Margins.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            document.add(new Paragraph(
                    "The left margin of this document is 36pt (0.5 inch); the right margin 72pt (1 inch); the top margin 108pt (1.5 inch); the bottom margin 180pt (2.5 inch). "));
            Paragraph paragraph = new Paragraph();
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            for (int i = 0; i < 20; i++) {
                paragraph.add("Hello World, Hello Sun, Hello Moon, Hello Stars, Hello Sea, Hello Land, Hello People. ");
            }
            document.add(paragraph);
            document.setMargins(180, 108, 72, 36);
            document.add(new Paragraph("Now we change the margins. You will see the effect on the next page."));
            document.add(paragraph);
            document.setMarginMirroring(true);
            document.add(new Paragraph("Starting on the next page, the margins will be mirrored."));
            document.add(paragraph);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}
