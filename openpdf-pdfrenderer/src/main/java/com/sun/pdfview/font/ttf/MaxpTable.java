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
 *
 * @author  jkaplan
 */
public class MaxpTable extends TrueTypeTable {
    
    /** Holds value of property version. */
    private int version;

    // the following are supposed to be USHORT, but will be Int to enclose the sign
    // (http://www.microsoft.com/typography/OTSpec/maxp.htm)
    /** Holds value of property numGlyphs. */
    private int numGlyphs;
    
    /** Holds value of property maxPoints. */
    private int maxPoints;
    
    /** Holds value of property maxContours. */
    private int maxContours;
    
    /** Holds value of property maxComponentPoints. */
    private int maxComponentPoints;
    
    /** Holds value of property maxComponentContours. */
    private int maxComponentContours;
    
    /** Holds value of property maxZones. */
    private int maxZones;
    
    /** Holds value of property maxTwilightPoints. */
    private int maxTwilightPoints;
    
    /** Holds value of property maxStorage. */
    private int maxStorage;
    
    /** Holds value of property maxFunctionDefs. */
    private int maxFunctionDefs;
    
    /** Holds value of property maxInstructionDefs. */
    private int maxInstructionDefs;
    
    /** Holds value of property maxStackElements. */
    private int maxStackElements;
    
    /** Holds value of property maxSizeOfInstructions. */
    private int maxSizeOfInstructions;
    
    /** Holds value of property maxComponentElements. */
    private int maxComponentElements;
    
    /** Holds value of property maxComponentDepth. */
    private int maxComponentDepth;
    
    /** Creates a new instance of MaxpTable */
    protected MaxpTable() {
        super (TrueTypeTable.MAXP_TABLE);
        
        setVersion(0x10000);
        setNumGlyphs(0);
        setMaxPoints(0);
        setMaxContours(0);
        setMaxComponentPoints(0);
        setMaxComponentContours(0);
        setMaxZones(2);
        setMaxTwilightPoints(0);
        setMaxStorage(0);
        setMaxFunctionDefs(0);
        setMaxInstructionDefs(0);
        setMaxStackElements(0);
        setMaxSizeOfInstructions(0);
        setMaxComponentElements(0);
        setMaxComponentDepth(0);
    }
    
    /**
     * Set the values from data
     */
    @Override
	public void setData(ByteBuffer data) {
        if (data.remaining() != 32) {
            throw new IllegalArgumentException("Bad size for Maxp table");
        }
        
        setVersion(data.getInt());
        setNumGlyphs(data.getShort());
        setMaxPoints(data.getShort());
        setMaxContours(data.getShort());
        setMaxComponentPoints(data.getShort());
        setMaxComponentContours(data.getShort());
        setMaxZones(data.getShort());
        setMaxTwilightPoints(data.getShort());
        setMaxStorage(data.getShort());
        setMaxFunctionDefs(data.getShort());
        setMaxInstructionDefs(data.getShort());
        setMaxStackElements(data.getShort());
        setMaxSizeOfInstructions(data.getShort());
        setMaxComponentElements(data.getShort());
        setMaxComponentDepth(data.getShort());
    }
    
    /**
     * Get a buffer from the data
     */
    @Override
	public ByteBuffer getData() {
        ByteBuffer buf = ByteBuffer.allocate(getLength());
        
        buf.putInt(getVersion());
        buf.putShort((short) getNumGlyphs());
        buf.putShort((short) getMaxPoints());
        buf.putShort((short) getMaxContours());
        buf.putShort((short) getMaxComponentPoints());
        buf.putShort((short) getMaxComponentContours());
        buf.putShort((short) getMaxZones());
        buf.putShort((short) getMaxTwilightPoints());
        buf.putShort((short) getMaxStorage());
        buf.putShort((short) getMaxFunctionDefs());
        buf.putShort((short) getMaxInstructionDefs());
        buf.putShort((short) getMaxStackElements());
        buf.putShort((short) getMaxSizeOfInstructions());
        buf.putShort((short) getMaxComponentElements());
        buf.putShort((short) getMaxComponentDepth());
    
        // reset the position to the beginning of the buffer
        buf.flip();
        
        return buf;
    }
    
