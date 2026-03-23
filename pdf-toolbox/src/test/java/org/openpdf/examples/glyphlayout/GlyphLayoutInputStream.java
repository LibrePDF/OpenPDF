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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.GlyphLayoutManager;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Prints all characters and sequences of DIN 91379 with correct glyph layout and kerning
 */
public class GlyphLayoutInputStream {

    public static final String TEXT_INTRO =
                    """
                    Test of GlyphLayoutManager loading the font from an input stream
                    """;

    public static final String LATIN_CHARS_DIN_91379_SEQUENCES =
                   """
                    bll; Latin Letters (normative)
                    ...
                    Sequences
                    A̋ C̀ C̄ C̆ C̈ C̕ C̣ C̦ C̨̆ D̂ F̀ F̄ G̀ H̄ H̦ H̱ J́ J̌ K̀ K̂ K̄ K̇ K̕ K̛ K̦ K͟H
                    K͟h L̂ L̥ L̥̄ L̦ M̀ M̂ M̆ M̐ N̂ N̄ N̆ N̦ P̀ P̄ P̕ P̣ R̆ R̥ R̥̄ S̀ S̄ S̛̄ S̱ T̀ T̄
                    T̈ T̕ T̛ U̇ Z̀ Z̄ Z̆ Z̈ Z̧ a̋ c̀ c̄ c̆ c̈ c̕ c̣ c̦ c̨̆ d̂ f̀ f̄ g̀ h̄ h̦ j́ k̀
                    k̂ k̄ k̇ k̕ k̛ k̦ k͟h l̂ l̥ l̥̄ l̦ m̀ m̂ m̆ m̐ n̂ n̄ n̆ n̦ p̀ p̄ p̕ p̣ r̆ r̥ r̥̄
                    s̀ s̄ s̛̄ s̱ t̀ t̄ t̕ t̛ u̇ z̀ z̄ z̆ z̈ z̧ Ç̆ Û̄ ç̆ û̄ ÿ́ Č̕ Č̣ č̕ č̣ ē̍ Ī́ ī́
                    ō̍ Ž̦ Ž̧ ž̦ ž̧ Ḳ̄ ḳ̄ Ṣ̄ ṣ̄ Ṭ̄ ṭ̄ Ạ̈ ạ̈ Ọ̈ ọ̈ Ụ̄ Ụ̈ ụ̄ ụ̈
                    """
            ;


  /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) throws Exception {
        test("GlyphLayoutInputStream.pdf");
    }


    /**
     * Run the test: Load the font from an input stream
     *
     * @param fileName   Name of output file
     */
    public static void test(String fileName) throws IOException {

        float fontSize = 12.0f;


        // The  OpenType fonts loaded with GlyphLayoutManager.loadFont() are
        // available for glyph layout.
        // Only these fonts can be used.
        GlyphLayoutManager glyphLayoutManager  = new GlyphLayoutManager();

        String fontDir = "org/openpdf/examples/fonts/";
        InputStream stream = BaseFont.getResourceStream(fontDir + "noto/NotoSans-Regular.ttf",
                GlyphLayoutInputStream.class.getClassLoader());
        // name has to end with ".ttf", otherwise the font is not loaded
        Font sansFont = glyphLayoutManager.loadFont("NotoSans-Regular.ttf", stream, fontSize);

        // Process the document with glyphLayoutManager
        try (Document document = new Document().setGlyphLayoutManager(glyphLayoutManager)) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(TEXT_INTRO, sansFont));
            document.add(new Chunk(LATIN_CHARS_DIN_91379_SEQUENCES, sansFont));
        }
    }
}
