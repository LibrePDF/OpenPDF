/*
 * Copyright (c) 2026 OpenPDF
 *
 * Licensed under the GNU Lesser General Public License (LGPL), version 2.1 or
 * later. See the project LICENSE file for details.
 */
package org.openpdf.css.phcss;

import com.helger.css.decl.CascadingStyleSheet;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

/**
 * Convenience instance-based wrapper around {@link PhCssStylesheetFactory}
 * that mirrors the surface of the legacy
 * {@code org.openpdf.css.parser.CSSParser} so callers can swap parser
 * implementations more easily during the migration to ph-css.
 *
 * <p>Currently this class is a thin pass-through; once the
 * {@link PhCssToOpenPdfAdapter} mapping is implemented this class will become
 * the primary integration point for the renderer.</p>
 *
 * @since 3.0.5
 */
public class PhCssParser {

    /**
     * Parse a stylesheet from a {@link Reader}.
     * The {@code uri} is not yet used; reserved for future diagnostics.
     */
    @Nullable
    public CascadingStyleSheet parseStylesheet(@Nullable String ignoredUri, Reader reader) {
        try {
            return PhCssStylesheetFactory.parse(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Parse a stylesheet from a CSS string.
     * The {@code uri} is not yet used; reserved for future diagnostics.
     */
    @Nullable
    public CascadingStyleSheet parseStylesheet(@Nullable String ignoredUri, String css) {
        return PhCssStylesheetFactory.parse(css);
    }
}

