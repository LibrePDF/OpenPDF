/*
 * $Id: Lists.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Demonstrates some List functionality.
 *
 * @author blowagie
 */

public class LongList {

    /**
     * Demonstrates some List functionality.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("the Long List example");
        // step 1: creation of a document-object
        Document document = new Document();
        // step 2:
        PdfWriter.getInstance(document, new FileOutputStream("longList.pdf"));
        // step 3: we open the document
        document.open();
        // step 4:
        List list = new List(true);
        for (int i = 0; i < 30; i++) {
            list.add(new ListItem("This is the line with the number " + (i + 1)
                    + ". Don't worry if the line is very long, but we need to a line break."));
        }
        document.add(list);
        // step 5: we close the document
        document.close();
    }
}
