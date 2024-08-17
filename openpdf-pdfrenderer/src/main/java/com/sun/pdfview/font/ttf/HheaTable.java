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

/**
 *
 * @author  jkaplan
 */
public class HheaTable extends TrueTypeTable {
    
    /** Holds value of property version. */
    private int version;
    
    /** Holds value of property ascent. */
    private short ascent;
    
    /** Holds value of property descent. */
    private short descent;
    
    /** Holds value of property lineGap. */
    private short lineGap;
    
    /** Holds value of property advanceWidthMax. */
    private short advanceWidthMax;
    
    /** Holds value of property minLeftSideBearing. */
    private short minLeftSideBearing;
    
    /** Holds value of property minRightSideBearing. */
    private short minRightSideBearing;
    
    /** Holds value of property xMaxExtent. */
    private short xMaxExtent;
    
    /** Holds value of property caretSlopeRise. */
    private short caretSlopeRise;
    
    /** Holds value of property caretSlopeRun. */
    private short caretSlopeRun;
    
    /** Holds value of property caretOffset. */
    private short caretOffset;
    
    /** Holds value of property metricDataFormat. */
    private short metricDataFormat;
    
    /** Holds value of property numOfLongHorMetrics. */
    private short numOfLongHorMetrics;
    
    /** Creates a new instance of HeadTable 
     * Makes up reasonable(?) defaults for all values
     */
    protected HheaTable() {
        super(TrueTypeTable.HEAD_TABLE);
        
        setVersion(0x10000);
    }
    
    /**
     * Parse the data before it is set
     */
    @Override
	public void setData(ByteBuffer data) {
        if (data.remaining() != 36) {
            throw new IllegalArgumentException("Bad Head table size");
        }
        setVersion(data.getInt());
        setAscent(data.getShort());
        setDescent(data.getShort());
        setLineGap(data.getShort());
        setAdvanceWidthMax(data.getShort());
        setMinLeftSideBearing(data.getShort());
        setMinRightSideBearing(data.getShort());
        setXMaxExtent(data.getShort());
        setCaretSlopeRise(data.getShort());
        setCaretSlopeRun(data.getShort());
        setCaretOffset(data.getShort());
        
        // padding
        data.getShort();
        data.getShort();
        data.getShort();
        data.getShort();
        
        setMetricDataFormat(data.getShort());
        setNumOfLongHorMetrics(data.getShort());
    }
    
    /**
     * Get the data we have stored
     */
    @Override
	public ByteBuffer getData() {
        ByteBuffer buf = ByteBuffer.allocate(getLength());
        
        buf.putInt(getVersion());
        buf.putShort(getAscent());
        buf.putShort(getDescent());
        buf.putShort(getLineGap());
        buf.putShort(getAdvanceWidthMax());
        buf.putShort(getMinLeftSideBearing());
        buf.putShort(getMinRightSideBearing());
        buf.putShort(getXMaxExtent());
        buf.putShort(getCaretSlopeRise());
        buf.putShort(getCaretSlopeRun());
        buf.putShort(getCaretOffset());
        
        // padding
        buf.putShort((short) 0);
        buf.putShort((short) 0);
        buf.putShort((short) 0);
        buf.putShort((short) 0);
        
        buf.putShort(getMetricDataFormat());
        buf.putShort((short) getNumOfLongHorMetrics());
    
        // reset the position to the start of the buffer
        buf.flip();
        
        return buf;
    }
    
    /**
     * Get the length of this table
     */
    @Override
	public int getLength() {
        return 36;
    }
    
    /** Getter for property version.
     * @return Value of property version.
     *
     */
    public int getVersion() {
        return this.version;
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
        
        buf.append(indent + "Version             : " + Integer.toHexString(getVersion()) + "\n");
        buf.append(indent + "Ascent              : " + getAscent() + "\n");
        buf.append(indent + "Descent             : " + getDescent() + "\n");
        buf.append(indent + "LineGap             : " + getLineGap() + "\n");
        buf.append(indent + "AdvanceWidthMax     : " + getAdvanceWidthMax() + "\n");
        buf.append(indent + "MinLSB              : " + getMinLeftSideBearing() + "\n");
        buf.append(indent + "MinRSB              : " + getMinRightSideBearing() + "\n");
        buf.append(indent + "MaxExtent           : " + getXMaxExtent() + "\n");
        buf.append(indent + "CaretSlopeRise      : " + getCaretSlopeRise() + "\n");
        buf.append(indent + "CaretSlopeRun       : " + getCaretSlopeRun() + "\n");
        buf.append(indent + "CaretOffset         : " + getCaretOffset() + "\n");
        buf.append(indent + "MetricDataFormat    : " + getMetricDataFormat() + "\n");
        buf.append(indent + "NumOfLongHorMetrics : " + getNumOfLongHorMetrics() + "\n");
        return buf.toString();
    }
    
    /** Getter for property ascent.
     * @return Value of property ascent.
     *
     */
    public short getAscent() {
        return this.ascent;
    }
    
