/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2006, 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.render;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.CssContext;
import org.openpdf.layout.LayoutContext;
import org.openpdf.layout.Styleable;

import java.util.List;

/**
 * An anonymous block box as defined in the CSS spec.  This class is only used
 * when wrapping inline content in a block box in order to ensure that a block
 * box only ever contains either block or inline content.  Other anonymous block
 * boxes create a {@code BlockBox} directly with the anonymous property is
 * true.
 */
public final class AnonymousBlockBox extends BlockBox {
    private final List<InlineBox> _openInlineBoxes;

    public AnonymousBlockBox(Element element, CalculatedStyle style, List<InlineBox> savedParents,
                             List<Styleable> inlineContent) {
        super(element, style, true);
        _openInlineBoxes = savedParents;
        setChildrenContentType(ContentType.INLINE);
        setInlineContent(inlineContent);
    }

    @Override
    public void layout(LayoutContext c) {
        layoutInlineChildren(c, 0, calcInitialBreakAtLine(c), true);
    }

    @Override
    public int getContentWidth() {
        return getContainingBlock().getContentWidth();
    }

    @Nullable
    @CheckReturnValue
    @Override
    public Box find(CssContext cssCtx, int absX, int absY, boolean findAnonymous) {
        Box result = super.find(cssCtx, absX, absY, findAnonymous);
        if (! findAnonymous && result == this) {
            return getParent();
        } else {
            return result;
        }
    }

    public List<InlineBox> getOpenInlineBoxes() {
        return _openInlineBoxes;
    }

    @Override
    public boolean isSkipWhenCollapsingMargins() {
        // An anonymous block will already have its children provided to it
        for (Styleable styleable : getInlineContent()) {
            CalculatedStyle style = styleable.getStyle();
            if (!(style.isFloated() || style.isAbsolute() || style.isFixed() || style.isRunning())) {
                return false;
            }
        }
        return true;
    }

    public void provideSiblingMarginToFloats(int margin) {
        for (Styleable styleable : getInlineContent()) {
            if (styleable instanceof BlockBox b) {
                if (b.isFloated()) {
                    b.getFloatedBoxData().setMarginFromSibling(margin);
                }
            }
        }
    }

    @Override
    public boolean isMayCollapseMarginsWithChildren() {
        return false;
    }

    @Override
    public void styleText(LayoutContext c) {
        styleText(c, getParent().getStyle());
    }

    @Override
    public BlockBox copyOf() {
        throw new IllegalArgumentException("cannot be copied");
    }
}
