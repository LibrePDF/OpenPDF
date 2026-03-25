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
            // Using fonts that have not been loaded with GlyphLayoutManager will throw exception
            GlyphLayoutFontNotLoadedThrowsException.test("GlyphLayoutFontNotLoadedThrowsException.pdf");
        } catch (Exception e) {
            System.err.println("Expected Exception:");
            e.printStackTrace();
        }
        try {
            // Type1 fonts are not supported
            GlyphLayoutType1FontThrowsException.test("GlyphLayoutType1FontThrowsException.pdf");
        } catch (Exception e) {
            System.err.println("Expected Exception:");
            e.printStackTrace();
        }
    }
}
