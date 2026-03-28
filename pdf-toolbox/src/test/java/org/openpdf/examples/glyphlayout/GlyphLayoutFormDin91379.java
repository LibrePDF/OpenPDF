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


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.AcroFields;
import org.openpdf.text.pdf.GlyphLayoutFontManager.FontLoadException;
import org.openpdf.text.pdf.GlyphLayoutManager;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfStamper;

/**
 * Prints characters and sequences of DIN 91379 with correct glyph layout in a PDF form
 */
public class GlyphLayoutFormDin91379 {

    public static final String TEXT_INTRO = GlyphLayoutDin91379.TEXT_INTRO;
    public static final String LATIN_CHARS_DIN_91379 = GlyphLayoutDin91379.LATIN_CHARS_DIN_91379;
    public static final String LATIN_CHARS_DIN_91379_MATH = GlyphLayoutDin91379.LATIN_CHARS_DIN_91379_MATH;
    public static final String LATIN_CHARS_ADDITIONAL = GlyphLayoutDin91379.LATIN_CHARS_ADDITIONAL;

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) {
        try {
            test("GlyphLayoutFormDin91379.pdf");
        } catch (FontLoadException | IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Run the test: Print the characters of DIN 91379 in a PDF form
     *
     * @param fileName Name of output file
     * @throws FontLoadException in case of error while loading font
     * @throws IOException       if an IO-exception occurs
     */
    public static void test(String fileName) throws FontLoadException, IOException {
        String formPath = "org/openpdf/examples/fonts/form/PdfFormGlyphLayoutManager.pdf";
        String text = TEXT_INTRO + LATIN_CHARS_DIN_91379 + LATIN_CHARS_DIN_91379_MATH + LATIN_CHARS_ADDITIONAL;

        GlyphLayoutManager glyphLayoutManager = new GlyphLayoutManager();

        try (InputStream acroFormInputStream = GlyphLayoutFormDin91379.class.getClassLoader()
                .getResourceAsStream(formPath);
                FileOutputStream outputStream = new FileOutputStream(fileName);
                PdfReader reader = new PdfReader(acroFormInputStream)
        ) {
            float fontSize = 10f;
            String fontDir = "org/openpdf/examples/fonts/";
            Font sansFont = glyphLayoutManager.loadFont(fontDir + "noto/NotoSans-Regular.ttf", fontSize);
            Font mathFont = glyphLayoutManager.loadFont(fontDir + "noto/NotoSansMath-Regular.ttf", fontSize);

            // Process the PDF file with glyphLayoutManager
            PdfStamper stamper = new PdfStamper(reader, outputStream).setGlyphLayoutManager(glyphLayoutManager);
            final AcroFields fields = stamper.getAcroFields();
            fields.addSubstitutionFont(mathFont.getBaseFont());

            Map<String, AcroFields.Item> allFields = fields.getAllFields();

            for (final String fieldName : allFields.keySet()) {
                fields.setFieldProperty(fieldName, "textfont", sansFont.getBaseFont(), null);
                fields.setFieldProperty(fieldName, "textsize", fontSize, null);
                fields.setField(fieldName, text);
            }

            stamper.setFormFlattening(true);
            stamper.setFullCompression();
            stamper.close();
        }
    }
}
