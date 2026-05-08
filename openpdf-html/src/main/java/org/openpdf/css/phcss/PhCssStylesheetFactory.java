/*
 * Copyright (c) 2026 OpenPDF
 *
 * Licensed under the GNU Lesser General Public License (LGPL), version 2.1 or
 * later. See the project LICENSE file for details.
 */
package org.openpdf.css.phcss;

import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.reader.CSSReader;
import com.helger.css.reader.CSSReaderSettings;
import com.helger.css.reader.errorhandler.ICSSParseErrorHandler;
import com.helger.css.reader.errorhandler.LoggingCSSParseErrorHandler;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Thin factory around <a href="https://github.com/phax/ph-css">ph-css</a>'
 * {@link CSSReader} that parses CSS source into a {@link CascadingStyleSheet}
 * model. Replaces the legacy {@code org.openpdf.css.parser.CSSParser}.
 *
 * @since 3.0.5
 */
public final class PhCssStylesheetFactory {

    private static final ICSSParseErrorHandler DEFAULT_ERROR_HANDLER = new LoggingCSSParseErrorHandler();

    private PhCssStylesheetFactory() {
    }

    /** Parse a CSS string with the default logging error handler. */
    @Nullable
    public static CascadingStyleSheet parse(String css) {
        return parse(css, null);
    }

    /** Parse a CSS string with an optional error handler (defaults to logging). */
    @Nullable
    public static CascadingStyleSheet parse(String css, @Nullable ICSSParseErrorHandler errorHandler) {
        CSSReaderSettings settings = new CSSReaderSettings()
                .setCustomErrorHandler(errorHandler != null ? errorHandler : DEFAULT_ERROR_HANDLER);
        return CSSReader.readFromStringReader(css, settings);
    }

    /** Parse a CSS stream using the supplied charset (defaults to UTF-8). */
    @Nullable
    public static CascadingStyleSheet parse(InputStream in, @Nullable Charset charset) throws IOException {
        Charset cs = charset != null ? charset : StandardCharsets.UTF_8;
        return parse(new InputStreamReader(in, cs));
    }

    /**
     * Parse CSS from a {@link Reader} by reading its full contents first.
     */
    @Nullable
    public static CascadingStyleSheet parse(Reader reader) throws IOException {
        return parse(readFully(reader));
    }

    private static String readFully(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];
        int n;
        while ((n = reader.read(buf)) != -1) {
            sb.append(buf, 0, n);
        }
        return sb.toString();
    }
}
