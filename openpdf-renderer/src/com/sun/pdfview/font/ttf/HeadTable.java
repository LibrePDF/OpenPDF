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
import java.util.Date;

/**
 *
 * @author  jkaplan
 */
public class HeadTable extends TrueTypeTable {
    
    /** Holds value of property version. */
    private int version;
    
    /** Holds value of property fontRevision. */
    private int fontRevision;
    
    /** Holds value of property checksumAdjustment. */
    private int checksumAdjustment;
    
    /** Holds value of property magicNumber. */
    private int magicNumber;
    
    /** Holds value of property flags. */
    private short flags;
    
    /** Holds value of property unitsPerEm. */
    private short unitsPerEm;
    
    /** Holds value of property created. */
    private long created;
    
    /** Holds value of property modified. */
    private long modified;
    
    /** Holds value of property xMin. */
    private short xMin;
    
    /** Holds value of property yMin. */
    private short yMin;
    
    /** Holds value of property xMax. */
    private short xMax;
    
    /** Holds value of property yMax. */
    private short yMax;
    
    /** Holds value of property macStyle. */
    private short macStyle;
    
    /** Holds value of property lowestRecPPem. */
    private short lowestRecPPem;
    
    /** Holds value of property fontDirectionHint. */
    private short fontDirectionHint;
    
    /** Holds value of property indexToLocFormat. */
    private short indexToLocFormat;
    
    /** Holds value of property glyphDataFormat. */
    private short glyphDataFormat;
    
    /** Creates a new instance of HeadTable 
     * Makes up reasonable(?) defaults for all values
     */
    protected HeadTable() {
        super(TrueTypeTable.HEAD_TABLE);
        
        setVersion(0x10000);
        setFontRevision(0x10000);
        setChecksumAdjustment(0);
        setMagicNumber(0x5f0f3cf5);
        setFlags((short) 0x0);
        setUnitsPerEm((short) 64);
        setCreated(System.currentTimeMillis());
        setModified(System.currentTimeMillis());
        setXMin((short) 0);
        setXMax(Short.MAX_VALUE);
        setYMin((short) 0);
        setYMax(Short.MAX_VALUE);
        setMacStyle((short) 0x0);
        setLowestRecPPem((short) 0);
        setFontDirectionHint((short) 0);
        setIndexToLocFormat((short) 0);
        setGlyphDataFormat((short) 0);
    }
    
    /**
     * Parse the data before it is set
     */
    @Override
	public void setData(ByteBuffer data) {
        if (data.remaining() < 54) {
            throw new IllegalArgumentException("Bad Head table size " + data.remaining());
        }
        setVersion(data.getInt());
        setFontRevision(data.getInt());
        setChecksumAdjustment(data.getInt());
        setMagicNumber(data.getInt());
        setFlags(data.getShort());
        setUnitsPerEm(data.getShort());
        setCreated(data.getLong());
        setModified(data.getLong());
        setXMin(data.getShort());
        setXMax(data.getShort());
        setYMin(data.getShort());
        setYMax(data.getShort());
        setMacStyle(data.getShort());
        setLowestRecPPem(data.getShort());
        setFontDirectionHint(data.getShort());
        setIndexToLocFormat(data.getShort());
        setGlyphDataFormat(data.getShort());
    }
    
    /**
     * Get the data we have stored
     */
    @Override
	public ByteBuffer getData() {
        ByteBuffer buf = ByteBuffer.allocate(getLength());
        
        buf.putInt(getVersion());
        buf.putInt(getFontRevision());
        buf.putInt(getChecksumAdjustment());
        buf.putInt(getMagicNumber());
        buf.putShort(getFlags());
        buf.putShort(getUnitsPerEm());
        buf.putLong(getCreated());
        buf.putLong(getModified());
        buf.putShort(getXMin());
        buf.putShort(getXMax());
        buf.putShort(getYMin());
        buf.putShort(getYMax());
        buf.putShort(getMacStyle());
        buf.putShort(getLowestRecPPem());
        buf.putShort(getFontDirectionHint());
        buf.putShort(getIndexToLocFormat());
        buf.putShort(getGlyphDataFormat());
    
        // reset the position to the start of the buffer
        buf.flip();
        
        return buf;
    }
    
