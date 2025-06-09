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
package org.openpdf.css.constants;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MarginBoxName {
    private static final Map<String, MarginBoxName> ALL = new HashMap<>();
    private static int _maxAssigned;

    public final int FS_ID;

    private final String _ident;
    private final IdentValue _textAlign;
    private final IdentValue _verticalAlign;

    public static final MarginBoxName TOP_LEFT_CORNER = addValue("top-left-corner", IdentValue.RIGHT, IdentValue.MIDDLE);
    public static final MarginBoxName TOP_LEFT = addValue("top-left", IdentValue.LEFT, IdentValue.MIDDLE);
    public static final MarginBoxName TOP_CENTER = addValue("top-center", IdentValue.CENTER, IdentValue.MIDDLE);
    public static final MarginBoxName TOP_RIGHT = addValue("top-right", IdentValue.RIGHT, IdentValue.MIDDLE);
    public static final MarginBoxName TOP_RIGHT_CORNER = addValue("top-right-corner", IdentValue.LEFT, IdentValue.MIDDLE);
    public static final MarginBoxName BOTTOM_LEFT_CORNER = addValue("bottom-left-corner", IdentValue.RIGHT, IdentValue.MIDDLE);
    public static final MarginBoxName BOTTOM_LEFT = addValue("bottom-left", IdentValue.LEFT, IdentValue.MIDDLE);
    public static final MarginBoxName BOTTOM_CENTER = addValue("bottom-center", IdentValue.CENTER, IdentValue.MIDDLE);
    public static final MarginBoxName BOTTOM_RIGHT = addValue("bottom-right", IdentValue.RIGHT, IdentValue.MIDDLE);
    public static final MarginBoxName BOTTOM_RIGHT_CORNER = addValue("bottom-right-corner", IdentValue.LEFT, IdentValue.MIDDLE);
    public static final MarginBoxName LEFT_TOP = addValue("left-top", IdentValue.CENTER, IdentValue.TOP);
    public static final MarginBoxName LEFT_MIDDLE = addValue("left-middle", IdentValue.CENTER, IdentValue.MIDDLE);
    public static final MarginBoxName LEFT_BOTTOM = addValue("left-bottom", IdentValue.CENTER, IdentValue.BOTTOM);
    public static final MarginBoxName RIGHT_TOP = addValue("right-top", IdentValue.CENTER, IdentValue.TOP);
    public static final MarginBoxName RIGHT_MIDDLE = addValue("right-middle", IdentValue.CENTER, IdentValue.MIDDLE);
    public static final MarginBoxName RIGHT_BOTTOM = addValue("right-bottom", IdentValue.CENTER, IdentValue.BOTTOM);

    // HACK to support page level XMP metadata.  For ease of implementation, it reuses
    // the margin box infrastructure, but is instead embedded in the PDF vs. being displayed
    // on the screen.
    public static final MarginBoxName FS_PDF_XMP_METADATA = addValue("-fs-pdf-xmp-metadata", IdentValue.TOP, IdentValue.LEFT);

    private MarginBoxName(String ident, IdentValue textAlign, IdentValue verticalAlign) {
        _ident = ident;
        _textAlign = textAlign;
        _verticalAlign = verticalAlign;

        this.FS_ID = _maxAssigned++;
    }

    private static MarginBoxName addValue(String ident, IdentValue textAlign, IdentValue verticalAlign) {
        MarginBoxName val = new MarginBoxName(ident, textAlign, verticalAlign);
        ALL.put(ident, val);
        return val;
    }

    @Override
    public String toString() {
        return _ident;
    }

    @Nullable
    @CheckReturnValue
    public static MarginBoxName valueOf(String ident) {
        return ALL.get(ident);
    }

    @Override
    public int hashCode() {
        return FS_ID;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MarginBoxName)) {
            return false;
        }

        return FS_ID == ((MarginBoxName)o).FS_ID;
    }

    public IdentValue getInitialTextAlign() {
        return _textAlign;
    }

    public IdentValue getInitialVerticalAlign() {
        return _verticalAlign;
    }
}
