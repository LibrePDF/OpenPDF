
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
 * Prints bidirectional text with correct glyph layout, kerning and ligatures globally enabled
 */
public class GlyphLayoutDocumentBidi {

    public static String INTRO_TEXT =
            "Test of bidirectional text\n" +
                    "Using LayoutProcessor for glyph layout with Java built-in routines.\n\n";

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) throws Exception {
        test("GlyphLayoutDocumentBidi.pdf");
    }

    /**
     * Run the test: Show bidirectional text
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

        FontFactory.register(fontDir + "noto/NotoSans-Regular.ttf", "notoSans");
        Font notoSans = FontFactory.getFont("notoSans", BaseFont.IDENTITY_H, true, fontSize);
        FontFactory.register(fontDir + "noto/NotoSansArabic-Regular.ttf", "notoSansArabic");
        Font notoSansArabic = FontFactory.getFont("notoSansArabic", BaseFont.IDENTITY_H, true, fontSize);

        try (Document document = new Document()) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(INTRO_TEXT + "Fonts: Noto Sans, Noto Sans Arabic\n\n", notoSans));

            document.add(new Chunk("Guten Tag ", notoSans));
            document.add(new Chunk("السلام عليكم", notoSansArabic));
            document.add(new Chunk(" Good afternoon", notoSans));
        }
        LayoutProcessor.disable();
    }
}