    /**
     * Get the length of this table
     */
    @Override
	public int getLength() {
        return 54;
    }
    
    /** Getter for property version.
     * @return Value of property version.
     *
     */
    public int getVersion() {
        return this.version;
    }
    
    /** Getter for property fontRevision.
     * @return Value of property fontRevision.
     *
     */
    public int getFontRevision() {
        return this.fontRevision;
    }
    
    /** Getter for property checksumAdjustment.
     * @return Value of property checksumAdjustment.
     *
     */
    public int getChecksumAdjustment() {
        return this.checksumAdjustment;
    }
    
    /** Getter for property magicNumber.
     * @return Value of property magicNumber.
     *
     */
    public int getMagicNumber() {
        return this.magicNumber;
    }
    
    /** Getter for property flags.
     * @return Value of property flags.
     *
     */
    public short getFlags() {
        return this.flags;
    }
    
    /** Getter for property unitsPerEm.
     * @return Value of property unitsPerEm.
     *
     */
    public short getUnitsPerEm() {
        return this.unitsPerEm;
    }
    
    /** Getter for property created.
     * @return Value of property created.
     *
     */
    public long getCreated() {
        return this.created;
    }
    
    /** Getter for property modified.
     * @return Value of property modified.
     *
     */
    public long getModified() {
        return this.modified;
    }
    
    /** Getter for property xMin.
     * @return Value of property xMin.
     *
     */
    public short getXMin() {
        return this.xMin;
    }
    
    /** Getter for property yMin.
     * @return Value of property yMin.
     *
     */
    public short getYMin() {
        return this.yMin;
    }
    
    /** Getter for property xMax.
     * @return Value of property xMax.
     *
     */
    public short getXMax() {
        return this.xMax;
    }
    
    /** Getter for property yMax.
     * @return Value of property yMax.
     *
     */
    public short getYMax() {
        return this.yMax;
    }
    
    /** Getter for property macStyle.
     * @return Value of property macStyle.
     *
     */
    public short getMacStyle() {
        return this.macStyle;
    }
    
    /** Getter for property lowestRecPPem.
     * @return Value of property lowestRecPPem.
     *
     */
    public short getLowestRecPPem() {
        return this.lowestRecPPem;
    }
    
    /** Getter for property fontDirectionHint.
     * @return Value of property fontDirectionHint.
     *
     */
    public short getFontDirectionHint() {
        return this.fontDirectionHint;
    }
    
    /** Getter for property indexToLocFormat.
     * @return Value of property indexToLocFormat.
     *
     */
    public short getIndexToLocFormat() {
        return this.indexToLocFormat;
    }
    
    /** Getter for property glyphDataFormat.
     * @return Value of property glyphDataFormat.
     *
     */
    public short getGlyphDataFormat() {
        return this.glyphDataFormat;
    }
    
    /** Setter for property XMax.
     * @param xMax New value of property XMax.
     *
     */
    public void setXMax(short xMax) {
        this.xMax = xMax;
    }
    
    /** Setter for property XMin.
     * @param xMin New value of property XMin.
     *
     */
    public void setXMin(short xMin) {
        this.xMin = xMin;
    }
    
    /** Setter for property YMax.
     * @param yMax New value of property YMax.
     *
     */
    public void setYMax(short yMax) {
        this.yMax = yMax;
    }
    
    /** Setter for property YMin.
     * @param yMin New value of property YMin.
     *
     */
    public void setYMin(short yMin) {
        this.yMin = yMin;
    }
    
    /** Setter for property checksumAdjustment.
     * @param checksumAdjustment New value of property checksumAdjustment.
     *
     */
    public void setChecksumAdjustment(int checksumAdjustment) {
        this.checksumAdjustment = checksumAdjustment;
    }
    
