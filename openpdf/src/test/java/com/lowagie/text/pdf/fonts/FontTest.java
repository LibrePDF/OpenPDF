package com.lowagie.text.pdf.fonts;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
//import jdk.nashorn.internal.ir.annotations.Ignore;
//import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Font Font}-related test cases.
 *
 * @author noavarice
 * @since 1.2.7
 */
class FontTest {

    private static final Map<Integer, Predicate<Font>> STYLES_TO_TEST_METHOD = new HashMap<Integer, Predicate<Font>> () {{
        put(Font.NORMAL, f -> !f.isBold() && !f.isItalic() && !f.isStrikethru() && !f.isUnderlined());
        put(Font.BOLD, Font::isBold);
        put(Font.ITALIC, Font::isItalic);
        put(Font.UNDERLINE, Font::isUnderlined);
        put(Font.STRIKETHRU, Font::isStrikethru);
        put(Font.BOLDITALIC, f -> f.isBold() && f.isItalic());
    }};

    private static final String FONT_NAME_WITHOUT_STYLES = "Tahoma";
    
    private static final String FONT_NAME_WITH_STYLES = "Courier";

    private static final float DEFAULT_FONT_SIZE = 16.0f;

    /**
     * Checks if style property value is preserved during font construction
     * through {@link FontFactory#getFont(String, float, int)} method by getting raw property value.
     *
     * @see Font#getStyle()
     */
//    @Test
    void testStyleSettingByValue() {
        FontFactory.registerDirectories();
        for (final int style: STYLES_TO_TEST_METHOD.keySet()) { // TODO: complement tests after adding enum with font styles
            final Font font = FontFactory.getFont(FONT_NAME_WITHOUT_STYLES, DEFAULT_FONT_SIZE, style);
            assertEquals(style, font.getStyle(), "For font " + FONT_NAME_WITHOUT_STYLES + ", Expected Style: " + style + ", got: " + font.getStyle());
        }
    }

    /**
     * Checks if style property value is preserved during font construction
     * through {@link FontFactory#getFont(String, float, int)} method by testing appropriate predicate.
     *
     * @see Font#isBold()
     * @see Font#isItalic()
     * @see Font#isStrikethru()
     * @see Font#isUnderlined()
     */
//    @Test
    void testStyleSettingByPredicate() {
        for (final int style: STYLES_TO_TEST_METHOD.keySet()) {
            final Font font = FontFactory.getFont(FONT_NAME_WITHOUT_STYLES, DEFAULT_FONT_SIZE, style);
            final Predicate<Font> p = STYLES_TO_TEST_METHOD.get(style);
            assertTrue(p.test(font));
        }
    }

//    @Test
    void testFontStyleOfStyledFont() {
        for (final int style : STYLES_TO_TEST_METHOD.keySet()) {
            final Font font = FontFactory.getFont(FONT_NAME_WITH_STYLES, DEFAULT_FONT_SIZE, style);

            // For the font Courier, there is no Courier-Underline or Courier-Strikethru font available.
            if (style == Font.UNDERLINE || style == Font.STRIKETHRU) {
                assertEquals(font.getStyle(), style);
            } else {
                assertEquals(Font.NORMAL, font.getStyle());
            }
        }
    }

}
