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


import org.openpdf.text.pdf.LayoutProcessor;

/**
 * Calls the glyph layout examples that are expected to fail
 *
 */
public class RunGlyphLayoutExamplesFailing {

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) {

        try {
            // Exception while loading font
            GlyphLayoutFontLoadException.test("GlyphLayoutFontLoadException.pdf");
        } catch (Exception e) {
            System.err.println("Expected Exception: "+e);
        }

        try {
            // Using fonts that have not been loaded with GlyphLayoutManager will throw exception
            GlyphLayoutFontNotLoadedThrowsException.test("GlyphLayoutFontNotLoadedThrowsException.pdf");
        } catch (Exception e) {
            System.err.println("Expected Exception: "+e);
        }

        try {
            // Exception while loading font
            GlyphLayoutLayoutProcessorEnabledException.test("GlyphLayoutLayoutProcessorEnabledException.pdf");
        } catch (Exception e) {
            System.err.println("Expected Exception: "+e);
            LayoutProcessor.disable();
        }

        try {
            // Type1 fonts are not supported
            GlyphLayoutType1FontThrowsException.test("GlyphLayoutType1FontThrowsException.pdf");
        } catch (Exception e) {
            System.err.println("Expected Exception: "+e);
        }
    }
}
