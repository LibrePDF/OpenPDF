/*
 * $Id: Tables.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is free software. It may only be copied or modified
 * if you include the following copyright notice:
 *
 * --> Copyright 2001-2005 by G. Martinelli and Bruno Lowagie <--
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDFtutorial/
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */
package com.lowagie.examples.objects.tables.pdfptable;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Adds a table to a page twice.
 */
public class Tables {

    /**
     * Adds a table to a page twice.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        Font font8 = FontFactory.getFont(FontFactory.HELVETICA, 8);

        // step 1
        Document document = new Document(PageSize.A4);

        try {
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("tables.pdf"));
            float width = document.getPageSize().getWidth();
            float height = document.getPageSize().getHeight();
            // step 3
            document.open();

            // step 4
            float[] columnDefinitionSize = {33.33F, 33.33F, 33.33F};

            float pos = height / 2;
            PdfPTable table = null;
            PdfPCell cell = null;

            table = new PdfPTable(columnDefinitionSize);
            table.getDefaultCell().setBorder(0);
            table.setHorizontalAlignment(0);
            table.setTotalWidth(width - 72);
            table.setLockedWidth(true);

            cell = new PdfPCell(new Phrase("Table added with document.add()"));
            cell.setColspan(columnDefinitionSize.length);
            table.addCell(cell);
            table.addCell(new Phrase("Louis Pasteur", font8));
            table.addCell(new Phrase("Albert Einstein", font8));
            table.addCell(new Phrase("Isaac Newton", font8));
            table.addCell(new Phrase("8, Rabic street", font8));
            table.addCell(new Phrase("2 Photons Avenue", font8));
            table.addCell(new Phrase("32 Gravitation Court", font8));
            table.addCell(new Phrase("39100 Dole France", font8));
            table.addCell(new Phrase("12345 Ulm Germany", font8));
            table.addCell(new Phrase("45789 Cambridge  England", font8));

            document.add(table);

            table = new PdfPTable(columnDefinitionSize);
            table.getDefaultCell().setBorder(0);
            table.setHorizontalAlignment(0);
            table.setTotalWidth(width - 72);
            table.setLockedWidth(true);

            cell = new PdfPCell(new Phrase("Table added with writeSelectedRows"));
            cell.setColspan(columnDefinitionSize.length);
            table.addCell(cell);
            table.addCell(new Phrase("Louis Pasteur", font8));
            table.addCell(new Phrase("Albert Einstein", font8));
            table.addCell(new Phrase("Isaac Newton", font8));
            table.addCell(new Phrase("8, Rabic street", font8));
            table.addCell(new Phrase("2 Photons Avenue", font8));
            table.addCell(new Phrase("32 Gravitation Court", font8));
            table.addCell(new Phrase("39100 Dole France", font8));
            table.addCell(new Phrase("12345 Ulm Germany", font8));
            table.addCell(new Phrase("45789 Cambridge  England", font8));

            table.writeSelectedRows(0, -1, 50, pos, writer.getDirectContent());
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        // step 5
        document.close();
    }
}