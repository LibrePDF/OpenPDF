/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
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
package org.openpdf.css.style;

import static org.openpdf.css.style.Length.LengthType.FIXED;
import static org.openpdf.css.style.Length.LengthType.PERCENT;
import static org.openpdf.css.style.Length.LengthType.VARIABLE;

// A simplified version of KHTML's Length type.  It's very convenient to be able
// to treat length values (including auto) in a uniform matter when calculating
// table column widths.  Our own LengthValue is too heavyweight for this purpose and
// doesn't encompass variable (auto) widths.
public class Length {
    // Should use something more reasonable here (e.g. a few feet based on the current
    // DPI)
    public static final int MAX_WIDTH = Integer.MAX_VALUE / 2;

    public static final Length ZERO = new Length();

    public enum LengthType {
        VARIABLE,
        FIXED,
        PERCENT,
    }

    private final LengthType _type;
    private final long _value;

    private Length() {
        this(0, VARIABLE);
    }

    public Length(long value, LengthType type) {
        _value = value;
        _type = type;
    }

    public long value() {
        return _value;
    }

    public LengthType type() {
        return _type;
    }

    public boolean isVariable() {
        return _type == VARIABLE;
    }

    public boolean isFixed() {
        return _type == FIXED;
    }

    public boolean isPercent() {
        return _type == PERCENT;
    }

    public long width(int maxWidth) {
        return switch (_type) {
            case FIXED -> _value;
            case PERCENT -> maxWidth * _value / 100;
            case VARIABLE -> maxWidth;
        };
    }

    public long minWidth(int maxWidth) {
        return switch (_type) {
            case FIXED -> _value;
            case PERCENT -> maxWidth * _value / 100;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        String type = switch (_type) {
            case FIXED -> "fixed";
            case PERCENT -> "percent";
            case VARIABLE -> "variable";
        };
        return "Length (type=%s, value=%d)".formatted(type, _value);
    }
}
