/*
 * $Id: ShadingPattern.java 3838 2009-04-07 18:34:15Z mstorer $
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
package org.openpdf.examples.directcontent.colors;

import java.awt.Color;
import java.io.FileOutputStream;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfShading;
import org.openpdf.text.pdf.PdfShadingPattern;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Shading example
 */
public class ShadingPattern {

    /**
     * Shading example.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Shading pattern");
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("shading_pattern.pdf"));
            document.open();

            PdfShading shading = PdfShading.simpleAxial(writer, 100, 100, 400, 100, Color.red, Color.cyan);
            PdfShadingPattern shadingPattern = new PdfShadingPattern(shading);
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_BOLD, BaseFont.WINANSI, false);
            cb.setShadingFill(shadingPattern);
            cb.beginText();
            cb.setTextMatrix(100, 100);
            cb.setFontAndSize(bf, 40);
            cb.showText("Look at this text!");
            cb.endText();
            PdfShading shadingR = PdfShading.simpleRadial(writer, 200, 500, 50, 300, 500, 100, new Color(255, 247, 148),
                    new Color(247, 138, 107), false, false);
            cb.paintShading(shadingR);
            cb.sanityCheck();
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}
