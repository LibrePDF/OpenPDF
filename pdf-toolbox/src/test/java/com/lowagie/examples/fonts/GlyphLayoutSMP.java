/*
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.lowagie.examples.fonts;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.LayoutProcessor;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test of letters from the supplementary multilingual plane
 *
 * @see <a href="https://unicode.org/charts/PDF/U1D400.pdf">Mathematical Alphanumeric Symbols</a>
 */
public class GlyphLayoutSMP {

    private static final String TEXT_INTRO =
            "Test of Letters from the Supplementary Multilingual Plane\n\n" + "Mathematical Alphanumeric Symbols\n";

    private static final int[] MATHEMATICAL_CODEPOINTS = new int[]{0x1D504, 0x1D505, 0x212D, 0x1D507, 0x1D508, 0x1D509,
            0x1D50A, 0x210C, 0x2111, 0x1D50D, 0x1D50E, 0x1D50F, 0x1D510, 0x1D511, 0x1D512, 0x1D513, 0x1D514, 0x211C,
            0x1D516, 0x1D517, 0x1D518, 0x1D519, 0x1D51A, 0x1D51B, 0x1D51C, 0x2128, 0x0A, 0x1D51E, 0x1D51F, 0x1D520,
            0x1D521, 0x1D522, 0x1D523, 0x1D524, 0x1D525, 0x1D526, 0x1D527, 0x1D528, 0x1D529, 0x1D52A, 0x1D52B, 0x1D52C,
            0x1D52D, 0x1D52E, 0x1D52F, 0x1D530, 0x1D531, 0x1D532, 0x1D533, 0x1D534, 0x1D535, 0x1D536, 0x1D537,
            0x0A, 0x0A,
            0x1D7D8, 0x1D7D9, 0x1D7DA, 0x1D7DB, 0x1D7DC, 0x1D7DD, 0x1D7DE, 0x1D7DF, 0x1D7E0, 0x1D7E1,
            0x0A, 0x0A,
            0x1D49C, 0x212C, 0x1D49E, 0x1D49F, 0x2130, 0x2131, 0x1D4A2, 0x210B, 0x2110, 0x1D4A5, 0x1D4A6, 0x2112,
            0x2133, 0x1D4A9, 0x1D4AA, 0x1D4AB, 0x1D4AC, 0x211B, 0x1D4AE, 0x1D4AF, 0x1D4B0, 0x1D4B1, 0x1D4B2, 0x1D4B3,
            0x1D4B4, 0x1D4B5,
            0x0A,
            0x1D4B6, 0x1D4B7, 0x1D4B8, 0x1D4B9, 0x212F, 0x1D4BB, 0x210A, 0x1D4BD, 0x1D4BE,
            0x1D4BF, 0x1D4C0, 0x1D4C1, 0x1D4C2, 0x1D4C3, 0x2134, 0x1D4C5, 0x1D4C6, 0x1D4C7, 0x1D4C8, 0x1D4C9, 0x1D4CA,
            0x1D4CB, 0x1D4CC, 0x1D4CD, 0x1D4CE, 0x1D4CF,
            0x0A, 0x0A};

    private static final String MATHEMATICAL = new String(MATHEMATICAL_CODEPOINTS, 0, MATHEMATICAL_CODEPOINTS.length);

    private static final String SMP_AND_GLYPH_LAYOUT =
            "A̋" + new String(new int[]{0x1F67C}, 0, 1) + "C̀" + new String(new int[]{0x1F67D}, 0, 1);

    /**
     * Register and get font
     *
     * @param path     of font file
     * @param alias    name
     * @param fontSize size of font
     * @return the loaded font
     */
    private static Font loadFont(String path, String alias, float fontSize) {
        FontFactory.register(path, alias);
        return FontFactory.getFont(alias, BaseFont.IDENTITY_H, fontSize);
    }


    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) throws Exception {

        test("TestSMP.pdf");

        // Enable the LayoutProcessor with kerning and ligatures
        LayoutProcessor.enableKernLiga();
        test("GlyphLayoutSMP.pdf");
        LayoutProcessor.disable();
    }


    /**
     * Run the test: Print some characters of the supplementary multilingual plane
     *
     * @param fileName Name of output file
     */
    public static void test(String fileName) throws IOException {

        float fontSize = 12.0f;

        String fontDir = "com/lowagie/examples/fonts/";
        Font sansFont = loadFont(fontDir + "noto/NotoSans-Regular.ttf", "sans", fontSize);
        Font sansMonoFont = loadFont(fontDir + "noto/NotoSansMono-Regular.ttf", "sans", fontSize);
        String sansMonoFontName = sansMonoFont.getBaseFont().getPostscriptFontName();
        Font mathFont = loadFont(fontDir + "noto/NotoSansMath-Regular.ttf", "math", fontSize);
        String mathFontName = mathFont.getBaseFont().getPostscriptFontName();

        try (Document document = new Document()) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(TEXT_INTRO, sansFont));
            document.add(new Chunk("Font used: " + mathFontName + "\n\n", sansFont));
            document.add(new Chunk(MATHEMATICAL, mathFont));
            document.add(new Chunk("Example with glyph layout and SMP\n", sansFont));
            document.add(new Chunk("Font used: " + sansMonoFontName + "\n\n" + SMP_AND_GLYPH_LAYOUT, sansMonoFont));
        }
    }
}
