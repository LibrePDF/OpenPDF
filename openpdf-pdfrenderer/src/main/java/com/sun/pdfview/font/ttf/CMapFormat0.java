/*
 * $Id: CMapFormat0.java,v 1.1 2009-07-01 12:43:20 bros Exp $
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

/**
 *
 * @author  jkaplan
 */
public class CMapFormat0 extends CMap {
    
    /**
     * The glyph index array
     */
    private byte[] glyphIndex;
    
    /** Creates a new instance of CMapFormat0 */
    protected CMapFormat0(short language) {
        super((short) 0, language);
    
        byte[] initialIndex = new byte[256];
        for (int i = 0; i < initialIndex.length; i++) {
            initialIndex[i] = (byte) i;
        }
        setMap(initialIndex);
    }
    
    /**
     * Get the length of this table
     */
    @Override
	public short getLength() {
        return (short) 262;
    }
    
    /** 
     * Map from a byte
     */
    @Override
	public byte map(byte src) {
        int i = 0xff & src;
        
        return this.glyphIndex[i];
    }
    
    /**
     * Cannot map from short
     */
    @Override
	public char map(char src) {
        if (src  < 0 || src > 255) {
            // out of range
            return (char) 0;
        }
    
        return (char) (map((byte) src) & 0xff);
    }
        
    
    /**
     * Get the src code which maps to the given glyphID
     */
    @Override
	public char reverseMap(short glyphID) {
        for (int i = 0; i < this.glyphIndex.length; i++) {
            if ((this.glyphIndex[i] & 0xff) == glyphID) {
                return (char) i;
            }
        }
        
        return (char) 0;
    }
    
    /**
     * Set the entire map
     */
    public void setMap(byte[] glyphIndex) {
        if (glyphIndex.length != 256) {
            throw new IllegalArgumentException("Glyph map must be size 256!");
        }
        
        this.glyphIndex = glyphIndex;
    }
    
    /**
     * Set a single mapping entry
     */
    public void setMap(byte src, byte dest) {
        int i = 0xff & src;
        
        this.glyphIndex[i] = dest;
    }
    
    /**
     * Get the whole map
     */
    protected byte[] getMap() {
        return this.glyphIndex;
    }
    
    /**
     * Get the data in this map as a ByteBuffer
     */
    @Override
	public ByteBuffer getData() {
        ByteBuffer buf = ByteBuffer.allocate(262);
        
        buf.putShort(getFormat());
        buf.putShort(getLength());
        buf.putShort(getLanguage());
        buf.put(getMap());
        
        // reset the position to the beginning of the buffer
        buf.flip();
        
        return buf;
    }
    
    /** 
     * Read the map in from a byte buffer
     */
    @Override
	public void setData(int length, ByteBuffer data) {
        if (length != 262) {
            throw new IllegalArgumentException("Bad length for CMap format 0");
        }
        
        if (data.remaining() != 256) {
            throw new IllegalArgumentException("Wrong amount of data for CMap format 0");
        }
        
        byte[] map = new byte[256];
        data.get(map);
        
        setMap(map);
    }
}