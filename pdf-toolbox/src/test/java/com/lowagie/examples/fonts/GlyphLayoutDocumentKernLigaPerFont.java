
/*
 * GlyphLayoutDocumentDinSpec91379
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

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.LayoutProcessor;
import com.lowagie.text.pdf.PdfWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test kerning and ligatures per font
 */
public class GlyphLayoutDocumentKernLigaPerFont {

    public static String TEXT_INTRO =
            "Test of kerning and ligatures per font\n"
                    + "Using LayoutProcessor for glyph layout with Java built-in routines.\n\n";


    public static String TEST_TEXT =
            "AVATAR Vector TeX ff ffi ffl fi fl.";

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) throws Exception {
        test("GlyphLayoutDocumentKernLigaPerFont.pdf");
    }

    /**
     * Register and get a font, caching is switched off
     *
     * @param path     the path to a font file
     * @param alias    the alias you want to use for the font
     * @param fontSize the size of this font
     * @return the Font constructed based on the parameters
     */
    public static Font loadFont(String path, String alias, float fontSize) {
        FontFactory.register(path, alias);
        return FontFactory.getFont(alias, BaseFont.IDENTITY_H, true, fontSize, Font.UNDEFINED, null, false);
        // cached has to be set to 'false', to allow different attributes for instances of one font
    }

    /**
     * Run the test: Show kerning and ligatures
     *
     * @param fileName Name of output file
     * @throws Exception if an error occurs
     */
    public static void test(String fileName) throws Exception {

        // Enable the LayoutProcessor
        LayoutProcessor.enable();

        float fontSize = 12.0f;

        // The  OpenType fonts loaded with FontFactory.register() are
        // available for glyph layout.
        String fontDir = "com/lowagie/examples/fonts/";
        Font serifFont = loadFont(fontDir + "noto/NotoSerif-Regular.ttf", "serif", fontSize);

        Font serifKernLiga1 = loadFont(fontDir + "noto/NotoSerif-Regular.ttf", "serif_kern_liga", fontSize);
        // Switch ligatures and kerning on for one font
        LayoutProcessor.setLigatures(serifKernLiga1);
        LayoutProcessor.setKerning(serifKernLiga1);

        Font serifKern1 = loadFont(fontDir + "noto/NotoSerif-Regular.ttf", "serif_kern1", fontSize);
        // Switch on kerning for one font
        LayoutProcessor.setKerning(serifKern1);

        Font serifLiga1 = loadFont(fontDir + "noto/NotoSerif-Regular.ttf", "serif_liga1", fontSize);
        // Switch on ligatures for one font
        LayoutProcessor.setLigatures(serifLiga1);

        try (Document document = new Document()) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(TEXT_INTRO, serifFont));

            document.add(new Chunk(TEST_TEXT + " no kerning, no ligatures\n", serifFont));
            document.add(new Chunk(TEST_TEXT + " kerning, ligatures\n", serifKernLiga1));
            document.add(new Chunk(TEST_TEXT + " kerning, no ligatures\n", serifKern1));
            document.add(new Chunk(TEST_TEXT + " no kerning, ligatures\n", serifLiga1));
            document.add(new Chunk(TEST_TEXT + " no kerning, no ligatures\n\n", serifFont));

        }
        LayoutProcessor.disable();
    }
}
