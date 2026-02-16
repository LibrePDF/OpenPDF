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
package org.openpdf.renderer.font.ttf;

import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openpdf.renderer.BaseWatchable;
import org.openpdf.renderer.PDFDebugger;

/**
 *
 * @author  jkaplan
 */
public class TrueTypeFont {

    private final int type;
    // could be a ByteBuffer or a TrueTypeTable

    private final SortedMap<String, Object> tables;

    /** Creates a new instance of TrueTypeParser */
    public TrueTypeFont (int type) {
        this.type = type;

        this.tables = Collections.synchronizedSortedMap (
                new TreeMap<String, Object> ());
    }

    /**
     * Parses a TrueType font from a byte array
     */
    public static TrueTypeFont parseFont (byte[] orig) {
        ByteBuffer inBuf = ByteBuffer.wrap (orig);
        return parseFont (inBuf);
    }

    /**
     * Parses a TrueType font from a byte buffer
     */
    public static TrueTypeFont parseFont (ByteBuffer inBuf) {
        int type = inBuf.getInt ();
        short numTables = inBuf.getShort ();
        @SuppressWarnings("unused")
        short searchRange = inBuf.getShort ();
        @SuppressWarnings("unused")
        short entrySelector = inBuf.getShort ();
        @SuppressWarnings("unused")
        short rangeShift = inBuf.getShort ();

        TrueTypeFont font = new TrueTypeFont (type);
        parseDirectories (inBuf, numTables, font);

        return font;
    }

    /**
     * Get the type of this font
     */
    public int getType () {
        return this.type;
    }

    /**
     * Add a table to the font
     *
     * @param tagString the name of this table, as a 4 character string
     *        (i.e. cmap or head)
     * @param data the data for this table, as a byte buffer
     */
    public void addTable (String tagString, ByteBuffer data) {
        this.tables.put (tagString, data);
    }

    /**
     * Add a table to the font
     *
     * @param tagString the name of this table, as a 4 character string
     *        (i.e. cmap or head)
     * @param table the table
     */
    public void addTable (String tagString, TrueTypeTable table) {
        this.tables.put (tagString, table);
    }

    /**
     * Get a table by name.  This command causes the table in question
     * to be parsed, if it has not already been parsed.
     *
     * @param tagString the name of this table, as a 4 character string
     *        (i.e. cmap or head)
     */
    public TrueTypeTable getTable (String tagString) {
        Object tableObj = this.tables.get (tagString);

        TrueTypeTable table = null;

        if (tableObj instanceof ByteBuffer) {
            // the table has not yet been parsed.  Parse it, and add the
            // parsed version to the map of tables.
            ByteBuffer data = (ByteBuffer) tableObj;

            table = TrueTypeTable.createTable (this, tagString, data);
            addTable (tagString, table);
        } else {
            table = (TrueTypeTable) tableObj;
        }

        return table;
    }

    /**
     * Remove a table by name
     *
     * @param tagString the name of this table, as a 4 character string
     *        (i.e. cmap or head)
     */
    public void removeTable (String tagString) {
        this.tables.remove (tagString);
    }

    /**
     * Get the number of tables
     */
    public short getNumTables () {
        return (short) this.tables.size ();
    }

    /**
     * Get the search range
     */
    public short getSearchRange () {
        double pow2 = Math.floor (Math.log (getNumTables ()) / Math.log (2));
        double maxPower = Math.pow (2, pow2);

        return (short) (16 * maxPower);
    }

    /**
     * Get the entry selector
     */
    public short getEntrySelector () {
        double pow2 = Math.floor (Math.log (getNumTables ()) / Math.log (2));
        double maxPower = Math.pow (2, pow2);

        return (short) (Math.log (maxPower) / Math.log (2));
    }

    /**
     * Get the range shift
     */
    public short getRangeShift () {
        double pow2 = Math.floor (Math.log (getNumTables ()) / Math.log (2));
        double maxPower = Math.pow (2, pow2);

        return (short) ((maxPower * 16) - getSearchRange ());
    }

