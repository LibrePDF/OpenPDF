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
package org.openpdf.render;

public class ContentLimit {
    public static final int UNDEFINED = -1;

    private int _top = UNDEFINED;
    private int _bottom = UNDEFINED;

    public int getTop() {
        return _top;
    }

    public void setTop(int top) {
        _top = top;
    }

    public void updateTop(int top) {
        if (_top == UNDEFINED || top < _top) {
            _top = top;
        }
    }

    public int getBottom() {
        return _bottom;
    }

    public void setBottom(int bottom) {
        _bottom = bottom;
    }

    public void updateBottom(int bottom) {
        if (_bottom == UNDEFINED || bottom > _bottom) {
            _bottom = bottom;
        }
    }

    @Override
    public String toString() {
        return "[top=" + _top + ", bottom=" + _bottom + "]";
    }
}
