/*
 * $Id: FormCombo.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Generates an Acroform with a Combobox
 *
 * @author blowagie
 */
public class FormCombo {

    /**
     * Generates an Acroform with a Combobox
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Combo");

        // step 1: creation of a document-object
        Document document = new Document(PageSize.A4);

        try {

            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("combo.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            PdfContentByte cb = writer.getDirectContent();
            cb.moveTo(0, 0);
            String[] options = {"Red", "Green", "Blue"};
            PdfFormField field = PdfFormField.createCombo(writer, true, options, 0);
            field.setWidget(new Rectangle(100, 700, 180, 720), PdfAnnotation.HIGHLIGHT_INVERT);
            field.setFieldName("ACombo");
            field.setValueAsString("Red");
            writer.addAnnotation(field);

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}