    /**
     * Write a font given the type and an array of Table Directory Entries
     */
    public byte[] writeFont () {
        // allocate a buffer to hold the font
        ByteBuffer buf = ByteBuffer.allocate (getLength ());
        
        // write the font header
        buf.putInt (getType ());
        buf.putShort (getNumTables ());
        buf.putShort (getSearchRange ());
        buf.putShort (getEntrySelector ());
        buf.putShort (getRangeShift ());

        // first offset is the end of the table directory entries
        int curOffset = 12 + (getNumTables () * 16);

        // write the tables
        for (Iterator<String> i = this.tables.keySet ().iterator (); i.hasNext ();) {
            String tagString = i.next ();
            int tag = TrueTypeTable.stringToTag (tagString);

            ByteBuffer data = null;

            Object tableObj = this.tables.get (tagString);
            if (tableObj instanceof TrueTypeTable) {
                data = ((TrueTypeTable) tableObj).getData ();
            } else {
                data = (ByteBuffer) tableObj;
            }

            int dataLen = data.remaining ();

            // write the table directory entry
            buf.putInt (tag);
            buf.putInt (calculateChecksum (tagString, data));
            buf.putInt (curOffset);
            buf.putInt (dataLen);

            // save the current position
            buf.mark ();

            // move to the current offset and write the data
            buf.position (curOffset);
            buf.put (data);

            // reset the data start pointer
            data.flip ();

            // return to the table directory entry
            buf.reset ();

            // udate the offset
            curOffset += dataLen;

            // don't forget the padding
            while ((curOffset % 4) > 0) {
                curOffset++;
            }
        }

        buf.position (curOffset);
        buf.flip ();

        // adjust the checksum
        updateChecksumAdj (buf);

        return buf.array ();
    }

    /**
     * Calculate the checksum for a given table
     * 
     * @param tagString the name of the data
     * @param data the data in the table
     */
    private static int calculateChecksum (String tagString, ByteBuffer data) {
        int sum = 0;

        data.mark ();

        // special adjustment for head table: always treat the 4-bytes
        // starting at byte 8 as 0x0000. This the checkSumAdjustment so
        // must be ignored here (see the TTF spec)
        if (tagString.equals ("head")) {
        	if(!data.isReadOnly()) {
            	data.putInt (8, 0);
        	}
        	sum += data.getInt();
        	sum += data.getInt();
        	// consume the uncounted checkSumAdjustment int
        	data.getInt();
        }

        int nlongs = (data.remaining () + 3) / 4;

        while (nlongs-- > 0) {
            if (data.remaining () > 3) {
                sum += data.getInt ();
            } else {
                byte b0 = (data.remaining () > 0) ? data.get () : 0;
                byte b1 = (data.remaining () > 0) ? data.get () : 0;
                byte b2 = (data.remaining () > 0) ? data.get () : 0;

                sum += ((0xff & b0) << 24) | ((0xff & b1) << 16) |
                        ((0xff & b2) << 8);
            }
        }

        data.reset ();

        return sum;
    }

    /**
     * Get directory entries from a font
     */
    private static void parseDirectories (ByteBuffer data, int numTables,
                                          TrueTypeFont ttf) {
        for (int i = 0; i < numTables; i++) {
            int tag = data.getInt ();
            String tagString = TrueTypeTable.tagToString (tag);
            PDFDebugger.debug("TTFFont.parseDirectories: " + tagString, 100);
            int checksum = data.getInt ();
            int offset = data.getInt ();
            int length = data.getInt ();

            // read the data
            PDFDebugger.debug("TTFFont.parseDirectories: checksum: " +
                    checksum + ", offset: " + offset + ", length: " + length, 100);
            data.mark ();
            data.position (offset);

            ByteBuffer tableData = data.slice ();
            tableData.limit (length);

            int calcChecksum = calculateChecksum (tagString, tableData);

            if (calcChecksum == checksum) {
                ttf.addTable (tagString, tableData);
            } else {
                PDFDebugger.debug("Mismatched checksums on table " + tagString + ": " + calcChecksum + " != " + checksum, 200);

                ttf.addTable (tagString, tableData);

            }
            data.reset ();
        }
    }

