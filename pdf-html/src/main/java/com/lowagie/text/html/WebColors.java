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
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.html;

import java.awt.Color;
import java.util.HashMap;
import java.util.StringTokenizer;
import com.lowagie.text.error_messages.MessageLocalization;

/**
 * This class is a HashMap that contains the names of colors as a key and the
 * corresponding Color as value. (Source: Wikipedia
 * http://en.wikipedia.org/wiki/Web_colors )
 * 
 * @author blowagie
 */
public class WebColors extends HashMap {
    
	private static final long serialVersionUID = 3542523100813372896L;
	/** HashMap containing all the names and corresponding color values. */
	public static final WebColors NAMES = new WebColors();
	static {
		NAMES.put("aliceblue", new int[] { 0xf0, 0xf8, 0xff, 0x00 });
		NAMES.put("antiquewhite", new int[] { 0xfa, 0xeb, 0xd7, 0x00 });
		NAMES.put("aqua", new int[] { 0x00, 0xff, 0xff, 0x00 });
		NAMES.put("aquamarine", new int[] { 0x7f, 0xff, 0xd4, 0x00 });
		NAMES.put("azure", new int[] { 0xf0, 0xff, 0xff, 0x00 });
		NAMES.put("beige", new int[] { 0xf5, 0xf5, 0xdc, 0x00 });
		NAMES.put("bisque", new int[] { 0xff, 0xe4, 0xc4, 0x00 });
		NAMES.put("black", new int[] { 0x00, 0x00, 0x00, 0x00 });
		NAMES.put("blanchedalmond", new int[] { 0xff, 0xeb, 0xcd, 0x00 });
		NAMES.put("blue", new int[] { 0x00, 0x00, 0xff, 0x00 });
		NAMES.put("blueviolet", new int[] { 0x8a, 0x2b, 0xe2, 0x00 });
		NAMES.put("brown", new int[] { 0xa5, 0x2a, 0x2a, 0x00 });
		NAMES.put("burlywood", new int[] { 0xde, 0xb8, 0x87, 0x00 });
		NAMES.put("cadetblue", new int[] { 0x5f, 0x9e, 0xa0, 0x00 });
		NAMES.put("chartreuse", new int[] { 0x7f, 0xff, 0x00, 0x00 });
		NAMES.put("chocolate", new int[] { 0xd2, 0x69, 0x1e, 0x00 });
		NAMES.put("coral", new int[] { 0xff, 0x7f, 0x50, 0x00 });
		NAMES.put("cornflowerblue", new int[] { 0x64, 0x95, 0xed, 0x00 });
		NAMES.put("cornsilk", new int[] { 0xff, 0xf8, 0xdc, 0x00 });
		NAMES.put("crimson", new int[] { 0xdc, 0x14, 0x3c, 0x00 });
		NAMES.put("cyan", new int[] { 0x00, 0xff, 0xff, 0x00 });
		NAMES.put("darkblue", new int[] { 0x00, 0x00, 0x8b, 0x00 });
		NAMES.put("darkcyan", new int[] { 0x00, 0x8b, 0x8b, 0x00 });
		NAMES.put("darkgoldenrod", new int[] { 0xb8, 0x86, 0x0b, 0x00 });
		NAMES.put("darkgray", new int[] { 0xa9, 0xa9, 0xa9, 0x00 });
		NAMES.put("darkgreen", new int[] { 0x00, 0x64, 0x00, 0x00 });
		NAMES.put("darkkhaki", new int[] { 0xbd, 0xb7, 0x6b, 0x00 });
		NAMES.put("darkmagenta", new int[] { 0x8b, 0x00, 0x8b, 0x00 });
		NAMES.put("darkolivegreen", new int[] { 0x55, 0x6b, 0x2f, 0x00 });
		NAMES.put("darkorange", new int[] { 0xff, 0x8c, 0x00, 0x00 });
		NAMES.put("darkorchid", new int[] { 0x99, 0x32, 0xcc, 0x00 });
		NAMES.put("darkred", new int[] { 0x8b, 0x00, 0x00, 0x00 });
		NAMES.put("darksalmon", new int[] { 0xe9, 0x96, 0x7a, 0x00 });
		NAMES.put("darkseagreen", new int[] { 0x8f, 0xbc, 0x8f, 0x00 });
		NAMES.put("darkslateblue", new int[] { 0x48, 0x3d, 0x8b, 0x00 });
		NAMES.put("darkslategray", new int[] { 0x2f, 0x4f, 0x4f, 0x00 });
		NAMES.put("darkturquoise", new int[] { 0x00, 0xce, 0xd1, 0x00 });
		NAMES.put("darkviolet", new int[] { 0x94, 0x00, 0xd3, 0x00 });
		NAMES.put("deeppink", new int[] { 0xff, 0x14, 0x93, 0x00 });
		NAMES.put("deepskyblue", new int[] { 0x00, 0xbf, 0xff, 0x00 });
		NAMES.put("dimgray", new int[] { 0x69, 0x69, 0x69, 0x00 });
		NAMES.put("dodgerblue", new int[] { 0x1e, 0x90, 0xff, 0x00 });
		NAMES.put("firebrick", new int[] { 0xb2, 0x22, 0x22, 0x00 });
		NAMES.put("floralwhite", new int[] { 0xff, 0xfa, 0xf0, 0x00 });
		NAMES.put("forestgreen", new int[] { 0x22, 0x8b, 0x22, 0x00 });
		NAMES.put("fuchsia", new int[] { 0xff, 0x00, 0xff, 0x00 });
		NAMES.put("gainsboro", new int[] { 0xdc, 0xdc, 0xdc, 0x00 });
		NAMES.put("ghostwhite", new int[] { 0xf8, 0xf8, 0xff, 0x00 });
		NAMES.put("gold", new int[] { 0xff, 0xd7, 0x00, 0x00 });
		NAMES.put("goldenrod", new int[] { 0xda, 0xa5, 0x20, 0x00 });
		NAMES.put("gray", new int[] { 0x80, 0x80, 0x80, 0x00 });
		NAMES.put("green", new int[] { 0x00, 0x80, 0x00, 0x00 });
		NAMES.put("greenyellow", new int[] { 0xad, 0xff, 0x2f, 0x00 });
		NAMES.put("honeydew", new int[] { 0xf0, 0xff, 0xf0, 0x00 });
		NAMES.put("hotpink", new int[] { 0xff, 0x69, 0xb4, 0x00 });
		NAMES.put("indianred", new int[] { 0xcd, 0x5c, 0x5c, 0x00 });
		NAMES.put("indigo", new int[] { 0x4b, 0x00, 0x82, 0x00 });
		NAMES.put("ivory", new int[] { 0xff, 0xff, 0xf0, 0x00 });
		NAMES.put("khaki", new int[] { 0xf0, 0xe6, 0x8c, 0x00 });
		NAMES.put("lavender", new int[] { 0xe6, 0xe6, 0xfa, 0x00 });
		NAMES.put("lavenderblush", new int[] { 0xff, 0xf0, 0xf5, 0x00 });
		NAMES.put("lawngreen", new int[] { 0x7c, 0xfc, 0x00, 0x00 });
		NAMES.put("lemonchiffon", new int[] { 0xff, 0xfa, 0xcd, 0x00 });
		NAMES.put("lightblue", new int[] { 0xad, 0xd8, 0xe6, 0x00 });
		NAMES.put("lightcoral", new int[] { 0xf0, 0x80, 0x80, 0x00 });
		NAMES.put("lightcyan", new int[] { 0xe0, 0xff, 0xff, 0x00 });
		NAMES.put("lightgoldenrodyellow", new int[] { 0xfa, 0xfa, 0xd2, 0x00 });
		NAMES.put("lightgreen", new int[] { 0x90, 0xee, 0x90, 0x00 });
		NAMES.put("lightgrey", new int[] { 0xd3, 0xd3, 0xd3, 0x00 });
		NAMES.put("lightpink", new int[] { 0xff, 0xb6, 0xc1, 0x00 });
		NAMES.put("lightsalmon", new int[] { 0xff, 0xa0, 0x7a, 0x00 });
		NAMES.put("lightseagreen", new int[] { 0x20, 0xb2, 0xaa, 0x00 });
		NAMES.put("lightskyblue", new int[] { 0x87, 0xce, 0xfa, 0x00 });
		NAMES.put("lightslategray", new int[] { 0x77, 0x88, 0x99, 0x00 });
		NAMES.put("lightsteelblue", new int[] { 0xb0, 0xc4, 0xde, 0x00 });
		NAMES.put("lightyellow", new int[] { 0xff, 0xff, 0xe0, 0x00 });
		NAMES.put("lime", new int[] { 0x00, 0xff, 0x00, 0x00 });
		NAMES.put("limegreen", new int[] { 0x32, 0xcd, 0x32, 0x00 });
		NAMES.put("linen", new int[] { 0xfa, 0xf0, 0xe6, 0x00 });
		NAMES.put("magenta", new int[] { 0xff, 0x00, 0xff, 0x00 });
		NAMES.put("maroon", new int[] { 0x80, 0x00, 0x00, 0x00 });
		NAMES.put("mediumaquamarine", new int[] { 0x66, 0xcd, 0xaa, 0x00 });
		NAMES.put("mediumblue", new int[] { 0x00, 0x00, 0xcd, 0x00 });
		NAMES.put("mediumorchid", new int[] { 0xba, 0x55, 0xd3, 0x00 });
		NAMES.put("mediumpurple", new int[] { 0x93, 0x70, 0xdb, 0x00 });
		NAMES.put("mediumseagreen", new int[] { 0x3c, 0xb3, 0x71, 0x00 });
		NAMES.put("mediumslateblue", new int[] { 0x7b, 0x68, 0xee, 0x00 });
		NAMES.put("mediumspringgreen", new int[] { 0x00, 0xfa, 0x9a, 0x00 });
		NAMES.put("mediumturquoise", new int[] { 0x48, 0xd1, 0xcc, 0x00 });
		NAMES.put("mediumvioletred", new int[] { 0xc7, 0x15, 0x85, 0x00 });
		NAMES.put("midnightblue", new int[] { 0x19, 0x19, 0x70, 0x00 });
		NAMES.put("mintcream", new int[] { 0xf5, 0xff, 0xfa, 0x00 });
		NAMES.put("mistyrose", new int[] { 0xff, 0xe4, 0xe1, 0x00 });
		NAMES.put("moccasin", new int[] { 0xff, 0xe4, 0xb5, 0x00 });
		NAMES.put("navajowhite", new int[] { 0xff, 0xde, 0xad, 0x00 });
		NAMES.put("navy", new int[] { 0x00, 0x00, 0x80, 0x00 });
		NAMES.put("oldlace", new int[] { 0xfd, 0xf5, 0xe6, 0x00 });
		NAMES.put("olive", new int[] { 0x80, 0x80, 0x00, 0x00 });
		NAMES.put("olivedrab", new int[] { 0x6b, 0x8e, 0x23, 0x00 });
		NAMES.put("orange", new int[] { 0xff, 0xa5, 0x00, 0x00 });
		NAMES.put("orangered", new int[] { 0xff, 0x45, 0x00, 0x00 });
		NAMES.put("orchid", new int[] { 0xda, 0x70, 0xd6, 0x00 });
		NAMES.put("palegoldenrod", new int[] { 0xee, 0xe8, 0xaa, 0x00 });
		NAMES.put("palegreen", new int[] { 0x98, 0xfb, 0x98, 0x00 });
		NAMES.put("paleturquoise", new int[] { 0xaf, 0xee, 0xee, 0x00 });
		NAMES.put("palevioletred", new int[] { 0xdb, 0x70, 0x93, 0x00 });
		NAMES.put("papayawhip", new int[] { 0xff, 0xef, 0xd5, 0x00 });
		NAMES.put("peachpuff", new int[] { 0xff, 0xda, 0xb9, 0x00 });
		NAMES.put("peru", new int[] { 0xcd, 0x85, 0x3f, 0x00 });
		NAMES.put("pink", new int[] { 0xff, 0xc0, 0xcb, 0x00 });
		NAMES.put("plum", new int[] { 0xdd, 0xa0, 0xdd, 0x00 });
		NAMES.put("powderblue", new int[] { 0xb0, 0xe0, 0xe6, 0x00 });
		NAMES.put("purple", new int[] { 0x80, 0x00, 0x80, 0x00 });
		NAMES.put("red", new int[] { 0xff, 0x00, 0x00, 0x00 });
		NAMES.put("rosybrown", new int[] { 0xbc, 0x8f, 0x8f, 0x00 });
		NAMES.put("royalblue", new int[] { 0x41, 0x69, 0xe1, 0x00 });
		NAMES.put("saddlebrown", new int[] { 0x8b, 0x45, 0x13, 0x00 });
		NAMES.put("salmon", new int[] { 0xfa, 0x80, 0x72, 0x00 });
		NAMES.put("sandybrown", new int[] { 0xf4, 0xa4, 0x60, 0x00 });
		NAMES.put("seagreen", new int[] { 0x2e, 0x8b, 0x57, 0x00 });
		NAMES.put("seashell", new int[] { 0xff, 0xf5, 0xee, 0x00 });
		NAMES.put("sienna", new int[] { 0xa0, 0x52, 0x2d, 0x00 });
		NAMES.put("silver", new int[] { 0xc0, 0xc0, 0xc0, 0x00 });
		NAMES.put("skyblue", new int[] { 0x87, 0xce, 0xeb, 0x00 });
		NAMES.put("slateblue", new int[] { 0x6a, 0x5a, 0xcd, 0x00 });
		NAMES.put("slategray", new int[] { 0x70, 0x80, 0x90, 0x00 });
		NAMES.put("snow", new int[] { 0xff, 0xfa, 0xfa, 0x00 });
		NAMES.put("springgreen", new int[] { 0x00, 0xff, 0x7f, 0x00 });
		NAMES.put("steelblue", new int[] { 0x46, 0x82, 0xb4, 0x00 });
		NAMES.put("tan", new int[] { 0xd2, 0xb4, 0x8c, 0x00 });
		NAMES.put("transparent", new int[] { 0x00, 0x00, 0x00, 0xff });
		NAMES.put("teal", new int[] { 0x00, 0x80, 0x80, 0x00 });
		NAMES.put("thistle", new int[] { 0xd8, 0xbf, 0xd8, 0x00 });
		NAMES.put("tomato", new int[] { 0xff, 0x63, 0x47, 0x00 });
		NAMES.put("turquoise", new int[] { 0x40, 0xe0, 0xd0, 0x00 });
		NAMES.put("violet", new int[] { 0xee, 0x82, 0xee, 0x00 });
		NAMES.put("wheat", new int[] { 0xf5, 0xde, 0xb3, 0x00 });
		NAMES.put("white", new int[] { 0xff, 0xff, 0xff, 0x00 });
		NAMES.put("whitesmoke", new int[] { 0xf5, 0xf5, 0xf5, 0x00 });
		NAMES.put("yellow", new int[] { 0xff, 0xff, 0x00, 0x00 });
		NAMES.put("yellowgreen", new int[] { 0x9, 0xacd, 0x32, 0x00 });
	}

