/*
 * $Id: FormList.java 4104 2009-11-23 18:28:22Z mstorer $
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
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.TextField;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Generates an Acroform with a List
 *
 * @author blowagie
 */
public class FormList {

    /**
     * Generates an Acroform with a list
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("List");

        // step 1: creation of a document-object
        Document document = new Document(PageSize.A4);

        try {

            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("d:\\listExamples\\listboxes.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            TextField fldDef = new TextField(writer, new Rectangle(100, 700, 180, 760), "AList");
            String[] options = {"Red", "Green", "Blue", "White", "Cyan", "Yellow", "Magenta", "Black"};
            fldDef.setChoices(options);

            // you must turn on multiselect before making multiple selections or they will be discarded.
            fldDef.setOptions(TextField.MULTISELECT);

            java.util.List<Integer> selections = new ArrayList<>(3);
            selections.add(1);
            selections.add(3);
            selections.add(5);
            fldDef.setChoiceSelections(selections); // index into chioces

            PdfFormField field = fldDef.getListField();
            writer.addAnnotation(field);


        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();

    }
}
