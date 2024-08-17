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

package com.sun.pdfview.font.ttf;

import java.nio.ByteBuffer;
import java.util.*;

/**
 *
 * @author  jkaplan
 */
public class CMapFormat6 extends CMap {
    /** First character code of subrange. */
    private short firstCode;
    /** Number of character codes in subrange. */
    private short entryCount;
    /** Array of glyph index values for character codes in the range. */
    private short [] glyphIndexArray;
    /** a reverse lookup from glyph id to index. */
    private HashMap<Short,Short> glyphLookup = new HashMap<Short,Short>();

    /** Creates a new instance of CMapFormat0 */
    protected CMapFormat6(short language) {
        super((short) 6, language);
    }

    /**
     * Get the length of this table
     */
    @Override
	public short getLength() {
        // start with the size of the fixed header
        short size = 5 * 2;

        // add the size of each segment header
        size += this.entryCount * 2;
        return size;
    }

    /**
     * Cannot map from a byte
     */
    @Override
	public byte map(byte src) {
        char c = map((char) src);
        if (c < Byte.MIN_VALUE || c > Byte.MAX_VALUE) {
            // out of range
            return 0;
        }
        return (byte) c;
    }

    /**
     * Map from char
     */
    @Override
	public char map(char src) {

        // find first segment with endcode > src
        if (src < this.firstCode || src > (this.firstCode + this.entryCount)) {
            // Codes outside of the range are assumed to be missing and are
            // mapped to the glyph with index 0
            return '\000';
        }
        return (char) this.glyphIndexArray[src - this.firstCode];
    }

    /**
     * Get the src code which maps to the given glyphID
     */
    @Override
	public char reverseMap(short glyphID) {
        Short result = this.glyphLookup.get(Short.valueOf(glyphID));
        if (result == null) {
            return '\000';
        }
        return (char) result.shortValue();
    }


    /**
     * Get the data in this map as a ByteBuffer
     */
    @Override
	public void setData(int length, ByteBuffer data) {
        // read the table size values
        this.firstCode = data.getShort();
        this.entryCount = data.getShort();

        this.glyphIndexArray = new short [this.entryCount];
        for (int i = 0; i < this.glyphIndexArray.length; i++) {
            this.glyphIndexArray[i] = data.getShort();
            this.glyphLookup.put(Short.valueOf(this.glyphIndexArray[i]),
                            Short.valueOf((short) (i + this.firstCode)));
        }
    }

    /**
     * Get the data in the map as a byte buffer
     */
    @Override
	public ByteBuffer getData() {
        ByteBuffer buf = ByteBuffer.allocate(getLength());

        // write the header
        buf.putShort(getFormat());
        buf.putShort(getLength());
        buf.putShort(getLanguage());

        // write the various values
        buf.putShort(this.firstCode);
        buf.putShort(this.entryCount);

        // write the endCodes
        for (int i = 0; i < this.glyphIndexArray.length; i++) {
            buf.putShort(this.glyphIndexArray[i]);
        }
        // reset the data pointer
        buf.flip();

        return buf;
    }
}