    /**
     * Get the length of this table
     */
    @Override
	public int getLength() {
        return 32;
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
    
    /** Getter for property numGlyphs.
     * @return Value of property numGlyphs.
     *
     */
    public int getNumGlyphs() {
        return this.numGlyphs & 0xFFFF;
    }
    
    /** Setter for property numGlyphs.
     * @param numGlyphs New value of property numGlyphs.
     *
     */
    public void setNumGlyphs(int numGlyphs) {
        this.numGlyphs = numGlyphs;
    }
    
    /** Getter for property maxPoints.
     * @return Value of property maxPoints.
     *
     */
    public int getMaxPoints() {
        return this.maxPoints & 0xFFFF;
    }
    
    /** Setter for property maxPoints.
     * @param maxPoints New value of property maxPoints.
     *
     */
    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }
    
    /** Getter for property maxContours.
     * @return Value of property maxContours.
     *
     */
    public int getMaxContours() {
        return this.maxContours & 0xFFFF;
    }
    
    /** Setter for property maxContours.
     * @param maxContours New value of property maxContours.
     *
     */
    public void setMaxContours(int maxContours) {
        this.maxContours = maxContours;
    }
    
    /** Getter for property maxComponentPoints.
     * @return Value of property maxComponentPoints.
     *
     */
    public int getMaxComponentPoints() {
        return this.maxComponentPoints & 0xFFFF;
    }
    
    /** Setter for property maxComponentPoints.
     * @param maxComponentPoints New value of property maxComponentPoints.
     *
     */
    public void setMaxComponentPoints(int maxComponentPoints) {
        this.maxComponentPoints = maxComponentPoints;
    }
    
    /** Getter for property maxComponentContours.
     * @return Value of property maxComponentContours.
     *
     */
    public int getMaxComponentContours() {
        return this.maxComponentContours & 0xFFFF;
    }
    
    /** Setter for property maxComponentContours.
     * @param maxComponentContours New value of property maxComponentContours.
     *
     */
    public void setMaxComponentContours(int maxComponentContours) {
        this.maxComponentContours = maxComponentContours;
    }
    
    /** Getter for property maxZones.
     * @return Value of property maxZones.
     *
     */
    public int getMaxZones() {
        return this.maxZones & 0xFFFF;
    }
    
    /** Setter for property maxZones.
     * @param maxZones New value of property maxZones.
     *
     */
    public void setMaxZones(int maxZones) {
        this.maxZones = maxZones;
    }
    
    /** Getter for property maxTwilightPoints.
     * @return Value of property maxTwilightPoints.
     *
     */
    public int getMaxTwilightPoints() {
        return this.maxTwilightPoints & 0xFFFF;
    }
    
    /** Setter for property maxTwilightPoints.
     * @param maxTwilightPoints New value of property maxTwilightPoints.
     *
     */
    public void setMaxTwilightPoints(int maxTwilightPoints) {
        this.maxTwilightPoints = maxTwilightPoints;
    }
    
    /** Getter for property maxStorage.
     * @return Value of property maxStorage.
     *
     */
    public int getMaxStorage() {
        return this.maxStorage & 0xFFFF;
    }
    
    /** Setter for property maxStorage.
     * @param maxStorage New value of property maxStorage.
     *
     */
    public void setMaxStorage(int maxStorage) {
        this.maxStorage = maxStorage;
    }
    
    /** Getter for property maxFunctionDefs.
     * @return Value of property maxFunctionDefs.
     *
     */
    public int getMaxFunctionDefs() {
        return this.maxFunctionDefs & 0xFFFF;
    }
    
    /** Setter for property maxFunctionDefs.
     * @param maxFunctionDefs New value of property maxFunctionDefs.
     *
     */
    public void setMaxFunctionDefs(int maxFunctionDefs) {
        this.maxFunctionDefs = maxFunctionDefs;
    }
    
