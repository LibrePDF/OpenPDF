package com.lowagie.text;

import java.io.IOException;

public enum StandardFonts {
    // Courier
    COURIER(Font.COURIER, Font.NORMAL),
    COURIER_ITALIC(Font.COURIER, Font.ITALIC),
    COURIER_BOLD(Font.COURIER, Font.BOLD),
    COURIER_BOLDITALIC(Font.COURIER, Font.BOLDITALIC),
    // Liberation Mono - Courier compatible
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    LIBERATION_MONO("font-fallback/LiberationMono-Regular.ttf"),
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    LIBERATION_MONO_ITALIC("font-fallback/LiberationMono-Italic.ttf"),
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    LIBERATION_MONO_BOLD("font-fallback/LiberationMono-Bold.ttf"),
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    LIBERATION_MONO_BOLDITALIC("font-fallback/LiberationMono-BoldItalic.ttf"),
    // Helvetica
    HELVETICA(Font.HELVETICA, Font.NORMAL),
    HELVETICA_ITALIC(Font.HELVETICA, Font.ITALIC),
    HELVETICA_BOLD(Font.HELVETICA, Font.BOLD),
    HELVETICA_BOLDITALIC(Font.HELVETICA, Font.BOLDITALIC),
    // Liberation Sans - Helvetica/Arial compatible
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    LIBERATION_SANS("font-fallback/LiberationSans-Regular.ttf"),
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    LIBERATION_SANS_ITALIC("font-fallback/LiberationSans-Italic.ttf"),
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    LIBERATION_SANS_BOLD("font-fallback/LiberationSans-Bold.ttf"),
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    LIBERATION_SANS_BOLDITALIC("font-fallback/LiberationSans-BoldItalic.ttf"),
    // Times
    TIMES(Font.TIMES_ROMAN, Font.NORMAL),
    TIMES_ITALIC(Font.TIMES_ROMAN, Font.ITALIC),
    TIMES_BOLD(Font.TIMES_ROMAN, Font.BOLD),
    TIMES_BOLDITALIC(Font.TIMES_ROMAN, Font.BOLDITALIC),
    // Liberation Serif - Times compatible
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    LIBERATION_SERIF("font-fallback/LiberationSerif-Regular.ttf"),
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    LIBERATION_SERIF_ITALIC("font-fallback/LiberationSerif-Italic.ttf"),
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    LIBERATION_SERIF_BOLD("font-fallback/LiberationSerif-Bold.ttf"),
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    LIBERATION_SERIF_BOLDITALIC("font-fallback/LiberationSerif-BoldItalic.ttf"),
    // Others
    SYMBOL(Font.SYMBOL, -1),
    ZAPFDINGBATS(Font.ZAPFDINGBATS, -1),
    ;

    private int family;
    private int style;
    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    private String trueTypeFile;

    StandardFonts(int family, int style) {
        this.family = family;
        this.style = style;
    }

    /**
     * @deprecated Use Liberation
     */
    @Deprecated
    StandardFonts(String trueTypeFile) {
        this.trueTypeFile = trueTypeFile;
    }

    public Font create() throws IOException {
        return create(Font.DEFAULTSIZE);
    }

    public Font create(int size) throws IOException {
        final Font font;
        if (trueTypeFile != null) {
            final String message = String
                    .format("%s: Please use fonts from openpdf-fonts-extra (Liberation)", this);
            throw new IOException(message);
        } else if (style == -1) {
            font = new Font(family, size);
        } else {
            font = new Font(family, size, style);
        }
        return font;
    }

    public boolean isDeprecated() {
        return trueTypeFile != null;
    }
}
