/*
 * $Id: RightToLeft.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.fonts.styles;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Writing RTL text such as Arabic or Hebrew.
 */
public class RightToLeft {

    /**
     * arabic text
     */
    public static String ar1 = "\u0623\u0648\u0631\u0648\u0628\u0627, \u0628\u0631\u0645\u062c\u064a\u0627\u062a "
            + "\u0627\u0644\u062d\u0627\u0633\u0648\u0628 + \u0627\u0646\u062a\u0631\u0646\u064a\u062a :\n\n";
    /**
     * arabic text
     */
    public static String ar2 = "\u062a\u0635\u0628\u062d \u0639\u0627\u0644\u0645\u064a\u0627 \u0645\u0639 "
            + "\u064a\u0648\u0646\u064a\u0643\u0648\u062f\n\n";
    /**
     * arabic text
     */
    public static String ar3 = "\u062a\u0633\u062c\u0651\u0644 \u0627\u0644\u0622\u0646 \u0644\u062d\u0636\u0648\u0631 "
            + "\u0627\u0644\u0645\u0624\u062a\u0645\u0631 \u0627\u0644\u062f\u0648\u0644\u064a "
            + "\u0627\u0644\u0639\u0627\u0634\u0631 \u0644\u064a\u0648\u0646\u064a\u0643\u0648\u062f, "
            + "\u0627\u0644\u0630\u064a \u0633\u064a\u0639\u0642\u062f \u0641\u064a 10-12 \u0622\u0630\u0627\u0631 "
            + "1997 \u0628\u0645\u062f\u064a\u0646\u0629 \u0645\u0627\u064a\u0646\u062a\u0633, "
            + "\u0623\u0644\u0645\u0627\u0646\u064a\u0627. \u0648\u0633\u064a\u062c\u0645\u0639 "
            + "\u0627\u0644\u0645\u0624\u062a\u0645\u0631 \u0628\u064a\u0646 \u062e\u0628\u0631\u0627\u0621 "
            + "\u0645\u0646  \u0643\u0627\u0641\u0629 \u0642\u0637\u0627\u0639\u0627\u062a "
            + "\u0627\u0644\u0635\u0646\u0627\u0639\u0629 \u0639\u0644\u0649 \u0627\u0644\u0634\u0628\u0643\u0629 "
            + "\u0627\u0644\u0639\u0627\u0644\u0645\u064a\u0629 \u0627\u0646\u062a\u0631\u0646\u064a\u062a "
            + "\u0648\u064a\u0648\u0646\u064a\u0643\u0648\u062f, \u062d\u064a\u062b \u0633\u062a\u062a\u0645, "
            + "\u0639\u0644\u0649 \u0627\u0644\u0635\u0639\u064a\u062f\u064a\u0646 "
            + "\u0627\u0644\u062f\u0648\u0644\u064a \u0648\u0627\u0644\u0645\u062d\u0644\u064a \u0639\u0644\u0649 "
            + "\u062d\u062f \u0633\u0648\u0627\u0621 \u0645\u0646\u0627\u0642\u0634\u0629 \u0633\u0628\u0644 "
            + "\u0627\u0633\u062a\u062e\u062f\u0627\u0645 \u064a\u0648\u0646\u0643\u0648\u062f  \u0641\u064a "
            + "\u0627\u0644\u0646\u0638\u0645 \u0627\u0644\u0642\u0627\u0626\u0645\u0629 "
            + "\u0648\u0641\u064a\u0645\u0627 \u064a\u062e\u0635 "
            + "\u0627\u0644\u062a\u0637\u0628\u064a\u0642\u0627\u062a "
            + "\u0627\u0644\u062d\u0627\u0633\u0648\u0628\u064a\u0629, \u0627\u0644\u062e\u0637\u0648\u0637, "
            + "\u062a\u0635\u0645\u064a\u0645 \u0627\u0644\u0646\u0635\u0648\u0635  "
            + "\u0648\u0627\u0644\u062d\u0648\u0633\u0628\u0629 \u0645\u062a\u0639\u062f\u062f\u0629 "
            + "\u0627\u0644\u0644\u063a\u0627\u062a.\n\n";
    /**
     * arabic text
     */
    public static String ar4 = "ع\u0646\u062f\u0645\u0627 \u064a\u0631\u064a\u062f "
            + "\u0627\u0644\u0639\u0627\u0644\u0645 \u0623\u0646 \u064a\u062a\u0643\u0644\u0651\u0645, "
            + "\u0641\u0647\u0648 \u064a\u062a\u062d\u062f\u0651\u062b \u0628\u0644\u063a\u0629 "
            + "\u064a\u0648\u0646\u064a\u0643\u0648\u062f\n\n";
    /**
     * hebrew text
     */
    public static String he1 = "\u05d0\u05d9\u05e8\u05d5\u05e4\u05d4, \u05ea\u05d5\u05db\u05e0\u05d4 "
            + "\u05d5\u05d4\u05d0\u05d9\u05e0\u05d8\u05e8\u05e0\u05d8:\n\n";
    /**
     * hebrew text
     */
    public static String he2 = "Unicode \u05d9\u05d5\u05e6\u05d0 \u05dc\u05e9\u05d5\u05e7 "
            + "\u05d4\u05e2\u05d5\u05dc\u05de\u05d9\n\n";
    /**
     * hebrew text
     */
    public static String he3 = "\u05d4\u05d9\u05e8\u05e9\u05de\u05d5 \u05db\u05e2\u05ea \u05dc\u05db\u05e0\u05e1 "
            + "Unicode \u05d4\u05d1\u05d9\u05e0\u05dc\u05d0\u05d5\u05de\u05d9 \u05d4\u05e2\u05e9\u05d9\u05e8\u05d9, "
            + "\u05e9\u05d9\u05d9\u05e2\u05e8\u05da \u05d1\u05d9\u05df "
            + "\u05d4\u05ea\u05d0\u05e8\u05d9\u05db\u05d9\u05dd 12\u05be10 \u05d1\u05de\u05e8\u05e5 1997, "
            + "\u05d1\u05de\u05d9\u05d9\u05e0\u05e5 \u05e9\u05d1\u05d2\u05e8\u05de\u05e0\u05d9\u05d4. "
            + "\u05d1\u05db\u05e0\u05e1 \u05d9\u05e9\u05ea\u05ea\u05e4\u05d5 \u05de\u05d5\u05de\u05d7\u05d9\u05dd "
            + "\u05de\u05db\u05dc \u05e2\u05e0\u05e4\u05d9 \u05d4\u05ea\u05e2\u05e9\u05d9\u05d9\u05d4 "
            + "\u05d1\u05e0\u05d5\u05e9\u05d0 \u05d4\u05d0\u05d9\u05e0\u05d8\u05e8\u05e0\u05d8 "
            + "\u05d4\u05e2\u05d5\u05dc\u05de\u05d9 \u05d5\u05d4\u05beUnicode, \u05d1\u05d4\u05ea\u05d0\u05de\u05d4 "
            + "\u05dc\u05e9\u05d5\u05e7 \u05d4\u05d1\u05d9\u05e0\u05dc\u05d0\u05d5\u05de\u05d9 "
            + "\u05d5\u05d4\u05de\u05e7\u05d5\u05de\u05d9, \u05d1\u05d9\u05d9\u05e9\u05d5\u05dd Unicode "
            + "\u05d1\u05de\u05e2\u05e8\u05db\u05d5\u05ea \u05d4\u05e4\u05e2\u05dc\u05d4 "
            + "\u05d5\u05d1\u05d9\u05d9\u05e9\u05d5\u05de\u05d9\u05dd, \u05d1\u05d2\u05d5\u05e4\u05e0\u05d9\u05dd, "
            + "\u05d1\u05e4\u05e8\u05d9\u05e1\u05ea \u05d8\u05e7\u05e1\u05d8 "
            + "\u05d5\u05d1\u05de\u05d7\u05e9\u05d5\u05d1 \u05e8\u05d1\u05be\u05dc\u05e9\u05d5\u05e0\u05d9.\n\n";
    /**
     * hebrew text
     */
    public static String he4 = "\u05db\u05d0\u05e9\u05e8 \u05d4\u05e2\u05d5\u05dc\u05dd \u05e8\u05d5\u05e6\u05d4 "
            + "\u05dc\u05d3\u05d1\u05e8, \u05d4\u05d5\u05d0 \u05de\u05d3\u05d1\u05e8 \u05d1\u05beUnicode\n\n";

