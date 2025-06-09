/*
 * {{{ header & license
 * Copyright (c) 2010 Wisconsin Court System
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

import org.openpdf.css.parser.PropertyValue;

public class BackgroundSize {
    private boolean _contain;
    private boolean _cover;
    private boolean _bothAuto;

    private PropertyValue _width;
    private PropertyValue _height;


    public BackgroundSize(boolean contain, boolean cover, boolean bothAuto) {
        _contain = contain;
        _cover = cover;
        _bothAuto = bothAuto;
    }

    public BackgroundSize(PropertyValue width, PropertyValue height) {
        _width = width;
        _height = height;
    }

    public boolean isContain() {
        return _contain;
    }

    public boolean isCover() {
        return _cover;
    }

    public boolean isBothAuto() {
        return _bothAuto;
    }

    public PropertyValue getWidth() {
        return _width;
    }

    public PropertyValue getHeight() {
        return _height;
    }
}
