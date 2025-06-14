/*
 * $Id: CMap.java,v 1.1 2009-07-01 12:43:20 bros Exp $
 *
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
package com.sun.pdfview.font.ttf;

import java.nio.ByteBuffer;

import com.sun.pdfview.PDFDebugger;

/**
 *
 * @author  jkaplan
 */
public abstract class CMap {

    /**
     * The format of this map
     */
    private final short format;

    /**
     * The language of this map, or 0 for language-independent
     */
    private final short language;

    /** Creates a new instance of CMap 
     * Don't use this directly, use <code>CMap.createMap()</code>
     */
    protected CMap (short format, short language) {
        this.format = format;
        this.language = language;
    }

    /**
     * Create a map for the given format and language

     * <p>The Macintosh standard character to glyph mapping is supported
     * by format 0.</p>
     *
     * <p>Format 2 supports a mixed 8/16 bit mapping useful for Japanese,
     * Chinese and Korean. </p>
     *
     * <p>Format 4 is used for 16 bit mappings.</p>
     *
     * <p>Format 6 is used for dense 16 bit mappings.</p>
     *
     * <p>Formats 8, 10, and 12 (properly 8.0, 10.0, and 12.0) are used
     * for mixed 16/32-bit and pure 32-bit mappings.<br>
     * This supports text encoded with surrogates in Unicode 2.0 and later.</p>
     *
     * <p>Reference:<br>
     * http://developer.apple.com/textfonts/TTRefMan/RM06/Chap6cmap.html </p>
     */
    public static CMap createMap (short format, short language) {
        CMap outMap = null;

        switch (format) {
            case 0: // CMap format 0 - single byte codes
                outMap = new CMapFormat0 (language);
                break;
            case 4: // CMap format 4 - two byte encoding
                outMap = new CMapFormat4 (language);
                break;
            case 6: // CMap format 6 - 16-bit, two byte encoding
                outMap = new CMapFormat6 (language);
                break;
//            case 8: // CMap format 8 - Mixed 16-bit and 32-bit coverage
//                outMap = new CMapFormat_8(language);
//                break;
//            // CMap format 10 - Format 10.0 is a bit like format 6, in that it
//            // defines a trimmed array for a tight range of 32-bit character codes:
//            case 10:
//                outMap = new CMapFormat_10(language);
//                break;
//            // Format 12.0 is a bit like format 4, in that it defines
//            // segments for sparse representation in 4-byte character space.
//            case 12: // CMap format 12 -
//                outMap = new CMapFormat_12(language);
//                break;
            default:
                PDFDebugger.debug("Unsupport CMap format: " + format);
                return null;
        }

        return outMap;
    }

    /**
     * Get a map from the given data
     *
     * This method reads the format, data and length variables of
     * the map.
     */
    public static CMap getMap (ByteBuffer data) {
        short format = data.getShort ();
        short lengthShort = data.getShort ();
        int length = 0xFFFF & lengthShort;
        PDFDebugger.debug("CMAP, length: " + length + ", short: " + lengthShort, 100);

        // make sure our slice of the data only contains up to the length
        // of this table
        data.limit (Math.min (length, data.limit ()));

        short language = data.getShort ();

        CMap outMap = createMap (format, language);
        if (outMap == null) {
            return null;
        }

        outMap.setData (data.limit (), data);

        return outMap;
    }

    /**
     * Get the format of this map
     */
    public short getFormat () {
        return this.format;
    }

    /**
     * Get the language of this map
     */
    public short getLanguage () {
        return this.language;
    }

    /**
     * Set the data for this map
     */
    public abstract void setData (int length, ByteBuffer data);

    /**
     * Get the data in this map as a byte buffer
     */
    public abstract ByteBuffer getData ();

    /**
     * Get the length of this map
     */
    public abstract short getLength ();

    /**
     * Map an 8 bit value to another 8 bit value
     */
    public abstract byte map (byte src);

    /**
     * Map a 16 bit value to another 16 but value
     */
    public abstract char map (char src);

    /**
     * Get the src code which maps to the given glyphID
     */
    public abstract char reverseMap (short glyphID);

    /** Print a pretty string */
    @Override
    public String toString () {
        String indent = "        ";

        return indent + " format: " + getFormat () + " length: " +
                getLength () + " language: " + getLanguage () + "\n";
    }
}