	/**
	 * Gives you a Color based on a name.
	 * 
	 * @param name
	 *            a name such as black, violet, cornflowerblue or #RGB or #RRGGBB
     *            or rgb(R,G,B)
	 * @return the corresponding Color object
	 * @throws IllegalArgumentException
	 *             if the String isn't a know representation of a color.
	 */
	public static Color getRGBColor(String name)
			throws IllegalArgumentException {
		int[] c = { 0, 0, 0, 0 };
		if (name.startsWith("#")) {
			if (name.length() == 4) {
				c[0] = Integer.parseInt(name.substring(1, 2), 16) * 16;
				c[1] = Integer.parseInt(name.substring(2, 3), 16) * 16;
				c[2] = Integer.parseInt(name.substring(3), 16) * 16;
				return new Color(c[0], c[1], c[2], c[3]);
			}
			if (name.length() == 7) {
				c[0] = Integer.parseInt(name.substring(1, 3), 16);
				c[1] = Integer.parseInt(name.substring(3, 5), 16);
				c[2] = Integer.parseInt(name.substring(5), 16);
				return new Color(c[0], c[1], c[2], c[3]);
			}
			throw new IllegalArgumentException(MessageLocalization.getComposedMessage("unknown.color.format.must.be.rgb.or.rrggbb"));
		}
        else if (name.startsWith("rgb(")) {
            StringTokenizer tok = new StringTokenizer(name, "rgb(), \t\r\n\f");
            for (int k = 0; k < 3; ++k) {
                String v = tok.nextToken();
                if (v.endsWith("%"))
                    c[k] = Integer.parseInt(v.substring(0, v.length() - 1)) * 255 / 100;
                else
                    c[k] = Integer.parseInt(v);
                if (c[k] < 0)
                    c[k] = 0;
                else if (c[k] > 255)
                    c[k] = 255;
            }
            return new Color(c[0], c[1], c[2], c[3]);
        }
		name = name.toLowerCase();
		if (!NAMES.containsKey(name))
			throw new IllegalArgumentException("Color '" + name
					+ "' not found.");
		c = (int[]) NAMES.get(name);
		return new Color(c[0], c[1], c[2], c[3]);
	}
}
