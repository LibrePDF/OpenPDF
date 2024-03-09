
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
 * Prints bidirectional text with correct glyph layout, kerning and ligatures globally enabled Direction can be chosen
 * per font
 */
public class GlyphLayoutDocumentBidiPerFont {

    public static String INTRO_TEXT =
            "Test of bidirectional text\n" +
                    "Using LayoutProcessor for glyph layout with Java built-in routines.\n\n";

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) throws Exception {
        test("GlyphLayoutDocumentBidiPerFont.pdf");
    }

    /**
     * Register and get a font, caching is switched off
     *
     * @param path     the path to a font file
     * @param alias    the alias you want to use for the font
     * @param fontSize the size of this font
     * @return the Font constructed based on the parameters
     */
    public static Font getFont(String path, String alias, float fontSize) {
        FontFactory.register(path, alias);
        return FontFactory.getFont(alias, BaseFont.IDENTITY_H, true, fontSize, Font.UNDEFINED, null, false);
        // cached has to be set to 'false', to allow different attributes for instances of one font
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

        Font notoSans = getFont(fontDir + "noto/NotoSans-Regular.ttf", "notoSans", fontSize);
        LayoutProcessor.setRunDirectionLtr(notoSans);
        Font notoSansArabic = getFont(fontDir + "noto/NotoSansArabic-Regular.ttf", "notoSansArabic", fontSize);
        LayoutProcessor.setRunDirectionRtl(notoSansArabic);

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
