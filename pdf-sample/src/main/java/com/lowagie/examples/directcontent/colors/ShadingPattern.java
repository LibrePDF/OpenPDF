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
package com.lowagie.examples.directcontent.colors;

import com.lowagie.examples.AbstractSample;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfShading;
import com.lowagie.text.pdf.PdfShadingPattern;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Shading example
 */
public class ShadingPattern  extends AbstractSample {

    @Override
    public String getFileName() {
        return "/shading_pattern";
    }

    public static void main(String[] args) {
        ShadingPattern templates = new ShadingPattern();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("DirectContent :: Colors :: Shading pattern");

        // tag::generation[]
        try (Document document = new Document(PageSize.A4, 50, 50, 50, 50)) {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path + getFileName() + ".pdf"));
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
            PdfShading shadingR = PdfShading.simpleRadial(writer, 200, 500, 50, 300, 500, 100, new Color(255, 247, 148), new Color(247, 138, 107), false, false);
            cb.paintShading(shadingR);
            cb.sanityCheck();
        } catch (Exception de) {
            de.printStackTrace();
        }
        // end::generation[]
    }
}
