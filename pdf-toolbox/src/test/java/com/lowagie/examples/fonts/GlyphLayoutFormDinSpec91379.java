/*
 * GlyphLayoutFormDinSpec91379
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


import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.LayoutProcessor;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

/**
 * Prints characters and sequences of DIN SPEC 91379
 * with correct glyph layout
 */
public class GlyphLayoutFormDinSpec91379 {

    public static String LATIN_CHARS_DIN_SPEC_91379 = 
            "Test of   formatting for Sequences defined in:\n"
            + "DIN SPEC 91379: Characters in   Unicode for the electronic processing of names and data\n"
            + "exchange in   Europe; with digital attachment\n"
            + "  https://www.xoev.de/downloads-2316#StringLatin\n"
            + "  https://www.din.de/de/wdc-beuth:din21:301228458\n"
            + "Fonts used: NotoSans, NotoSansMath, see https://github.com/googlefonts/noto-fonts/tree/master/hinted/ttf\n"
            + "\n"
            + "Formatting with Java builtin routines\n"
            + "\n"
            + "Latin Letters (normative) Characters\n"
            + "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z a b c d e f g h i j k l m n o p q r s t \n"
            + "u v w x y z À Á Â Ã Ä Å Æ Ç È É Ê Ë Ì Í Î Ï Ð Ñ Ò Ó Ô Õ Ö Ø Ù Ú Û Ü Ý Þ ß à á â ã ä å æ \n"
            + "ç è é ê ë ì í î ï ð ñ ò ó ô õ ö ø ù ú û ü ý þ ÿ Ā ā Ă ă Ą ą Ć ć Ĉ ĉ Ċ ċ Č č Ď ď Đ đ Ē ē Ĕ ĕ Ė ė \n"
            + "Ę ę Ě ě Ĝ ĝ Ğ ğ Ġ ġ Ģ ģ Ĥ ĥ Ħ ħ Ĩ ĩ Ī ī Ĭ ĭ Į į İ ı Ĳ ĳ Ĵ ĵ Ķ ķ ĸ Ĺ ĺ Ļ ļ Ľ ľ Ŀ ŀ Ł ł Ń ń Ņ \n"
            + "ņ Ň ň ŉ Ŋ ŋ Ō ō Ŏ ŏ Ő ő Œ œ Ŕ ŕ Ŗ ŗ Ř ř Ś ś Ŝ ŝ Ş ş Š š Ţ ţ Ť ť Ŧ ŧ Ũ ũ Ū ū Ŭ ŭ Ů ů Ű ű Ų ų \n"
            + "Ŵ ŵ Ŷ ŷ Ÿ Ź ź Ż ż Ž ž Ƈ ƈ Ə Ɨ Ơ ơ Ư ư Ʒ Ǎ ǎ Ǐ ǐ Ǒ ǒ Ǔ ǔ Ǖ ǖ Ǘ ǘ Ǚ ǚ Ǜ ǜ Ǟ ǟ Ǣ ǣ Ǥ ǥ Ǧ ǧ Ǩ ǩ \n"
            + "Ǫ ǫ Ǭ ǭ Ǯ ǯ ǰ Ǵ ǵ Ǹ ǹ Ǻ ǻ Ǽ ǽ Ǿ ǿ Ȓ ȓ Ș ș Ț ț Ȟ ȟ ȧ Ȩ ȩ Ȫ ȫ Ȭ ȭ Ȯ ȯ Ȱ ȱ Ȳ ȳ ə ɨ ʒ Ḃ ḃ Ḇ ḇ Ḋ \n"
            + "ḋ Ḍ ḍ Ḏ ḏ Ḑ ḑ Ḝ ḝ Ḟ ḟ Ḡ ḡ Ḣ ḣ Ḥ ḥ Ḧ ḧ Ḩ ḩ Ḫ ḫ ḯ Ḱ ḱ Ḳ ḳ Ḵ ḵ Ḷ ḷ Ḻ ḻ Ṁ ṁ Ṃ ṃ Ṅ ṅ Ṇ ṇ Ṉ ṉ Ṓ ṓ \n"
            + "Ṕ ṕ Ṗ ṗ Ṙ ṙ Ṛ ṛ Ṟ ṟ Ṡ ṡ Ṣ ṣ Ṫ ṫ Ṭ ṭ Ṯ ṯ Ẁ ẁ Ẃ ẃ Ẅ ẅ Ẇ ẇ Ẍ ẍ Ẏ ẏ Ẑ ẑ Ẓ ẓ Ẕ ẕ ẖ ẗ ẞ Ạ ạ Ả ả Ấ \n"
            + "ấ Ầ ầ Ẩ ẩ Ẫ ẫ Ậ ậ Ắ ắ Ằ ằ Ẳ ẳ Ẵ ẵ Ặ ặ Ẹ ẹ Ẻ ẻ Ẽ ẽ Ế ế Ề ề Ể ể Ễ ễ Ệ ệ Ỉ ỉ Ị ị Ọ ọ Ỏ ỏ Ố ố Ồ \n"
            + "ồ Ổ ổ Ỗ ỗ Ộ ộ Ớ ớ Ờ ờ Ở ở Ỡ ỡ Ợ ợ Ụ ụ Ủ ủ Ứ ứ Ừ ừ Ử ử Ữ ữ Ự ự Ỳ ỳ Ỵ ỵ Ỷ ỷ Ỹ ỹ \n" 
            + "Latin Letters (normative) Sequences\n"
            + "A̋ C̀ C̄ C̆ C̈ C̕ C̣ C̦ C̨̆ D̂ F̀ F̄ G̀ H̄ H̦ H̱ J́ J̌ K̀ K̂ K̄ K̇ K̕ K̛ K̦ K͟H K͟h L̂ L̥ L̥̄ L̦ M̀ M̂ M̆ M̐ N̂ N̄ N̆ N̦ P̀ P̄ P̕ P̣ R̆ R̥ \n"
            + "R̥̄ S̀ S̄ S̛̄ S̱ T̀ T̄ T̈ T̕ T̛ U̇ Z̀ Z̄ Z̆ Z̈ Z̧ a̋ c̀ c̄ c̆ c̈ c̕ c̣ c̦ c̨̆ d̂ f̀ f̄ g̀ h̄ h̦ j́ k̀ k̂ k̄ k̇ k̕ k̛ k̦ k͟h l̂ l̥ l̥̄ l̦ m̀ \n"
            + "m̂ m̆ m̐ n̂ n̄ n̆ n̦ p̀ p̄ p̕ p̣ r̆ r̥ r̥̄ s̀ s̄ s̛̄ s̱ t̀ t̄ t̕ t̛ u̇ z̀ z̄ z̆ z̈ z̧ Ç̆ Û̄ ç̆ û̄ ÿ́ Č̕ Č̣ č̕ č̣ Ī́ ī́ Ž̦ Ž̧ ž̦ ž̧ Ḳ̄ ḳ̄ \n"
            + "Ṣ̄ ṣ̄ Ṭ̄ ṭ̄ Ạ̈ ạ̈ Ọ̈ ọ̈ Ụ̄ Ụ̈ ụ̄ ụ̈ \n"
            + "Non Letters N1 (normative)  ' , - . ` ~ ¨ ´ · ʹ ʺ ʾ ʿ ˈ ˌ ’ ‡ \n" 
            + "Non Letters N2 (normative)\n"
            + "! \" # $ % & ( ) * + / 0 1 2 3 4 5 6 7 8 9 : ; < = > ? @ [ \\ ] ^ _ { | } ¡ ¢ £ ¥ § © \n"
            + "ª « ¬ ® ¯ ° ± ² ³ µ ¶ ¹ º » ¿ × ÷ € \n"
            + "Non Letters N3 (normative)  ¤ ¦ ¸ ¼ ½ ¾ \n" 
            + "Non Letters E1 (extended)\n"
            + "ƒ ʰ ʳ ˆ ˜ ˢ ᵈ ᵗ ‘ ‚ “ ” „ † … ‰ ‹ › ⁰ ⁴ ⁵ ⁶ ⁷ ⁸ ⁹ ⁿ ₀ ₁ ₂ ₃ ₄ ₅ ₆ ₇ ₈ ₉ ™ ∞ ≤ ≥ \n" 
            ;