    /** Getter for property maxInstructionDefs.
     * @return Value of property maxInstructionDefs.
     *
     */
    public int getMaxInstructionDefs() {
        return this.maxInstructionDefs & 0xFFFF;
    }
    
    /** Setter for property maxInstructionDefs.
     * @param maxInstructionDefs New value of property maxInstructionDefs.
     *
     */
    public void setMaxInstructionDefs(int maxInstructionDefs) {
        this.maxInstructionDefs = maxInstructionDefs;
    }
    
    /** Getter for property maxStackElements.
     * @return Value of property maxStackElements.
     *
     */
    public int getMaxStackElements() {
        return this.maxStackElements & 0xFFFF;
    }
    
    /** Setter for property maxStackElements.
     * @param maxStackElements New value of property maxStackElements.
     *
     */
    public void setMaxStackElements(int maxStackElements) {
        this.maxStackElements = maxStackElements;
    }
    
    /** Getter for property maxSizeOfInstructions.
     * @return Value of property maxSizeOfInstructions.
     *
     */
    public int getMaxSizeOfInstructions() {
        return this.maxSizeOfInstructions & 0xFFFF;
    }
    
    /** Setter for property maxSizeOfInstructions.
     * @param maxSizeOfInstructions New value of property maxSizeOfInstructions.
     *
     */
    public void setMaxSizeOfInstructions(int maxSizeOfInstructions) {
        this.maxSizeOfInstructions = maxSizeOfInstructions;
    }
    
    /** Getter for property maxComponentElements.
     * @return Value of property maxComponentElements.
     *
     */
    public int getMaxComponentElements() {
        return this.maxComponentElements & 0xFFFF;
    }
    
    /** Setter for property maxComponentElements.
     * @param maxComponentElements New value of property maxComponentElements.
     *
     */
    public void setMaxComponentElements(int maxComponentElements) {
        this.maxComponentElements = maxComponentElements;
    }
    
    /** Getter for property maxComponentDepth.
     * @return Value of property maxComponentDepth.
     *
     */
    public int getMaxComponentDepth() {
        return this.maxComponentDepth & 0xFFFF;
    }
    
    /** Setter for property maxComponentDepth.
     * @param maxComponentDepth New value of property maxComponentDepth.
     *
     */
    public void setMaxComponentDepth(int maxComponentDepth) {
        this.maxComponentDepth = maxComponentDepth;
    }
    
    /**
     * Create a pretty String
     */
    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();
        String indent = "    ";
        
        buf.append(indent + "Version          : " + Integer.toHexString(getVersion()) + "\n");
        buf.append(indent + "NumGlyphs        : " + getNumGlyphs() + "\n");
        buf.append(indent + "MaxPoints        : " + getMaxPoints() + "\n");
        buf.append(indent + "MaxContours      : " + getMaxContours() + "\n");
        buf.append(indent + "MaxCompPoints    : " + getMaxComponentPoints() + "\n");
        buf.append(indent + "MaxCompContours  : " + getMaxComponentContours() + "\n");
        buf.append(indent + "MaxZones         : " + getMaxZones() + "\n");
        buf.append(indent + "MaxTwilightPoints: " + getMaxTwilightPoints() + "\n");
        buf.append(indent + "MaxStorage       : " + getMaxStorage() + "\n");
        buf.append(indent + "MaxFuncDefs      : " + getMaxFunctionDefs() + "\n");
        buf.append(indent + "MaxInstDefs      : " + getMaxInstructionDefs() + "\n");
        buf.append(indent + "MaxStackElements : " + getMaxStackElements() + "\n");
        buf.append(indent + "MaxSizeInst      : " + getMaxSizeOfInstructions() + "\n");
        buf.append(indent + "MaxCompElements  : " + getMaxComponentElements() + "\n");
        buf.append(indent + "MaxCompDepth     : " + getMaxComponentDepth() + "\n");
    
        return buf.toString();
    }
}