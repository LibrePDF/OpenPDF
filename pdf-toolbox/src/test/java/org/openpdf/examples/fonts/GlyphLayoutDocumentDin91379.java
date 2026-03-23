/*
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.openpdf.examples.fonts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.LayoutProcessor;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Prints characters and sequences of DIN 91379 with correct glyph layout and kerning
 *
 * @deprecated use GlyphLayountManager
 */
@Deprecated
public class GlyphLayoutDocumentDin91379 {

    public static String TEXT_INTRO =
            "Test of formatting for letters and sequences defined in:\n"
                    + "DIN 91379:2022-08: Characters and defined character sequences in Unicode for the\n"
                    + "electronic processing of names and data exchange in Europe, with CD-ROM.\n"
                    + "See https://github.com/String-Latin/DIN-91379-Characters-and-Sequences\n"
                    + "and https://en.wikipedia.org/wiki/DIN_91379\n\n"
                    + "Fonts used: Noto Sans Regular, Noto Sans Math Regular, Noto Serif Regular\n"
                    + "See https://fonts.google.com/noto/specimen/Noto+Sans\n"
                    + "and https://github.com/googlefonts/noto-fonts/tree/main/hinted/ttf\n"
                    + "Using LayoutProcessor for glyph layout with Java built-in routines.\n\n";

