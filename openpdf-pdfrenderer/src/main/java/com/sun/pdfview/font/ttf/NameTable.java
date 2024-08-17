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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author  jon
 */
public class NameTable extends TrueTypeTable {
    /**
     * Values for platformID
     */
    public static final short PLATFORMID_UNICODE    = 0;
    public static final short PLATFORMID_MACINTOSH  = 1;
    public static final short PLATFORMID_MICROSOFT  = 3;
    
    /**
     * Values for platformSpecificID if platform is Mac
     */
    public static final short ENCODINGID_MAC_ROMAN = 0;
    
    /**
     * Values for platformSpecificID if platform is Unicode
     */
    public static final short ENCODINGID_UNICODE_DEFAULT = 0;
    public static final short ENCODINGID_UNICODE_V11     = 1;
    public static final short ENCODINGID_UNICODE_V2      = 3;
    
    /**
     * Values for language ID if platform is Mac
     */
    public static final short LANGUAGEID_MAC_ENGLISH     = 0;
    
    /**
     * Values for nameID
     */
    public static final short NAMEID_COPYRIGHT        = 0;
    public static final short NAMEID_FAMILY           = 1;
    public static final short NAMEID_SUBFAMILY        = 2;
    public static final short NAMEID_SUBFAMILY_UNIQUE = 3;
    public static final short NAMEID_FULL_NAME        = 4;
    public static final short NAMEID_VERSION          = 5;
    public static final short NAMEID_POSTSCRIPT_NAME  = 6;
    public static final short NAMEID_TRADEMARK        = 7;
    /**
     * The format of this table
     */
    private short format;
    
    /**
     * The actual name records
     */
    private SortedMap<NameRecord,String> records;
    
    
    /** Creates a new instance of NameTable */
    protected NameTable() {
        super (TrueTypeTable.NAME_TABLE);
        
        this.records = Collections.synchronizedSortedMap(new TreeMap<NameRecord,String>());
    }
    
    /**
     * Add a record to the table
     */
    public void addRecord(short platformID, short platformSpecificID,
                          short languageID, short nameID,
                          String value) {
        NameRecord rec = new NameRecord(platformID, platformSpecificID,
                                        languageID, nameID);
        this.records.put(rec, value);
    }
    
    /**
     * Get a record from the table
     */
    public String getRecord(short platformID, short platformSpecificID,
                            short languageID, short nameID) {
    
        NameRecord rec = new NameRecord(platformID, platformSpecificID,
                                        languageID, nameID);
        return this.records.get(rec);
    }
    
    /**
     * Remove a record from the table
     */
    public void removeRecord(short platformID, short platformSpecificID,
                             short languageID, short nameID) {
        NameRecord rec = new NameRecord(platformID, platformSpecificID,
                                        languageID, nameID);
        this.records.remove(rec);
    }
    
    /**
     * Determine if we have any records with a given platform ID
     */
    public boolean hasRecords(short platformID) {
        for (Iterator i = this.records.keySet().iterator(); i.hasNext(); ) {
            NameRecord rec = (NameRecord) i.next();
            
            if (rec.platformID == platformID) {
                return true;
            }
        }
        
        return false;
    }
    
