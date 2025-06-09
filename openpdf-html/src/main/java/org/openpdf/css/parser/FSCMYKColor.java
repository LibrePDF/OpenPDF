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
package org.openpdf.css.parser;

import com.google.errorprone.annotations.CheckReturnValue;

public class FSCMYKColor implements FSColor {
    private final float _cyan;
    private final float _magenta;
    private final float _yellow;
    private final float _black;

    public FSCMYKColor(float cyan, float magenta, float yellow, float black) {
        _cyan = validateColor(cyan, "Cyan");
        _magenta = validateColor(magenta, "Magenta");
        _yellow = validateColor(yellow, "Yellow");
        _black = validateColor(black, "Black");
    }

    private float validateColor(float c, String name) {
        if (c < 0 || c > 1) {
            throw new IllegalArgumentException(String.format("%s %s is out of range [0, 1]", name, c));
        }
        return c;
    }

    public float getCyan() {
        return _cyan;
    }

    public float getMagenta() {
        return _magenta;
    }

    public float getYellow() {
        return _yellow;
    }

    public float getBlack() {
        return _black;
    }

    @Override
    public String toString() {
        return "cmyk(" + _cyan + ", " + _magenta + ", " + _yellow + ", " + _black + ")";
    }

    @CheckReturnValue
    @Override
    public FSColor lightenColor() {
        return new FSCMYKColor(_cyan * 0.8f, _magenta * 0.8f, _yellow * 0.8f, _black);
    }

    @CheckReturnValue
    @Override
    public FSColor darkenColor() {
        return new FSCMYKColor(
                Math.min(1.0f, _cyan / 0.8f), Math.min(1.0f, _magenta / 0.8f),
                Math.min(1.0f, _yellow / 0.8f), _black);
    }
}
