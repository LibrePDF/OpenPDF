/*
 * $Id: TableBorders.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Demonstrates different borderstyles.
 */
public class TableBorders {

    /**
     * Demonstrates different borderstyles.
     *
     * @param args the number of rows for each table fragment.
     */
    public static void main(String[] args) {

        System.out.println("Table Borders");
        // step1
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            // step2
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("TableBorders.pdf"));
            // step3
            document.open();
            // step4

            // page 1
            Font tableFont = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
            float padding = 0f;
            Rectangle border = new Rectangle(0f, 0f);
            border.setBorderWidthLeft(6f);
            border.setBorderWidthBottom(5f);
            border.setBorderWidthRight(4f);
            border.setBorderWidthTop(2f);
            border.setBorderColorLeft(Color.RED);
            border.setBorderColorBottom(Color.ORANGE);
            border.setBorderColorRight(Color.YELLOW);
            border.setBorderColorTop(Color.GREEN);
            makeTestPage(tableFont, border, writer, document, padding, true, true);
            Font font = FontFactory.getFont("Helvetica", 10);
            Paragraph p;
            p = new Paragraph("\nVarious border widths and colors\nuseAscender=true, useDescender=true", font);
            document.add(p);

            document.newPage();

            // page 2
            padding = 2f;
            border = new Rectangle(0f, 0f);
            border.setBorderWidthLeft(1f);
            border.setBorderWidthBottom(2f);
            border.setBorderWidthRight(1f);
            border.setBorderWidthTop(2f);
            border.setBorderColor(Color.BLACK);
            makeTestPage(tableFont, border, writer, document, padding, true, true);
            p = new Paragraph(
                    "More typical use - padding of 2\nuseBorderPadding=true, useAscender=true, useDescender=true",
                    font);
            document.add(p);

            document.newPage();

            // page 3
            padding = 0f;
            border = new Rectangle(0f, 0f);
            border.setBorderWidthLeft(1f);
            border.setBorderWidthBottom(2f);
            border.setBorderWidthRight(1f);
            border.setBorderWidthTop(2f);
            border.setBorderColor(Color.BLACK);
            makeTestPage(tableFont, border, writer, document, padding, false, true);
            p = new Paragraph("\nuseBorderPadding=true, useAscender=false, useDescender=true", font);
            document.add(p);

            document.newPage();

            // page 4
            padding = 0f;
            border = new Rectangle(0f, 0f);
            border.setBorderWidthLeft(1f);
            border.setBorderWidthBottom(2f);
            border.setBorderWidthRight(1f);
            border.setBorderWidthTop(2f);
            border.setBorderColor(Color.BLACK);
            makeTestPage(tableFont, border, writer, document, padding, false, false);
            p = new Paragraph("\nuseBorderPadding=true, useAscender=false, useDescender=false", font);
            document.add(p);

            document.newPage();

            // page 5
            padding = 0f;
            border = new Rectangle(0f, 0f);
            border.setBorderWidthLeft(1f);
            border.setBorderWidthBottom(2f);
            border.setBorderWidthRight(1f);
            border.setBorderWidthTop(2f);
            border.setBorderColor(Color.BLACK);
            makeTestPage(tableFont, border, writer, document, padding, true, false);
            p = new Paragraph("\nuseBorderPadding=true, useAscender=true, useDescender=false", font);
            document.add(p);
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step5
        document.close();
    }

    private static void makeTestPage(Font tableFont, Rectangle borders, PdfWriter writer, Document document,
            float padding, boolean ascender, boolean descender) throws DocumentException {
        document.newPage();
        PdfPTable table = null;
        table = new PdfPTable(4);
        table.setWidthPercentage(100f);

        float leading = tableFont.getSize() * 1.2f;

        table.addCell(
                makeCell("1-Top", Element.ALIGN_TOP, Element.ALIGN_LEFT, tableFont, leading, padding, borders, ascender,
                        descender));
        table.addCell(
                makeCell("2-Middle", Element.ALIGN_MIDDLE, Element.ALIGN_LEFT, tableFont, leading, padding, borders,
                        ascender, descender));
        table.addCell(
                makeCell("3-Bottom", Element.ALIGN_BOTTOM, Element.ALIGN_LEFT, tableFont, leading, padding, borders,
                        ascender, descender));
        table.addCell(makeCell("4-Has a y", Element.ALIGN_TOP, Element.ALIGN_LEFT, tableFont, leading, padding, borders,
                ascender, descender));

        table.addCell(makeCell("5-Abcdy", Element.ALIGN_TOP, Element.ALIGN_LEFT, tableFont, leading, padding, borders,
                ascender, descender));
        table.addCell(
                makeCell("6-Abcdy", Element.ALIGN_MIDDLE, Element.ALIGN_LEFT, tableFont, leading, padding, borders,
                        ascender, descender));
        table.addCell(
                makeCell("7-Abcdy", Element.ALIGN_BOTTOM, Element.ALIGN_LEFT, tableFont, leading, padding, borders,
                        ascender, descender));
        table.addCell(
                makeCell("8-This\nis\na little\ntaller", Element.ALIGN_TOP, Element.ALIGN_LEFT, tableFont, leading,
                        padding, borders, ascender, descender));
        document.add(table);
    }

    private static PdfPCell makeCell(String text, int vAlignment, int hAlignment, Font font, float leading,
            float padding, Rectangle borders, boolean ascender, boolean descender) {
        Paragraph p = new Paragraph(text, font);
        p.setLeading(leading);

        PdfPCell cell = new PdfPCell(p);
        cell.setLeading(leading, 0);
        cell.setVerticalAlignment(vAlignment);
        cell.setHorizontalAlignment(hAlignment);
        cell.cloneNonPositionParameters(borders);
        cell.setUseAscender(ascender);
        cell.setUseDescender(descender);
        cell.setUseBorderPadding(true);
        cell.setPadding(padding);
        return cell;
    }
}