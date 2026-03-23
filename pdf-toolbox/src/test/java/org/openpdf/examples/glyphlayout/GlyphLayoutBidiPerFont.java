
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
 * Prints bidirectional text with correct glyph layout, run direction can be chosen
 * per font
 */
public class GlyphLayoutBidiPerFont {

    public static String INTRO_TEXT =
            """
            Test of bidirectional text.
            In this example the run direction is set per font.
            This should only be necessary in rare occasions - normally the direction is
            recognized automatically. See also GlyphLayoutBidi and GlyphLayoutBidiRotated
 
            Using GlyphLayoutManager for glyph layout with Java built-in routines.
            """;

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) throws Exception {
        test("GlyphLayoutBidiPerFont.pdf");
    }

    /**
     * Run the test: Show bidirectional text.
     * In most cases it is not necessary to set the direction explicitly
     * See GlyphLayoutBidi and GlyphLayoutBidiRotated
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
        Font sans = glyphLayoutManager.loadFont(fontDir + "noto/NotoSans-Regular.ttf", fontSize);
        Font sansRtl = glyphLayoutManager.loadFont(fontDir + "noto/NotoSans-Regular.ttf", fontSize,
                new FontOptions().setRunDirectionRtl());

        // Process the document with glyphLayoutManager
        try (Document document = new Document().setGlyphLayoutManager(glyphLayoutManager)) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(INTRO_TEXT + "Font: Noto Sans\n\n", sans));
            document.add(new Chunk(" Hello", sansRtl));
            document.add(new Chunk(" = Hello backwards", sans));
        }
    }
}