package com.lowagie.text.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;

public class TableRowSpanEvenSplitTest {
    @Test
    public void threeRowSpanTest() {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        PdfPTable table = new PdfPTable(2);
        PdfPCell cell = new PdfPCell();
        cell.setRowspan(3);
        cell.addElement(new Chunk("rowspan\nrowspan\nrowspan"));
        table.addCell(cell);

        table.addCell("row1");
        table.addCell("row2");
        table.addCell("row3");
        document.open();
        document.add(table);
        float heightRow1 = table.getRows().get(0).getMaxHeights();
        float heightRow2 = table.getRows().get(1).getMaxHeights();
        float heightRow3 = table.getRows().get(2).getMaxHeights();
        document.close();
        Assertions.assertEquals(0, heightRow1 - heightRow2);
        Assertions.assertEquals(0, heightRow3 - heightRow2);
    }

    @Test
    public void threeWithOneUnevenTest() {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        PdfPTable table = new PdfPTable(2);
        PdfPCell cell = new PdfPCell();
        cell.setRowspan(3);
        cell.addElement(new Chunk("rowspan\nrowspan\nrowspan"));
        table.addCell(cell);

        table.addCell("row1\nrow1");
        table.addCell("row2");
        table.addCell("row3");
        document.open();
        document.add(table);
        float heightRow1 = table.getRows().get(0).getMaxHeights();
        float heightRow2 = table.getRows().get(1).getMaxHeights();
        float heightRow3 = table.getRows().get(2).getMaxHeights();
        document.close();
        Assertions.assertEquals(0, heightRow2 - heightRow3);
        Assertions.assertNotEquals(0, heightRow1 - heightRow2);
    }

    @Test
    public void threeWithLargeRowspanCellTest() {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        PdfPTable table = new PdfPTable(2);
        PdfPCell cell = new PdfPCell();
        cell.setRowspan(3);
        cell.addElement(new Chunk("rowspan\nrowspan\nrowspan\nrowspan\nrowspan\nrowspan\nrowspan"));
        table.addCell(cell);

        table.addCell("row1");
        table.addCell("row2");
        table.addCell("row3");
        document.open();
        document.add(table);
        float heightRow1 = table.getRows().get(0).getMaxHeights();
        float heightRow2 = table.getRows().get(1).getMaxHeights();
        float heightRow3 = table.getRows().get(2).getMaxHeights();
        document.close();
        Assertions.assertEquals(0, heightRow2 - heightRow3);
        Assertions.assertEquals(0, heightRow1 - heightRow2);
    }

    @Test
    public void threeWithLargeRowspanCellTestUnevenDistribution() {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        PdfPTable table = new PdfPTable(2);
        PdfPCell cell = new PdfPCell();
        cell.setRowspan(3);
        cell.addElement(new Chunk("rowspan\nrowspan\nrowspan\nrowspan\nrowspan\nrowspan\nrowspan"));
        table.addCell(cell);

        table.addCell("row1\nrow1\nrow1\nrow1\nrow1\nrow1");
        table.addCell("row2");
        table.addCell("row3");
        document.open();
        document.add(table);
        float heightRow1 = table.getRows().get(0).getMaxHeights();
        float heightRow2 = table.getRows().get(1).getMaxHeights();
        float heightRow3 = table.getRows().get(2).getMaxHeights();
        document.close();
        Assertions.assertEquals(0, heightRow2 - heightRow3);
        Assertions.assertNotEquals(0, heightRow1 - heightRow2);
        Assertions.assertTrue(heightRow1 > heightRow2);
    }
}