    /** 
     * Determine if we have any records with a given platform ID and
     * platform-specific ID
     */
    public boolean hasRecords(short platformID, short platformSpecificID) {
        for (Iterator i = this.records.keySet().iterator(); i.hasNext(); ) {
            NameRecord rec = (NameRecord) i.next();
            
            if (rec.platformID == platformID && 
                    rec.platformSpecificID == platformSpecificID) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Read the table from data
     */
    @Override
	public void setData(ByteBuffer data) {
        //read table header
        setFormat(data.getShort());
        int count = data.getShort();
        int stringOffset = data.getShort();
        
        // read the records
        for (int i = 0; i < count; i++) {
            short platformID = data.getShort();
            short platformSpecificID = data.getShort();
            short languageID = data.getShort();
            short nameID = data.getShort();
            
            int length = data.getShort() & 0xFFFF;
            int offset = data.getShort() & 0xFFFF;
            
            // read the String data
            data.mark();
            data.position(stringOffset + offset);
            
            ByteBuffer stringBuf = data.slice();
            stringBuf.limit(length);
            
            data.reset();
            
            // choose the character set
            String charsetName = getCharsetName(platformID, platformSpecificID);
            Charset charset = Charset.forName(charsetName);
            
            // parse the data as a string
            String value = charset.decode(stringBuf).toString();
        
            // add to the mix
            addRecord(platformID, platformSpecificID, languageID, nameID, value);
        }
    }
    
    /**
     * Get the data in this table as a buffer
     */
    @Override
	public ByteBuffer getData() {
        // alocate the output buffer
        ByteBuffer buf = ByteBuffer.allocate(getLength());
        
        // the start of string data
        short headerLength = (short) (6 + (12 * getCount()));
        
        // write the header
        buf.putShort(getFormat());
        buf.putShort(getCount());
        buf.putShort(headerLength);
        
        // the offset from the start of the strings table
        short curOffset = 0;
        
        // add the size of each record
        for (Iterator i = this.records.keySet().iterator(); i.hasNext();) {
            NameRecord rec = (NameRecord) i.next();
            String value = this.records.get(rec);
        
            // choose the charset
            String charsetName = getCharsetName(rec.platformID,
                rec.platformSpecificID);
            Charset charset = Charset.forName(charsetName);
            
            // encode
            ByteBuffer strBuf = charset.encode(value);
            short strLen = (short) (strBuf.remaining() & 0xFFFF);
            
            // write the IDs
            buf.putShort(rec.platformID);
            buf.putShort(rec.platformSpecificID);
            buf.putShort(rec.languageID);
            buf.putShort(rec.nameID);
            
            // write the size and offset
            buf.putShort(strLen);
            buf.putShort(curOffset);
            
            // remember or current position
            buf.mark();
            
            // move to the current offset and write the data
            buf.position(headerLength + curOffset);
            buf.put(strBuf);
            
            // reset stuff
            buf.reset();
            
            // increment offset
            curOffset += strLen;
        }
        
        // reset the pointer on the buffer
        buf.position(headerLength + curOffset);
        buf.flip();
        
        return buf;
    }
    
    /**
     * Get the length of this table
     */
    @Override
	public int getLength() {
        // start with the size of the fixed header plus the size of the
        // records
        int length = 6 + (12 * getCount());
        
        // add the size of each record
        for (Iterator i = this.records.keySet().iterator(); i.hasNext();) {
            NameRecord rec = (NameRecord) i.next();
            String value = this.records.get(rec);
        
            // choose the charset
            String charsetName = getCharsetName(rec.platformID,
                rec.platformSpecificID);
            Charset charset = Charset.forName(charsetName);
            
            // encode
            ByteBuffer buf = charset.encode(value);
                
            // add the size of the coded buffer
            length += buf.remaining();
        }
        
        return length;
    }
    
    /**
     * Get the format of this table
     */
    public short getFormat() {
        return this.format;
    }
    
    /**
     * Set the format of this table
     */
    public void setFormat(short format) {
        this.format = format;
    }
    
    /**
     * Get the number of records in the table
     */
    public short getCount() {
        return (short) this.records.size();
    }
    
    /**
     * Get the charset name for a given platform, encoding and language
     */
    public static String getCharsetName(int platformID, int encodingID) {
        String charset = "US-ASCII";   
            
        switch (platformID) {
            case PLATFORMID_UNICODE:
                charset = "UTF-16";
                break;
            case PLATFORMID_MICROSOFT:
                charset = "UTF-16";
                break;
        }
        
        return charset;
    }
    
    /** Get a pretty string */
    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();
        String indent = "    ";
        
        buf.append(indent + "Format: " + getFormat() + "\n");
        buf.append(indent + "Count : " + getCount() + "\n");
        
        for (Iterator i = this.records.keySet().iterator(); i.hasNext();) {
            NameRecord rec = (NameRecord) i.next();
            
            buf.append(indent + " platformID: " + rec.platformID);
            buf.append(" platformSpecificID: " + rec.platformSpecificID);
            buf.append(" languageID: " + rec.languageID);
            buf.append(" nameID: " + rec.nameID + "\n");
            buf.append(indent + "  " + this.records.get(rec) + "\n");
        }
        
        return buf.toString();
    }
    
    public Collection<String> getNames()
    {
    	return Collections.unmodifiableCollection(records.values());
    }
    
    /**
     * A class to hold the data associated with each record
     */
    static class NameRecord implements Comparable {
        /**
         * Platform ID
         */
        short platformID;
        
        /**
         * Platform Specific ID (Encoding)
         */
        short platformSpecificID;
        
        /**
         * Language ID
         */
        short languageID;
        
        /**
         * Name ID
         */
        short nameID;
        
        /**
         * Create a new record
         */
        NameRecord(short platformID, short platformSpecificID,
                   short languageID, short nameID) {
            this.platformID = platformID;
            this.platformSpecificID = platformSpecificID;
            this.languageID = languageID;
            this.nameID = nameID;
        }
        
        
        /**
         * Compare two records
         */
        @Override
		public boolean equals(Object o) {
            return (compareTo(o) == 0);
        }
        
        /**
         * Compare two records
         */
        @Override
		public int compareTo(Object obj) {
            if (!(obj instanceof NameRecord)) {
                return -1;
            }
            
            NameRecord rec = (NameRecord) obj;
            
            if (this.platformID > rec.platformID) {
                return 1;
            } else if (this.platformID < rec.platformID) {
                return -1;
            } else if (this.platformSpecificID > rec.platformSpecificID) {
                return 1;
            } else if (this.platformSpecificID < rec.platformSpecificID) {
                return -1;
            } else if (this.languageID > rec.languageID) {
                return 1;
            } else if (this.languageID < rec.languageID) {
                return -1;
            } else if (this.nameID > rec.nameID) {
                return 1;
            } else if (this.nameID < rec.nameID) {
                return -1;
            } else {
                return 0;
            }
        }
        
        
    }
}