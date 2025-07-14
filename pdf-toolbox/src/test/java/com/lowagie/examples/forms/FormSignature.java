/*
 * $Id: FormSignature.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.forms;


import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfAcroForm;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Generates an Acroform with a Signature
 *
 * @author blowagie
 */
public class FormSignature {

    /**
     * Generates an Acroform with a Signature
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Signature");

        // step 1: creation of a document-object
        Document document = new Document(PageSize.A4);

        try {

            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("signature.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            PdfAcroForm acroForm = writer.getAcroForm();
            document.add(new Paragraph("Hello World"));
            acroForm.addSignature("mysig", 73, 705, 149, 759);

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}