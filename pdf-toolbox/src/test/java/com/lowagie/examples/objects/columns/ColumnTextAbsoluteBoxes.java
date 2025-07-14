/*
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.lowagie.examples.objects.columns;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import org.librepdf.openpdf.examples.content.Constants;

/**
 * Demonstrates the use of ColumnText to place text boxes at absolute positions
 *
 * @author Matthias Luppi
 */
public class ColumnTextAbsoluteBoxes {

    /**
     * Demonstrating the use of ColumnText to place text boxes at absolute positions
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Text boxes at absolute positions with ColumnText");

        // create a new document-object
        Document document = new Document(PageSize.A4);
        try {
            // create a new writer to write the document to a file
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("ColumnTextAbsoluteBoxes.pdf"));

            // open the document to add content to the body
            document.open();

            // get the direct content for this document
            PdfContentByte canvas = writer.getDirectContent();

            // create a new ColumnText object and add some content
            ColumnText ct = new ColumnText(canvas);
            ct.addElement(new Paragraph(Constants.EA_RES));
            ct.addElement(new Paragraph(Constants.HIS_REBUS + " The end!"));

            // define parameters (for convenience)
            Rectangle box = new Rectangle(40, 735, 370, 800);
            float deltaX = 25;
            float deltaY = 100;

            int boxNr = 0;
            do {
                // start a new page if necessary
                if (box.getBottom() - (deltaY * boxNr) < 0) {
                    document.newPage();
                    boxNr = 0; // reset box counter
                }

                // define a box (column) with absolute coordinates
                ct.setSimpleColumn(
                        box.getLeft() + (deltaX * boxNr), // llx
                        box.getBottom() - (deltaY * boxNr), // lly
                        box.getRight() + (deltaX * boxNr), // urx
                        box.getTop() - (deltaY * boxNr) // ury
                );

                boxNr++; // increment box counter

                // ct.go() returns NO_MORE_COLUMN or NO_MORE_TEXT
                // therefore the below statement loops as long as the text overflows
            } while (ct.go() == ColumnText.NO_MORE_COLUMN);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // close the document
        document.close();
    }
}
