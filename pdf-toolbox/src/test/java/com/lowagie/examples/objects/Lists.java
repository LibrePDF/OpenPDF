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

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.HtmlWriter;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates some List functionality.
 *
 * @author blowagie
 */

public class Lists {

    /**
     * Demonstrates some List functionality.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("the List object");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            PdfWriter.getInstance(document, new FileOutputStream("lists.pdf"));
            HtmlWriter.getInstance(document, new FileOutputStream("lists.html"));

            // step 3: we open the document
            document.open();

            // step 4:
            List list = new List(true, 20);
            list.add(new ListItem("First line"));
            list.add(new ListItem(
                    "The second line is longer to see what happens once the end of the line is reached. Will it start on a new line?"));
            list.add(new ListItem("Third line"));
            document.add(list);

            document.add(new Paragraph("some books I really like:"));
            ListItem listItem;
            list = new List(true, 15);
            listItem = new ListItem("When Harlie was one", FontFactory.getFont(FontFactory.TIMES_ROMAN, 12));
            listItem.add(new Chunk(" by David Gerrold", FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.ITALIC)));
            list.add(listItem);
            listItem = new ListItem("The World according to Garp", FontFactory.getFont(FontFactory.TIMES_ROMAN, 12));
            listItem.add(new Chunk(" by John Irving", FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.ITALIC)));
            list.add(listItem);
            listItem = new ListItem("Decamerone", FontFactory.getFont(FontFactory.TIMES_ROMAN, 12));
            listItem.add(
                    new Chunk(" by Giovanni Boccaccio", FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.ITALIC)));
            list.add(listItem);
            document.add(list);

            Paragraph paragraph = new Paragraph("some movies I really like:");
            list = new List(false, 10);
            list.add("Wild At Heart");
            list.add("Casablanca");
            list.add("When Harry met Sally");
            list.add("True Romance");
            list.add("Le mari de la coiffeuse");
            paragraph.add(list);
            document.add(paragraph);

            document.add(new Paragraph("Some authors I really like:"));
            list = new List(false, 20);
            list.setListSymbol(new Chunk("\u2022", FontFactory.getFont(FontFactory.HELVETICA, 20, Font.BOLD)));
            listItem = new ListItem("Isaac Asimov");
            list.add(listItem);
            List sublist;
            sublist = new List(false, true, 10);
            sublist.setListSymbol(new Chunk("", FontFactory.getFont(FontFactory.HELVETICA, 8)));
            sublist.add("The Foundation Trilogy");
            sublist.add("The Complete Robot");
            sublist.add("Caves of Steel");
            sublist.add("The Naked Sun");
            list.add(sublist);
            listItem = new ListItem("John Irving");
            list.add(listItem);
            sublist = new List(false, true, 10);
            sublist.setFirst('a');
            sublist.setListSymbol(new Chunk("", FontFactory.getFont(FontFactory.HELVETICA, 8)));
            sublist.add("The World according to Garp");
            sublist.add("Hotel New Hampshire");
            sublist.add("A prayer for Owen Meany");
            sublist.add("Widow for a year");
            list.add(sublist);
            listItem = new ListItem("Kurt Vonnegut");
            list.add(listItem);
            sublist = new List(false, true, 10);
            sublist.setListSymbol(new Chunk("", FontFactory.getFont(FontFactory.HELVETICA, 8)));
            sublist.add("Slaughterhouse 5");
            sublist.add("Welcome to the Monkey House");
            sublist.add("The great pianola");
            sublist.add("Galapagos");
            list.add(sublist);
            document.add(list);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}