    /**
     * Main method
     * @param args -- not used
     */
    public static void main(String[] args) {
        try {
            test("GlyphLayoutFormDinSpec91379.pdf", "com/lowagie/examples/fonts/PdfFormDinSpec91379.pdf", LATIN_CHARS_DIN_SPEC_91379);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the test: Print the characters of DIN SPEC 91379 in a pdf form
     * @param fileName Name of output file 
     * @param formPath Name of input pdf form
     * @param text Text to show
     * @throws Exception
     */
    public static void test(String fileName, String formPath, String text) throws Exception {

        // Enable the LayoutProcessor with optional flags
        LayoutProcessor.enable();
        //LayoutProcessor.enable(java.awt.Font.LAYOUT_LEFT_TO_RIGHT);
        
        try (  InputStream acroFormInputStream = GlyphLayoutFormDinSpec91379.class.getClassLoader().getResourceAsStream(formPath);
                FileOutputStream outputStream = new FileOutputStream(fileName);
                PdfReader reader = new PdfReader(acroFormInputStream);
            ) {

            PdfStamper stamper = new PdfStamper(reader, outputStream);
            String fontFileName = "com/lowagie/examples/fonts/NotoSans-Regular.ttf";
            FontFactory.register(fontFileName, "sans");
            Font font = FontFactory.getFont("sans", BaseFont.IDENTITY_H);
            BaseFont baseFont = font.getBaseFont();
            float fontSize = 10f;
            FontFactory.register("com/lowagie/examples/fonts/NotoSansMath-Regular.ttf", "sans-math");
            Font fontMath = FontFactory.getFont("sans-math", BaseFont.IDENTITY_H);
            BaseFont baseFontMath = fontMath.getBaseFont();

            final AcroFields fields = stamper.getAcroFields();
            fields.addSubstitutionFont(baseFontMath);

            Map<String, AcroFields.Item> allFields = fields.getAllFields();

            for (final String fieldName : allFields.keySet()) {
                fields.setFieldProperty(fieldName, "textfont", baseFont, null);
                fields.setFieldProperty(fieldName, "textsize", fontSize, null);
                fields.setField(fieldName, text);
            }

            stamper.setFormFlattening(true);
            stamper.setFullCompression();
            stamper.close();
            reader.close();
        }
    }
}
