/*
 * $Id: Vertical.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.VerticalText;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Writing Vertical Text.
 */
public class Vertical {

    static String[] texts = {
            "Some very long text to check if it wraps (or not).",
            " In blue.",
            "And now in orange another very long text.",
            "", "", ""};

    static String[] encs = {"UniJIS-UCS2-V", "Identity-V"};

    /**
     * @param text the text to convert
     * @return converted text
     */
    public static String convertCid(String text) {
        char[] cid = text.toCharArray();
        for (int k = 0; k < cid.length; ++k) {
            char c = cid[k];
            if (c == '\n') {
                cid[k] = '\uff00';
            } else {
                cid[k] = (char) (c - ' ' + 8720);
            }
        }
        return new String(cid);
    }

    /**
     * Writing vertical text.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            texts[3] = convertCid(texts[0]);
            texts[4] = convertCid(texts[1]);
            texts[5] = convertCid(texts[2]);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("vertical.pdf"));
            int idx = 0;
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            for (int j = 0; j < 2; ++j) {
                BaseFont bf = BaseFont.createFont("KozMinPro-Regular", encs[j], false);
                cb.setRGBColorStroke(255, 0, 0);
                cb.setLineWidth(0);
                float x = 400;
                float y = 700;
                float height = 400;
                float leading = 30;
                int maxLines = 6;
                for (int k = 0; k < maxLines; ++k) {
                    cb.moveTo(x - k * leading, y);
                    cb.lineTo(x - k * leading, y - height);
                }
                cb.rectangle(x, y, -leading * (maxLines - 1), -height);
                cb.stroke();
                int status;
                VerticalText vt = new VerticalText(cb);
                vt.setVerticalLayout(x, y, height, maxLines, leading);
                vt.addText(new Chunk(texts[idx++], new Font(bf, 20)));
                vt.addText(new Chunk(texts[idx++], new Font(bf, 20, 0, Color.blue)));
                status = vt.go();
                System.out.println(status);
                vt.setAlignment(Element.ALIGN_RIGHT);
                vt.addText(new Chunk(texts[idx++], new Font(bf, 20, 0, Color.orange)));
                status = vt.go();
                System.out.println(status);
                document.newPage();
            }
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }


}
