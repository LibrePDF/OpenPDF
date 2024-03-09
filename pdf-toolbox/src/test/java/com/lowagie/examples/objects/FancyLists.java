/*
 * $Id: FancyLists.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.GreekList;
import com.lowagie.text.ListItem;
import com.lowagie.text.RomanList;
import com.lowagie.text.ZapfDingbatsList;
import com.lowagie.text.ZapfDingbatsNumberList;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates some List functionality.
 *
 * @author blowagie
 */

public class FancyLists {

    public static final String BLAH_BLAH = "second item blah blah blah blah blah blah blah blah blah blah blah blah "
            + "blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah "
            + "blah blah blah blah blah blah blah blah blah blah blah blah blah";

    /**
     * Demonstrates some List functionality.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Some Fancy Lists");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            PdfWriter.getInstance(document, new FileOutputStream("fancylists.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:

            // RomanList
            RomanList roman = new RomanList(35);
            roman.setLowercase(true);
            roman.add(new ListItem("first item"));
            roman.add(new ListItem(BLAH_BLAH));
            for (int i = 3; i < 151; i++) {
                roman.add(i + "th item");
            }
            document.add(roman);
            document.newPage();
            RomanList roman2 = new RomanList(35);
            roman2.add(new ListItem("first item"));
            roman2.add(new ListItem("second item"));
            for (int i = 3; i < 11; i++) {
                roman2.add(i + "th item");
            }
            document.add(roman2);
            document.newPage();

            // GreekList
            GreekList greek = new GreekList(15);
            greek.setLowercase(true);
            greek.add(new ListItem("first item"));
            greek.add(new ListItem("second item"));
            for (int i = 3; i < 20; i++) {
                greek.add(i + "th item");
            }
            document.add(greek);
            document.newPage();

            // GreekList
            GreekList greek2 = new GreekList(15);
            greek2.setLowercase(false);
            greek2.add(new ListItem("first item"));
            greek2.add(new ListItem("second item"));
            for (int i = 3; i < 20; i++) {
                greek2.add(i + "th item");
            }
            document.add(greek2);

            // ZapfDingbatsList
            ZapfDingbatsList z = new ZapfDingbatsList(42, 15);
            z.add(new ListItem("first item"));
            z.add(new ListItem("second item"));
            for (int i = 3; i < 20; i++) {
                z.add(i + "th item");
            }
            document.add(z);
            document.newPage();

            // ZapfDingbatsNumberList
            ZapfDingbatsNumberList z0 = new ZapfDingbatsNumberList(0, 15);
            z0.add(new ListItem("first item"));
            z0.add(new ListItem("second item"));
            for (int i = 3; i < 11; i++) {
                z0.add(i + "th item");
            }
            document.add(z0);
            ZapfDingbatsNumberList z1 = new ZapfDingbatsNumberList(1, 15);
            z1.add(new ListItem("first item"));
            z1.add(new ListItem("second item"));
            for (int i = 3; i < 11; i++) {
                z1.add(i + "th item");
            }
            document.add(z1);
            ZapfDingbatsNumberList z2 = new ZapfDingbatsNumberList(2, 15);
            z2.add(new ListItem("first item"));
            z2.add(new ListItem("second item"));
            for (int i = 3; i < 11; i++) {
                z2.add(i + "th item");
            }
            document.add(z2);
            ZapfDingbatsNumberList z3 = new ZapfDingbatsNumberList(3, 15);
            z3.add(new ListItem("first item"));
            z3.add(new ListItem("second item"));
            for (int i = 3; i < 11; i++) {
                z3.add(i + "th item");
            }
            document.add(z3);

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}