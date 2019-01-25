package com.lowagie.text.pdf.fonts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;

/**
 * {@link Font Font}-related test cases.
 *
 * @author noavarice
 * @since 1.2.7
 */
class FontTest {

    private static final Map<Integer, Predicate<Font>> STYLES_TO_TEST_METHOD = new HashMap<>();
    static {
        STYLES_TO_TEST_METHOD.put(Font.NORMAL, f -> !f.isBold() && !f.isItalic() && !f.isStrikethru() && !f.isUnderlined());
        STYLES_TO_TEST_METHOD.put(Font.BOLD, Font::isBold);
        STYLES_TO_TEST_METHOD.put(Font.ITALIC, Font::isItalic);
        STYLES_TO_TEST_METHOD.put(Font.UNDERLINE, Font::isUnderlined);
        STYLES_TO_TEST_METHOD.put(Font.STRIKETHRU, Font::isStrikethru);
        STYLES_TO_TEST_METHOD.put(Font.BOLDITALIC, f -> f.isBold() && f.isItalic());
    }

    private static final String DEFAULT_FONT_NAME = "Courier";

    private static final float DEFAULT_FONT_SIZE = 16.0f;

    /**
     * Checks if style property value is preserved during font construction
     * through {@link FontFactory#getFont(String, float, int)} method by getting raw property value.
     *
     * @see Font#getStyle()
     */
    @Test
    void testStyleSettingByValue() {
        FontFactory.registerDirectories();
        for (final int style: STYLES_TO_TEST_METHOD.keySet()) { // TODO: complement tests after adding enum with font styles
            final Font font = FontFactory.getFont(DEFAULT_FONT_NAME, DEFAULT_FONT_SIZE, style);
            assertEquals(font.getStyle(), style);
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
    @Test
    void testStyleSettingByPredicate() {
        for (final int style: STYLES_TO_TEST_METHOD.keySet()) {
            final Font font = FontFactory.getFont(DEFAULT_FONT_NAME, DEFAULT_FONT_SIZE, style);
            final Predicate<Font> p = STYLES_TO_TEST_METHOD.get(style);
            assertTrue(p.test(font));
        }
    }
}
