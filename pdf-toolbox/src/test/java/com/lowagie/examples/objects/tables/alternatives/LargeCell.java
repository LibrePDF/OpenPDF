/*
 * $Id: LargeCell.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.tables.alternatives;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Using CellsFitPage for a Cell that doesn't fit the page.
 */
public class LargeCell {

    /**
     * Demonstrates the features of the old Table class.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("large cell");
        // step 1: creation of a document-object
        Document document = new Document(PageSize.A6);
        try {
            // step 2: creation of the writer-object
            PdfWriter.getInstance(document, new FileOutputStream("largecell.pdf"));
            // step 3: we open the document
            document.open();
            // step 4: we create a table and add it to the document
            Table table = new Table(3);
            table.setCellsFitPage(true);
            String text = "long long long long long long long long long long long long long long long long long long "
                    + "long long long long long long long long long long long long long long long long long long long "
                    + "long long long long long long long long long long long long long long long long long long long "
                    + "long long long long long long long long long long long long long long long long long long long "
                    + "long long long long long long long long long long long long long long long long long long long "
                    + "long long long long long long long long long long long long long long long long long long long "
                    + "long long long long long long long long long long long long long long long long long long long "
                    + "long long long long long long long long long long long long long long long long long long long "
                    + "long long long long long long long long long long long text";
            table.addCell("text");
            table.addCell("text");
            table.addCell("text");
            table.addCell(text);
            table.addCell(text + text);
            table.addCell(text);
            table.addCell("text");
            table.addCell("text");
            table.addCell("text");
            document.add(table);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        // step 5: we close the document
        document.close();
    }
}
