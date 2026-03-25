
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
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.GlyphLayoutFontManager.FontLoadException;
import org.openpdf.text.pdf.GlyphLayoutManager;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Prints bidirectional text on a rotated page
 */
public class GlyphLayoutBidiRotated {

    public static String INTRO_TEXT =
            """
                    Test for bidirectional text and rotated page
                    Using GlyphLayoutManager for glyph layout with Java built-in routines.
                    
                    """;


    public static String TEST_TEXT =
            "A̋ C̀ C̄ C̆ C̈ C̕ C̣ C̦ C̨̆ \n\n";

    public static String TEST_TEXT2 = "نحن الآن في شهر رمضان 1447 هجري";

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) throws Exception {
        try {
            test("GlyphLayoutBidiRotated.pdf");
        } catch (FontLoadException | IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Run the test: Show kerning and ligatures
     *
     * @param fileName Name of output file
     * @throws Exception if an error occurs
     */
    public static void test(String fileName) throws FontLoadException, IOException {

        float fontSize = 12.0f;
        GlyphLayoutManager glyphLayoutManager = new GlyphLayoutManager();
        // The  OpenType fonts loaded with glyphLayoutManager.loadFont() are
        // available for glyph layout. Only these fonts can be used.
        String fontDir = "org/openpdf/examples/fonts/";

        Font serif = glyphLayoutManager.loadFont(fontDir + "noto/NotoSerif-Regular.ttf", fontSize);
        Font sansArabic = glyphLayoutManager.loadFont(fontDir + "noto/NotoSansArabic-Regular.ttf", fontSize);

        // Process the rotated document with glyphLayoutManager
        try (Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10)
                .setGlyphLayoutManager(glyphLayoutManager)) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(INTRO_TEXT, serif));
            document.add(new Chunk(TEST_TEXT, serif));
            document.add(new Chunk(TEST_TEXT2, sansArabic));
        }
    }
}
