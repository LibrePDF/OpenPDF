package com.lowagie.toolbox.plugins.watermarker;

import static java.awt.Color.BLACK;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

/**
 * This class is the API equivalent of the WatermarkerTool.
 * It lets you add a text watermark to all pages of a document given in input.
 */
public class Watermarker {

    private final PdfReader reader;
    private final ByteArrayOutputStream outputStream;
    private final PdfStamper stamp;
    private final String text;
    private final int fontsize;
    private final float opacity;
    private Color color = BLACK;

    /**
     *  The main constructor with all mandatory arguments. By default, the color stays black.
     *
     * @param input the pdf content as a byte[]
     * @param text the text to write as watermark
     * @param fontsize the fontsize of the watermark
     * @param opacity the opacity of the watermark
     * @throws IOException on error
     * @throws DocumentException on error
     */
    public Watermarker(byte[] input, String text, int fontsize, float opacity) throws IOException, DocumentException {
        this.reader = new PdfReader(input);
        this.outputStream = new ByteArrayOutputStream();
        this.stamp = new PdfStamper(reader, outputStream);
        this.text = text;
        this.fontsize = fontsize;
        this.opacity = opacity;
    }

    /**
     * To change the default black color by a new one.
     *
     * @param color the new color to use
     * @return the current builder instance
     */
    public Watermarker withColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * Write the watermark to the pdf given in entry.
     *
     * @return a brand new byte[] without modifying the original one.
     * @throws IOException on error
     * @throws DocumentException on error
     */
    public byte[] write() throws IOException, DocumentException {
        Writer writer = new Writer(reader, stamp, text, fontsize, opacity, color);
        writer.write();
        return outputStream.toByteArray();
    }
}
