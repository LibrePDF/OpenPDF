/*
 * $Id: PdfVersion.java 3373 2008-05-12 16:21:24Z xlv $
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


import com.lowagie.examples.AbstractSample;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates the use of setPdfVersion.
 *
 * @author blowagie
 */
public class PdfVersion extends AbstractSample {

    @Override
    public String getFileName() {
        return "/pdf_version";
    }

    public static void main(String[] args) {
        PdfVersion templates = new PdfVersion();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {

        System.out.println("General :: FAQ :: PDF version");
        // step 1: creation of a document-object
        Document document = new Document();

        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path + getFileName() + ".pdf"));
            writer.setPdfVersion(PdfWriter.VERSION_1_2);
            // step 3: we open the document
            document.open();

            // step 4:
            document.add(new Paragraph("This is a PDF-1.2 document"));
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}