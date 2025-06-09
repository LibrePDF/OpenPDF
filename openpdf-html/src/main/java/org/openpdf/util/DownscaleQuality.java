/*
 * {{{ header & license
 * Copyright (c) 2007 Patrick Wright
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
package org.openpdf.util;

import java.util.HashMap;
import java.util.Map;


/**
 * Simple enumerated constants for downscaling (scaling to smaller image size)--since we have various options
 * for what algorithm to use. Not general-purpose, applies only to methods used in ImageUtil. Types constants
 * can be looked up using {@link #forString(String, DownscaleQuality)} and the corresponding string
 * for the quality
*/
// made a separate class only to reduce size of ImageUtil
public class DownscaleQuality {
    /** Internal map string type to DQ instance */
    private static final Map<String, DownscaleQuality> constList = new HashMap<>();

    /**
     * Highest-quality downscaling; probably slowest as well.
     */
    public static final DownscaleQuality HIGH_QUALITY = addConstant("HIGH");

    /** Low-quality, but not the worst quality */
    public static final DownscaleQuality LOW_QUALITY = addConstant("MED");

    /** Low quality, but very fast. */
    public static final DownscaleQuality FAST = addConstant("LOW");

    /** One step, fast, but should be better than low-quality. */
    public static final DownscaleQuality AREA = addConstant("AREA");

    private final String type;

    /**
     * Create and add constant instance
     * @param type Unique string for the instance
     * @return The constant for that type
     */
    private static DownscaleQuality addConstant(String type) {
        if ( constList.containsKey(type)) {
            throw new RuntimeException("Type strings for DownscaleQuality should be unique; " + type +
            " is declared twice");
        }
        DownscaleQuality q = new DownscaleQuality(type);
        constList.put(type, q);
        return q;
    }

    private DownscaleQuality(String type) {
        this.type = type;
    }

    public String asString() {
        return type;
    }

    /**
     *  Retrieves the DownscaleQuality instance for the corresponding string.
     *
     * @param type The string describing the quality, e.g. HIGH
     * @param defaultValue Default value to use if not found
     * @return The constant quality instance for the type, or the default if not found.
     */
    public static DownscaleQuality forString(String type, DownscaleQuality defaultValue) {
        DownscaleQuality q = constList.get(type);

        return q == null ? defaultValue : q;
    }
}