    /**
     * Get the length of the font
     *
     * @return the length of the entire font, in bytes
     */
    private int getLength () {
        // the size of all the table directory entries
        int length = 12 + (getNumTables () * 16);

        // for each directory entry, get the size,
        // and don't forget the padding!
        for (Iterator<Object> i = this.tables.values ().iterator (); i.hasNext ();) {
            Object tableObj = i.next ();

            // add the length of the entry
            if (tableObj instanceof TrueTypeTable) {
                length += ((TrueTypeTable) tableObj).getLength ();
            } else {
                length += ((ByteBuffer) tableObj).remaining ();
            }

            // pad
            if ((length % 4) != 0) {
                length += (4 - (length % 4));
            }
        }

        return length;
    }

    /**
     * Update the checksumAdj field in the head table
     */
    private void updateChecksumAdj (ByteBuffer fontData) {
        int checksum = calculateChecksum ("", fontData);
        int checksumAdj = 0xb1b0afba - checksum;

        // find the head table
        int offset = 12 + (getNumTables () * 16);

        // find the head table
        for (Iterator<String> i = this.tables.keySet ().iterator (); i.hasNext ();) {
            String tagString = i.next ();

            // adjust the checksum
            if (tagString.equals ("head")) {
                fontData.putInt (offset + 8, checksumAdj);
                return;
            }

            // add the length of the entry 
            Object tableObj = this.tables.get (tagString);
            if (tableObj instanceof TrueTypeTable) {
                offset += ((TrueTypeTable) tableObj).getLength ();
            } else {
                offset += ((ByteBuffer) tableObj).remaining ();
            }

            // pad
            if ((offset % 4) != 0) {
                offset += (4 - (offset % 4));
            }
        }
    }

    /**
     * Write the font to a pretty string
     */
    @Override
	public String toString () {
        StringBuffer buf = new StringBuffer ();

        buf.append("Type         : ").append(getType()).append("\n");
        buf.append("NumTables    : ").append(getNumTables()).append("\n");
        buf.append("SearchRange  : ").append(getSearchRange()).append("\n");
        buf.append("EntrySelector: ").append(getEntrySelector()).append("\n");
        buf.append("RangeShift   : ").append(getRangeShift()).append("\n");

        for (Iterator<Map.Entry<String, Object>> i = this.tables.entrySet ().iterator (); i.hasNext ();) {
            Map.Entry<String, Object> e = i.next ();

            TrueTypeTable table = null;
            if (e.getValue () instanceof ByteBuffer) {
                table = getTable (e.getKey ());
            } else {
                table = (TrueTypeTable) e.getValue ();
            }

            buf.append(table).append("\n");
        }

        return buf.toString ();
    }

    public Collection<String> getNames() {
        NameTable table = (NameTable) getTable("name");
        if (table != null) {
            return table.getNames();
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main (String[] args) {
        if (args.length != 1) {
            System.out.println ("Usage: ");
            System.out.println ("    TrueTypeParser <filename>");
            System.exit (-1);
        }

        try {
            RandomAccessFile raf = new RandomAccessFile (args[0], "r");

            int size = (int) raf.length ();
            byte[] data = new byte[size];

            raf.readFully (data);

            TrueTypeFont ttp = TrueTypeFont.parseFont (data);

            System.out.println (ttp);

            InputStream fontStream = new ByteArrayInputStream (ttp.writeFont ());

            @SuppressWarnings("unused")
            Font f = Font.createFont (Font.TRUETYPE_FONT, fontStream);
            raf.close();
        } catch (Exception e) {
            BaseWatchable.getErrorHandler().publishException(e);
        }
    }
}