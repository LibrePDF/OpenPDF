/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
package org.openpdf.newtable;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.FSColor;
import org.openpdf.css.style.derived.BorderPropertySet;

/**
 * This class encapsulates all information related to a particular border side
 * along with an overall precedence (e.g. cell borders take precedence over
 * row borders).  It is used when comparing overlapping borders when calculating
 * collapsed borders.
 */
public class CollapsedBorderValue {
    @Nullable
    private final IdentValue _style;
    private final int _width;
    private final FSColor _color;
    private final int _precedence;

    public CollapsedBorderValue(@Nullable IdentValue style, int width, FSColor color, int precedence) {
        _style = style;
        _width = width;
        _color = color;
        _precedence = precedence;
    }

    public FSColor color() {
        return _color;
    }

    @Nullable
    @CheckReturnValue
    public IdentValue style() {
        return _style;
    }

    @CheckReturnValue
    public int width() {
        return _width;
    }

    @CheckReturnValue
    public CollapsedBorderValue withWidth(int width) {
        return new CollapsedBorderValue(_style, width, _color, _precedence);
    }

    @CheckReturnValue
    public int precedence() {
        return _precedence;
    }

    @CheckReturnValue
    public boolean defined() {
        return _style != null;
    }

    @CheckReturnValue
    public boolean exists() {
        return _style != null && _style != IdentValue.NONE && _style != IdentValue.HIDDEN;
    }

    @CheckReturnValue
    public boolean hidden() {
        return _style == IdentValue.HIDDEN;
    }

    @CheckReturnValue
    public static CollapsedBorderValue borderLeft(BorderPropertySet border, int precedence) {
        return new CollapsedBorderValue(
                border.leftStyle(), (int)border.left(), border.leftColor(), precedence);
    }

    @CheckReturnValue
    public static CollapsedBorderValue borderRight(BorderPropertySet border, int precedence) {
        return new CollapsedBorderValue(
                border.rightStyle(), (int)border.right(), border.rightColor(), precedence);
    }

    @CheckReturnValue
    public static CollapsedBorderValue borderTop(BorderPropertySet border, int precedence) {
        return new CollapsedBorderValue(
                border.topStyle(), (int)border.top(), border.topColor(), precedence);
    }

    @CheckReturnValue
    public static CollapsedBorderValue borderBottom(BorderPropertySet border, int precedence) {
        return new CollapsedBorderValue(
                border.bottomStyle(), (int)border.bottom(), border.bottomColor(), precedence);
    }
}