    /** Setter for property ascent.
     * @param ascent New value of property ascent.
     *
     */
    public void setAscent(short ascent) {
        this.ascent = ascent;
    }
    
    /** Getter for property descent.
     * @return Value of property descent.
     *
     */
    public short getDescent() {
        return this.descent;
    }
    
    /** Setter for property descent.
     * @param descent New value of property descent.
     *
     */
    public void setDescent(short descent) {
        this.descent = descent;
    }
    
    /** Getter for property lineGap.
     * @return Value of property lineGap.
     *
     */
    public short getLineGap() {
        return this.lineGap;
    }
    
    /** Setter for property lineGap.
     * @param lineGap New value of property lineGap.
     *
     */
    public void setLineGap(short lineGap) {
        this.lineGap = lineGap;
    }
    
    /** Getter for property advanceWidthMax.
     * @return Value of property advanceWidthMax.
     *
     */
    public short getAdvanceWidthMax() {
        return this.advanceWidthMax;
    }
    
    /** Setter for property advanceWidthMax.
     * @param advanceWidthMax New value of property advanceWidthMax.
     *
     */
    public void setAdvanceWidthMax(short advanceWidthMax) {
        this.advanceWidthMax = advanceWidthMax;
    }
    
    /** Getter for property minLeftSideBearing.
     * @return Value of property minLeftSideBearing.
     *
     */
    public short getMinLeftSideBearing() {
        return this.minLeftSideBearing;
    }
    
    /** Setter for property minLeftSideBearing.
     * @param minLeftSideBearing New value of property minLeftSideBearing.
     *
     */
    public void setMinLeftSideBearing(short minLeftSideBearing) {
        this.minLeftSideBearing = minLeftSideBearing;
    }
    
    /** Getter for property minRIghtSideBearing.
     * @return Value of property minRIghtSideBearing.
     *
     */
    public short getMinRightSideBearing() {
        return this.minRightSideBearing;
    }
    
    /** Setter for property minRIghtSideBearing.
     * @param minRightSideBearing New value of property minRIghtSideBearing.
     *
     */
    public void setMinRightSideBearing(short minRightSideBearing) {
        this.minRightSideBearing = minRightSideBearing;
    }
    
    /** Getter for property xMaxExtent.
     * @return Value of property xMaxExtent.
     *
     */
    public short getXMaxExtent() {
        return this.xMaxExtent;
    }
    
    /** Setter for property xMaxExtent.
     * @param xMaxExtent New value of property xMaxExtent.
     *
     */
    public void setXMaxExtent(short xMaxExtent) {
        this.xMaxExtent = xMaxExtent;
    }
    
    /** Getter for property caretSlopeRise.
     * @return Value of property caretSlopeRise.
     *
     */
    public short getCaretSlopeRise() {
        return this.caretSlopeRise;
    }
    
    /** Setter for property caretSlopeRise.
     * @param caretSlopeRise New value of property caretSlopeRise.
     *
     */
    public void setCaretSlopeRise(short caretSlopeRise) {
        this.caretSlopeRise = caretSlopeRise;
    }
    
    /** Getter for property caretSlopeRun.
     * @return Value of property caretSlopeRun.
     *
     */
    public short getCaretSlopeRun() {
        return this.caretSlopeRun;
    }
    
    /** Setter for property caretSlopeRun.
     * @param caretSlopeRun New value of property caretSlopeRun.
     *
     */
    public void setCaretSlopeRun(short caretSlopeRun) {
        this.caretSlopeRun = caretSlopeRun;
    }
    
    /** Getter for property caretOffset.
     * @return Value of property caretOffset.
     *
     */
    public short getCaretOffset() {
        return this.caretOffset;
    }
    
    /** Setter for property caretOffset.
     * @param caretOffset New value of property caretOffset.
     *
     */
    public void setCaretOffset(short caretOffset) {
        this.caretOffset = caretOffset;
    }
    
    /** Getter for property metricDataFormat.
     * @return Value of property metricDataFormat.
     *
     */
    public short getMetricDataFormat() {
        return this.metricDataFormat;
    }
    
    /** Setter for property metricDataFormat.
     * @param metricDataFormat New value of property metricDataFormat.
     *
     */
    public void setMetricDataFormat(short metricDataFormat) {
        this.metricDataFormat = metricDataFormat;
    }
    
    /** Getter for property numOfLongHorMetrics.
     * @return Value of property numOfLongHorMetrics.
     *
     */
    public int getNumOfLongHorMetrics() {
        return this.numOfLongHorMetrics & 0xFFFF;
    }
    
    /** Setter for property numOfLongHorMetrics.
     * @param numOfLongHorMetrics New value of property numOfLongHorMetrics.
     *
     */
    public void setNumOfLongHorMetrics(short numOfLongHorMetrics) {
        this.numOfLongHorMetrics = numOfLongHorMetrics;
    }
    
}