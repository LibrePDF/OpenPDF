package com.lowagie.text.pdf;

import java.io.UnsupportedEncodingException;
import java.nio.IntBuffer;
import java.util.Map;
import org.apache.fop.complexscripts.fonts.GlyphSubstitutionTable;
import org.apache.fop.complexscripts.util.CharScript;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.fonts.truetype.TTFFile;

/**
 * Utilizing Fop advanced typography capabilities for TrueType fonts.
 *
 * @author Gajendra kumar (raaz2.gajendra@gmail.com)
 */
public class FopGlyphProcessor {

    private static boolean isFopSupported;

    static {
        try {
            Class.forName("org.apache.fop.complexscripts.util.GlyphSequence");
            isFopSupported = true;
        } catch (ClassNotFoundException e) {
            isFopSupported = false;
        }
    }

    public static boolean isFopSupported() {
        return isFopSupported;
    }

    public static byte[] convertToBytesWithGlyphs(BaseFont font, String text, String fileName,
            Map<Integer, int[]> longTag, String language) throws UnsupportedEncodingException {
        TrueTypeFontUnicode ttu = (TrueTypeFontUnicode) font;
        IntBuffer charBuffer = IntBuffer.allocate(text.length());
        IntBuffer glyphBuffer = IntBuffer.allocate(text.length());
        int textLength = text.length();
        for (char c : text.toCharArray()) {
            int[] metrics = ttu.getMetricsTT(c);
            // metrics will be null in case glyph not defined in TTF font, skip these characters.
            if (metrics == null) {
                textLength--;
                continue;
            }
            charBuffer.put(c);
            glyphBuffer.put(metrics[0]);
        }
        charBuffer.limit(textLength);
        glyphBuffer.limit(textLength);

        GlyphSequence glyphSequence = new GlyphSequence(charBuffer, glyphBuffer, null);
        TTFFile ttf = TTFCache.getTTFFile(fileName, ttu);
        GlyphSubstitutionTable gsubTable = ttf.getGSUB();
        if (gsubTable != null) {
            String script = CharScript.scriptTagFromCode(CharScript.dominantScript(text));
            if ("zyyy".equals(script) || "auto".equals(script)) {
                script = "*";
            }
            glyphSequence = gsubTable.substitute(glyphSequence, script, language);
        }
        int limit = glyphSequence.getGlyphs().limit();
        int[] processedChars = glyphSequence.getGlyphs().array();
        char[] charEncodedGlyphCodes = new char[limit];

        for (int i = 0; i < limit; i++) {
            charEncodedGlyphCodes[i] = (char) processedChars[i];
            Integer glyphCode = processedChars[i];
            if (!longTag.containsKey(glyphCode)) {
                longTag.put(glyphCode,
                        new int[]{processedChars[i], ttu.getGlyphWidth(processedChars[i]), charBuffer.get(i)});
            }
        }
        return new String(charEncodedGlyphCodes).getBytes(CJKFont.CJK_ENCODING);
    }

}
