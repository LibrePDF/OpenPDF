
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

import java.nio.file.Files;
import java.nio.file.Paths;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.GlyphLayoutFontManager.FontOptions;
import org.openpdf.text.pdf.GlyphLayoutManager;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Prints text with correct glyph layout, kerning and ligatures globally enabled
 */
public class GlyphLayoutKernLigaPerFont {

    public static String INTRO_TEXT =
                    """
                    Test kerning and ligatures per font
                    
                    Using GlyphLayoutManager for glyph layout with Java built-in routines.
                    """;


    public static String TEST_TEXT =
            "AVATAR Vector TeX ff ffi ffl fi fl.";

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) throws Exception {
        test("GlyphLayoutKernLigaPerFont.pdf");
    }

    /**
     * Run the test: Show kerning and ligatures
     *
     * @param fileName Name of output file
     * @throws Exception if an error occurs
     */
    public static void test(String fileName) throws Exception {

        float fontSize = 12.0f;
        GlyphLayoutManager glyphLayoutManager  = new GlyphLayoutManager();
        // The  OpenType fonts loaded with glyphLayoutManager.loadFont() are
        // available for glyph layout. Only these fonts can be used.
        String fontDir = "org/openpdf/examples/fonts/";

        Font serif = glyphLayoutManager.loadFont(fontDir + "noto/NotoSerif-Regular.ttf",
                fontSize);
        Font serifKerning = glyphLayoutManager.loadFont(fontDir + "noto/NotoSerif-Regular.ttf",
                fontSize, new FontOptions().setKerningOn());
        Font serifLigatures = glyphLayoutManager.loadFont(fontDir + "noto/NotoSerif-Regular.ttf",
                fontSize, new FontOptions().setLigaturesOn());
        Font serifKerningLigatures = glyphLayoutManager.loadFont(fontDir + "noto/NotoSerif-Regular.ttf",
                fontSize, new FontOptions().setKerningOn().setLigaturesOn());

        // Process the document with glyphLayoutManager
        try (Document document = new Document().setGlyphLayoutManager(glyphLayoutManager)) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(INTRO_TEXT + "Font: Noto Serif Regular\n\n", serif));
            document.add(new Chunk(TEST_TEXT + " Default\n", serif));
            document.add(new Chunk(TEST_TEXT + " Kerning\n", serifKerning));
            document.add(new Chunk(TEST_TEXT + " Ligatures\n", serifLigatures));
            document.add(new Chunk(TEST_TEXT + " Kerning and ligatures\n", serifKerningLigatures));
        }
    }
}
