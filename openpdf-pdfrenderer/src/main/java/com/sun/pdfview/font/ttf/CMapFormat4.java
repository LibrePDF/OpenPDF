/*
 * $Id: CMapFormat4.java,v 1.3 2011-04-15 15:44:14 xphc Exp $
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
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import com.sun.pdfview.font.ttf.CMapFormat4.Segment;

/**
 *
 * @author  jkaplan
 */
public class CMapFormat4 extends CMap {
   
    /**
     * The segments and associated data can be a char[] or an Integer
     */
    public SortedMap<Segment,Object> segments;
    
    /** Creates a new instance of CMapFormat0 */
    protected CMapFormat4(short language) {
        super((short) 4, language);
    
        this.segments = Collections.synchronizedSortedMap(new TreeMap<Segment,Object>());
        
        char[] map = new char[1];
        map[0] = (char) 0;
        addSegment((short) 0xffff, (short) 0xffff, map);
    }
    
    /**
     * Add a segment with a map 
     */
    public void addSegment(short startCode, short endCode, char[] map) {
        if (map.length != (endCode - startCode) + 1) {
            throw new IllegalArgumentException("Wrong number of entries in map");
        }
        
        Segment s = new Segment(startCode, endCode, true);
        // make sure we remove any old entries
        this.segments.remove(s);
        this.segments.put(s, map);
    }
    
    /**
     * Add a segment with an idDelta
     */
    public void addSegment(short startCode, short endCode, short idDelta) {
        Segment s = new Segment(startCode, endCode, false);
        // make sure we remove any old entries
        this.segments.remove(s);
        this.segments.put(s, Integer.valueOf(idDelta));
    }
    
    /**
     * Remove a segment
     */
    public void removeSegment(short startCode, short endCode) {
        Segment s = new Segment(startCode, endCode, true);
        this.segments.remove(s);
    }
    
    /**
     * Get the length of this table
     */
    @Override
	public short getLength() {
        // start with the size of the fixed header
        short size = 16;
        
        // add the size of each segment header
        size += this.segments.size() * 8;
        
        // add the total number of mappings times the size of a mapping
        for (Iterator i = this.segments.keySet().iterator(); i.hasNext();) {
            Segment s = (Segment) i.next();
            
            // see if there's a map
            if (s.hasMap) {
                // if there is, add its size
                char[] map = (char[]) this.segments.get(s);
                size += map.length * 2;
            }
        }
        
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
        for (Iterator i = this.segments.keySet().iterator(); i.hasNext();) {
            Segment s = (Segment) i.next();
            
            if (s.endCode >= src) {
                // are we within range?
                if (s.startCode <= src) {
                    if (s.hasMap) {
                        // return the index of this character in 
                        // the segment's map
                        char[] map = (char[]) this.segments.get(s);
                        return map[src - s.startCode];
                    } else {
                        // return the character code + idDelta
                        Integer idDelta = (Integer) this.segments.get(s);
                        return (char) (src + idDelta.intValue());
                    }
                } else {
                    // undefined character
                    return (char) 0;
                }
            }
        }
        
        // shouldn't get here!
        return (char) 0;
    }
    
