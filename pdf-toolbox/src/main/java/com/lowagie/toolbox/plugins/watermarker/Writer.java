package com.lowagie.toolbox.plugins.watermarker;

import static com.lowagie.text.pdf.BaseFont.WINANSI;
import static com.lowagie.text.pdf.BaseFont.createFont;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import java.awt.Color;
import java.io.IOException;

/**
 * The concrete class which will write a the watermark on the stamp. It is meant to be used through WatermarkerTool or
 * WatermarkerBuilder.
 */
class Writer {

    private PdfReader reader;
    private PdfStamper stamp;
    private String text;
    private int fontsize;
    private float opacity;
    private Color color;
    private BaseFont font;

    Writer(PdfReader reader, PdfStamper stamp, String text, int fontsize, float opacity, Color color) {
        this.reader = reader;
        this.stamp = stamp;
        this.text = text;
        this.fontsize = fontsize;
        this.opacity = opacity;
        this.color = color;
    }

    Writer withFont(final BaseFont font) {
        this.font = font;
        return this;
    }

    /**
     * Does the magic, with all parameters already set and valid. At the end, the PDF file configured through the stamp
     * parameter will be written.
     *
     * @throws DocumentException if the default "Helvetica" font cannot be created
     * @throws IOException       if the default "Helvetica" font cannot be created
     */
    void write() throws IOException, DocumentException {
        final BaseFont bf = (font != null) ? font : createFont("Helvetica", WINANSI, false);
        int pagecount = reader.getNumberOfPages();
        PdfGState gs1 = new PdfGState();
        gs1.setFillOpacity(opacity);
        float txtwidth = bf.getWidthPoint(text, fontsize);
        for (int i = 1; i <= pagecount; i++) {
            PdfContentByte seitex = stamp.getOverContent(i);
            Rectangle recc = reader.getCropBox(i);
            recc.normalize();
            float winkel = (float) Math.atan(recc.getHeight()
                    / recc.getWidth());
            float m1 = (float) Math.cos(winkel);
            float m2 = (float) -Math.sin(winkel);
            float m3 = (float) Math.sin(winkel);
            float m4 = (float) Math.cos(winkel);
            float xoff = (float) (-Math.cos(winkel) * txtwidth / 2 - Math
                    .sin(winkel) * fontsize / 2);
            float yoff = (float) (Math.sin(winkel) * txtwidth / 2 - Math
                    .cos(winkel) * fontsize / 2);
            seitex.saveState();
            seitex.setGState(gs1);
            seitex.beginText();
            seitex.setFontAndSize(bf, fontsize);
            seitex.setColorFill(color);
            seitex.setTextMatrix(m1, m2, m3, m4,
                    xoff + recc.getWidth() / 2, yoff + recc.getHeight() / 2);
            seitex.showText(text);
            seitex.endText();
            seitex.restoreState();
        }
        stamp.close();
    }

}
