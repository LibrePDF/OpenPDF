package com.lowagie.text.html;

/**
 * Named font sizes defined by CSS
 * <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/font-size">font-size</a> property
 */
public enum FontSize {

    // Absolute-size keywords, based on the user's default font size (which is medium).
    XX_SMALL("xx-small", 0.6f, false),
    X_SMALL("x-small", 0.75f, false),
    SMALL("small", 0.89f, false),
    MEDIUM("medium", 1.0f, false),
    LARGE("large", 1.2f, false),
    X_LARGE("x-large", 1.5f, false),
    XX_LARGE("xx-large", 2.0f, false),
    XXX_LARGE("xxx-large", 2.5f, false),

    // Relative-size keywords. The font will be larger or smaller relative to the parent element's font size,
    // roughly by the ratio used to separate the absolute-size keywords above.
    SMALLER("smaller", 0.89f, true),
    LARGER("larger", 1.2f, true);

    private final float scale;
    private final String textValue;
    private final boolean relative;

    FontSize(String textValue, float scale, boolean relative) {
        this.textValue = textValue;
        this.scale = scale;
        this.relative = relative;
    }

    public static FontSize parse(String text) {
        if (text == null || text.isEmpty() || !Character.isLetter(text.charAt(0))) {
            return null;
        }
        for (FontSize fontSize : values()) {
            if (fontSize.getTextValue().equalsIgnoreCase(text)) {
                return fontSize;
            }
        }
        return null;
    }

    public String getTextValue() {
        return textValue;
    }

    public float getScale() {
        return scale;
    }

    public boolean isRelative() {
        return relative;
    }
}
