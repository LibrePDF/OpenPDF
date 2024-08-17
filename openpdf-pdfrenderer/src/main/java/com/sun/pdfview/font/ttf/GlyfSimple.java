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
 * A single simple glyph in a pdf font. 
 */
public class GlyfSimple extends Glyf {
    /** the end points of the various contours */
    private short[] contourEndPts;
    
    /** the instructions */
    private byte[] instructions;
    
    /** the flags */
    private byte[] flags;
    
    /** the x coordinates */
    private short[] xCoords;
    
    /** the y coordinates */
    private short[] yCoords;
    
    /** 
     * Creates a new instance of a simple glyf
     */
    protected GlyfSimple() {
    }
    
    /**
     * Set the data for this glyf.
     */
    @Override
	public void setData(ByteBuffer data) {
        // int pos = data.position();
        // byte[] prdata = new byte[data.remaining()];
        // data.get(prdata);
        // HexDump.printData(prdata);
        // data.position(pos);
        
        
        // read the contour end points
        short[] contourEndPts = new short[getNumContours()];
        for (int i = 0; i < contourEndPts.length; i++) {
            contourEndPts[i] = data.getShort();
        }
        setContourEndPoints(contourEndPts);
        
        // the number of points in the glyf is the number of the end
        // point in the last contour
        int numPoints = getContourEndPoint(getNumContours() - 1) + 1;
        
        // read the instructions
        short numInstructions = data.getShort();
        byte[] instructions = new byte[numInstructions];
        for (int i = 0; i < instructions.length; i++) {
            instructions[i] = data.get();
        }
        setInstructions(instructions);
        
        // read the flags
        byte[] flags = new byte[numPoints];
        for (int i = 0; i < flags.length; i++) {
            flags[i] = data.get();
            
            // check for repeats
            if ((flags[i] & 0x8) != 0) {
                byte f = flags[i];
                int n = (data.get() & 0xff);
                for (int c = 0; c < n; c++) {
                    flags[++i] =  f;
                }
            }
        }
        setFlags(flags);
        
        // read the x coordinates
        short[] xCoords = new short[numPoints];
        for (int i = 0; i < xCoords.length; i++) {
             if (i > 0) {
                 xCoords[i] = xCoords[i - 1];
             }

             // read this value
            if (xIsByte(i)) {
                int val = (data.get() & 0xff);
                if (!xIsSame(i)) {
                    // the xIsSame bit controls the sign
                    val = -val;
                }
                xCoords[i] += val;
            } else if (!xIsSame(i)) {
                xCoords[i] += data.getShort();
            }
        }
        setXCoords(xCoords);
        
        // read the y coordinates
        short[] yCoords = new short[numPoints];
        for (int i = 0; i < yCoords.length; i++) {
            if (i > 0) {
                yCoords[i] = yCoords[i - 1];
            } 
            // read this value
            if (yIsByte(i)) {   
                int val = (data.get() & 0xff);
                if (!yIsSame(i)) {
                    // the xIsSame bit controls the sign
                    val = -val;
                }
                yCoords[i] += val;
            } else if (!yIsSame(i)) {
                yCoords[i] += data.getShort();
            }
        }
        setYCoords(yCoords);
    }
    
    /**
     * Get the data in this glyf as a byte buffer.  Return the basic
     * glyf data only, since there is no specific data.  This method returns
     * the data un-flipped, so subclasses can simply append to the allocated
     * buffer.
     */
    @Override
	public ByteBuffer getData() {
        ByteBuffer buf = super.getData();
        
        // write the contour end points
        for (int i = 0; i < getNumContours(); i++) {
            buf.putShort(getContourEndPoint(i));
        }
        
        // write the instructions
        buf.putShort(getNumInstructions());
        for (int i = 0; i < getNumInstructions(); i++) {
            buf.put(getInstruction(i));
        }
        
        // write the flags
        for (int i = 0; i < getNumPoints(); i++) {
            // check for repeats
            byte r = 0;
            while (i > 0 && (getFlag(i) == getFlag(i - 1))) {
                r++;
                i++;
            }
            if (r > 0) {
                buf.put(r);
            } else {
                buf.put(getFlag(i));
            }
        }
        
        // write the x coordinates
        for (int i = 0; i < getNumPoints(); i++) {
            if (xIsByte(i)) {
                buf.put((byte) getXCoord(i));
            } else if (!xIsSame(i)) {
                buf.putShort(getXCoord(i));
            }
        }
        
        // write the y coordinates
        for (int i = 0; i < getNumPoints(); i++) {
            if (yIsByte(i)) {
                buf.put((byte) getYCoord(i));
            } else if (!yIsSame(i)) {
                buf.putShort(getYCoord(i));
            }
        }
        
        // don't flip the buffer, since it may be used by subclasses
        return buf;
    }
    
