/*
 * $Id: HelloWorldMeta.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Generates a simple PDF file with metadata.
 *
 * @author blowagie
 */

public class HelloWorldMeta {

    /**
     * Generates a PDF file with metadata
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Metadata");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter.getInstance(document,
                    new FileOutputStream("HelloWorldMeta.pdf"));

            // step 3: we add some metadata open the document
            document.addTitle("Hello World example");
            document.addSubject("This example explains how to add metadata.");
            document.addKeywords("iText, Hello World, step 3, metadata");
            document.addCreator("My program using iText");
            document.addAuthor("Bruno Lowagie");
            document.open();
            // step 4: we add a paragraph to the document
            document.add(new Paragraph("Hello World"));
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}