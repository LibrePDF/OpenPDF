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
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.LayoutProcessor;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Prints characters and sequences of DIN 91379 with correct glyph layout and kerning
 */
public class GlyphLayoutDocumentWithImage {


    /**
     * Register and get font
     *
     * @param path of font file
     * @param alias name
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
        test("GlyphLayoutDocumentWithImage.pdf");
    }


    /**
     * Run the test: Print the characters of DIN 91379 in a pdf document
     *
     * @param fileName Name of output file
     */
    public static void test(String fileName) throws IOException {

        // Enable the LayoutProcessor with kerning and ligatures
        LayoutProcessor.enableKernLiga();

        float fontSize = 16.0f;

        // The  OpenType fonts loaded with FontFactory.register() are
        // available for glyph layout.
        // Only these fonts can be used.
        String fontDir = "com/lowagie/examples/fonts/";
        Font font = loadFont(fontDir + "noto/NotoSans-Regular.ttf", "sans", fontSize);

        try (Document document = new Document()) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(20.0f);
            document.open();

            document.add(new Chunk("Te", font));
            document.add(new Chunk("xt\nwith NewLine\n", font));

            document.add(new Chunk("Test of several Chunks on one line: A", font));
            Image image = Image.getInstance("pdf-toolbox/src/test/resources/com/lowagie/examples/fonts/images/mushroom.png");
            image.scaleToFit(80f, 50f);
            document.add(new Chunk(image, 0.0f, 0.0f));
            document.add(new Chunk("A̋", font));
            document.add(new Chunk("C̀\nC̄C̆C̈", font));

            document.add(new Chunk("ab\nc", font));
            document.add(new Chunk("C̈C̕C̣C̦C̨̆", font));
            document.add(new Chunk(".\n", font));

            document.add(new Chunk("Ṣ̄ṣ̄Ṭ̄ṭ̄Ạ̈ạ̈Ọ̈ọ̈Ụ̄Ụ̈ụ̄ụ̈", font));
            document.add(new Chunk("xyz", font));
            document.add(new Chunk("j́S̛̄s̛̄K̛", font));
            document.add(new Chunk(".\n", font));
        }
        LayoutProcessor.disable();
    }
}