    /**
     * Get the length of this glyf. 
     */
    @Override
	public short getLength() {
        // start with the length of the superclass
        short length = super.getLength();
        
        // add the length of the end points
        length += getNumContours() * 2;
        
        // add the length of the instructions
        length += 2 + getNumInstructions();
        
        // add the length of the flags, avoiding repeats
        for (int i = 0; i < getNumPoints(); i++) {
            // check for repeats
            while (i > 0 && (getFlag(i) == getFlag(i - 1)));
            length++;
        }
        
        // add the length of the xCoordinates
        for (int i = 0; i < getNumPoints(); i++) {
            if (xIsByte(i)) {
                length++;
            } else if (!xIsSame(i)) {
                length += 2;
            }
            
            if (yIsByte(i)) {
                length++;
            } else if (!yIsSame(i)) {
                length += 2;
            }
        }
         
        return length;
    }
    
    /**
     * Get the end point of a given contour
     */
    public short getContourEndPoint(int index) {
        return this.contourEndPts[index];
    }
    
    /**
     * Set the number of contours in this glyf
     */
    protected void setContourEndPoints(short[] contourEndPts) {
        this.contourEndPts = contourEndPts;
    }
    
   /**
    * Get the number of instructions
    */
    public short getNumInstructions() {
        return (short) this.instructions.length;
    }
    
    /**
     * Get a given instruction
     */
    public byte getInstruction(int index) {
        return this.instructions[index];
    }
    
    /**
     * Set the instructions
     */
    protected void setInstructions(byte[] instructions) {
        this.instructions = instructions;
    }
    
    /**
     * Get the number of points in the glyf
     */
    public short getNumPoints() {
        return (short) this.flags.length;
    }
    
    /**
     * Get a given flag
     */
    public byte getFlag(int pointIndex) {
        return this.flags[pointIndex];
    }
    
    /**
     * Determine whether the given point is on the curve
     */ 
    public boolean onCurve(int pointIndex) {
        return ((getFlag(pointIndex) & 0x1) != 0);
    }
    
    /**
     * Determine whether the x value for the given point is byte or short.
     * If true, it is a byte, if false it is a short
     */ 
    protected boolean xIsByte(int pointIndex) {
        return ((getFlag(pointIndex) & 0x2) != 0);
    }
    
    /**
     * Determine whether the x value for the given point is byte or short.
     * If true, it is a byte, if false it is a short
     */ 
    protected boolean yIsByte(int pointIndex) {
        return ((getFlag(pointIndex) & 0x4) != 0);
    }
    
    /**
     * Determine whether this flag repeats
     */ 
    protected boolean repeat(int pointIndex) {
        return ((getFlag(pointIndex) & 0x8) != 0);
    }
    
    /**
     * Determine whether the x value for the given point is the same as 
     * the previous value.
     */ 
    protected boolean xIsSame(int pointIndex) {
        return ((getFlag(pointIndex) & 0x10) != 0);
    }
    
    /**
     * Determine whether the y value for the given point is the same as 
     * the previous value.
     */ 
    protected boolean yIsSame(int pointIndex) {
        return ((getFlag(pointIndex) & 0x20) != 0);
    }
    
    /**
     * Set the flags
     */
    protected void setFlags(byte[] flags) {
        this.flags = flags;
    }
    
    /**
     * Get a given x coordinate
     */
    public short getXCoord(int pointIndex) {
        return this.xCoords[pointIndex];
    }
    
    /**
     * Set the x coordinates
     */
    protected void setXCoords(short[] xCoords) {
        this.xCoords = xCoords;
    }
    
    /**
     * Get a given y coordinate
     */
    public short getYCoord(int pointIndex) {
        return this.yCoords[pointIndex];
    }
    
    /**
     * Set the x coordinates
     */
    protected void setYCoords(short[] yCoords) {
        this.yCoords = yCoords;
    }
}