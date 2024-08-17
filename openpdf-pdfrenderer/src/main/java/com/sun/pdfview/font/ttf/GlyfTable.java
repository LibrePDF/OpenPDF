/* Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
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
 * Model the TrueType Glyf table
 */
public class GlyfTable extends TrueTypeTable {
    /** 
     * the glyph data, as either a byte buffer (unparsed) or a 
     * glyph object (parsed)
     */
    private Object[] glyphs;
    
    /**
     * The glyph location table
     */
    private LocaTable loca;
    
    /** Creates a new instance of HmtxTable */
    protected GlyfTable(TrueTypeFont ttf) {
        super (TrueTypeTable.GLYF_TABLE);
    
        this.loca = (LocaTable) ttf.getTable("loca");
        
        MaxpTable maxp = (MaxpTable) ttf.getTable("maxp");
        int numGlyphs = maxp.getNumGlyphs();
        
        this.glyphs = new Object[numGlyphs]; 
    }
  
    /**
     * Get the glyph at a given index, parsing it as needed
     */
    public Glyf getGlyph(int index) {
        Object o = this.glyphs[index];
        if (o == null) {
            return null;
        }
        
        if (o instanceof ByteBuffer) {
            Glyf g = Glyf.getGlyf((ByteBuffer) o);
            this.glyphs[index] = g;
            
            return g;
        } else {
            return (Glyf) o;
        }
    }
  
    /** get the data in this map as a ByteBuffer */
    @Override
	public ByteBuffer getData() {
        int size = getLength();
        
        ByteBuffer buf = ByteBuffer.allocate(size);
        
        // write the offsets
        for (int i = 0; i < this.glyphs.length; i++) {
            Object o = this.glyphs[i];
            if (o == null) {
		continue;
            }

            ByteBuffer glyfData = null;
            if (o instanceof ByteBuffer) {
                glyfData = (ByteBuffer) o;
            } else {
                glyfData = ((Glyf) o).getData();
            }
            
            glyfData.rewind();
            buf.put(glyfData);
            glyfData.flip();
        }
        
        // reset the start pointer
        buf.flip();
        
        return buf;
    }
    
    /** Initialize this structure from a ByteBuffer */
    @Override
	public void setData(ByteBuffer data) {
        for (int i = 0; i < this.glyphs.length; i++) {
            int location = this.loca.getOffset(i);
            int length = this.loca.getSize(i);
            
            if (length == 0) {
                // undefined glyph
                continue;
            }
            
            data.position(location);
            ByteBuffer glyfData = data.slice();
            glyfData.limit(length);
            
            this.glyphs[i] = glyfData;
        }
    }
    
    /**
     * Get the length of this table
     */
    @Override
	public int getLength() {
        int length = 0;
        
        for (int i = 0; i < this.glyphs.length; i++) {
            Object o = this.glyphs[i];
            if (o == null) {
                continue;
            }
            
            if (o instanceof ByteBuffer) {
                length += ((ByteBuffer) o).remaining();
            } else {
                length += ((Glyf) o).getLength();
            }
        }
        
        return length;
    }
    
    /**
     * Create a pretty String
     */
    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();
        String indent = "    ";
     
        buf.append(indent + "Glyf Table: (" + this.glyphs.length + " glyphs)\n");
        buf.append(indent + "  Glyf 0: " + getGlyph(0));
        
        return buf.toString();
    }
}