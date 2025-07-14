/*
 * $Id: EncodingFont.java 3428 2008-05-24 18:33:09Z xlv $
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
package com.lowagie.examples.fonts;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Fonts and encoding.
 */
public class EncodingFont {

    final static char[] SYMBOL_TABLE = {
            ' ', '!', '\u2200', '#', '\u2203', '%', '&', '\u220b', '(', ')', '*', '+', ',', '-', '.', '/',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
            '\u2245', '\u0391', '\u0392', '\u03a7', '\u0394', '\u0395', '\u03a6', '\u0393', '\u0397', '\u0399',
            '\u03d1', '\u039a', '\u039b', '\u039c', '\u039d', '\u039f',
            '\u03a0', '\u0398', '\u03a1', '\u03a3', '\u03a4', '\u03a5', '\u03c2', '\u03a9', '\u039e', '\u03a8',
            '\u0396', '[', '\u2234', ']', '\u22a5', '_',
            '\u0305', '\u03b1', '\u03b2', '\u03c7', '\u03b4', '\u03b5', '\u03d5', '\u03b3', '\u03b7', '\u03b9',
            '\u03c6', '\u03ba', '\u03bb', '\u03bc', '\u03bd', '\u03bf',
            '\u03c0', '\u03b8', '\u03c1', '\u03c3', '\u03c4', '\u03c5', '\u03d6', '\u03c9', '\u03be', '\u03c8',
            '\u03b6', '{', '|', '}', '~', '\0',
            '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
            '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
            '\u20ac', '\u03d2', '\u2032', '\u2264', '\u2044', '\u221e', '\u0192', '\u2663', '\u2666', '\u2665',
            '\u2660', '\u2194', '\u2190', '\u2191', '\u2192', '\u2193',
            '\u00b0', '\u00b1', '\u2033', '\u2265', '\u00d7', '\u221d', '\u2202', '\u2022', '\u00f7', '\u2260',
            '\u2261', '\u2248', '\u2026', '\u2502', '\u2500', '\u21b5',
            '\u2135', '\u2111', '\u211c', '\u2118', '\u2297', '\u2295', '\u2205', '\u2229', '\u222a', '\u2283',
            '\u2287', '\u2284', '\u2282', '\u2286', '\u2208', '\u2209',
            '\u2220', '\u2207', '\u00ae', '\u00a9', '\u2122', '\u220f', '\u221a', '\u2022', '\u00ac', '\u2227',
            '\u2228', '\u21d4', '\u21d0', '\u21d1', '\u21d2', '\u21d3',
            '\u25ca', '\u2329', '\0', '\0', '\0', '\u2211', '\u239b', '\u239c', '\u239d', '\u23a1', '\u23a2', '\u23a3',
            '\u23a7', '\u23a8', '\u23a9', '\u23aa',
            '\0', '\u232a', '\u222b', '\u2320', '\u23ae', '\u2321', '\u239e', '\u239f', '\u23a0', '\u23a4', '\u23a5',
            '\u23a6', '\u23ab', '\u23ac', '\u23ad', '\0'
    };
    final static char[] DINGBATS_TABLE = {
            '\u0020', '\u2701', '\u2702', '\u2703', '\u2704', '\u260e', '\u2706', '\u2707', '\u2708', '\u2709',
            '\u261b', '\u261e', '\u270C', '\u270D', '\u270E', '\u270F',
            '\u2710', '\u2711', '\u2712', '\u2713', '\u2714', '\u2715', '\u2716', '\u2717', '\u2718', '\u2719',
            '\u271A', '\u271B', '\u271C', '\u271D', '\u271E', '\u271F',
            '\u2720', '\u2721', '\u2722', '\u2723', '\u2724', '\u2725', '\u2726', '\u2727', '\u2605', '\u2729',
            '\u272A', '\u272B', '\u272C', '\u272D', '\u272E', '\u272F',
            '\u2730', '\u2731', '\u2732', '\u2733', '\u2734', '\u2735', '\u2736', '\u2737', '\u2738', '\u2739',
            '\u273A', '\u273B', '\u273C', '\u273D', '\u273E', '\u273F',
            '\u2740', '\u2741', '\u2742', '\u2743', '\u2744', '\u2745', '\u2746', '\u2747', '\u2748', '\u2749',
            '\u274A', '\u274B', '\u25cf', '\u274D', '\u25a0', '\u274F',
            '\u2750', '\u2751', '\u2752', '\u25b2', '\u25bc', '\u25c6', '\u2756', '\u25d7', '\u2758', '\u2759',
            '\u275A', '\u275B', '\u275C', '\u275D', '\u275E', '\u0000',
            '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
            '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0',
            '\u0000', '\u2761', '\u2762', '\u2763', '\u2764', '\u2765', '\u2766', '\u2767', '\u2663', '\u2666',
            '\u2665', '\u2660', '\u2460', '\u2461', '\u2462', '\u2463',
            '\u2464', '\u2465', '\u2466', '\u2467', '\u2468', '\u2469', '\u2776', '\u2777', '\u2778', '\u2779',
            '\u277A', '\u277B', '\u277C', '\u277D', '\u277E', '\u277F',
            '\u2780', '\u2781', '\u2782', '\u2783', '\u2784', '\u2785', '\u2786', '\u2787', '\u2788', '\u2789',
            '\u278A', '\u278B', '\u278C', '\u278D', '\u278E', '\u278F',
            '\u2790', '\u2791', '\u2792', '\u2793', '\u2794', '\u2192', '\u2194', '\u2195', '\u2798', '\u2799',
            '\u279A', '\u279B', '\u279C', '\u279D', '\u279E', '\u279F',
            '\u27A0', '\u27A1', '\u27A2', '\u27A3', '\u27A4', '\u27A5', '\u27A6', '\u27A7', '\u27A8', '\u27A9',
            '\u27AA', '\u27AB', '\u27AC', '\u27AD', '\u27AE', '\u27AF',
            '\u0000', '\u27B1', '\u27B2', '\u27B3', '\u27B4', '\u27B5', '\u27B6', '\u27B7', '\u27B8', '\u27B9',
            '\u27BA', '\u27BB', '\u27BC', '\u27BD', '\u27BE', '\u0000'
    };

