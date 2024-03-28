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

import com.lowagie.text.pdf.LayoutProcessor;
import com.lowagie.text.pdf.LayoutProcessor.Version;

/**
 * Calls glyph layout examples with current and deprecated version
 */
public class RunGlyphLayoutExamples {

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) throws Exception {
        //Version[] versions = new Version[]{Version.ONE, Version.TWO}; // Version.ONE is deprecated!
        Version[] versions = new Version[]{Version.TWO};

        for (Version version : versions) {
            LayoutProcessor.setVersion(version);
            GlyphLayoutDocumentBidi.test(String.format("GlyphLayoutDocumentBidi-%s.pdf", version));
            LayoutProcessor.setVersion(version);
            GlyphLayoutDocumentBidiPerFont.test(String.format("GlyphLayoutDocumentBidiPerFont-%s.pdf", version));
            LayoutProcessor.setVersion(version);
            GlyphLayoutDocumentDin91379.test(String.format("GlyphLayoutDocumentDin91379-%s.pdf", version));
            LayoutProcessor.setVersion(version);
            GlyphLayoutDocumentKernLiga.test(String.format(" GlyphLayoutDocumentKernLiga-%s.pdf", version));
            LayoutProcessor.setVersion(version);
            GlyphLayoutDocumentKernLigaPerFont.test(
                    String.format("GlyphLayoutDocumentKernLigaPerFont-%s.pdf", version));
            LayoutProcessor.setVersion(version);
            GlyphLayoutDocumentWithImage.test(String.format("GlyphLayoutDocumentWithImage-%s.pdf", version));
            LayoutProcessor.setVersion(version);
            GlyphLayoutFormDin91379.test(String.format("GlyphLayoutFormDin91379-%s.pdf", version));
        }
    }
}
