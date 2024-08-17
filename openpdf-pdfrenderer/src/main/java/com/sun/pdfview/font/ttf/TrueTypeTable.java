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
 * The base class for TrueType tables.  Specific tables can extend this
 * to add more functionality
 */
public class TrueTypeTable {

    /**
     * Well known tables
     */
    public static final int CMAP_TABLE = 0x636d6170;
    public static final int GLYF_TABLE = 0x676c7966;
    public static final int HEAD_TABLE = 0x68656164;
    public static final int HHEA_TABLE = 0x68686561;
    public static final int HMTX_TABLE = 0x686d7478;
    public static final int MAXP_TABLE = 0x6d617870;
    public static final int NAME_TABLE = 0x6e616d65;
    public static final int POST_TABLE = 0x706f7374;
    public static final int LOCA_TABLE = 0x6c6f6361;
    /**
     * This table's tag
     */
    private int tag;
    /**
     * The data in this table, in ByteBuffer form
     */
    private ByteBuffer data;

    /** 
     * Creates a new instance of TrueTypeTable.
     *
     * This method is protected.  Use the <code>getTable()</code> methods
     * to get new instances.
     *
     * @param tag the tag for this table
     */
    protected TrueTypeTable(int tag) {
        this.tag = tag;
    }

    /**
     * Get a new instance of an empty table by tag string
     *
     * @param ttf the font that contains this table
     * @param tagString the tag for this table, as a 4 character string
     *        (e.g. head or cmap)
     */
    public static TrueTypeTable createTable(TrueTypeFont ttf,
            String tagString) {
        return createTable(ttf, tagString, null);
    }

    /**
     * Get a new instance of a table with provided data
     *
     * @param ttf the font that contains this table
     * @param tagString the tag for this table, as a 4 character string
     *        (e.g. head or cmap)
     * @param data the table data
     */
    public static TrueTypeTable createTable(TrueTypeFont ttf,
            String tagString, ByteBuffer data) {
        TrueTypeTable outTable = null;

        int tag = stringToTag(tagString);

        switch (tag) {
            case CMAP_TABLE: // cmap table
                outTable = new CmapTable();
                break;
            case GLYF_TABLE:
                outTable = new GlyfTable(ttf);
                break;
            case HEAD_TABLE: // head table
                outTable = new HeadTable();
                break;
            case HHEA_TABLE:  // hhea table
                outTable = new HheaTable();
                break;
            case HMTX_TABLE:
                outTable = new HmtxTable(ttf);
                break;
            case LOCA_TABLE:
                outTable = new LocaTable(ttf);
                break;
            case MAXP_TABLE:  // maxp table
                outTable = new MaxpTable();
                break;
            case NAME_TABLE: // name table
                outTable = new NameTable();
                break;
            case POST_TABLE: // post table
                outTable = new PostTable();
                break;
            default:
                outTable = new TrueTypeTable(tag);
                break;
        }

        if (data != null) {
            outTable.setData(data);
        }

        return outTable;
    }

    /**
     * Get the table's tag
     */
    public int getTag() {
        return this.tag;
    }

    /**
     * Get the data in the table
     */
    public ByteBuffer getData() {
        return this.data;
    }

    /**
     * Set the data in the table
     */
    public void setData(ByteBuffer data) {
        this.data = data;
    }

    /**
     * Get the size of the table, in bytes
     */
    public int getLength() {
        return getData().remaining();
    }

    /**
     * Get the tag as a string
     */
    public static String tagToString(int tag) {
        char[] c = new char[4];
        c[0] = (char) (0xff & (tag >> 24));
        c[1] = (char) (0xff & (tag >> 16));
        c[2] = (char) (0xff & (tag >> 8));
        c[3] = (char) (0xff & (tag));

        return new String(c);
    }

    /**
     * Turn a string into a tag
     */
    public static int stringToTag(String tag) {
        char[] c = tag.toCharArray();

        if (c.length != 4) {
            throw new IllegalArgumentException("Bad tag length: " + tag);
        }

        return c[0] << 24 | c[1] << 16 | c[2] << 8 | c[3];
    }

    /**
     * Put into a nice string
     */
    @Override
	public String toString() {
        String out = "    " + tagToString(getTag()) + " Table.  Data is: ";
        if (getData() == null) {
            out += "not set";
        } else {
            out += "set";
        }
        return out;
    }
}