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
import java.util.Arrays;

/**
 * Model the TrueType Post table
 *
 * @author  jkaplan
 */
public class HmtxTable extends TrueTypeTable {
    /** advance widths for any glyphs that have one */
    short advanceWidths[];
    
    /** left side bearings for each glyph */
    short leftSideBearings[];
    
    /** Creates a new instance of HmtxTable */
    protected HmtxTable(TrueTypeFont ttf) {
        super (TrueTypeTable.HMTX_TABLE);

        // the number of glyphs stored in the maxp table may be incorrect
        // in the case of subsetted fonts produced by some pdf generators
        MaxpTable maxp = (MaxpTable) ttf.getTable("maxp");
        int numGlyphs = maxp.getNumGlyphs();
        
        HheaTable hhea = (HheaTable) ttf.getTable("hhea");
        int numOfLongHorMetrics = hhea.getNumOfLongHorMetrics();
        
        this.advanceWidths = new short[numOfLongHorMetrics];
        this.leftSideBearings = new short[numGlyphs]; 
    }
    
    /** get the advance of a given glyph */
    public short getAdvance(int glyphID) {
        if (glyphID < this.advanceWidths.length) {
            return this.advanceWidths[glyphID];
        } else {
            return this.advanceWidths[this.advanceWidths.length - 1];
        }
    }
      
    /** get the left side bearing of a given glyph */
    public short getLeftSideBearing(int glyphID) {
        return this.leftSideBearings[glyphID];
    }
    
    /** get the data in this map as a ByteBuffer */
    @Override
	public ByteBuffer getData() {
        int size = getLength();
        
        ByteBuffer buf = ByteBuffer.allocate(size);
        
        // write the metrics
        for (int i = 0; i < this.leftSideBearings.length; i++) {
            if (i < this.advanceWidths.length) {
                buf.putShort(this.advanceWidths[i]);
            }
            
            buf.putShort(this.leftSideBearings[i]);
        }
        
        // reset the start pointer
        buf.flip();
        
        return buf;
    }
    
    /** Initialize this structure from a ByteBuffer */
    @Override
	public void setData(ByteBuffer data) {
        // some PDF writers subset the font but don't update the number of glyphs in the maxp table,
        // this would appear to break the TTF spec.
        // A better solution might be to try and override the numGlyphs in the maxp table based
        // on the number of entries in the cmap table or by parsing the glyf table, but this
        // appears to be the only place that gets affected by the discrepancy... so far!...
        // so updating this allows it to work.
        int i;
        // only read as much data as is available
        for (i = 0; i < this.leftSideBearings.length && data.hasRemaining(); i++) {
            if (i < this.advanceWidths.length) {
                this.advanceWidths[i] = data.getShort();
            }
            
            this.leftSideBearings[i] = data.getShort();
        }
        // initialise the remaining advanceWidths and leftSideBearings to 0
        if (i < this.advanceWidths.length) {
            Arrays.fill(this.advanceWidths, i, this.advanceWidths.length-1, (short) 0);
        }
        if (i < this.leftSideBearings.length) {
            Arrays.fill(this.leftSideBearings, i, this.leftSideBearings.length-1, (short) 0);
        }
    }
    
    /**
     * Get the length of this table
     */
    @Override
	public int getLength() {
        return (this.advanceWidths.length * 2) + (this.leftSideBearings.length * 2);
    }
}