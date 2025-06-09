/*
 * RenderingContext.java
 * Copyright (c) 2004, 2005 Josh Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.openpdf.render;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.context.StyleReference;
import org.openpdf.css.style.CssContext;
import org.openpdf.css.value.FontSpecification;
import org.openpdf.extend.FSCanvas;
import org.openpdf.extend.FontContext;
import org.openpdf.extend.FontResolver;
import org.openpdf.extend.OutputDevice;
import org.openpdf.extend.TextRenderer;
import org.openpdf.extend.UserAgentCallback;
import org.openpdf.layout.Layer;
import org.openpdf.layout.SharedContext;

import java.awt.*;

/**
 * Supplies information about the context in which rendering will take place
 *
 * @author jmarinacci
 *         November 16, 2004
 */
public class RenderingContext implements CssContext {
    protected final SharedContext sharedContext;
    private final OutputDevice outputDevice;
    private final FontContext fontContext;

    private int pageCount;

    private int pageNo;

    @Nullable
    private PageBox page;

    @Nullable
    private final Layer rootLayer;

    private final int initialPageNo;

    /**
     * needs a new instance every run
     */
    public RenderingContext(SharedContext sharedContext, OutputDevice outputDevice, FontContext fontContext,
                            @Nullable Layer rootLayer,
                            int initialPageNo) {
        this.sharedContext = sharedContext;
        this.outputDevice = outputDevice;
        this.fontContext = fontContext;
        this.rootLayer = rootLayer;
        this.initialPageNo = initialPageNo;
    }

    public UserAgentCallback getUac() {
        return sharedContext.getUac();
    }

    public String getBaseURL() {
        return sharedContext.getBaseURL();
    }

    public float getDPI() {
        return sharedContext.getDPI();
    }

    @Override
    public float getMmPerDot() {
        return sharedContext.getMmPerPx();
    }

    @Override
    public int getDotsPerPixel() {
        return sharedContext.getDotsPerPixel();
    }

    @Override
    public float getFontSize2D(FontSpecification font) {
        return sharedContext.getFont(font).getSize2D();
    }

    @Override
    public float getXHeight(FontSpecification parentFont) {
        return sharedContext.getXHeight(getFontContext(), parentFont);
    }

    public TextRenderer getTextRenderer() {
        return sharedContext.getTextRenderer();
    }

    /**
     * Returns true if the currently set media type is paged. Currently, returns
     * true only for <i>print</i> , <i>projection</i> , and <i>embossed</i> ,
     * <i>handheld</i> , and <i>tv</i> . See the <a
     * href="http://www.w3.org/TR/CSS21/media.html">media section</a> of the CSS
     * 2.1 spec for more information on media types.
     *
     * @return The paged value
     */
    public boolean isPaged() {
        return sharedContext.isPaged();
    }

    public FontResolver getFontResolver() {
        return sharedContext.getFontResolver();
    }

    @Nullable
    @CheckReturnValue
    @Override
    public FSFont getFont(FontSpecification font) {
        return sharedContext.getFont(font);
    }

    @Nullable
    @CheckReturnValue
    public FSCanvas getCanvas() {
        return sharedContext.getCanvas();
    }

    public Rectangle getFixedRectangle() {
        Rectangle result = isPrint() ?
            new Rectangle(0, -this.page.getTop(), this.page.getContentWidth(this), this.page.getContentHeight(this) - 1) :
            sharedContext.getFixedRectangle();
        result.translate(-1, -1);
        return result;
    }

    public Rectangle getViewportRectangle() {
        Rectangle result = new Rectangle(getFixedRectangle());
        result.y *= -1;

        return result;
    }

    public boolean debugDrawBoxes() {
        return sharedContext.debugDrawBoxes();
    }

    public boolean debugDrawLineBoxes() {
        return sharedContext.debugDrawLineBoxes();
    }

    public boolean debugDrawInlineBoxes() {
        return sharedContext.debugDrawInlineBoxes();
    }

    public boolean debugDrawFontMetrics() {
        return sharedContext.debugDrawFontMetrics();
    }

    public boolean isInteractive() {
        return sharedContext.isInteractive();
    }

    public boolean isPrint() {
        return sharedContext.isPrint();
    }

    public OutputDevice getOutputDevice() {
        return outputDevice;
    }

    public FontContext getFontContext() {
        return fontContext;
    }

    public void setPage(int pageNo, PageBox page) {
        this.pageNo = pageNo;
        this.page = page;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    @Nullable
    public PageBox getPage() {
        return page;
    }

    public int getPageNo() {
        return pageNo;
    }

    @CheckReturnValue
    @Override
    public StyleReference getCss() {
        return sharedContext.getCss();
    }

    @CheckReturnValue
    @Override
    public FSFontMetrics getFSFontMetrics(FSFont font) {
        return getTextRenderer().getFSFontMetrics(getFontContext(), font, "");
    }

    @Nullable
    public Layer getRootLayer() {
        return rootLayer;
    }

    public int getInitialPageNo() {
        return initialPageNo;
    }

    @Nullable
    @CheckReturnValue
    public Box getBoxById(String id) {
        return sharedContext.getBoxById(id);
    }
}

