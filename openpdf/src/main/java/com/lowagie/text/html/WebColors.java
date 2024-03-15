/*
 * $Id: WebColors.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2001, 2002 by Bruno Lowagie.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.html;

import com.lowagie.text.error_messages.MessageLocalization;
import java.awt.Color;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * This class is a HashMap that contains the names of colors as a key and the corresponding Color as value. (Source:
 * Wikipedia http://en.wikipedia.org/wiki/Web_colors )
 * <p>
 * CSS4 Implementation based on: https://developer.mozilla.org/en-US/docs/Web/CSS/color_value
 *
 * @author blowagie
 */
public class WebColors extends HashMap<String, int[]> {

    /**
     * HashMap containing all the names and corresponding color values.
     */
    public static final WebColors NAMES = new WebColors();
    private static final long serialVersionUID = 3542523100813372896L;

    static {
        NAMES.put("aliceblue", new int[]{0xf0, 0xf8, 0xff, 0xff});
        NAMES.put("antiquewhite", new int[]{0xfa, 0xeb, 0xd7, 0xff});
        NAMES.put("aqua", new int[]{0x00, 0xff, 0xff, 0xff});
        NAMES.put("aquamarine", new int[]{0x7f, 0xff, 0xd4, 0xff});
        NAMES.put("azure", new int[]{0xf0, 0xff, 0xff, 0xff});
        NAMES.put("beige", new int[]{0xf5, 0xf5, 0xdc, 0xff});
        NAMES.put("bisque", new int[]{0xff, 0xe4, 0xc4, 0xff});
        NAMES.put("black", new int[]{0x00, 0x00, 0x00, 0xff});
        NAMES.put("blanchedalmond", new int[]{0xff, 0xeb, 0xcd, 0xff});
        NAMES.put("blue", new int[]{0x00, 0x00, 0xff, 0xff});
        NAMES.put("blueviolet", new int[]{0x8a, 0x2b, 0xe2, 0xff});
        NAMES.put("brown", new int[]{0xa5, 0x2a, 0x2a, 0xff});
        NAMES.put("burlywood", new int[]{0xde, 0xb8, 0x87, 0xff});
        NAMES.put("cadetblue", new int[]{0x5f, 0x9e, 0xa0, 0xff});
        NAMES.put("chartreuse", new int[]{0x7f, 0xff, 0x00, 0xff});
        NAMES.put("chocolate", new int[]{0xd2, 0x69, 0x1e, 0xff});
        NAMES.put("coral", new int[]{0xff, 0x7f, 0x50, 0xff});
        NAMES.put("cornflowerblue", new int[]{0x64, 0x95, 0xed, 0xff});
        NAMES.put("cornsilk", new int[]{0xff, 0xf8, 0xdc, 0xff});
        NAMES.put("crimson", new int[]{0xdc, 0x14, 0x3c, 0xff});
        NAMES.put("cyan", new int[]{0x00, 0xff, 0xff, 0xff});
        NAMES.put("darkblue", new int[]{0x00, 0x00, 0x8b, 0xff});
        NAMES.put("darkcyan", new int[]{0x00, 0x8b, 0x8b, 0xff});
        NAMES.put("darkgoldenrod", new int[]{0xb8, 0x86, 0x0b, 0xff});
        NAMES.put("darkgray", new int[]{0xa9, 0xa9, 0xa9, 0xff});
        NAMES.put("darkgreen", new int[]{0x00, 0x64, 0x00, 0xff});
        NAMES.put("darkkhaki", new int[]{0xbd, 0xb7, 0x6b, 0xff});
        NAMES.put("darkmagenta", new int[]{0x8b, 0x00, 0x8b, 0xff});
        NAMES.put("darkolivegreen", new int[]{0x55, 0x6b, 0x2f, 0xff});
        NAMES.put("darkorange", new int[]{0xff, 0x8c, 0x00, 0xff});
        NAMES.put("darkorchid", new int[]{0x99, 0x32, 0xcc, 0xff});
        NAMES.put("darkred", new int[]{0x8b, 0x00, 0x00, 0xff});
        NAMES.put("darksalmon", new int[]{0xe9, 0x96, 0x7a, 0xff});
        NAMES.put("darkseagreen", new int[]{0x8f, 0xbc, 0x8f, 0xff});
        NAMES.put("darkslateblue", new int[]{0x48, 0x3d, 0x8b, 0xff});
        NAMES.put("darkslategray", new int[]{0x2f, 0x4f, 0x4f, 0xff});
        NAMES.put("darkslategrey", new int[]{0x2f, 0x4f, 0x4f, 0xff});
        NAMES.put("darkturquoise", new int[]{0x00, 0xce, 0xd1, 0xff});
        NAMES.put("darkviolet", new int[]{0x94, 0x00, 0xd3, 0xff});
        NAMES.put("deeppink", new int[]{0xff, 0x14, 0x93, 0xff});
        NAMES.put("deepskyblue", new int[]{0x00, 0xbf, 0xff, 0xff});
        NAMES.put("dimgray", new int[]{0x69, 0x69, 0x69, 0xff});
        NAMES.put("dimgrey", new int[]{0x69, 0x69, 0x69, 0xff});
        NAMES.put("dodgerblue", new int[]{0x1e, 0x90, 0xff, 0xff});
        NAMES.put("firebrick", new int[]{0xb2, 0x22, 0x22, 0xff});
        NAMES.put("floralwhite", new int[]{0xff, 0xfa, 0xf0, 0xff});
        NAMES.put("forestgreen", new int[]{0x22, 0x8b, 0x22, 0xff});
        NAMES.put("fuchsia", new int[]{0xff, 0x00, 0xff, 0xff});
        NAMES.put("gainsboro", new int[]{0xdc, 0xdc, 0xdc, 0xff});
        NAMES.put("ghostwhite", new int[]{0xf8, 0xf8, 0xff, 0xff});
        NAMES.put("gold", new int[]{0xff, 0xd7, 0x00, 0xff});
        NAMES.put("goldenrod", new int[]{0xda, 0xa5, 0x20, 0xff});
        NAMES.put("gray", new int[]{0x80, 0x80, 0x80, 0xff});
        NAMES.put("green", new int[]{0x00, 0x80, 0x00, 0xff});
        NAMES.put("greenyellow", new int[]{0xad, 0xff, 0x2f, 0xff});
        NAMES.put("honeydew", new int[]{0xf0, 0xff, 0xf0, 0xff});
        NAMES.put("hotpink", new int[]{0xff, 0x69, 0xb4, 0xff});
        NAMES.put("indianred", new int[]{0xcd, 0x5c, 0x5c, 0xff});
        NAMES.put("indigo", new int[]{0x4b, 0x00, 0x82, 0xff});
        NAMES.put("ivory", new int[]{0xff, 0xff, 0xf0, 0xff});
        NAMES.put("khaki", new int[]{0xf0, 0xe6, 0x8c, 0xff});
        NAMES.put("lavender", new int[]{0xe6, 0xe6, 0xfa, 0xff});
        NAMES.put("lavenderblush", new int[]{0xff, 0xf0, 0xf5, 0xff});
        NAMES.put("lawngreen", new int[]{0x7c, 0xfc, 0x00, 0xff});
        NAMES.put("lemonchiffon", new int[]{0xff, 0xfa, 0xcd, 0xff});
        NAMES.put("lightblue", new int[]{0xad, 0xd8, 0xe6, 0xff});
        NAMES.put("lightcoral", new int[]{0xf0, 0x80, 0x80, 0xff});
        NAMES.put("lightcyan", new int[]{0xe0, 0xff, 0xff, 0xff});
        NAMES.put("lightgoldenrodyellow", new int[]{0xfa, 0xfa, 0xd2, 0xff});
        NAMES.put("lightgray", new int[]{0xd3, 0xd3, 0xd3, 0xff});
        NAMES.put("lightgreen", new int[]{0x90, 0xee, 0x90, 0xff});
        NAMES.put("lightgrey", new int[]{0xd3, 0xd3, 0xd3, 0xff});
        NAMES.put("lightpink", new int[]{0xff, 0xb6, 0xc1, 0xff});
        NAMES.put("lightsalmon", new int[]{0xff, 0xa0, 0x7a, 0xff});
        NAMES.put("lightseagreen", new int[]{0x20, 0xb2, 0xaa, 0xff});
        NAMES.put("lightskyblue", new int[]{0x87, 0xce, 0xfa, 0xff});
        NAMES.put("lightslategray", new int[]{0x77, 0x88, 0x99, 0xff});
        NAMES.put("lightslategrey", new int[]{0x77, 0x88, 0x99, 0xff});
        NAMES.put("lightsteelblue", new int[]{0xb0, 0xc4, 0xde, 0xff});
        NAMES.put("lightyellow", new int[]{0xff, 0xff, 0xe0, 0xff});
        NAMES.put("lime", new int[]{0x00, 0xff, 0x00, 0xff});
        NAMES.put("limegreen", new int[]{0x32, 0xcd, 0x32, 0xff});
        NAMES.put("linen", new int[]{0xfa, 0xf0, 0xe6, 0xff});
        NAMES.put("magenta", new int[]{0xff, 0x00, 0xff, 0xff});
        NAMES.put("maroon", new int[]{0x80, 0x00, 0x00, 0xff});
        NAMES.put("mediumaquamarine", new int[]{0x66, 0xcd, 0xaa, 0xff});
        NAMES.put("mediumblue", new int[]{0x00, 0x00, 0xcd, 0xff});
        NAMES.put("mediumorchid", new int[]{0xba, 0x55, 0xd3, 0xff});
        NAMES.put("mediumpurple", new int[]{0x93, 0x70, 0xdb, 0xff});
        NAMES.put("mediumseagreen", new int[]{0x3c, 0xb3, 0x71, 0xff});
        NAMES.put("mediumslateblue", new int[]{0x7b, 0x68, 0xee, 0xff});
        NAMES.put("mediumspringgreen", new int[]{0x00, 0xfa, 0x9a, 0xff});
        NAMES.put("mediumturquoise", new int[]{0x48, 0xd1, 0xcc, 0xff});
        NAMES.put("mediumvioletred", new int[]{0xc7, 0x15, 0x85, 0xff});
        NAMES.put("midnightblue", new int[]{0x19, 0x19, 0x70, 0xff});
        NAMES.put("mintcream", new int[]{0xf5, 0xff, 0xfa, 0xff});
        NAMES.put("mistyrose", new int[]{0xff, 0xe4, 0xe1, 0xff});
        NAMES.put("moccasin", new int[]{0xff, 0xe4, 0xb5, 0xff});
        NAMES.put("navajowhite", new int[]{0xff, 0xde, 0xad, 0xff});
        NAMES.put("navy", new int[]{0x00, 0x00, 0x80, 0xff});
        NAMES.put("oldlace", new int[]{0xfd, 0xf5, 0xe6, 0xff});
        NAMES.put("olive", new int[]{0x80, 0x80, 0x00, 0xff});
        NAMES.put("olivedrab", new int[]{0x6b, 0x8e, 0x23, 0xff});
        NAMES.put("orange", new int[]{0xff, 0xa5, 0x00, 0xff});
        NAMES.put("orangered", new int[]{0xff, 0x45, 0x00, 0xff});
        NAMES.put("orchid", new int[]{0xda, 0x70, 0xd6, 0xff});
        NAMES.put("palegoldenrod", new int[]{0xee, 0xe8, 0xaa, 0xff});
        NAMES.put("palegreen", new int[]{0x98, 0xfb, 0x98, 0xff});
        NAMES.put("paleturquoise", new int[]{0xaf, 0xee, 0xee, 0xff});
        NAMES.put("palevioletred", new int[]{0xdb, 0x70, 0x93, 0xff});
        NAMES.put("papayawhip", new int[]{0xff, 0xef, 0xd5, 0xff});
        NAMES.put("peachpuff", new int[]{0xff, 0xda, 0xb9, 0xff});
        NAMES.put("peru", new int[]{0xcd, 0x85, 0x3f, 0xff});
        NAMES.put("pink", new int[]{0xff, 0xc0, 0xcb, 0xff});
        NAMES.put("plum", new int[]{0xdd, 0xa0, 0xdd, 0xff});
        NAMES.put("powderblue", new int[]{0xb0, 0xe0, 0xe6, 0xff});
        NAMES.put("purple", new int[]{0x80, 0x00, 0x80, 0xff});
        NAMES.put("rebeccapurple", new int[]{0x66, 0x33, 0x99, 0xff});
        NAMES.put("red", new int[]{0xff, 0x00, 0x00, 0xff});
        NAMES.put("rosybrown", new int[]{0xbc, 0x8f, 0x8f, 0xff});
        NAMES.put("royalblue", new int[]{0x41, 0x69, 0xe1, 0xff});
        NAMES.put("saddlebrown", new int[]{0x8b, 0x45, 0x13, 0xff});
        NAMES.put("salmon", new int[]{0xfa, 0x80, 0x72, 0xff});
        NAMES.put("sandybrown", new int[]{0xf4, 0xa4, 0x60, 0xff});
        NAMES.put("seagreen", new int[]{0x2e, 0x8b, 0x57, 0xff});
        NAMES.put("seashell", new int[]{0xff, 0xf5, 0xee, 0xff});
        NAMES.put("sienna", new int[]{0xa0, 0x52, 0x2d, 0xff});
        NAMES.put("silver", new int[]{0xc0, 0xc0, 0xc0, 0xff});
        NAMES.put("skyblue", new int[]{0x87, 0xce, 0xeb, 0xff});
        NAMES.put("slateblue", new int[]{0x6a, 0x5a, 0xcd, 0xff});
        NAMES.put("slategray", new int[]{0x70, 0x80, 0x90, 0xff});
        NAMES.put("slategrey", new int[]{0x70, 0x80, 0x90, 0xff});
        NAMES.put("snow", new int[]{0xff, 0xfa, 0xfa, 0xff});
        NAMES.put("springgreen", new int[]{0x00, 0xff, 0x7f, 0xff});
        NAMES.put("steelblue", new int[]{0x46, 0x82, 0xb4, 0xff});
        NAMES.put("tan", new int[]{0xd2, 0xb4, 0x8c, 0xff});
        NAMES.put("transparent", new int[]{0x00, 0x00, 0x00, 0x00});
        NAMES.put("teal", new int[]{0x00, 0x80, 0x80, 0xff});
        NAMES.put("thistle", new int[]{0xd8, 0xbf, 0xd8, 0xff});
        NAMES.put("tomato", new int[]{0xff, 0x63, 0x47, 0xff});
        NAMES.put("turquoise", new int[]{0x40, 0xe0, 0xd0, 0xff});
        NAMES.put("violet", new int[]{0xee, 0x82, 0xee, 0xff});
        NAMES.put("wheat", new int[]{0xf5, 0xde, 0xb3, 0xff});
        NAMES.put("white", new int[]{0xff, 0xff, 0xff, 0xff});
        NAMES.put("whitesmoke", new int[]{0xf5, 0xf5, 0xf5, 0xff});
        NAMES.put("yellow", new int[]{0xff, 0xff, 0x00, 0xff});
        NAMES.put("yellowgreen", new int[]{0x9, 0xacd, 0x32, 0xff});
    }