    public static String LATIN_CHARS_DIN_91379 =
            "bll; Latin Letters (normative)\n"
                    + "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z \n"
                    + "a b c d e f g h i j k l m n o p q r s t u v w x y z \n"
                    + "À Á Â Ã Ä Å Æ Ç È É Ê Ë Ì Í Î Ï Ð Ñ Ò Ó Ô Õ Ö Ø Ù Ú \n"
                    + "Û Ü Ý Þ ß à á â ã ä å æ ç è é ê ë ì í î ï ð ñ ò ó ô \n"
                    + "õ ö ø ù ú û ü ý þ ÿ Ā ā Ă ă Ą ą Ć ć Ĉ ĉ Ċ ċ Č č Ď ď \n"
                    + "Đ đ Ē ē Ĕ ĕ Ė ė Ę ę Ě ě Ĝ ĝ Ğ ğ Ġ ġ Ģ ģ Ĥ ĥ Ħ ħ Ĩ ĩ \n"
                    + "Ī ī Ĭ ĭ Į į İ ı Ĳ ĳ Ĵ ĵ Ķ ķ ĸ Ĺ ĺ Ļ ļ Ľ ľ Ŀ ŀ Ł ł Ń \n"
                    + "ń Ņ ņ Ň ň ŉ Ŋ ŋ Ō ō Ŏ ŏ Ő ő Œ œ Ŕ ŕ Ŗ ŗ Ř ř Ś ś Ŝ ŝ \n"
                    + "Ş ş Š š Ţ ţ Ť ť Ŧ ŧ Ũ ũ Ū ū Ŭ ŭ Ů ů Ű ű Ų ų Ŵ ŵ Ŷ ŷ \n"
                    + "Ÿ Ź ź Ż ż Ž ž Ƈ ƈ Ə Ɨ Ơ ơ Ư ư Ʒ Ǎ ǎ Ǐ ǐ Ǒ ǒ Ǔ ǔ Ǖ ǖ \n"
                    + "Ǘ ǘ Ǚ ǚ Ǜ ǜ Ǟ ǟ Ǣ ǣ Ǥ ǥ Ǧ ǧ Ǩ ǩ Ǫ ǫ Ǭ ǭ Ǯ ǯ ǰ Ǵ ǵ Ǹ \n"
                    + "ǹ Ǻ ǻ Ǽ ǽ Ǿ ǿ Ȓ ȓ Ș ș Ț ț Ȟ ȟ ȧ Ȩ ȩ Ȫ ȫ Ȭ ȭ Ȯ ȯ Ȱ ȱ \n"
                    + "Ȳ ȳ ə ɨ ʒ Ḃ ḃ Ḇ ḇ Ḋ ḋ Ḍ ḍ Ḏ ḏ Ḑ ḑ ḗ Ḝ ḝ Ḟ ḟ Ḡ ḡ Ḣ ḣ \n"
                    + "Ḥ ḥ Ḧ ḧ Ḩ ḩ Ḫ ḫ ḯ Ḱ ḱ Ḳ ḳ Ḵ ḵ Ḷ ḷ Ḻ ḻ Ṁ ṁ Ṃ ṃ Ṅ ṅ Ṇ \n"
                    + "ṇ Ṉ ṉ Ṓ ṓ Ṕ ṕ Ṗ ṗ Ṙ ṙ Ṛ ṛ Ṟ ṟ Ṡ ṡ Ṣ ṣ Ṫ ṫ Ṭ ṭ Ṯ ṯ Ẁ \n"
                    + "ẁ Ẃ ẃ Ẅ ẅ Ẇ ẇ Ẍ ẍ Ẏ ẏ Ẑ ẑ Ẓ ẓ Ẕ ẕ ẖ ẗ ẞ Ạ ạ Ả ả Ấ ấ \n"
                    + "Ầ ầ Ẩ ẩ Ẫ ẫ Ậ ậ Ắ ắ Ằ ằ Ẳ ẳ Ẵ ẵ Ặ ặ Ẹ ẹ Ẻ ẻ Ẽ ẽ Ế ế \n"
                    + "Ề ề Ể ể Ễ ễ Ệ ệ Ỉ ỉ Ị ị Ọ ọ Ỏ ỏ Ố ố Ồ ồ Ổ ổ Ỗ ỗ Ộ ộ \n"
                    + "Ớ ớ Ờ ờ Ở ở Ỡ ỡ Ợ ợ Ụ ụ Ủ ủ Ứ ứ Ừ ừ Ử ử Ữ ữ Ự ự Ỳ ỳ \n"
                    + "Ỵ ỵ Ỷ ỷ Ỹ ỹ \n"
                    + "Sequences\n"
                    + "A̋ C̀ C̄ C̆ C̈ C̕ C̣ C̦ C̨̆ D̂ F̀ F̄ G̀ H̄ H̦ H̱ J́ J̌ K̀ K̂ K̄ K̇ K̕ K̛ K̦ K͟H \n"
                    + "K͟h L̂ L̥ L̥̄ L̦ M̀ M̂ M̆ M̐ N̂ N̄ N̆ N̦ P̀ P̄ P̕ P̣ R̆ R̥ R̥̄ S̀ S̄ S̛̄ S̱ T̀ T̄ \n"
                    + "T̈ T̕ T̛ U̇ Z̀ Z̄ Z̆ Z̈ Z̧ a̋ c̀ c̄ c̆ c̈ c̕ c̣ c̦ c̨̆ d̂ f̀ f̄ g̀ h̄ h̦ j́ k̀ \n"
                    + "k̂ k̄ k̇ k̕ k̛ k̦ k͟h l̂ l̥ l̥̄ l̦ m̀ m̂ m̆ m̐ n̂ n̄ n̆ n̦ p̀ p̄ p̕ p̣ r̆ r̥ r̥̄ \n"
                    + "s̀ s̄ s̛̄ s̱ t̀ t̄ t̕ t̛ u̇ z̀ z̄ z̆ z̈ z̧ Ç̆ Û̄ ç̆ û̄ ÿ́ Č̕ Č̣ č̕ č̣ ē̍ Ī́ ī́ \n"
                    + "ō̍ Ž̦ Ž̧ ž̦ ž̧ Ḳ̄ ḳ̄ Ṣ̄ ṣ̄ Ṭ̄ ṭ̄ Ạ̈ ạ̈ Ọ̈ ọ̈ Ụ̄ Ụ̈ ụ̄ ụ̈ \n"
                    + "bnlreq; Non-Letters N1 (normative)\n"
                    + "  ' , - . ` ~ ¨ ´ · ʹ ʺ ʾ ʿ ˈ ˌ ’ ‡ \n"
                    + "bnl; Non-Letters N2 (normative)\n"
                    + "! \" # $ % & ( ) * + / 0 1 2 3 4 5 6 7 8 9 : ; < = > \n"
                    + "? @ [ \\ ] ^ _ { | } ¡ ¢ £ ¥ § © ª « ¬ ® ¯ ° ± ² ³ µ \n"
                    + "¶ ¹ º » ¿ × ÷ € \n"
                    + "bnlopt; Non-Letters N3 (normative)\n"
                    + "¤ ¦ ¸ ¼ ½ ¾ \n"
                    + "bnlnot; Non-Letters N4 (normative)\n"
                    + "-omitted-\n"
                    + "dc; Combining diacritics (normative)\n"
                    + "-omitted-\n"
                    + "gl; Greek Letters (extended)\n"
                    + "Ά Έ Ή Ί Ό Ύ Ώ ΐ Α Β Γ Δ Ε Ζ Η Θ Ι Κ Λ Μ Ν Ξ Ο Π Ρ Σ \n"
                    + "Τ Υ Φ Χ Ψ Ω Ϊ Ϋ ά έ ή ί ΰ α β γ δ ε ζ η θ ι κ λ μ ν \n"
                    + "ξ ο π ρ ς σ τ υ φ χ ψ ω ϊ ϋ ό ύ ώ \n"
                    + "cl; Cyrillic Letters (extended)\n"
                    + "Ѝ А Б В Г Д Е Ж З И Й К Л М Н О П Р С Т У Ф Х Ц Ч Ш \n"
                    + "Щ Ъ Ь Ю Я а б в г д е ж з и й к л м н о п р с т у ф \n"
                    + "х ц ч ш щ ъ ь ю я ѝ \n"
                    + "enl; Non-Letters E1 (extended)\n"
                    + "ƒ ʰ ʳ ˆ ˜ ˢ ᵈ ᵗ ‘ ‚ “ ” „ † … ‰ ′ ″ ‹ › ⁰ ⁴ ⁵ ⁶ ⁷ ⁸ \n"
                    + "⁹ ⁿ ₀ ₁ ₂ ₃ ₄ ₅ ₆ ₇ ₈ ₉ ™ \n\n";

