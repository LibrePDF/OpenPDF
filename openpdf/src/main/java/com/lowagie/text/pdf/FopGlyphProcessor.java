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
 * @author  Gajendra kumar (raaz2.gajendra@gmail.com)
 */
public class FopGlyphProcessor {

    /**
     * This character will be used if some unicode character used for which glyph index not defined in font.
     */
    private static final char CHAR_TO_USE_IF_GLYPH_NOT_FOUND = '#';

    private static boolean isFopSupported = false;

    static {
        try {
            Class.forName("org.apache.fop.complexscripts.util.GlyphSequence");
            isFopSupported = true;
        } catch (ClassNotFoundException e) {
            isFopSupported = false;
        }
    }

    public static boolean isFopSupported(){
        return isFopSupported;
    }

    public static byte[] convertToBytesWithGlyphs(BaseFont font, String text, String fileName,
                                                  Map<Integer, int[]> longTag) throws UnsupportedEncodingException {
        TrueTypeFontUnicode ttu = (TrueTypeFontUnicode)font;
        IntBuffer charBuffer = IntBuffer.allocate(text.length());
        IntBuffer ghyphBuffer = IntBuffer.allocate(text.length());
        for (char c : text.toCharArray()) {
            int[] metrics = ttu.getMetricsTT(c);
            // metrics will be null in case glyph not defined in TTF font, so default character should be used.
            if (metrics == null){
                c = CHAR_TO_USE_IF_GLYPH_NOT_FOUND;
                metrics = ttu.getMetricsTT(c);
            }
            charBuffer.put(c);
            ghyphBuffer.put(metrics[0]);
        }

        GlyphSequence glyphSequence = new GlyphSequence(charBuffer, ghyphBuffer, null);
        TTFFile ttf = TTFCache.getTTFFile(fileName, ttu);
        GlyphSubstitutionTable gsubTable = ttf.getGSUB();
        if (gsubTable != null) {
            String script = CharScript.scriptTagFromCode(CharScript.dominantScript(text));
            String language = "dflt";
            if ("zyyy".equals(script) || "auto".equals(script)) {
                script = "*";
            }
            glyphSequence = gsubTable.substitute(glyphSequence, script, language);
        }
        int limit  = glyphSequence.getGlyphs().limit();
        int[] processedChars = glyphSequence.getGlyphs().array();
        char[] charEncodedGlyphCodes = new char[limit];

        for (int i = 0; i < limit; i++) {
            charEncodedGlyphCodes[i] = (char) processedChars[i];
            Integer glyphCode = processedChars[i];
            if (!longTag.containsKey(glyphCode)) {
                longTag.put(glyphCode,
                        new int[] { processedChars[i], ttu.getGlyphWidth(processedChars[i]), processedChars[i] });
            }
        }
        return new String(charEncodedGlyphCodes).getBytes(CJKFont.CJK_ENCODING);
    }

}