    /**
     * Fonts and encoding.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Encodings");
        try {
            // step 1
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            // step 2
            PdfWriter.getInstance(document, new FileOutputStream("encodingfont.pdf"));
            // step 3
            document.open();
            // step 4
            String[] all = {"Symbol", "ZapfDingbats"};
            Font hex = new Font(Font.HELVETICA, 5);
            for (int z = 0; z < all.length; ++z) {
                String file = all[z];
                document.add(new Paragraph("Unicode equivalence for the font \""
                        + file
                        + "\" with the encoding \""
                        + file
                        + "\"\n"));
                char[] tb;
                if (z == 0) {
                    tb = SYMBOL_TABLE;
                } else {
                    tb = DINGBATS_TABLE;
                }
                BaseFont bf;
                bf = BaseFont.createFont(file, file, true);
                Font f = new Font(bf, 12);
                PdfPTable table = new PdfPTable(16);
                table.setWidthPercentage(100);
                table.getDefaultCell().setBorderWidth(1);
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                for (char c : tb) {
                    if (bf.charExists(c)) {
                        Phrase ph = new Phrase(12, new String(new char[]{c}), f);
                        ph.add(new Phrase(12, "\n\n" + cst(c), hex));
                        table.addCell(ph);
                    } else {
                        Phrase ph = new Phrase("\u00a0");
                        ph.add(new Phrase(12, "\n\n" + cst(c), hex));
                        table.addCell(ph);
                    }
                }
                document.add(table);
                document.newPage();
            }
            // step 5
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    static String cst(char c) {
        if (c == 0) {
            return "\u00a0";
        }
        String s = Integer.toHexString(c);
        s = s.toUpperCase();
        s = "0000" + s;
        return s.substring(s.length() - 4);
    }
}
