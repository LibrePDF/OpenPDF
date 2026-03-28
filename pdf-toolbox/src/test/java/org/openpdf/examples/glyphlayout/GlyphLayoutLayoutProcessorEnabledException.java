
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
import org.openpdf.text.pdf.LayoutProcessor;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Processing fails if LayoutProcessor is enabled
 *
 * Do not enable the deprecated LayoutProcessor
 */
public class GlyphLayoutLayoutProcessorEnabledException {

    public static String INTRO_TEXT =
            """
                    Processing fails if LayoutProcessor is enabled
                    Do not enable the deprecated LayoutProcessor
                    """;

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) {
        try {
            test("GlyphLayoutLayoutProcessorEnabledException.pdf");
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
        LayoutProcessor.enable(); // Do not enable LayoutProcessor!
        GlyphLayoutManager glyphLayoutManager = new GlyphLayoutManager();
        String fontDir = "org/openpdf/examples/fonts/";
        Font sans = glyphLayoutManager.loadFont(fontDir + "noto/NotoSans-Regular.ttf", fontSize);

        // Process the document with glyphLayoutManager
        try (Document document = new Document().setGlyphLayoutManager(glyphLayoutManager)) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(INTRO_TEXT, sans));
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
