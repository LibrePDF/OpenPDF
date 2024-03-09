/*
 * $Id: NewPage.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.general.faq;


import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates the use of newPage.
 *
 * @author blowagie
 */
public class NewPage {

    /**
     * Creates a PDF document with different pages.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Using newPage()");
        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("NewPage.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            document.add(new Paragraph("This is the first page."));
            document.newPage();
            document.add(new Paragraph("This is a new page"));
            document.newPage();
            document.newPage();
            document.add(new Paragraph(
                    "We invoked new page twice, yet there was no blank page added. Between the second page and this one. This is normal behaviour."));
            document.newPage();
            writer.setPageEmpty(false);
            document.newPage();
            document.add(new Paragraph("We told the writer the page wasn't empty."));
            document.newPage();
            document.add(Chunk.NEWLINE);
            document.newPage();
            document.add(new Paragraph("You can also add something invisible if you want a blank page."));
            document.add(Chunk.NEXTPAGE);
            document.add(new Paragraph("Using Chunk.NEXTPAGE also jumps to the next page"));
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}