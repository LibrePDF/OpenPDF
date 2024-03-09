/*
 * $Id: Transparency.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent.colors;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPatternPainter;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RGBColor;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Demonstrates transparency and images.
 */
public class TableCellTransparency {

    /**
     * Demonstrates the Table Cell Transparency functionality.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Table Cell Transparency");

        try (Document document = new Document(PageSize.A4, 50, 50, 50, 50)) {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("table-cell-transparency.pdf"));
            document.open();
            Rectangle page = document.getPageSize();
            PdfContentByte cb = writer.getDirectContent();
            Font fontPlain = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.NORMAL,
                    new Color(255, 255, 255, 255));
            Font fontHalf = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.NORMAL,
                    new Color(255, 255, 255, 125));

            // Table cell transparency with RGB Color
            PdfPTable pdfTableRGB = new PdfPTable(3);
            PdfPCell cell;

            for (int k = 1; k <= 6; ++k) {
                cell = new PdfPCell(new Phrase("Content Plain" + k, fontHalf));
                cell.setBackgroundColor(new RGBColor(0, 204, 255));
                pdfTableRGB.addCell(cell);
                cell = new PdfPCell(new Phrase("Content Alpha Half" + k, fontPlain));
                cell.setBackgroundColor(new RGBColor(0, 204, 255, 125));
                pdfTableRGB.addCell(cell);
            }
            pdfTableRGB.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
            pdfTableRGB.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - document.topMargin(), cb);

            // Table cell transparency with CMYK Color
            PdfPTable pdfPTableCMYK = new PdfPTable(3);

            for (int k = 1; k <= 6; ++k) {
                cell = new PdfPCell(new Phrase("Content Plain" + k, fontHalf));
                cell.setBackgroundColor(new CMYKColor(255, 95, 232, 23));
                pdfPTableCMYK.addCell(cell);
                cell = new PdfPCell(new Phrase("Content Alpha Half" + k, fontPlain));
                cell.setBackgroundColor(new CMYKColor(255, 95, 232, 23, 125));
                pdfPTableCMYK.addCell(cell);
            }
            pdfPTableCMYK.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
            pdfPTableCMYK.writeSelectedRows(0, -1, document.leftMargin(),
                    page.getHeight() - document.topMargin() - pdfTableRGB.getTotalHeight() - 10f, cb);

            // Draw pattern filled rectangle using CMYK Color
            PdfTemplate tp = addPatternFilledRectangle(cb, new CMYKColor(.8f, .2f, .1f, .5f, .5f));
            cb.addTemplate(tp, 50, 50);

            tp = addPatternFilledRectangle(cb, new CMYKColor(.8f, .2f, .1f, .5f));
            cb.addTemplate(tp, 250, 50);

            // Draw pattern filled rectangle using RGB Color
            tp = addPatternFilledRectangle(cb, new RGBColor(.1f, .8f, .2f, .5f));
            cb.addTemplate(tp, 50, 350);

            tp = addPatternFilledRectangle(cb, new RGBColor(.1f, .8f, .2f));
            cb.addTemplate(tp, 250, 350);

            // Draw plain rectangle using CMYK Color
            drawRedRectangle(cb);

            cb.sanityCheck();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    private static PdfTemplate addPatternFilledRectangle(PdfContentByte cb, Color patternColor) {
        PdfPatternPainter pat = cb.createPattern(15, 15, null);
        pat.rectangle(5, 5, 8, 8);
        pat.fill();
        pat.sanityCheck();

        PdfTemplate tp = cb.createTemplate(400, 300);
        // 60, 80, 0, 10
        tp.setPatternFill(pat, patternColor, .9F);
        tp.rectangle(0, 0, 200, 200);
        tp.fill();
        tp.sanityCheck();
        return tp;
    }

    private static void drawRedRectangle(PdfContentByte cb) {
        cb.moveTo(100f, 20f);
        cb.lineTo(100f, 830f);
        cb.lineTo(400f, 830f);
        cb.lineTo(400f, 20f);
        // because we change the fill color BEFORE we stroke the triangle
        // the color of the triangle will be red instead of green
        cb.setCMYKColorFillF(0f, 0f, 200f, 0f, .5f);
        cb.closePathFillStroke();
        cb.resetGrayFill();
    }
}