    public static String LATIN_CHARS_DIN_91379_MATH =
            "enl; Non Letters E1 (extended) math\n"
                    + "∞ ≤ ≥ \n";

    public static String LATIN_CHARS_ADDITIONAL =
            "Additional non-letters (not included in DIN 91379)\n"
                    + "– — •�\n\n";


    /**
     * Register and get font
     *
     * @param path     of font file
     * @param alias    name
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
        test("GlyphLayoutDocumentDin91379.pdf");
    }


    /**
     * Run the test: Print the characters of DIN 91379 in a pdf document
     *
     * @param fileName   Name of output file
     */
    public static void test(String fileName) throws IOException {

        // Enable the LayoutProcessor with kerning and ligatures
        LayoutProcessor.enableKernLiga();

        float fontSize = 12.0f;

        // The  OpenType fonts loaded with FontFactory.register() are
        // available for glyph layout.
        // Only these fonts can be used.
        String fontDir = "org/openpdf/examples/fonts/";
        Font sansFont = loadFont(fontDir + "noto/NotoSans-Regular.ttf", "sans", fontSize);
        String sansFontName = sansFont.getBaseFont().getPostscriptFontName();
        Font mathFont = loadFont(fontDir + "noto/NotoSansMath-Regular.ttf", "math", fontSize);
        String mathFontName = mathFont.getBaseFont().getPostscriptFontName();

        try (Document document = new Document()) {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(fileName)));
            writer.setInitialLeading(16.0f);
            document.open();

            document.add(new Chunk(TEXT_INTRO, sansFont));

            document.add(new Chunk(sansFontName + "\n" + LATIN_CHARS_DIN_91379, sansFont));
            document.add(new Chunk(mathFontName + "\n" + LATIN_CHARS_DIN_91379_MATH, mathFont));
            document.add(new Chunk(sansFontName + "\n" + LATIN_CHARS_ADDITIONAL, sansFont));
        }
        LayoutProcessor.disable();
    }
}
