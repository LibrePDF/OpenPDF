/*
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.lowagie.examples.objects.bookmarks;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import org.librepdf.openpdf.examples.content.Constants;

/**
 * Creates a document with outlines (bookmarks) for ListItems.
 *
 * @author Matthias Luppi
 */
public class ListItemBookmarks {

    /**
     * Creates a document with outlines for ListItems.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Bookmarks for ListItems");

        // create a new document-object
        Document document = new Document(PageSize.A4);
        try {
            // create a new writer to write the document to a file
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("ListItemBookmarks.pdf"));

            // set viewer preference to make the outlines visible (some viewers ignore this)
            writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);

            // open the document to add content to the body
            document.open();

            // create a new list and add some ListItems containing a Chunk object
            // that has a local destination set (this identifier is used below)
            List list = new List();
            list.add(new ListItem(new Chunk(Constants.EA_RES).setLocalDestination("item1")));
            list.add(new ListItem(new Chunk(Constants.HIS_REBUS).setLocalDestination("item2")));
            list.add(new ListItem(new Chunk(Constants.APUD_HELVETIOS).setLocalDestination("item3")));
            document.add(list);

            // get the root outline by accessing the direct content
            PdfOutline rootOutline = writer.getDirectContent().getRootOutline();

            // add outline elements with a goto-PdfAction referring back to the
            // local destination which was set on the Chunks added to the list above
            new PdfOutline(rootOutline, PdfAction.gotoLocalPage("item1", false), "Ea res est Helvetiis");
            new PdfOutline(rootOutline, PdfAction.gotoLocalPage("item2", false), "His rebus adducti");
            new PdfOutline(rootOutline, PdfAction.gotoLocalPage("item3", false), "Apud Helvetios longe");

        } catch (Exception e) {
            e.printStackTrace();
        }

        // close document
        document.close();
    }
}
