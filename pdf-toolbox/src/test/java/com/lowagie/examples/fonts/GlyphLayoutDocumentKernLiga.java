
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
 * Prints text with correct glyph layout, kerning and ligatures globally enabled
 */
public class GlyphLayoutDocumentKernLiga {

    public static String INTRO_TEXT =
            "Test of text attributes for kerning and ligatures\n"
                    + "Using LayoutProcessor for glyph layout with Java built-in routines.\n\n";


    public static String TEST_TEXT =
            "AVATAR Vector TeX ff ffi ffl fi fl.\n\n";

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) throws Exception {
        test("GlyphLayoutDocumentKernLiga.pdf");
    }

    /**
     * Run the test: Show kerning and ligatures
     *
     * @param fileName Name of output file
     * @throws Exception if an error occurs
     */
    public static void test(String fileName) throws Exception {

        // Enable the LayoutProcessor with kerning and ligatures
        LayoutProcessor.enableKernLiga();

        float fontSize = 12.0f;

        // The  OpenType fonts loaded with FontFactory.register() are
        // available for glyph layout.
        String fontDir = "com/lowagie/examples/fonts/";

        FontFactory.register(fontDir + "noto/NotoSerif-Regular.ttf", "notoSerif");
        Font notoSerif = FontFactory.getFont("notoSerif", BaseFont.IDENTITY_H, true, fontSize);

        try (Document document = new Document()) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(INTRO_TEXT + "Font: Noto Serif Regular\n\n", notoSerif));
            document.add(new Chunk(TEST_TEXT, notoSerif));
        }
        LayoutProcessor.disable();
    }
}