    /**
     * Writing RTL text such as Arabic or Hebrew.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        try {
            // step 1
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("righttoleft.pdf"));
            // step 3
            document.open();
            // step 4
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = BaseFont.createFont("c:\\windows\\fonts\\times.ttf", BaseFont.IDENTITY_H, true);
            Font f2 = new Font(bf, 24, Font.NORMAL, Color.BLUE);
            float llx = 100;
            float lly = 100;
            float urx = 500;
            float ury = 800;
            ColumnText ct = new ColumnText(cb);
            ct.setSimpleColumn(llx, lly, urx, ury, 24, Element.ALIGN_LEFT);
            ct.setSpaceCharRatio(PdfWriter.NO_SPACE_CHAR_RATIO);
            ct.setLeading(0, 1);
            ct.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            ct.setAlignment(Element.ALIGN_CENTER);
            ct.addText(new Chunk(ar1, new Font(bf, 16)));
            ct.addText(new Chunk(ar2, new Font(bf, 16, Font.NORMAL, Color.red)));
            ct.go();
            ct.setAlignment(Element.ALIGN_JUSTIFIED);
            ct.addText(new Chunk(ar3, new Font(bf, 12)));
            ct.go();
            ct.setAlignment(Element.ALIGN_CENTER);
            ct.addText(new Chunk(ar4, new Font(bf, 14)));
            ct.go();

            ct.setSpaceCharRatio(PdfWriter.SPACE_CHAR_RATIO_DEFAULT);
            ct.setAlignment(Element.ALIGN_CENTER);
            ct.addText(new Chunk("\n\n\n", new Font(bf, 16)));
            ct.addText(new Chunk(he1, new Font(bf, 16)));
            ct.addText(new Chunk(he2, new Font(bf, 16, Font.NORMAL, Color.red)));
            ct.go();
            ct.setAlignment(Element.ALIGN_JUSTIFIED);
            ct.addText(new Chunk(he3, new Font(bf, 12)));
            ct.go();
            ct.setAlignment(Element.ALIGN_CENTER);
            ct.addText(new Chunk(he4, new Font(bf, 14)));
            ct.go();

            document.newPage();
            String atext = "\u062a\u0635\u0628\u062d ";
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setRunDirection(PdfWriter.RUN_DIRECTION_NO_BIDI);
            for (int k = 0; k < 5; ++k) {
                PdfPCell cell = new PdfPCell(new Phrase(10, atext + k, f2));
                if (k == 2) {
                    cell.setColspan(2);
                    ++k;
                }
                table.addCell(cell);
            }
            table.setRunDirection(PdfWriter.RUN_DIRECTION_LTR);
            for (int k = 0; k < 5; ++k) {
                PdfPCell cell = new PdfPCell(new Phrase(10, atext + k, f2));
                if (k == 2) {
                    cell.setColspan(2);
                    ++k;
                }
                table.addCell(cell);
            }
            table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            for (int k = 0; k < 5; ++k) {
                PdfPCell cell = new PdfPCell(new Phrase(10, atext + k, f2));
                if (k == 2) {
                    cell.setColspan(2);
                    ++k;
                }
                table.addCell(cell);
            }
            document.add(table);
            // step 5
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}