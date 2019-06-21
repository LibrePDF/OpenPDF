package com.lowagie.text;

import com.lowagie.text.pdf.BaseFont;

import java.io.IOException;

public enum StandardFonts {
    // Courier
    COURIER(Font.COURIER, Font.NORMAL),
    COURIER_ITALIC(Font.COURIER, Font.ITALIC),
    COURIER_BOLD(Font.COURIER, Font.BOLD),
    COURIER_BOLDITALIC(Font.COURIER, Font.BOLDITALIC),
    // Liberation Mono - Courier compatible
    LIBERATION_MONO("com/lowagie/text/pdf/fonts/liberation/LiberationMono-Regular.ttf"),
    LIBERATION_MONO_ITALIC("com/lowagie/text/pdf/fonts/liberation/LiberationMono-Italic.ttf"),
    LIBERATION_MONO_BOLD("com/lowagie/text/pdf/fonts/liberation/LiberationMono-Bold.ttf"),
    LIBERATION_MONO_BOLDITALIC("com/lowagie/text/pdf/fonts/liberation/LiberationMono-BoldItalic.ttf"),
    // Helvetica
    HELVETICA(Font.HELVETICA, Font.NORMAL),
    HELVETICA_ITALIC(Font.HELVETICA, Font.ITALIC),
    HELVETICA_BOLD(Font.HELVETICA, Font.BOLD),
    HELVETICA_BOLDITALIC(Font.HELVETICA, Font.BOLDITALIC),
    // Liberation Sans - Helvetica/Arial compatible
    LIBERATION_SANS("com/lowagie/text/pdf/fonts/liberation/LiberationSans-Regular.ttf"),
    LIBERATION_SANS_ITALIC("com/lowagie/text/pdf/fonts/liberation/LiberationSans-Italic.ttf"),
    LIBERATION_SANS_BOLD("com/lowagie/text/pdf/fonts/liberation/LiberationSans-Bold.ttf"),
    LIBERATION_SANS_BOLDITALIC("com/lowagie/text/pdf/fonts/liberation/LiberationSans-BoldItalic.ttf"),
    // Times
    TIMES(Font.TIMES_ROMAN, Font.NORMAL),
    TIMES_ITALIC(Font.TIMES_ROMAN, Font.ITALIC),
    TIMES_BOLD(Font.TIMES_ROMAN, Font.BOLD),
    TIMES_BOLDITALIC(Font.TIMES_ROMAN, Font.BOLDITALIC),
    // Liberation Serif - Times compatible
    LIBERATION_SERIF("com/lowagie/text/pdf/fonts/liberation/LiberationSerif-Regular.ttf"),
    LIBERATION_SERIF_ITALIC("com/lowagie/text/pdf/fonts/liberation/LiberationSerif-Italic.ttf"),
    LIBERATION_SERIF_BOLD("com/lowagie/text/pdf/fonts/liberation/LiberationSerif-Bold.ttf"),
    LIBERATION_SERIF_BOLDITALIC("com/lowagie/text/pdf/fonts/liberation/LiberationSerif-BoldItalic.ttf"),
    // Others
    SYMBOL(Font.SYMBOL, -1),
    ZAPFDINGBATS(Font.ZAPFDINGBATS, -1),
    ;

    private int family;
    private int style;
    private String trueTypeFile;

    StandardFonts(int family, int style) {
        this.family = family;
        this.style = style;
    }

    StandardFonts(String trueTypeFile) {
        this.trueTypeFile = trueTypeFile;
    }

    public Font create() throws IOException {
        return create(Font.DEFAULTSIZE);
    }

    public Font create(int size) throws IOException {
        final Font font;
        if (trueTypeFile != null) {
            final BaseFont baseFont = BaseFont.createFont(trueTypeFile, BaseFont.IDENTITY_H, false);
            font = new Font(baseFont, size);
        } else if (style == -1) {
            font = new Font(family, size);
        } else {
            font = new Font(family, size, style);
        }
        return font;
    }
}
