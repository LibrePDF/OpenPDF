/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.sun.pdfview.font.cid;

import java.io.IOException;
import java.util.HashMap;

import com.sun.pdfview.PDFDebugger;
import com.sun.pdfview.PDFObject;

/**
 * A CMap maps from a character in a composite font to a font/glyph number
 * pair in a CID font.
 *
 * @author  jkaplan
 */
public abstract class PDFCMap {
    /**
     * A cache of known CMaps by name
     */
    private static HashMap<String, PDFCMap> cache;
    
    /** Creates a new instance of CMap */
    protected PDFCMap() {}
    
    /**
     * Get a CMap, given a PDF object containing one of the following:
     *  a string name of a known CMap
     *  a stream containing a CMap definition
     */
    public static PDFCMap getCMap(PDFObject map) throws IOException {
        if (map.getType() == PDFObject.NAME) {
            return getCMap(map.getStringValue());
        } else if (map.getType() == PDFObject.STREAM) {
            return parseCMap(map);
        } else {
            throw new IOException("CMap type not Name or Stream!");
        }
    }
       
    /**
     * Get a CMap, given a string name
     */
    public static PDFCMap getCMap(String mapName) throws IOException {
        if (cache == null) {
            populateCache();
        }
        
        if (!cache.containsKey(mapName)) {
            //throw new IOException("Unknown CMap: " + mapName);
        	PDFDebugger.debug("Unknown CMap: '" + mapName + "' procced with 'Identity-H'");
	       	return cache.get("Identity-H");
        }
            
        return cache.get(mapName);
    }
    
    /**
     * Populate the cache with well-known types
     */
    protected static void populateCache() {
        cache = new HashMap<String, PDFCMap>();
    
        // add the Identity-H map
        cache.put("Identity-H", new PDFCMap() {
            @Override
			public char map(char src) {
                return src;
            }
        });
    }
    
    /**
     * Parse a CMap from a CMap stream
     */
    protected static PDFCMap parseCMap(PDFObject map) throws IOException {
       	return new ToUnicodeMap(map);
    }
    
    /**
     * Map a given source character to a destination character
     */
    public abstract char map(char src);
    
    /**
     * Get the font number assoicated with a given source character
     */
    public int getFontID(char src) {
        return 0;
    }
    
}
