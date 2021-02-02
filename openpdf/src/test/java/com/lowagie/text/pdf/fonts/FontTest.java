package com.lowagie.text.pdf.fonts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.RectangleReadOnly;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfLiteral;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link Font Font}-related test cases.
 *
 * @author noavarice
 * @since 1.2.7
 */
class FontTest {

    private static final Map<Integer, Predicate<Font>> STYLES_TO_TEST_METHOD = new HashMap<Integer, Predicate<Font>>() {{
        put(Font.NORMAL, f -> !f.isBold() && !f.isItalic() && !f.isStrikethru() && !f.isUnderlined());
        put(Font.BOLD, Font::isBold);
        put(Font.ITALIC, Font::isItalic);
        put(Font.UNDERLINE, Font::isUnderlined);
        put(Font.STRIKETHRU, Font::isStrikethru);
        put(Font.BOLDITALIC, f -> f.isBold() && f.isItalic());
        put(Font.UNDERLINE | Font.BOLD, f -> f.isUnderlined() && f.isBold());
    }};

    private static final String FONT_NAME_WITHOUT_STYLES = "non-existing-font";

    private static final String FONT_NAME_WITH_STYLES = "Courier";

    private static final float DEFAULT_FONT_SIZE = 16.0f;

    /**
     * Checks if style property value is preserved during font construction through {@link FontFactory#getFont(String,
     * float, int)} method by getting raw property value.
     *
     * @see Font#getStyle()
     */
    @ParameterizedTest(name = "Style {0}")
    @MethodSource("getStyles")
    void testStyleSettingByValue(int style) {
        FontFactory.registerDirectories();
        // TODO: complement tests after adding enum with font styles
        final Font font = FontFactory.getFont(FONT_NAME_WITHOUT_STYLES, DEFAULT_FONT_SIZE, style);
        assertEquals(font.getStyle(), style);
    }

    private static Set<Integer> getStyles() {
        return STYLES_TO_TEST_METHOD.keySet();
    }

    /**
     * Checks if style property value is preserved during font construction through {@link FontFactory#getFont(String,
     * float, int)} method by testing appropriate predicate.
     *
     * @see Font#isBold()
     * @see Font#isItalic()
     * @see Font#isStrikethru()
     * @see Font#isUnderlined()
     */
    @ParameterizedTest(name = "Style {0}")
    @MethodSource("getStyles")
    void testStyleSettingByPredicate(int style) {
        final Font font = FontFactory.getFont(FONT_NAME_WITHOUT_STYLES, DEFAULT_FONT_SIZE, style);
        final Predicate<Font> p = STYLES_TO_TEST_METHOD.get(style);
        assertTrue(p.test(font), "Style " + style);
    }

    @ParameterizedTest(name = "Style {0}")
    @MethodSource("getStyles")
    void testFontStyleOfStyledFont(int style) {
        final Font font = FontFactory.getFont(FONT_NAME_WITH_STYLES, DEFAULT_FONT_SIZE, style);

        // For the font Courier, there is no Courier-Underline or Courier-Strikethru font available.
        if (style == Font.UNDERLINE || style == Font.STRIKETHRU) {
            assertEquals(style, font.getStyle(), "Style: " + style);
        } else {
            assertEquals(style, font.getCombinedStyle(), "Total style should be the given style: " + style);
            assertEquals(font.getBaseFontStyle(), style ^ font.getCalculatedStyle(), "Styles should not repeat in"
                    + " Font and BaseFont.");
        }
    }

    @Test
    void testAllStylesInOneStyledFont() {
        // given
        final int allStyles = Font.NORMAL | Font.BOLD | Font.ITALIC | Font.UNDERLINE | Font.STRIKETHRU;
        final int expectedInFont = Font.NORMAL | Font.UNDERLINE | Font.STRIKETHRU;
        final int expectedInBaseFont = Font.BOLD | Font.ITALIC;
        // then
        final Font font = FontFactory.getFont(FontFactory.COURIER, 12f, allStyles);
        assertThat(font.getStyle()).as("style").isEqualTo(expectedInFont);
        assertThat(font.getCalculatedStyle()).as("calculatedStyle").isEqualTo(expectedInFont);
        assertThat(font.getBaseFontStyle()).as("baseFontStyle").isEqualTo(expectedInBaseFont);
        assertThat(font.getCombinedStyle()).as("combinedStyle").isEqualTo(allStyles);

    }

    /**
     * checks if the stroke width is correctly set after a text in simulated bold font is written
     * <p>
     * Disabled, because it failes when releasing new versions, because the pdf contains the OpenPDF version number.
     *
     * @throws Exception
     */
    @Test
    @Disabled
    void testBoldSimulationAndStrokeWidth() throws Exception {
        FileOutputStream outputStream = new FileOutputStream("target/resultSimulatedBold.pdf");
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        // set hardcoded documentID to be able to compare the resulting document with the reference
        writer.getInfo().put(PdfName.FILEID, new PdfLiteral("[<1><2>]"));
        document.open();
        document.setPageSize(new RectangleReadOnly(200, 70));
        document.newPage();
        PdfContentByte cb = writer.getDirectContentUnder();

        // Overwrite Helvetica bold with the standard Helvetica, to force simulated bold mode
        DefaultFontMapper fontMapper = new DefaultFontMapper();
        java.awt.Font font = new java.awt.Font("Helvetica", java.awt.Font.BOLD, 8);
        DefaultFontMapper.BaseFontParameters p = new DefaultFontMapper.BaseFontParameters("Helvetica");
        fontMapper.putName("Helvetica", p);
        Graphics2D graphics2D =
                cb.createGraphics(document.getPageSize().getWidth(), document.getPageSize().getHeight(), fontMapper);
        // setting the color is important to pass line 484 of PdfGraphics2D
        graphics2D.setColor(Color.BLACK);
        graphics2D.setStroke(new BasicStroke(2f));
        graphics2D.drawLine(10, 10, 10, 50);
        graphics2D.setFont(font);
        graphics2D.drawString("Simulated Bold String", 20, 30);
        graphics2D.setStroke(new BasicStroke(2f));
        graphics2D.drawLine(120, 10, 120, 50);

        graphics2D.dispose();
        document.close();
        outputStream.close();

        File original = new File(getClass().getClassLoader().getResource("SimulatedBoldAndStrokeWidth.pdf").getFile());
        File current = new File("target/resultSimulatedBold.pdf");
        assertTrue(FileUtils.contentEquals(original, current));
    }

}
