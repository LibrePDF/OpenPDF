package com.lowagie.text.pdf.fonts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * {@link Font Font}-related test cases.
 *
 * @author noavarice
 * @since 1.2.7
 */
class FontTest {

    private static final Map<Integer, FontTester> STYLES_TO_TEST_METHOD = new HashMap<Integer, FontTester>() {{
        put(Font.NORMAL, new FontTester() {
            @Override
            public boolean test(Font f) {
                return !f.isBold() && !f.isItalic() && !f.isStrikethru() && !f.isUnderlined();
            }
        });
        put(Font.BOLD, new FontTester() {
            @Override
            public boolean test(Font font) {
                return font.isBold();
            }
        });
        put(Font.ITALIC, new FontTester() {
            @Override
            public boolean test(Font font) {
                return font.isItalic();
            }
        });
        put(Font.UNDERLINE, new FontTester() {
            @Override
            public boolean test(Font font) {
                return font.isUnderlined();
            }
        });
        put(Font.STRIKETHRU, new FontTester() {
            @Override
            public boolean test(Font font) {
                return font.isStrikethru();
            }
        });
        put(Font.BOLDITALIC, new FontTester() {
            @Override
            public boolean test(Font f) {
                return f.isBold() && f.isItalic();
            }
        });
    }};

    private static final String FONT_NAME_WITHOUT_STYLES = "non-existing-font";

    private static final String FONT_NAME_WITH_STYLES = "Courier";

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
            final Font font = FontFactory.getFont(FONT_NAME_WITHOUT_STYLES, DEFAULT_FONT_SIZE, style);
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
            final Font font = FontFactory.getFont(FONT_NAME_WITHOUT_STYLES, DEFAULT_FONT_SIZE, style);
            final FontTester p = STYLES_TO_TEST_METHOD.get(style);
            assertTrue(p.test(font));
        }
    }

    @Test
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

    private interface FontTester {
        boolean test(Font font);
    }

}
