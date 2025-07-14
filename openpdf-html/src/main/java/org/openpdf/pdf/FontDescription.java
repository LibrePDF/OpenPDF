package org.openpdf.pdf;

import org.openpdf.text.pdf.BaseFont;
import org.openpdf.css.constants.IdentValue;

public class FontDescription {
    private static final int DEFAULT_FONT_WEIGHT = 400;
    private final IdentValue _style;
    private final BaseFont _font;
    private final Decorations _decorations;
    private final boolean _isFromFontFace;

    public FontDescription(BaseFont font) {
        this(font, false);
    }

    public FontDescription(BaseFont font, boolean isFromFontFace) {
        this(font, isFromFontFace, IdentValue.NORMAL);
    }

    public FontDescription(BaseFont font, IdentValue style, int weight) {
        this(font, false, style, defaultDecorations(font, weight));
    }

    public FontDescription(BaseFont font, boolean isFromFontFace, IdentValue style) {
        this(font, isFromFontFace, style, defaultDecorations(font, DEFAULT_FONT_WEIGHT));
    }

    public FontDescription(BaseFont font, boolean isFromFontFace, IdentValue style, Decorations decorations) {
        _font = font;
        _isFromFontFace = isFromFontFace;
        _style = style;
        _decorations = decorations;
    }

    public BaseFont getFont() {
        return _font;
    }

    public int getWeight() {
        return _decorations.weight();
    }

    public IdentValue getStyle() {
        return _style;
    }

    /**
     * This refers to the top of the underline stroke
     */
    public float getUnderlinePosition() {
        return _decorations.underlinePosition();
    }

    public float getUnderlineThickness() {
        return _decorations.underlineThickness();
    }

    public float getYStrikeoutPosition() {
        return _decorations.yStrikeoutPosition();
    }

    public float getYStrikeoutSize() {
        return _decorations.yStrikeoutSize();
    }

    private static Decorations defaultDecorations(BaseFont font, int weight) {
        int underlinePosition = -50;
        int underlineThickness = 50;

        int[] box = font.getCharBBox('x');
        if (box != null) {
            float yStrikeoutPosition = box[3] / 2f + 50;
            float yStrikeoutSize = 100;
            return new Decorations(weight, yStrikeoutSize, yStrikeoutPosition, underlinePosition, underlineThickness);
        } else {
            // Do what the JDK does, size will be calculated by ITextTextRenderer
            float yStrikeoutPosition = font.getFontDescriptor(BaseFont.BBOXURY, 1000.0f) / 3.0f;
            return new Decorations(weight, 0, yStrikeoutPosition, underlinePosition, underlineThickness);
        }
    }

    public boolean isFromFontFace() {
        return _isFromFontFace;
    }

    @Override
    public String toString() {
        return String.format("Font %s:%s", _font.getPostscriptFontName(), getWeight());
    }

    public record Decorations(
            int weight,
            float yStrikeoutSize,
            float yStrikeoutPosition,
            float underlinePosition,
            float underlineThickness
    ) {
    }
}
