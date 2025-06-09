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

import java.util.Objects;

public class FSRGBColor implements FSColor {
    public static final FSRGBColor TRANSPARENT = new FSRGBColor(0, 0, 0);
    public static final FSRGBColor RED = new FSRGBColor(255, 0, 0);
    public static final FSRGBColor GREEN = new FSRGBColor(0, 255, 0);
    public static final FSRGBColor BLUE = new FSRGBColor(0, 0, 255);

    private final int _red;
    private final int _green;
    private final int _blue;
    private final float _alpha;

    public FSRGBColor(int red, int green, int blue) {
        this(red, green, blue, 1.0f);
    }

    public FSRGBColor(int red, int green, int blue, float alpha) {
        _red = validateColor("Red", red);
        _green = validateColor("Green", green);
        _blue = validateColor("Blue", blue);
        _alpha = validateAlpha(alpha);
    }

    private int validateColor(String name, int color) {
        if (color < 0 || color > 255) {
            throw new IllegalArgumentException(String.format("%s %s is out of range [0, 255]", name, color));
        }
        return color;
    }

    private float validateAlpha(float alpha) {
        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException(String.format("alpha %s is out of range [0, 1]", alpha));
        }
        return alpha;
    }

    public FSRGBColor(int color) {
        this(((color & 0xff0000) >> 16), ((color & 0x00ff00) >> 8), color & 0xff);
    }

    public int getBlue() {
        return _blue;
    }

    public int getGreen() {
        return _green;
    }

    public int getRed() {
        return _red;
    }

    public float getAlpha() {
        return _alpha;
    }


    @Override
    public String toString() {
        if (_alpha != 1) {
            return "rgba(" + _red + "," + _green + "," + _blue + "," + _alpha + ")";
        } else {
            return '#' + toString(_red) + toString(_green) + toString(_blue);
        }
    }

    private String toString(int color) {
        return String.format("%02x", color);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FSRGBColor that)) return false;

        return _blue == that._blue && _green == that._green && _red == that._red && _alpha == that._alpha;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_red, _green, _blue);
    }

    @CheckReturnValue
    @Override
    public FSColor lightenColor() {
        HSBColor hsb = toHSB();
        float sLighter = 0.35f * hsb.brightness() * hsb.saturation();
        float bLighter = 0.6999f + 0.3f * hsb.brightness();
        return new HSBColor(hsb.hue(), sLighter, bLighter).toRGB();
    }

    @CheckReturnValue
    @Override
    public FSColor darkenColor() {
        HSBColor hsb = toHSB();
        float hBase = hsb.hue();
        float sBase = hsb.saturation();
        float bBase = hsb.brightness();
        float bDarker = 0.56f * bBase;

        return new HSBColor(hBase, sBase, bDarker).toRGB();
    }

    HSBColor toHSB() {
        return RGBtoHSB(getRed(), getGreen(), getBlue());
    }

    // Taken from java.awt.Color to avoid dependency on it
    private static HSBColor RGBtoHSB(int r, int g, int b) {
        final float cmax = max(r, g, b);
        final float cmin = min(r, g, b);
        final float brightness = cmax / 255.0f;
        final float saturation = cmax == 0f ? 0f : (cmax - cmin) / cmax;
        final float hue = saturation == 0 ? 0 : calculateHue(r, g, b, cmax, cmin);
        return new HSBColor(hue, saturation, brightness);
    }

    private static float calculateHue(int r, int g, int b, float cmax, float cmin) {
        float redc = (cmax - r) / (cmax - cmin);
        float greenc = (cmax - g) / (cmax - cmin);
        float bluec = (cmax - b) / (cmax - cmin);
        final float hue1 = (r == cmax) ?
            bluec - greenc : (g == cmax) ?
            2.0f + redc - bluec :
            4.0f + greenc - redc;

        float hue2 = hue1 / 6.0f;
        return hue1 < 0 ? hue2 + 1.0f : hue2;
    }

    private static float max(int a, int b, int c) {
        return Math.max(Math.max(a, b), c);
    }

    private static float min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
}
