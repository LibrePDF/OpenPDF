/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Patrick Wright
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
package org.openpdf.css.parser.property;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.FSRGBColor;
import org.openpdf.css.parser.PropertyValue;

import java.util.HashMap;
import java.util.Map;

public class Conversions {
    private static final Map<String, FSRGBColor> COLORS = new HashMap<>();
    private static final Map<Float, IdentValue> NUMERIC_FONT_WEIGHTS = new HashMap<>();
    private static final Map<String, PropertyValue> BORDER_WIDTHS = new HashMap<>();

    static {
        COLORS.put("cyan", new FSRGBColor(0x00FFFF));
        COLORS.put("magenta", new FSRGBColor(0xFF00FF));
        COLORS.put("black",new FSRGBColor(0x000000));
        COLORS.put("gray",new FSRGBColor(0x808080));
        COLORS.put("grey",new FSRGBColor(0x808080));
        COLORS.put("maroon",new FSRGBColor(0x800000));
        COLORS.put("red",new FSRGBColor(0xFF0000));
        COLORS.put("green",new FSRGBColor(0x008000));
        COLORS.put("lime",new FSRGBColor(0x00FF00));
        COLORS.put("olive",new FSRGBColor(0x808000));
        COLORS.put("yellow",new FSRGBColor(0xFFFF00));
        COLORS.put("navy",new FSRGBColor(0x000080));
        COLORS.put("blue",new FSRGBColor(0x0000FF));
        COLORS.put("purple",new FSRGBColor(0x800080));
        COLORS.put("fuchsia",new FSRGBColor(0xFF00FF));
        COLORS.put("teal",new FSRGBColor(0x008080));
        COLORS.put("aqua",new FSRGBColor(0x00FFFF));
        COLORS.put("silver",new FSRGBColor(0xC0C0C0));
        COLORS.put("white",new FSRGBColor(0xFFFFFF));
        COLORS.put("aliceblue",new FSRGBColor(0xF0F8FF));
        COLORS.put("antiquewhite",new FSRGBColor(0xFAEBD7));
        COLORS.put("aquamarine",new FSRGBColor(0x7FFFD4));
        COLORS.put("azure",new FSRGBColor(0xF0FFFF));
        COLORS.put("beige",new FSRGBColor(0xF5F5DC));
        COLORS.put("blueviolet",new FSRGBColor(0x8A2BE2));
        COLORS.put("brown",new FSRGBColor(0xA52A2A));
        COLORS.put("burlywood",new FSRGBColor(0xDEB887));
        COLORS.put("cadetblue",new FSRGBColor(0x5F9EA0));
        COLORS.put("chartreuse",new FSRGBColor(0x7FFF00));
        COLORS.put("chocolate",new FSRGBColor(0xD2691E));
        COLORS.put("coral",new FSRGBColor(0xFF7F50));
        COLORS.put("cornflowerblue",new FSRGBColor(0x6495ED));
        COLORS.put("cornsilk",new FSRGBColor(0xFFF8DC));
        COLORS.put("crimson",new FSRGBColor(0xDC143C));
        COLORS.put("darkblue",new FSRGBColor(0x00008B));
        COLORS.put("darkcyan",new FSRGBColor(0x008B8B));
        COLORS.put("darkgoldenrod",new FSRGBColor(0xB8860B));
        COLORS.put("darkgray",new FSRGBColor(0xA9A9A9));
        COLORS.put("darkgreen",new FSRGBColor(0x006400));
        COLORS.put("darkkhaki",new FSRGBColor(0xBDB76B));
        COLORS.put("darkmagenta",new FSRGBColor(0x8B008B));
        COLORS.put("darkolivegreen",new FSRGBColor(0x556B2F));
        COLORS.put("darkorange",new FSRGBColor(0xFF8C00));
        COLORS.put("darkorchid",new FSRGBColor(0x9932CC));
        COLORS.put("darkred",new FSRGBColor(0x8B0000));
        COLORS.put("darksalmon",new FSRGBColor(0xE9967A));
        COLORS.put("darkseagreen",new FSRGBColor(0x8FBC8F));
        COLORS.put("darkslateblue",new FSRGBColor(0x483D8B));
        COLORS.put("darkslategray",new FSRGBColor(0x2F4F4F));
        COLORS.put("darkturquoise",new FSRGBColor(0x00CED1));
        COLORS.put("darkviolet",new FSRGBColor(0x9400D3));
        COLORS.put("deeppink",new FSRGBColor(0xFF1493));
        COLORS.put("deepskyblue",new FSRGBColor(0x00BFFF));
        COLORS.put("dimgray",new FSRGBColor(0x696969));
        COLORS.put("dodgerblue",new FSRGBColor(0x1E90FF));
        COLORS.put("firebrick",new FSRGBColor(0xB22222));
        COLORS.put("floralwhite",new FSRGBColor(0xFFFAF0));
        COLORS.put("forestgreen",new FSRGBColor(0x228B22));
        COLORS.put("gainsboro",new FSRGBColor(0xDCDCDC));
        COLORS.put("ghostwhite",new FSRGBColor(0xF8F8FF));
        COLORS.put("gold",new FSRGBColor(0xFFD700));
        COLORS.put("goldenrod",new FSRGBColor(0xDAA520));
        COLORS.put("greenyellow",new FSRGBColor(0xADFF2F));
        COLORS.put("honeydew",new FSRGBColor(0xF0FFF0));
        COLORS.put("hotpink",new FSRGBColor(0xFF69B4));
        COLORS.put("indianred",new FSRGBColor(0xCD5C5C));
        COLORS.put("indigo",new FSRGBColor(0x4B0082));
        COLORS.put("ivory",new FSRGBColor(0xFFFFF0));
        COLORS.put("khaki",new FSRGBColor(0xF0E68C));
        COLORS.put("lavender",new FSRGBColor(0xE6E6FA));
        COLORS.put("lavenderblush",new FSRGBColor(0xFFF0F5));
        COLORS.put("lawngreen",new FSRGBColor(0x7CFC00));
        COLORS.put("lemonchiffon",new FSRGBColor(0xFFFACD));
        COLORS.put("lightblue",new FSRGBColor(0xADD8E6));
        COLORS.put("lightcoral",new FSRGBColor(0xF08080));
        COLORS.put("lightcyan",new FSRGBColor(0xE0FFFF));
        COLORS.put("lightgoldenrodyellow",new FSRGBColor(0xFAFAD2));
        COLORS.put("lightgreen",new FSRGBColor(0x90EE90));
        COLORS.put("lightgrey",new FSRGBColor(0xD3D3D3));
        COLORS.put("lightpink",new FSRGBColor(0xFFB6C1));
        COLORS.put("lightsalmon",new FSRGBColor(0xFFA07A));
        COLORS.put("lightseagreen",new FSRGBColor(0x20B2AA));
        COLORS.put("lightskyblue",new FSRGBColor(0x87CEFA));
        COLORS.put("lightslategray",new FSRGBColor(0x778899));
        COLORS.put("lightsteelblue",new FSRGBColor(0xB0C4DE));
        COLORS.put("lightyellow",new FSRGBColor(0xFFFFE0));
        COLORS.put("limegreen",new FSRGBColor(0x32CD32));
        COLORS.put("linen",new FSRGBColor(0xFAF0E6));
        COLORS.put("mediumaquamarine",new FSRGBColor(0x66CDAA));
        COLORS.put("mediumblue",new FSRGBColor(0x0000CD));
        COLORS.put("mediumorchid",new FSRGBColor(0xBA55D3));
        COLORS.put("mediumpurple",new FSRGBColor(0x9370DB));
        COLORS.put("mediumseagreen",new FSRGBColor(0x3CB371));
        COLORS.put("mediumslateblue",new FSRGBColor(0x7B68EE));
        COLORS.put("mediumspringgreen",new FSRGBColor(0x00FA9A));
        COLORS.put("mediumturquoise",new FSRGBColor(0x48D1CC));
        COLORS.put("mediumvioletred",new FSRGBColor(0xC71585));
        COLORS.put("midnightblue",new FSRGBColor(0x191970));
        COLORS.put("mintcream",new FSRGBColor(0xF5FFFA));
        COLORS.put("mistyrose",new FSRGBColor(0xFFE4E1));
        COLORS.put("moccasin",new FSRGBColor(0xFFE4B5));
        COLORS.put("navajowhite",new FSRGBColor(0xFFDEAD));
        COLORS.put("oldlace",new FSRGBColor(0xFDF5E6));
        COLORS.put("olivedrab",new FSRGBColor(0x6B8E23));
        COLORS.put("orange",new FSRGBColor(0xFFA500));
        COLORS.put("orangered",new FSRGBColor(0xFF4500));
        COLORS.put("orchid",new FSRGBColor(0xDA70D6));
        COLORS.put("palegoldenrod",new FSRGBColor(0xEEE8AA));
        COLORS.put("palegreen",new FSRGBColor(0x98FB98));
        COLORS.put("paleturquoise",new FSRGBColor(0xAFEEEE));
        COLORS.put("palevioletred",new FSRGBColor(0xDB7093));
        COLORS.put("papayawhip",new FSRGBColor(0xFFEFD5));
        COLORS.put("peachpuff",new FSRGBColor(0xFFDAB9));
        COLORS.put("peru",new FSRGBColor(0xCD853F));
        COLORS.put("pink",new FSRGBColor(0xFFC0CB));
        COLORS.put("plum",new FSRGBColor(0xDDA0DD));
        COLORS.put("powderblue",new FSRGBColor(0xB0E0E6));
        COLORS.put("rosybrown",new FSRGBColor(0xBC8F8F));
        COLORS.put("royalblue",new FSRGBColor(0x4169E1));
        COLORS.put("saddlebrown",new FSRGBColor(0x8B4513));
        COLORS.put("salmon",new FSRGBColor(0xFA8072));
        COLORS.put("sandybrown",new FSRGBColor(0xF4A460));
        COLORS.put("seagreen",new FSRGBColor(0x2E8B57));
        COLORS.put("seashell",new FSRGBColor(0xFFF5EE));
        COLORS.put("sienna",new FSRGBColor(0xA0522D));
        COLORS.put("skyblue",new FSRGBColor(0x87CEEB));
        COLORS.put("slateblue",new FSRGBColor(0x6A5ACD));
        COLORS.put("slategray",new FSRGBColor(0x708090));
        COLORS.put("snow",new FSRGBColor(0xFFFAFA));
        COLORS.put("springgreen",new FSRGBColor(0x00FF7F));
        COLORS.put("steelblue",new FSRGBColor(0x4682B4));
        COLORS.put("tan",new FSRGBColor(0xD2B48C));
        COLORS.put("thistle",new FSRGBColor(0xD8BFD8));
        COLORS.put("tomato",new FSRGBColor(0xFF6347));
        COLORS.put("turquoise",new FSRGBColor(0x40E0D0));
        COLORS.put("violet",new FSRGBColor(0xEE82E));
        COLORS.put("wheat",new FSRGBColor(0xF5DEB3));
        COLORS.put("whitesmoke",new FSRGBColor(0xF5F5F5));
        COLORS.put("yellowgreen",new FSRGBColor(0x9ACD32));
    }