    /** Setter for property created.
     * @param created New value of property created.
     *
     */
    public void setCreated(long created) {
        this.created = created;
    }
    
    /** Setter for property flags.
     * @param flags New value of property flags.
     *
     */
    public void setFlags(short flags) {
        this.flags = flags;
    }
    
    /** Setter for property fontDirectionHint.
     * @param fontDirectionHint New value of property fontDirectionHint.
     *
     */
    public void setFontDirectionHint(short fontDirectionHint) {
        this.fontDirectionHint = fontDirectionHint;
    }
    
    /** Setter for property fontRevision.
     * @param fontRevision New value of property fontRevision.
     *
     */
    public void setFontRevision(int fontRevision) {
        this.fontRevision = fontRevision;
    }
    
    /** Setter for property glyphDataFormat.
     * @param glyphDataFormat New value of property glyphDataFormat.
     *
     */
    public void setGlyphDataFormat(short glyphDataFormat) {
        this.glyphDataFormat = glyphDataFormat;
    }
    
    /** Setter for property indexToLocFormat.
     * @param indexToLocFormat New value of property indexToLocFormat.
     *
     */
    public void setIndexToLocFormat(short indexToLocFormat) {
        this.indexToLocFormat = indexToLocFormat;
    }
    
    /** Setter for property lowestRecPPem.
     * @param lowestRecPPem New value of property lowestRecPPem.
     *
     */
    public void setLowestRecPPem(short lowestRecPPem) {
        this.lowestRecPPem = lowestRecPPem;
    }
    
    /** Setter for property macStyle.
     * @param macStyle New value of property macStyle.
     *
     */
    public void setMacStyle(short macStyle) {
        this.macStyle = macStyle;
    }
    
    /** Setter for property magicNumber.
     * @param magicNumber New value of property magicNumber.
     *
     */
    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }
    
    /** Setter for property modified.
     * @param modified New value of property modified.
     *
     */
    public void setModified(long modified) {
        this.modified = modified;
    }
    
    /** Setter for property unitsPerEm.
     * @param unitsPerEm New value of property unitsPerEm.
     *
     */
    public void setUnitsPerEm(short unitsPerEm) {
        this.unitsPerEm = unitsPerEm;
    }
    
    /** Setter for property version.
     * @param version New value of property version.
     *
     */
    public void setVersion(int version) {
        this.version = version;
    }
    
    /**
     * Create a pretty string
     */
    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();
        String indent = "    ";
        
        buf.append(indent + "Version          : " + Integer.toHexString(getVersion()) + "\n");
        buf.append(indent + "Revision         : " + Integer.toHexString(getFontRevision()) + "\n");
        buf.append(indent + "ChecksumAdj      : " + Integer.toHexString(getChecksumAdjustment()) + "\n");
        buf.append(indent + "MagicNumber      : " + Integer.toHexString(getMagicNumber()) + "\n");
        buf.append(indent + "Flags            : " + Integer.toBinaryString(getFlags()) + "\n");
        buf.append(indent + "UnitsPerEm       : " + getUnitsPerEm() + "\n");
        buf.append(indent + "Created          : " + new Date(getCreated()) + "\n");
        buf.append(indent + "Modified         : " + new Date(getModified()) + "\n");
        buf.append(indent + "XMin             : " + getXMin() + "\n");
        buf.append(indent + "XMax             : " + getXMax() + "\n");
        buf.append(indent + "YMin             : " + getYMin() + "\n");
        buf.append(indent + "YMax             : " + getYMax() + "\n");
        buf.append(indent + "MacStyle         : " + Integer.toBinaryString(getMacStyle()) + "\n");
        buf.append(indent + "LowestPPem       : " + getLowestRecPPem() + "\n");
        buf.append(indent + "FontDirectionHint: " + getFontDirectionHint() + "\n");
        buf.append(indent + "IndexToLocFormat : " + getIndexToLocFormat() + "\n");
        buf.append(indent + "GlyphDataFormat  : " + getGlyphDataFormat() + "\n");
    
        return buf.toString();
    }
}