/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
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
import org.openpdf.css.style.CssContext;

import java.awt.*;

/**
 * A dummy box representing the viewport
 */
public class ViewportBox extends BlockBox {
    private final Rectangle _viewport;

    public ViewportBox(Rectangle viewport) {
        _viewport = viewport;
    }

    @Override
    public int getWidth() {
        return _viewport.width;
    }

    @Override
    public int getHeight() {
        return _viewport.height;
    }

    @Override
    public int getContentWidth() {
        return _viewport.width;
    }

    @Override
    public Rectangle getContentAreaEdge(int left, int top, CssContext cssCtx) {
        return new Rectangle(-_viewport.x, -_viewport.y, _viewport.width, _viewport.height);
    }

    @CheckReturnValue
    @Override
    public Rectangle getPaddingEdge(int left, int top, CssContext cssCtx) {
        return new Rectangle(-_viewport.x, -_viewport.y, _viewport.width, _viewport.height);
    }

    @Override
    protected int getPaddingWidth(CssContext cssCtx) {
        return _viewport.width;
    }

    @Override
    public BlockBox copyOf() {
        throw new IllegalArgumentException("cannot be copied");
    }

    @Override
    public boolean isAutoHeight() {
        return false;
    }

    @Override
    protected int getCSSHeight(CssContext c) {
        return _viewport.height;
    }

    @Override
    protected boolean isInitialContainingBlock() {
        return true;
    }

    public Rectangle getExtents() {
        return _viewport;
    }
}
