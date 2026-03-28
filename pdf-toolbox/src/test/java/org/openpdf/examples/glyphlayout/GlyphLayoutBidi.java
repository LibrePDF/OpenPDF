
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
import java.nio.file.Files;
import java.nio.file.Paths;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.GlyphLayoutFontManager.FontLoadException;
import org.openpdf.text.pdf.GlyphLayoutManager;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Prints bidirectional text with correct glyph layout, kerning and ligatures globally enabled
 */
public class GlyphLayoutBidi {

    public static String INTRO_TEXT =
            """
                    Test of bidirectional text
                    Using GlyphLayoutManager for glyph layout with Java built-in routines.
                    """;

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) {
        try {
            test("GlyphLayoutBidi.pdf");
        } catch (FontLoadException e) {
            System.err.println(e);
        }
    }

    /**
     * Run the test: Show bidirectional text
     *
     * @param fileName Name of output file
     *
     * @throws FontLoadException if font can not be loaded
     */
    public static void test(String fileName) throws FontLoadException {
        float fontSize = 12.0f;
        GlyphLayoutManager glyphLayoutManager = new GlyphLayoutManager();
        // The  OpenType fonts loaded with glyphLayoutManager.loadFont() are
        // available for glyph layout. Only these fonts can be used.
        String fontDir = "org/openpdf/examples/fonts/";
        Font sans = glyphLayoutManager.loadFont(fontDir + "noto/NotoSans-Regular.ttf", fontSize);
        Font sansArabic = glyphLayoutManager.loadFont(fontDir + "noto/NotoSansArabic-Regular.ttf",
                fontSize);

        // Process the document with glyphLayoutManager
        try (Document document = new Document().setGlyphLayoutManager(glyphLayoutManager)) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(INTRO_TEXT + "Fonts: Noto Sans, Noto Sans Arabic\n\n", sans));

            document.add(new Chunk("Guten Tag ", sans));
            document.add(new Chunk("السلام عليكم", sansArabic));
            document.add(new Chunk(" Good afternoon", sans));
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