    static {
        NUMERIC_FONT_WEIGHTS.put(100.0f, IdentValue.FONT_WEIGHT_100);
        NUMERIC_FONT_WEIGHTS.put(200.0f, IdentValue.FONT_WEIGHT_200);
        NUMERIC_FONT_WEIGHTS.put(300.0f, IdentValue.FONT_WEIGHT_300);
        NUMERIC_FONT_WEIGHTS.put(400.0f, IdentValue.FONT_WEIGHT_400);
        NUMERIC_FONT_WEIGHTS.put(500.0f, IdentValue.FONT_WEIGHT_500);
        NUMERIC_FONT_WEIGHTS.put(600.0f, IdentValue.FONT_WEIGHT_600);
        NUMERIC_FONT_WEIGHTS.put(700.0f, IdentValue.FONT_WEIGHT_700);
        NUMERIC_FONT_WEIGHTS.put(800.0f, IdentValue.FONT_WEIGHT_800);
        NUMERIC_FONT_WEIGHTS.put(900.0f, IdentValue.FONT_WEIGHT_900);
    }

    static {
        BORDER_WIDTHS.put("thin", new PropertyValue(CSSPrimitiveValue.CSS_PX, 1.0f, "1px"));
        BORDER_WIDTHS.put("medium", new PropertyValue(CSSPrimitiveValue.CSS_PX, 2.0f, "2px"));
        BORDER_WIDTHS.put("thick", new PropertyValue(CSSPrimitiveValue.CSS_PX, 3.0f, "3px"));
    }

    @Nullable
    public static FSRGBColor getColor(String ident) {
        return COLORS.get(ident);
    }

    public static IdentValue getNumericFontWeight(float weight) {
        return NUMERIC_FONT_WEIGHTS.get(weight);
    }

    public static PropertyValue getBorderWidth(String ident) {
        return BORDER_WIDTHS.get(ident);
    }

}
