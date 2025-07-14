/*
 * $Id: Shading.java 3838 2009-04-07 18:34:15Z mstorer $
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
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfFunction;
import com.lowagie.text.pdf.PdfShading;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Shading example
 */
public class Shading {

    /**
     * Shading example.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Shading");
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("shading.pdf"));
            document.open();

            PdfFunction function1 = PdfFunction.type2(writer, new float[]{0, 1}, null,
                    new float[]{.929f, .357f, 1, .298f}, new float[]{.631f, .278f, 1, .027f}, 1.048f);
            PdfFunction function2 = PdfFunction.type2(writer, new float[]{0, 1}, null,
                    new float[]{.929f, .357f, 1, .298f}, new float[]{.941f, .4f, 1, .102f}, 1.374f);
            PdfFunction function3 = PdfFunction.type3(writer, new float[]{0, 1}, null,
                    new PdfFunction[]{function1, function2}, new float[]{.708f}, new float[]{1, 0, 0, 1});
            PdfShading shading = PdfShading.type3(writer, new CMYKColor(0, 0, 0, 0),
                    new float[]{0, 0, .096f, 0, 0, 1}, null, function3, new boolean[]{true, true});
            PdfContentByte cb = writer.getDirectContent();
            cb.moveTo(316.789f, 140.311f);
            cb.curveTo(303.222f, 146.388f, 282.966f, 136.518f, 279.122f, 121.983f);
            cb.lineTo(277.322f, 120.182f);
            cb.curveTo(285.125f, 122.688f, 291.441f, 121.716f, 298.156f, 119.386f);
            cb.lineTo(336.448f, 119.386f);
            cb.curveTo(331.072f, 128.643f, 323.346f, 137.376f, 316.789f, 140.311f);
            cb.clip();
            cb.newPath();
            cb.saveState();
            cb.concatCTM(27.7843f, 0, 0, -27.7843f, 310.2461f, 121.1521f);
            cb.paintShading(shading);
            cb.restoreState();

            cb.sanityCheck();

            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}
