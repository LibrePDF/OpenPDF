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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import com.sun.pdfview.PDFDebugger;

/**
 * Represents the TTF "cmap" table
 *
 * @author  jkaplan
 */
public class CmapTable extends TrueTypeTable {
    
    /** Holds value of property version. */
    private short version;
    
    /**
     * Holds the CMap subtables, sorted properly
     */
    private final SortedMap<CmapSubtable,CMap> subtables;
    
    /** Creates a new instance of CmapTable */
    protected CmapTable() {
        super(TrueTypeTable.CMAP_TABLE);
        
        setVersion((short) 0x0);
    
        this.subtables = Collections.synchronizedSortedMap(new TreeMap<CmapSubtable,CMap>());
    }
    
      /**
     * Add a CMap
     */
    public void addCMap(short platformID, short platformSpecificID,
                        CMap cMap) {
        CmapSubtable key = new CmapSubtable(platformID, platformSpecificID);
        this.subtables.put(key, cMap);
    }
    
    /**
     * Get a CMap by platform and specific ID
     */
    public CMap getCMap(short platformID, short platformSpecificID) {
        CmapSubtable key = new CmapSubtable(platformID, platformSpecificID);
        return this.subtables.get(key);
    }
    
    /**
     * Get all CMaps
     */
    public CMap[] getCMaps() {
        Collection<CMap> c = new ArrayList<CMap>();
        
        CMap cmap_3_1 = this.getCMap((short)3, (short)1);
        if (cmap_3_1 != null) {
        	c.add(cmap_3_1);
        }
        CMap cmap_1_0 = this.getCMap((short)1, (short)0);
        if (cmap_1_0 != null) {
        	c.add(cmap_1_0);
        }

        for (CMap cmap : this.subtables.values()) {
        	if (!c.contains(cmap)) {
        		c.add(cmap);
        	}
        }
                ;
        CMap[] maps = new CMap[c.size()];
        
        c.toArray(maps);
        
        return maps;
    }
    
    /**
     * Remove a CMap
     */
    public void removeCMap(short platformID, short platformSpecificID) {
        CmapSubtable key = new CmapSubtable(platformID, platformSpecificID);
        this.subtables.remove(key);
    }
    
    @Override public void setData(ByteBuffer data) {
        setVersion(data.getShort());
        
        short numberSubtables = data.getShort();
        
        for (int i = 0; i < numberSubtables; i++) {
            short platformID = data.getShort();
            short platformSpecificID = data.getShort();
            int offset = data.getInt();
            
            data.mark();
            
            // get the position from the start of this buffer 
            data.position(offset);
            
            ByteBuffer mapData = data.slice();
            
            data.reset();
            
            try {
                CMap cMap = CMap.getMap(mapData);
                if (cMap != null) {
                    addCMap(platformID, platformSpecificID, cMap);
                }
            } catch (Exception ex) {
                PDFDebugger.debug("Error reading map.  PlatformID=" +
                                    platformID + ", PlatformSpecificID=" + 
                                    platformSpecificID);
                PDFDebugger.debug("Reason: " + ex);
            }
        }
    }
    
    @Override public ByteBuffer getData() {
        ByteBuffer buf = ByteBuffer.allocate(getLength());
    
        // write the table header
        buf.putShort(getVersion());
        buf.putShort((short) this.subtables.size());
        
        // the current offset to write to, starts at the end of the
        // subtables
        int curOffset = 4 + (this.subtables.size() * 8);
        
        // write the subtables
        for (Iterator i = this.subtables.keySet().iterator(); i.hasNext();) {
            CmapSubtable cms = (CmapSubtable) i.next();
            CMap map = this.subtables.get(cms);
            
            buf.putShort(cms.platformID);
            buf.putShort(cms.platformSpecificID);
            buf.putInt(curOffset);
            
            curOffset += map.getLength();
        }
        
        // write the tables
        for (Iterator i = this.subtables.values().iterator(); i.hasNext();) {
            CMap map = (CMap) i.next();
            buf.put(map.getData());
        }
        
        // reset the position to the start of the buffer
        buf.flip();
        
        return buf;
    }
    
    /**
     * Get the size of the table, in bytes
     */
    @Override public int getLength() {
        // start with the size of the fixed data
        int length = 4;
       
        // add the size of the subtables 
        length += this.subtables.size() * 8;
        
        // add the size of the dynamic data
        for (Iterator i = this.subtables.values().iterator(); i.hasNext();) {     
            // add the size of the subtable data
            CMap map = (CMap) i.next();
            length += map.getLength();
        }
    
        return length;
    }
    
    
    /** Getter for property version.
     * @return Value of property version.
     *
     */
    public short getVersion() {
        return this.version;
    }
    
    /** Setter for property version.
     * @param version New value of property version.
     *
     */
    public void setVersion(short version) {
        this.version = version;
    }

    /**
     * Get the number of tables
     */
    public short getNumberSubtables() {
        return (short) this.subtables.size();
    }
    
    /** Print a pretty string */
    @Override public String toString() {
        StringBuffer buf = new StringBuffer();
        String indent = "    ";
    
        buf.append(indent + "Version: " + this.getVersion() + "\n");
        buf.append(indent + "NumMaps: " + this.getNumberSubtables() + "\n");
        
        for (Iterator i = this.subtables.keySet().iterator(); i.hasNext();) {
            CmapSubtable key = (CmapSubtable) i.next();
            
            buf.append(indent + "Map: platformID: " + key.platformID +
                       " PlatformSpecificID: " + key.platformSpecificID + "\n");
            
            CMap map = this.subtables.get(key);
            
            buf.append(map.toString());
        }
        
        return buf.toString();
    }
    
    static class CmapSubtable implements Comparable {
        /**
         * The platformID for this subtable
         */
        short platformID;
        
        /**
         * The platform-specific id
         */
        short platformSpecificID;
        
        /** 
         * Create a Cmap subtable
         */
        protected CmapSubtable(short platformID, short platformSpecificID) {
            this.platformID = platformID;
            this.platformSpecificID = platformSpecificID;
        }
            
        /**
         * Compare two subtables
         */
        @Override public boolean equals(Object obj) {
            return (compareTo(obj) == 0);
        }
        
        /**
         * Sort ascending by platform ID and then specific ID
         */
        @Override
		public int compareTo(Object obj) {
            if (!(obj instanceof CmapSubtable)) {
                return -1;
            }
            
            CmapSubtable cms = (CmapSubtable) obj;
            if (this.platformID < cms.platformID) {
                return -1;
            } else if (this.platformID > cms.platformID) {
                return 1;
            } else {
                if (this.platformSpecificID < cms.platformSpecificID) {
                    return -1;
                } else if (this.platformSpecificID > cms.platformSpecificID) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
    
}