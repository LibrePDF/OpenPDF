package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;
import org.junit.jupiter.api.Test;

public class TablePdfTest {

    @Test
    void testTableSpacingPercentage() throws Exception {
        Document document = PdfTestBase.createPdf("testTableSpacingPercentage.pdf");
        document.setMargins(72, 72, 72, 72);
        document.open();
        PdfPTable table = new PdfPTable(1);
        table.setSpacingBefore(20);
        table.setWidthPercentage(100);
        PdfPCell cell;
        cell = new PdfPCell();
        Phrase phase = new Phrase("John Doe");
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // This has no
        // effect
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // This has no effect
        cell.addElement(phase);
        table.addCell(cell);
        document.add(table);
        document.close();
    }

    @Test
    void testTableArrayOutOfBoundsSpan() throws Exception {
        Document document = PdfTestBase
                .createPdf("testTableArrayOutOfBoundsSpan.pdf");
        document.open();
        PdfPTable table = new PdfPTable(2);
        table.setComplete(false);

        // First page

        // add five rows
        // the first cell has rowspan 10
        // the second column gets just 5 cells

        PdfPCell cellWithRowspan = new PdfPCell();
        cellWithRowspan.setRowspan(10);
        cellWithRowspan.addElement(new Phrase("Rowspan 10"));
        table.addCell(cellWithRowspan);

        for (int i = 0; i < 5; ++i) {
            PdfPCell cell = new PdfPCell();
            cell.addElement(new Phrase("Cell " + i));
            table.addCell(cell);
            table.completeRow();
        }

        // force page break (this would result from business rules)

        document.add(table);
        document.newPage();

        // Second page

        // would like to have the remaining rowspan (5 rows) of first column to
        // continue on this page
        // BUT: adding the table to the document lost the information about
        // cells with rowspan

        // as a consequence adding the remaining 5 rows of cells for column 2
        // does not work as expected
        // => crashes in PdfPTable.rowSpanAbove with NullPointerException
        for (int i = 6; i < 9; ++i) {
            PdfPCell cell = new PdfPCell();
            cell.addElement(new Phrase("Cell " + i));
            table.addCell(cell);
        }

        // finish second page

        table.setComplete(true);
        document.add(table);
        document.close();
    }

    @Test
    void testCreateTable() throws Exception {
        // create document
        Document document = PdfTestBase.createPdf("testCreateTable.pdf");
        try {
            // new page with a table
            document.open();
            document.newPage();

            PdfPTable table = createPdfTable(2);

            for (int i = 0; i < 10; i++) {
                PdfPCell cell = new PdfPCell();
                cell.setRowspan(2);
                table.addCell(cell);

            }
            table.calculateHeights(true);
            document.add(table);
            document.newPage();

        } finally {
            // close document
            if (document != null)
                document.close();
        }

    }

    private PdfPTable createPdfTable(int numberOfColumns)
            throws DocumentException {

        PdfPTable table = new PdfPTable(numberOfColumns);

        table.getDefaultCell().setBorder(1);
        table.setSpacingBefore(0f);
        table.setSpacingAfter(0);
        table.setKeepTogether(true);
        table.getDefaultCell().setUseAscender(true);
        table.getDefaultCell().setUseDescender(true);
        table.getDefaultCell().setUseBorderPadding(false);

        return table;
    }

}
