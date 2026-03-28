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
import org.openpdf.text.Image;
import org.openpdf.text.pdf.GlyphLayoutFontManager.FontLoadException;
import org.openpdf.text.pdf.GlyphLayoutManager;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Test of glyph layout of some characters and sequences of DIN 91379 with an image
 */
public class GlyphLayoutWithImage {

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) {
        try {
            test("GlyphLayoutWithImage.pdf");
        } catch (FontLoadException | IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Test of glyph layout of some characters and sequences of DIN 91379 with an image
     *
     * @param fileName Name of output file
     * @throws FontLoadException if font can not be loaded
     * @throws IOException       if an IO error occurs
     */
    public static void test(String fileName) throws FontLoadException, IOException {

        float fontSize = 16.0f;
        float fontSizeSmall = 10.0f;

        GlyphLayoutManager glyphLayoutManager = new GlyphLayoutManager();
        // The  OpenType fonts loaded with glyphLayoutManager.loadFont() are
        // available for glyph layout. Only these fonts can be used.
        String fontDir = "org/openpdf/examples/fonts/";
        Font font = glyphLayoutManager.loadFont(fontDir + "noto/NotoSans-Regular.ttf", fontSize);
        Font fontSmall = glyphLayoutManager.loadFont(fontDir + "noto/NotoSans-Regular.ttf", fontSizeSmall);

        // Process the document with glyphLayoutManager
        try (Document document = new Document().setGlyphLayoutManager(glyphLayoutManager)) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(20.0f);
            document.open();

            document.add(new Chunk("Te", font));
            document.add(new Chunk("xt\nwith NewLine\n", font));

            document.add(new Chunk("Test of several Chunks on one line: A", font));
            Image image = Image.getInstance(
                    "pdf-toolbox/src/test/resources/org/openpdf/examples/fonts/images/mushroom.png");
            image.scaleToFit(80f, 50f);
            document.add(new Chunk(image, 0.0f, 0.0f));
            document.add(new Chunk("A̋", font));
            document.add(new Chunk("C̀\nC̄C̆C̈", font));

            document.add(new Chunk("ab\nc", font));
            document.add(new Chunk("C̈C̕C̣C̦C̨̆", font));
            document.add(new Chunk(".\n", font));

            document.add(new Chunk("Ṣ̄ṣ̄Ṭ̄ṭ̄Ạ̈ạ̈Ọ̈ọ̈Ụ̄Ụ̈ụ̄ụ̈", fontSmall));
            document.add(new Chunk("xyz", fontSmall));
            document.add(new Chunk("j́S̛̄s̛̄K̛", font));
            document.add(new Chunk(".\n", font));
        }
    }
}
