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
 * Test of exception while trying to load a font from an input stream that is null
 */
public class GlyphLayoutInputStreamNullThrowsException {

    public static final String TEXT_INTRO =
            """
                    Test of exception while trying to load a font from an input stream that is null
                    """;

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) {
        try {
            test("GlyphLayoutInputStreamNullThrowsException.pdf");
        } catch (FontLoadException | IOException e) {
            System.err.println(e);
        }
    }


    /**
     * Run the test
     *
     * @param fileName Name of output file
     * @throws FontLoadException if font can not be loaded
     * @throws IOException       if an IO error occurs
     */
    public static void test(String fileName) throws FontLoadException, IOException {

        float fontSize = 12.0f;

        // The  OpenType fonts loaded with GlyphLayoutManager.loadFont() are
        // available for glyph layout.
        // Only these fonts can be used.
        GlyphLayoutManager glyphLayoutManager = new GlyphLayoutManager();

        // When loading a null stream a NullPointerException is thrown
        Font sansFont = glyphLayoutManager.loadFont("Null.ttf", null, fontSize);

        // Process the document with glyphLayoutManager
        try (Document document = new Document().setGlyphLayoutManager(glyphLayoutManager)) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(TEXT_INTRO, sansFont));
        }
    }
}