    /**
     * Get the src code which maps to the given glyphID
     */
    @Override
	public char reverseMap(short glyphID) {
        // look at each segment
        for (Iterator i = this.segments.keySet().iterator(); i.hasNext();) {
            Segment s = (Segment) i.next();
            
            // see if we have a map or a delta
            if (s.hasMap) {
                char[] map = (char[]) this.segments.get(s);
                
                // if we have a map, we have to iterate through it
                for (int c = 0; c < map.length; c++) {
                    if (map[c] == glyphID) {
                        return (char) (s.startCode + c);
                    }
                }
            } else {
                Integer idDelta = (Integer) this.segments.get(s);
                
                // we can do the math to see if we're in range
                int start = s.startCode + idDelta.intValue();
                int end = s.endCode + idDelta.intValue();
                
                if (glyphID >= start && glyphID <= end) {
                    // we're in the range
                    return (char) (glyphID - idDelta.intValue());
                }
            }
        }
        
        // not found!
        return (char) 0;
    }
    
    
    /**
     * Get the data in this map as a ByteBuffer
     */
    @Override
	public void setData(int length, ByteBuffer data) {
        // read the table size values
        short segCount = (short) (data.getShort() / 2);
        short searchRange = data.getShort();
        short entrySelector = data.getShort();
        short rangeShift = data.getShort();
    
        // create arrays to store segment info
        short[] endCodes = new short[segCount];
        short[] startCodes = new short[segCount];
        short[] idDeltas = new short[segCount];
        short[] idRangeOffsets = new short[segCount];
          
        // the start of the glyph array
        int glyphArrayPos = 16 + (8 * segCount);
        
        // read the endCodes
        for (int i = 0; i < segCount; i++) {
           endCodes[i] = data.getShort();
        }
        
        // read the pad
        data.getShort();
        
        // read the start codes
        for (int i = 0; i < segCount; i++) {
            startCodes[i] = data.getShort();
        }
        
        // read the idDeltas
        for (int i = 0; i < segCount; i++) {
            idDeltas[i] = data.getShort();
        }
        
        // read the id range offsets
        for (int i = 0; i < segCount; i++) {
            idRangeOffsets[i] = data.getShort();
            
            // calculate the actual offset
            if (idRangeOffsets[i] <= 0) {
                // the easy way
                addSegment(startCodes[i], endCodes[i], idDeltas[i]);
            } else {
                // find the start of the data segment
                int offset = (data.position() - 2) + idRangeOffsets[i];
            
                // get the number of entries in the map
                int size = (endCodes[i] - startCodes[i]) + 1;
            
                // allocate the actual map
                char[] map = new char[size];
                
                // remember our offset
                data.mark();
                 
                // read the mappings    
                for (int c = 0; c < size; c++) {
                    data.position(offset + (c * 2));
                    map[c] = data.getChar();
                }
      
                // reset the position
                data.reset();
                
                addSegment(startCodes[i], endCodes[i], map);
            }
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
        buf.putShort((short) (getSegmentCount() * 2));
        buf.putShort(getSearchRange());
        buf.putShort(getEntrySelector());
        buf.putShort(getRangeShift());
        
        // write the endCodes
        for (Iterator<Segment> i = this.segments.keySet().iterator(); i.hasNext();) {
            Segment s = i.next();
            buf.putShort((short) s.endCode);
        }
        
        // write the pad
        buf.putShort((short) 0);
        
        // write the startCodes
        for (Iterator<Segment> i = this.segments.keySet().iterator(); i.hasNext();) {
            Segment s = i.next();
            buf.putShort((short) s.startCode);
        }
        
        // write the idDeltas for segments using deltas
        for (Iterator<Segment> i = this.segments.keySet().iterator(); i.hasNext();) {
            Segment s = i.next();
            
            if (!s.hasMap) {
                Integer idDelta = (Integer) this.segments.get(s);
                buf.putShort(idDelta.shortValue());
            } else {
                buf.putShort((short) 0);
            }
        }
        
        // the start of the glyph array
        int glyphArrayOffset = 16 + (8 * getSegmentCount());
        
        // write the idRangeOffsets and maps for segments using maps
        for (Iterator<Segment> i = this.segments.keySet().iterator(); i.hasNext();) {
            Segment s = i.next();
            
            if (s.hasMap) {
                // first set the offset, which is the number of bytes from the
                // current position to the current offset
                buf.putShort((short) (glyphArrayOffset - buf.position()));
                
                // remember the current position
                buf.mark();
                
                // move the position to the offset
                buf.position(glyphArrayOffset);
                
                // now write the map
                char[] map = (char[]) this.segments.get(s);
                for (int c = 0; c < map.length; c++) {
                    buf.putChar(map[c]);
                }
                
                // reset the data pointer
                buf.reset();
                
                // update the offset
                glyphArrayOffset += map.length * 2;
            } else {
                buf.putShort((short) 0);
            }
        }

        // make sure we are at the end of the buffer before we flip
        buf.position(glyphArrayOffset);
        
        // reset the data pointer
        buf.flip();
        
        return buf;
    }
    
    /**
     * Get the segment count
     */
    public short getSegmentCount() {
        return (short) this.segments.size();
    }
    
    /**
     * Get the search range
     */
    public short getSearchRange() {
        double pow = Math.floor(Math.log(getSegmentCount()) / Math.log(2));
        double pow2 = Math.pow(2, pow);
        
        return (short) (2 * pow2);
    }
    
    /**
     * Get the entry selector
     */
    public short getEntrySelector() {
        int sr2 = getSearchRange() / 2;
        return (short) (Math.log(sr2) / Math.log(2));
    }
    
    /**
     * Get the rangeShift()
     */
    public short getRangeShift() {
        return (short) ((2 * getSegmentCount()) - getSearchRange());
    }
    
    /** Get a pretty string */
    @Override public String toString() {
        StringBuffer buf = new StringBuffer();
        String indent = "        ";
        
        buf.append(super.toString());
        buf.append(indent + "SegmentCount : " + getSegmentCount() + "\n");
        buf.append(indent + "SearchRange  : " + getSearchRange() + "\n");
        buf.append(indent + "EntrySelector: " + getEntrySelector() + "\n");
        buf.append(indent + "RangeShift   : " + getRangeShift() + "\n");
        
        for (Iterator<Segment> i = this.segments.keySet().iterator(); i.hasNext();) {
            Segment s = i.next();
            
            buf.append(indent);
            buf.append("Segment: " + Integer.toHexString(s.startCode));
            buf.append("-" + Integer.toHexString(s.endCode) + " ");
            buf.append("hasMap: " + s.hasMap + " ");
            
            if (!s.hasMap) {
                buf.append("delta: " + this.segments.get(s));
            }
            
            buf.append("\n");
        }
        
        return buf.toString();
    }
    
    static class Segment implements Comparable {
        /** the end code (highest code in this segment) */
        int endCode;
        
        /** the start code (lowest code in this segment) */
        int startCode;
        
        /** whether it is a map or a delta */
        boolean hasMap;
        
        /** Create a new segment */
        public Segment(short startCode, short endCode, boolean hasMap) {
            // convert from unsigned short
            this.endCode   = (0xffff & endCode);
            this.startCode = (0xffff & startCode);
            
            this.hasMap = hasMap;
        }
        
        /** Equals based on compareTo (only compares endCode) */
        @Override public boolean equals(Object o) {
            return (compareTo(o) == 0);
        }
        
        /** Segments sort by increasing endCode */
        @Override
		public int compareTo(Object o) {
            if (!(o instanceof Segment)) {
                return -1;
            }
            
            Segment s = (Segment) o;
        
            // if regions overlap at all, declare the segments equal,
            // to avoid overlap in the segment list
            if (((s.endCode >= this.startCode) && (s.endCode <= this.endCode)) ||
                ((s.startCode >= this.startCode) && (s.startCode <= this.endCode))) {
                return 0;
            } if (this.endCode > s.endCode) {
                return 1;
            } else if (this.endCode < s.endCode) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}