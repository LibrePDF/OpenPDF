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
import org.openpdf.text.FontFactory;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.GlyphLayoutFontManager.FontLoadException;
import org.openpdf.text.pdf.GlyphLayoutManager;
import org.openpdf.text.pdf.PdfWriter;

/**
 * All fonts must be loaded with GlyphLayoutManager.loadFont(...) Otherwise execution will fail with en Exception
 * <p>
 * This is an example how the execution will fail when fonts are not loaded using GlyphLayoutManager.loadFont(...). The
 * program will throw an exception.
 */
public class GlyphLayoutFontNotLoadedThrowsException {

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) {
        try {
            test("GlyphLayoutFontNotLoadedThrowsException.pdf");
        } catch (FontLoadException | IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Test (failing of) execution when a font is not loaded using GlyphLayoutManager.loadFont(...)
     *
     * @param fileName Name of output file
     *
     * @throws FontLoadException if font can not be loaded
     * @throws IOException       if an IO error occurs
     */
    public static void test(String fileName) throws FontLoadException, IOException {

        float fontSize = 16.0f;

        GlyphLayoutManager glyphLayoutManager = new GlyphLayoutManager();
        // The OpenType fonts loaded with glyphLayoutManager.loadFont() are
        // available for glyph layout. Only these fonts can be used.

        // CORRECT: Always load fonts with glyphLayoutManager.loadFont(...)
        String fontDir = "org/openpdf/examples/fonts/"; // CORRECT
        Font font = glyphLayoutManager.loadFont(fontDir + "noto/NotoSans-Regular.ttf", fontSize); // CORRECT

        // WRONG, DOES NOT WORK: Load font only with OpenPdf FontFactory
        FontFactory.register(fontDir + "noto/NotoSerif-Regular.ttf", "serif");  // WRONG, DOES NOT WORK
        Font serif = FontFactory.getFont("serif", BaseFont.IDENTITY_H, fontSize); // WRONG, DOES NOT WORK

        // Process the document with glyphLayoutManager
        try (Document document = new Document().setGlyphLayoutManager(glyphLayoutManager)) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(20.0f);
            document.open();
            document.add(new Chunk("All Fonts must be loaded with GlyphLayoutManager.loadFont(...)", font));
            document.add(new Chunk("Otherwise execution will fail with an exception.", serif)); // An Exception is
            // thrown
        }
    }
}
