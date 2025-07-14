package com.lowagie.examples.objects.columns;


import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Scenario description: Generate a pdf file with a table by the ColumnText class, and this table is place at the bottom
 * of the file.
 */
public class ColumnTextTable {

    public static float A4_MARGIN_LEFT = 40;
    public static float A4_MARGIN_RIGHT = 40;
    public static float A4_MARGIN_TOP = 100;
    public static float A4_MARGIN_BOTTOM = A4_MARGIN_TOP;
    public static float A4_HEIGHT_BODY = PageSize.A4.getHeight() - A4_MARGIN_TOP - A4_MARGIN_BOTTOM;
    public static float A4_WIDTH_BODY = PageSize.A4.getWidth() - A4_MARGIN_LEFT - A4_MARGIN_RIGHT;
    protected PdfWriter pdfWriter;

    public static void main(String[] args) throws IOException {

        ColumnTextTable columnTextTable = new ColumnTextTable();

        File outputPDF = new File("columnTextTable.pdf");

        Document document = new Document(PageSize.A4);
        document.setMargins(A4_MARGIN_LEFT, A4_MARGIN_RIGHT, A4_MARGIN_TOP, A4_MARGIN_BOTTOM);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        columnTextTable.pdfWriter = PdfWriter.getInstance(document, baos);
        columnTextTable.pdfWriter.setStrictImageSequence(true);

        document.open();

        document.add(new Chunk("The example page"));

        PdfPTable table = columnTextTable.getPdfPTable();

        ColumnText ct = new ColumnText(columnTextTable.pdfWriter.getDirectContent());
        ct.setSimpleColumn(A4_MARGIN_LEFT, A4_MARGIN_BOTTOM, A4_WIDTH_BODY + A4_MARGIN_LEFT,
                A4_HEIGHT_BODY + A4_MARGIN_BOTTOM);

        float space = columnTextTable.getHeightOfBlock(table);

        ct.addElement(table);

        ct.setYLine(A4_MARGIN_BOTTOM + space);

        ct.go(false);

        document.close();

        FileOutputStream fos = new FileOutputStream(outputPDF);
        fos.write(baos.toByteArray());
        fos.close();
    }


    /**
     * Get the PdfPTable which will be written into the PDF file.
     *
     * @return PdfPTable
     */
    PdfPTable getPdfPTable() {
        Paragraph f1, f2;
        PdfPTable table;
        table = new PdfPTable(1);
        table.setTotalWidth(A4_WIDTH_BODY);
        table.setLockedWidth(true);
        table.setSplitRows(false);
        PdfPCell cell;
        f1 = new Paragraph(new Chunk("cell1 example content"));
        f1.add(Chunk.NEWLINE);
        f1.setAlignment(Element.ALIGN_CENTER);
        f2 = new Paragraph(new Chunk("cell2 example content"));
        f2.add(Chunk.NEWLINE);
        f2.setAlignment(Element.ALIGN_CENTER);
        cell = new PdfPCell();
        cell.addElement(f1);
        table.addCell(cell);
        cell = new PdfPCell();
        cell.addElement(f2);
        table.addCell(cell);
        return table;
    }


    /**
     * Get the height of the input block.
     *
     * @param elements The input elements which will be used to calculate the height.
     * @return height, which is the height of the input block.
     */
    float getHeightOfBlock(Element... elements) {
        ColumnText ct = new ColumnText(pdfWriter.getDirectContent());
        float startY = A4_HEIGHT_BODY + A4_MARGIN_BOTTOM;

        float height;

        ct.setSimpleColumn(A4_MARGIN_LEFT, A4_MARGIN_BOTTOM, A4_WIDTH_BODY + A4_MARGIN_LEFT,
                A4_HEIGHT_BODY + A4_MARGIN_BOTTOM);
        ct.setYLine(startY);

        for (Element element : elements) {
            ct.addElement(element);
        }

        int result = ct.go(true);
        if (ColumnText.hasMoreText(result)) {
            return -1;
        } else {
            height = startY - ct.getYLine();
            return height;
        }
    }


}
