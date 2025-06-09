/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
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
import org.openpdf.css.parser.PropertyValue;

import java.util.HashMap;
import java.util.Map;

public class PageSize {
    /**
     * ISO A5 media: 148mm wide and 210 mm high
     */
    public static final PageSize A5 = new PageSize(
            new PropertyValue(CSSPrimitiveValue.CSS_MM, 148f, "148mm"),
            new PropertyValue(CSSPrimitiveValue.CSS_MM, 210f, "210mm"));

    /**
     * IS0 A4 media: 210 mm wide and 297 mm high
     */
    public static final PageSize A4 = new PageSize(
            new PropertyValue(CSSPrimitiveValue.CSS_MM, 210f, "210mm"),
            new PropertyValue(CSSPrimitiveValue.CSS_MM, 297f, "297mm"));

    /**
     * ISO A3 media: 297mm wide and 420mm high
     */
    public static final PageSize A3 = new PageSize(
            new PropertyValue(CSSPrimitiveValue.CSS_MM, 297f, "297mm"),
            new PropertyValue(CSSPrimitiveValue.CSS_MM, 420f, "420mm"));

    /**
     * ISO B3 media: 176mm wide by 250mm high
     */
    public static final PageSize B3 = new PageSize(
            new PropertyValue(CSSPrimitiveValue.CSS_MM, 176f, "176mm"),
            new PropertyValue(CSSPrimitiveValue.CSS_MM, 250, "250mm"));

    /**
     * ISO B4 media: 250mm wide by 353mm high
     */
    public static final PageSize B4 = new PageSize(
            new PropertyValue(CSSPrimitiveValue.CSS_MM, 250f, "250mm"),
            new PropertyValue(CSSPrimitiveValue.CSS_MM, 353f, "353mm"));

    /**
     * ISO B5 media: 176mm wide by 250 high
     */
    public static final PageSize B5 = new PageSize(
            new PropertyValue(CSSPrimitiveValue.CSS_MM, 176f, "176mm"),
            new PropertyValue(CSSPrimitiveValue.CSS_MM, 250f, "250mm"));

    /**
     * North American letter media: 8.5 inches wide and 11 inches high
     */
    public static final PageSize LETTER = new PageSize(
            new PropertyValue(CSSPrimitiveValue.CSS_IN, 8.5f, "8.5in"),
            new PropertyValue(CSSPrimitiveValue.CSS_IN, 11f, "11in"));

    /**
     * North American legal: 8.5 inches wide by 14 inches high
     */
    public static final PageSize LEGAL = new PageSize(
            new PropertyValue(CSSPrimitiveValue.CSS_IN, 8.5f, "8.5in"),
            new PropertyValue(CSSPrimitiveValue.CSS_IN, 14f, "14in"));

    /**
     * North American ledger: 11 inches wide by 17 inches high
     */
    public static final PageSize LEDGER = new PageSize(
            new PropertyValue(CSSPrimitiveValue.CSS_IN, 11f, "11in"),
            new PropertyValue(CSSPrimitiveValue.CSS_IN, 17f, "17in"));

    private static final Map<String, PageSize> SIZE_MAP = new HashMap<>();

    static {
        SIZE_MAP.put("a3", A3);
        SIZE_MAP.put("a4", A4);
        SIZE_MAP.put("a5", A5);
        SIZE_MAP.put("b3", B3);
        SIZE_MAP.put("b4", B4);
        SIZE_MAP.put("b5", B5);
        SIZE_MAP.put("letter", LETTER);
        SIZE_MAP.put("legal", LEGAL);
        SIZE_MAP.put("ledger", LEDGER);
    }

    private final CSSPrimitiveValue _pageWidth;
    private final CSSPrimitiveValue _pageHeight;

    private PageSize(CSSPrimitiveValue width, CSSPrimitiveValue height) {
        _pageWidth = width;
        _pageHeight = height;
    }

    public CSSPrimitiveValue getPageHeight() {
        return _pageHeight;
    }

    public CSSPrimitiveValue getPageWidth() {
        return _pageWidth;
    }

    @Nullable
    public static PageSize getPageSize(String pageSize) {
        return SIZE_MAP.get(pageSize);
    }
}
