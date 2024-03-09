/*
 * $Id: AddBigTable.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.tables;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Add a big table to a PDF with document.add().
 */
public class AddBigTable {

    /**
     * Big PdfPTable with document.add().
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("document.add(BigTable)");
        // step1
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        try {
            // step2
            PdfWriter.getInstance(document,
                    new FileOutputStream("AddBigTable.pdf"));
            // step3
            document.open();
            // step4
            String[] bogusData = {"M0065920", "SL", "FR86000P", "PCGOLD",
                    "119000", "96 06", "2001-08-13", "4350", "6011648299",
                    "FLFLMTGP", "153", "119000.00"};
            int NumColumns = 12;

            PdfPTable datatable = new PdfPTable(NumColumns);
            int[] headerwidths = {9, 4, 8, 10, 8, 11, 9, 7, 9, 10, 4, 10}; // percentage
            datatable.setWidths(headerwidths);
            datatable.setWidthPercentage(100); // percentage
            datatable.getDefaultCell().setPadding(3);
            datatable.getDefaultCell().setBorderWidth(2);
            datatable.getDefaultCell().setHorizontalAlignment(
                    Element.ALIGN_CENTER);
            datatable.addCell("Clock #");
            datatable.addCell("Trans Type");
            datatable.addCell("Cusip");
            datatable.addCell("Long Name");
            datatable.addCell("Quantity");
            datatable.addCell("Fraction Price");
            datatable.addCell("Settle Date");
            datatable.addCell("Portfolio");
            datatable.addCell("ADP Number");
            datatable.addCell("Account ID");
            datatable.addCell("Reg Rep ID");
            datatable.addCell("Amt To Go ");

            datatable.setHeaderRows(1); // this is the end of the table header

            datatable.getDefaultCell().setBorderWidth(1);
            for (int i = 1; i < 750; i++) {
                if (i % 2 == 1) {
                    datatable.getDefaultCell().setGrayFill(0.9f);
                }
                for (int x = 0; x < NumColumns; x++) {
                    datatable.addCell(bogusData[x]);
                }
                if (i % 2 == 1) {
                    datatable.getDefaultCell().setGrayFill(1);
                }
            }
            document.add(datatable);
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step5
        document.close();
    }
}