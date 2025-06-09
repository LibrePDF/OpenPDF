package org.openpdf.css.style;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.context.StyleReference;
import org.openpdf.css.value.FontSpecification;
import org.openpdf.render.FSFont;
import org.openpdf.render.FSFontMetrics;

/**
 * User: tobe
 * Date: 2005-jun-23
 */
public interface CssContext {
    float getMmPerDot();

    int getDotsPerPixel();

    float getFontSize2D(FontSpecification font);

    float getXHeight(FontSpecification parentFont);

    @Nullable
    @CheckReturnValue
    FSFont getFont(FontSpecification font);

    // FIXME Doesn't really belong here, but this is
    // the only common interface of LayoutContext
    // and RenderingContext
    @CheckReturnValue
    StyleReference getCss();

    @CheckReturnValue
    FSFontMetrics getFSFontMetrics(FSFont font);
}