    /**
     * Gives you a Color based on a name.
     *
     * @param name the css color name to convert. You can use CSS4 color values
     *             <ul>
     *                 <li>a name such as black, violet, cornflowerblue</li>
     *                 <li>#RGB, #RRGGBB, #RGBA or #RRGGBBAA</li>
     *                 <li>rgb(R, G, B) rgb(R,G,B,A) or rgba(R,G,B) or rgba(R,G,B,A)</li>
     *                 <li>hsl(H, S, L) hsl(H,S,L,A) or hslq(H,S,L) or hsla(H,S,L,A)</li>
     *             </ul>
     * @return the corresponding Color object
     * @throws IllegalArgumentException if the String isn't a know representation of a color.
     */
    public static Color getRGBColor(String name) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        String colorName = name.trim().toLowerCase(Locale.ROOT);
        if ("".equals(colorName)) {
            throw new IllegalArgumentException("name must not be empty");
        }

        if (colorName.startsWith("#")) {
            return getRGBFromHex(colorName);
        } else if (name.startsWith("rgb")) {
            return getRGBFromRGB(colorName);
        } else if (name.startsWith("hsl")) {
            return getRGBFromHSL(colorName);
        } else {
            return getRGBFromName(colorName);
        }
    }

    private static Color getRGBFromHex(String colorName) {

        int length = colorName.length();
        if (!colorName.matches("^#[a-f0-9]{3,8}$") || !(length == 4 || length == 5 || length == 7 || length == 9)) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("unknown.color.format.must.be.rgb.or.rrggbb"));
        }
        String rgb = colorName.substring(1);
        if (length == 4 || length == 5) {
            StringBuilder sb = new StringBuilder();
            for (char c : rgb.toCharArray()) {
                sb.append(c).append(c);
            }
            rgb = sb.toString();
        }

        int[] c = {0, 0, 0, 0xff};
        c[0] = Integer.parseInt(rgb.substring(0, 2), 16);
        c[1] = Integer.parseInt(rgb.substring(2, 4), 16);
        c[2] = Integer.parseInt(rgb.substring(4, 6), 16);
        if (rgb.length() > 6) {
            // alpha component
            c[3] = Integer.parseInt(rgb.substring(6, 8), 16);
        }
        return new Color(c[0], c[1], c[2], c[3]);
    }

    private static Color getRGBFromRGB(String colorName) {
        int[] c = {0, 0, 0, 0xff};

        String rgb = "";
        if (colorName.startsWith("rgba")) {
            rgb = colorName.substring(4);
        } else {
            rgb = colorName.substring(3);
        }

        StringTokenizer tok = new StringTokenizer(rgb, "()/, \t\r\n\f");

        for (int k = 0; k < 3; k++) {
            String v = tok.nextToken();
            if (v.endsWith("%")) {
                c[k] = getFromPercent(v, 255);
            } else {
                c[k] = (int) Double.parseDouble(v);
            }
            if (c[k] < 0) {
                c[k] = 0;
            } else if (c[k] > 255) {
                c[k] = 255;
            }
        }
        if (tok.hasMoreElements()) {
            // alpha
            String v = tok.nextToken();
            if (v.endsWith("%")) {
                c[3] = getFromPercent(v, 255);
            } else {
                c[3] = (int) (Double.parseDouble(v) * 255);
            }
        }
        return new Color(c[0], c[1], c[2], c[3]);
    }

    private static int getFromPercent(String v, int max) {
        double percent = Double.parseDouble(v.substring(0, v.length() - 1));
        double result = percent * max / 100;
        return Double.valueOf(result).intValue();
    }


    private static Color getRGBFromName(String colorName) {
        if (!NAMES.containsKey(colorName)) {
            throw new IllegalArgumentException("Color '" + colorName + "' not found.");
        }
        int[] c = NAMES.get(colorName);
        return new Color(c[0], c[1], c[2], c[3]);
    }


    private static Color getRGBFromHSL(String colorName) {
        String hsl = "";
        if (colorName.startsWith("hsla")) {
            hsl = colorName.substring(4);
        } else {
            hsl = colorName.substring(3);
        }
        StringTokenizer tok = new StringTokenizer(hsl, "()/, \t\r\n\f");
        String hue = "";
        String saturation = "";
        String lightness = "";
        String alpha = "1";
        if (tok.hasMoreElements()) {
            hue = tok.nextToken();
        }
        if (tok.hasMoreElements()) {
            saturation = tok.nextToken();
        }
        if (tok.hasMoreElements()) {
            lightness = tok.nextToken();
        }
        if (tok.hasMoreElements()) {
            alpha = tok.nextToken();
        }
        if (hue.isEmpty() || saturation.isEmpty() || lightness.isEmpty() || !saturation.endsWith("%")
                || !lightness.endsWith("%")) {
            throw new IllegalArgumentException("Not a valid hsl color:" + colorName);
        }
        float hueDegrees = Double.valueOf(toDegrees(hue)).floatValue();
        float sat = Float.parseFloat(saturation.substring(0, saturation.length() - 1));
        float light = Float.parseFloat(lightness.substring(0, lightness.length() - 1));
        int alp = 255;
        if (alpha.endsWith("%")) {
            alp = getFromPercent(alpha, 255);
        } else {
            alp = (int) (Double.parseDouble(alpha) * 255);
        }
        int[] rgb = hsl2rgb(hueDegrees, sat / 100.0f, light / 100.0f);

        return new Color(rgb[0], rgb[1], rgb[2], alp);


    }

    // H (hue) is an <angle> of the color circle given in degs, rads, grads, or turns
    private static double toDegrees(String hueString) {
        if (hueString.endsWith("deg")) {
            double degrees = Double.parseDouble(hueString.substring(0, hueString.length() - 3));
            return degrees % 360;
        }
        if (hueString.endsWith("rad")) {
            double radians = Double.parseDouble(hueString.substring(0, hueString.length() - 3));
            double degrees = Math.toDegrees(radians);
            return degrees % 360;

        }
        if (hueString.endsWith("grad")) {
            double gradians = Double.parseDouble(hueString.substring(0, hueString.length() - 4));
            double degrees = gradians * 360 / 400;
            return degrees % 360;
        }
        if (hueString.endsWith("turn")) {
            double turns = Double.parseDouble(hueString.substring(0, hueString.length() - 4));
            double degrees = turns * 360;
            return degrees % 360;
        }
        double degrees = Double.parseDouble(hueString);
        return degrees % 360;
    }


    /**
     * Convert from hsl to rgb
     *
     * @param hue        Hue angle in degrees
     * @param saturation saturation (0 to 1)
     * @param lightness  lightness (0 to 1)
     * @return int[] rgb
     */
    private static int[] hsl2rgb(float hue, float saturation, float lightness) {
        int[] rgb = new int[3];
        float r = 0, g = 0, b = 0;

        if (saturation == 0) {
            // gray values
            r = g = b = (lightness * 255);
        } else {
            float h = (float) hue / 360;

            float q = (lightness < 0.5) ? (lightness * (1 + saturation))
                    : ((lightness + saturation) - (lightness * saturation));
            float p = 2 * lightness - q;

            r = (256 * hue2rgb(p, q, h + (1.0f / 3)));
            g = (256 * hue2rgb(p, q, h));
            b = (256 * hue2rgb(p, q, h - (1.0f / 3)));
        }
        rgb[0] = Math.min(255, (int) r);
        rgb[1] = Math.min(255, (int) g);
        rgb[2] = Math.min(255, (int) b);

        return rgb;
    }

    private static float hue2rgb(float p, float q, float t) {
        if (t < 0f) {
            t += 1f;
        }
        if (t > 1f) {
            t -= 1;
        }
        if (t < 1f / 6f) {
            return (p + (q - p) * 6f * t);
        }
        if (t < 1f / 2f) {
            return q;
        }
        if (t < 2f / 3f) {
            return (p + (q - p) * ((2.0f / 3f) - t) * 6);
        }
        return p;
    }
}
