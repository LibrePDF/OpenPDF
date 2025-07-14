/*
 * $Id: DefaultPageSize.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates the use of PageSize.
 *
 * @author blowagie
 */
public class DefaultPageSize {

    /**
     * Creates a PDF document with a certain pagesize
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("The default PageSize and some other standard sizes");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file

            PdfWriter.getInstance(document, new FileOutputStream("DefaultPageSize.pdf"));

            // step 3: we open the document
            document.open();

            // step 4: we add some paragraphs to the document
            document.add(new Paragraph("The default PageSize is DIN A4."));
            document.setPageSize(PageSize.A3);
            document.newPage();
            document.add(new Paragraph("This PageSize is DIN A3."));
            document.setPageSize(PageSize.A2);
            document.newPage();
            document.add(new Paragraph("This PageSize is DIN A2."));
            document.setPageSize(PageSize.A1);
            document.newPage();
            document.add(new Paragraph("This PageSize is DIN A1."));
            document.setPageSize(PageSize.A0);
            document.newPage();
            document.add(new Paragraph("This PageSize is DIN A0."));
            document.setPageSize(PageSize.A5);
            document.newPage();
            document.add(new Paragraph("This PageSize is DIN A5."));
            document.setPageSize(PageSize.A6);
            document.newPage();
            document.add(new Paragraph("This PageSize is DIN A6."));
            document.setPageSize(PageSize.A7);
            document.newPage();
            document.add(new Paragraph("This PageSize is DIN A7."));
            document.setPageSize(PageSize.A8);
            document.newPage();
            document.add(new Paragraph("This PageSize is DIN A8."));
            document.setPageSize(PageSize.LETTER);
            document.newPage();
            document.add(new Paragraph("This PageSize is LETTER."));
            document.add(new Paragraph("A lot of other standard PageSizes are available."));

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}
