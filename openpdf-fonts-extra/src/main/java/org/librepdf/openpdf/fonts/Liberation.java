package org.librepdf.openpdf.fonts;

import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import java.io.IOException;

public enum Liberation {
    MONO("liberation/LiberationMono-Regular.ttf"),
    MONO_ITALIC("liberation/LiberationMono-Italic.ttf"),
    MONO_BOLD("liberation/LiberationMono-Bold.ttf"),
    MONO_BOLDITALIC("liberation/LiberationMono-BoldItalic.ttf"),
    SANS("liberation/LiberationSans-Regular.ttf"),
    SANS_ITALIC("liberation/LiberationSans-Italic.ttf"),
    SANS_BOLD("liberation/LiberationSans-Bold.ttf"),
    SANS_BOLDITALIC("liberation/LiberationSans-BoldItalic.ttf"),
    SERIF("liberation/LiberationSerif-Regular.ttf"),
    SERIF_ITALIC("liberation/LiberationSerif-Italic.ttf"),
    SERIF_BOLD("liberation/LiberationSerif-Bold.ttf"),
    SERIF_BOLDITALIC("liberation/LiberationSerif-BoldItalic.ttf");

    private final String trueTypeFile;

    Liberation(String trueTypeFile) {
        this.trueTypeFile = trueTypeFile;
    }

    public Font create() throws IOException {
        return create(Font.DEFAULTSIZE);
    }

    public Font create(int size) throws IOException {
        final Font font;
        final BaseFont baseFont = BaseFont.createFont(trueTypeFile, BaseFont.IDENTITY_H, false);
        font = new Font(baseFont, size);
        return font;
    }
}
