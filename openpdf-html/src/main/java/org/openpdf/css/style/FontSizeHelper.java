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
package org.openpdf.css.style;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.PropertyValue;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class FontSizeHelper {
    private static final Map<IdentValue, PropertyValue> PROPORTIONAL_FONT_SIZES = new LinkedHashMap<>();
    private static final Map<IdentValue, PropertyValue> FIXED_FONT_SIZES = new LinkedHashMap<>();

    private static final PropertyValue DEFAULT_SMALLER = new PropertyValue(CSSPrimitiveValue.CSS_EMS, 0.8f, "0.8em");
    private static final PropertyValue DEFAULT_LARGER = new PropertyValue(CSSPrimitiveValue.CSS_EMS, 1.2f, "1.2em");

    static {
        // XXX Should come from (or be influenced by) the UA.  These sizes
        // correspond to the Firefox defaults
        PROPORTIONAL_FONT_SIZES.put(IdentValue.XX_SMALL, new PropertyValue(CSSPrimitiveValue.CSS_PX, 9f, "9px"));
        PROPORTIONAL_FONT_SIZES.put(IdentValue.X_SMALL, new PropertyValue(CSSPrimitiveValue.CSS_PX, 10f, "10px"));
        PROPORTIONAL_FONT_SIZES.put(IdentValue.SMALL, new PropertyValue(CSSPrimitiveValue.CSS_PX, 13f, "13px"));
        PROPORTIONAL_FONT_SIZES.put(IdentValue.MEDIUM, new PropertyValue(CSSPrimitiveValue.CSS_PX, 16f, "16px"));
        PROPORTIONAL_FONT_SIZES.put(IdentValue.LARGE, new PropertyValue(CSSPrimitiveValue.CSS_PX, 18f, "18px"));
        PROPORTIONAL_FONT_SIZES.put(IdentValue.X_LARGE, new PropertyValue(CSSPrimitiveValue.CSS_PX, 24f, "24px"));
        PROPORTIONAL_FONT_SIZES.put(IdentValue.XX_LARGE, new PropertyValue(CSSPrimitiveValue.CSS_PX, 32f, "32px"));

        FIXED_FONT_SIZES.put(IdentValue.XX_SMALL, new PropertyValue(CSSPrimitiveValue.CSS_PX, 9f, "9px"));
        FIXED_FONT_SIZES.put(IdentValue.X_SMALL, new PropertyValue(CSSPrimitiveValue.CSS_PX, 10f, "10px"));
        FIXED_FONT_SIZES.put(IdentValue.SMALL, new PropertyValue(CSSPrimitiveValue.CSS_PX, 12f, "12px"));
        FIXED_FONT_SIZES.put(IdentValue.MEDIUM, new PropertyValue(CSSPrimitiveValue.CSS_PX, 13f, "13px"));
        FIXED_FONT_SIZES.put(IdentValue.LARGE, new PropertyValue(CSSPrimitiveValue.CSS_PX, 16f, "16px"));
        FIXED_FONT_SIZES.put(IdentValue.X_LARGE, new PropertyValue(CSSPrimitiveValue.CSS_PX, 20f, "20px"));
        FIXED_FONT_SIZES.put(IdentValue.XX_LARGE, new PropertyValue(CSSPrimitiveValue.CSS_PX, 26f, "26px"));
    }

    public static IdentValue getNextSmaller(IdentValue absFontSize) {
        IdentValue prev = null;
        for (IdentValue ident : PROPORTIONAL_FONT_SIZES.keySet()) {
            if (ident == absFontSize) {
                return prev;
            }
            prev = ident;
        }
        return null;
    }

    public static IdentValue getNextLarger(IdentValue absFontSize) {
        for (Iterator<IdentValue> i = PROPORTIONAL_FONT_SIZES.keySet().iterator(); i.hasNext(); ) {
            IdentValue ident = i.next();
            if (ident == absFontSize && i.hasNext()) {
                return i.next();
            }
        }
        return null;
    }

    public static PropertyValue resolveAbsoluteFontSize(IdentValue fontSize, String[] fontFamilies) {
        boolean monospace = isMonospace(fontFamilies);

        if (monospace) {
            return FIXED_FONT_SIZES.get(fontSize);
        } else {
            return PROPORTIONAL_FONT_SIZES.get(fontSize);
        }
    }

    public static PropertyValue getDefaultRelativeFontSize(IdentValue fontSize) {
        if (fontSize == IdentValue.LARGER) {
            return DEFAULT_LARGER;
        } else if (fontSize == IdentValue.SMALLER) {
            return DEFAULT_SMALLER;
        } else {
            return null;
        }
    }

    private static boolean isMonospace(String[] fontFamilies) {
        for (String fontFamily : fontFamilies) {
            if (fontFamily.equals("monospace")) {
                return true;
            }
        }
        return false;
    }
}
