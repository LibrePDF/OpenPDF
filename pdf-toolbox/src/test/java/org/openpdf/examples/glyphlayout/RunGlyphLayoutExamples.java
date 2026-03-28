/*
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Volker Kunert 2026
 */
package org.openpdf.examples.glyphlayout;


import java.io.IOException;
import org.openpdf.text.pdf.GlyphLayoutFontManager.FontLoadException;

/**
 * Calls all glyph layout examples using GlyphLayoutManager
 *
 */
public class RunGlyphLayoutExamples {

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) {

        try {
            GlyphLayoutBidi.test("GlyphLayoutBidi.pdf");
            GlyphLayoutBidiPerFont.test("GlyphLayoutBidiPerFont.pdf");
            GlyphLayoutBidiRotated.test("GlyphLayoutBidiRotated.pdf");
            GlyphLayoutDin91379.test("GlyphLayoutDin91379.pdf");
            GlyphLayoutFormDin91379.test("GlyphLayoutFormDin91379.pdf");
            GlyphLayoutInputStream.test("GlyphLayoutInputStream.pdf");
            GlyphLayoutKernLiga.test(" GlyphLayoutKernLiga.pdf");
            GlyphLayoutKernLigaPerFont.test("GlyphLayoutKernLigaPerFont.pdf");
            GlyphLayoutSMP.test("GlyphLayoutSMP.pdf");
            GlyphLayoutWithImage.test("GlyphLayoutWithImage.pdf");
        } catch (FontLoadException | IOException e) {
            System.err.println(e);
        }
    }
}
