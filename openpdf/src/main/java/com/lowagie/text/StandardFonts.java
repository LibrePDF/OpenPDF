package com.lowagie.text;

import java.io.IOException;

public enum StandardFonts {
    // Courier
    COURIER(Font.COURIER, Font.NORMAL),
    COURIER_ITALIC(Font.COURIER, Font.ITALIC),
    COURIER_BOLD(Font.COURIER, Font.BOLD),
    COURIER_BOLDITALIC(Font.COURIER, Font.BOLDITALIC),
    // Helvetica
    HELVETICA(Font.HELVETICA, Font.NORMAL),
    HELVETICA_ITALIC(Font.HELVETICA, Font.ITALIC),
    HELVETICA_BOLD(Font.HELVETICA, Font.BOLD),
    HELVETICA_BOLDITALIC(Font.HELVETICA, Font.BOLDITALIC),
    // Times
    TIMES(Font.TIMES_ROMAN, Font.NORMAL),
    TIMES_ITALIC(Font.TIMES_ROMAN, Font.ITALIC),
    TIMES_BOLD(Font.TIMES_ROMAN, Font.BOLD),
    TIMES_BOLDITALIC(Font.TIMES_ROMAN, Font.BOLDITALIC),
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
