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
import java.util.ArrayList;
import java.util.List;

/**
 * A single simple glyph in a pdf font.
 */
public class GlyfCompound extends Glyf {
    /** flags */
    private static final int ARG_1_AND_2_ARE_WORDS    = 0x1;
    private static final int ARGS_ARE_XY_VALUES       = 0x2;
    private static final int ROUND_XY_TO_GRID         = 0x4;
    private static final int WE_HAVE_A_SCALE          = 0x8;
    private static final int MORE_COMPONENTS          = 0x20;
    private static final int WE_HAVE_AN_X_AND_Y_SCALE = 0x40; 
    private static final int WE_HAVE_A_TWO_BY_TWO     = 0x80;
    private static final int WE_HAVE_INSTRUCTIONS     = 0x100;
    private static final int USE_MY_METRICS 	      = 0x200;
    private static final int OVERLAP_COMPOUND         = 0x400;

    /** the flags for each compound glyph */
    private GlyfComponent[] components;
    
    /** the instructions for the compound as a whole */
    private byte[] instructions;
    
    /**
     * Creates a new instance of a simple glyf
     */
    protected GlyfCompound() {
    }
    
    /**
     * Set the data for this glyf.
     */
    @Override public void setData(ByteBuffer data) {
        // int pos = data.position();
        // byte[] prdata = new byte[data.remaining()];
        // data.get(prdata);
        // HexDump.printData(prdata);
        // data.position(pos);
              
        // read the contour end points
        List<GlyfComponent> comps = new ArrayList<GlyfComponent>();
        GlyfComponent cur = null;
        boolean hasInstructions = false;
        
        do {
            cur = new GlyfComponent();
            cur.flags = data.getShort();
            cur.glyphIndex = data.getShort() & 0xFFFF;
          
            // read either e/f or matching points, as shorts or bytes...
            if (((cur.flags & ARG_1_AND_2_ARE_WORDS) != 0) &&
                ((cur.flags & ARGS_ARE_XY_VALUES) != 0)) {
                cur.e = data.getShort();
                cur.f = data.getShort();
            } else if (!((cur.flags & ARG_1_AND_2_ARE_WORDS) != 0) &&
                        ((cur.flags & ARGS_ARE_XY_VALUES) != 0)) {
                cur.e = data.get();
                cur.f = data.get();
            } else if ( ((cur.flags & ARG_1_AND_2_ARE_WORDS) != 0) &&
                       !((cur.flags & ARGS_ARE_XY_VALUES) != 0)) {
                cur.compoundPoint = data.getShort();
                cur.componentPoint = data.getShort();
            } else {
                cur.compoundPoint = data.get();
                cur.componentPoint = data.get();
            }
         
            // read the linear transform
            if ((cur.flags & WE_HAVE_A_SCALE) != 0) {
                cur.a = (float) data.getShort() / (float) (1 << 14);
                cur.d = cur.a;
            } else if ((cur.flags & WE_HAVE_AN_X_AND_Y_SCALE) != 0) {
                cur.a = (float) data.getShort() / (float) (1 << 14);
                cur.d = (float) data.getShort() / (float) (1 << 14);
            } else if ((cur.flags & WE_HAVE_A_TWO_BY_TWO) != 0) {
                cur.a = (float) data.getShort() / (float) (1 << 14);
                cur.b = (float) data.getShort() / (float) (1 << 14);
                cur.c = (float) data.getShort() / (float) (1 << 14);
                cur.d = (float) data.getShort() / (float) (1 << 14);
            }
        
            if ((cur.flags & WE_HAVE_INSTRUCTIONS) != 0) {
  	        hasInstructions = true;
            }

            comps.add(cur);
        } while ((cur.flags & MORE_COMPONENTS) != 0);

        GlyfComponent[] componentArray = new GlyfComponent[comps.size()];
        comps.toArray(componentArray);
        setComponents(componentArray);
        
        byte[] instr = null;
        if (hasInstructions) {
            // read the instructions
            short numInstructions = data.getShort();
            instr = new byte[numInstructions];
            for (int i = 0; i < instr.length; i++) {
                instr[i] = data.get();
            }
        } else {
            instr = new byte[0];
        }
        setInstructions(instr);
    }
    
    /**
     * Get the data in this glyf as a byte buffer.  Not implemented.
     */
    @Override public ByteBuffer getData() {
        ByteBuffer buf = super.getData();
        
        // don't flip the buffer, since it may be used by subclasses
        return buf;
    }
    
    /**
     * Get the length of this glyf.  Not implemented.
     */
    @Override public short getLength() {
        
        // start with the length of the superclass
        short length = super.getLength();
        return length;
    }
    
    /**
     * Get the number of components in this compound
     */
    public int getNumComponents() {
        return this.components.length;
    }
    
    /**
     * Get a given flag
     */
    public short getFlag(int index) {
        return this.components[index].flags;
    }
    
    /**
     * Get the glyf index for a given glyf
     */
    public int getGlyphIndex(int index) {
        return this.components[index].glyphIndex;
    }
    
    /**
     * Get the base affine transform.  This is based on a whacy formula
     * defined in the true type font spec.
     */
    public double[] getTransform(int index) {
        GlyfComponent gc = this.components[index];

        float m = Math.max(Math.abs(gc.a), Math.abs(gc.b));
        if (Math.abs(Math.abs(gc.a) - Math.abs(gc.c)) < (33 / 65536)) {
            m *= 2;
        }

	float n = Math.max(Math.abs(gc.c), Math.abs(gc.d));
        if (Math.abs(Math.abs(gc.c) - Math.abs(gc.d)) < (33 / 65536)) {
            n *= 2;
        }
        
        float e = m * gc.e;
        float f = n * gc.f;
        
        return new double[] { gc.a, gc.b, gc.c, gc.d, e, f }; 
    }
  
    /**
     * Get the point in the compound glyph to match
     */
    public int getCompoundPoint(int index) {
        return this.components[index].compoundPoint;
    }
    
    /**
     * Get the point in the component glyph to match
     */
    public int getComponentPoint(int index) {
        return this.components[index].componentPoint;
    }
 
    /**
     * Determine whether args 1 and 2 are words or bytes
     */
    public boolean argsAreWords(int index) {
        return ((getFlag(index) & ARG_1_AND_2_ARE_WORDS) != 0);
    }
    
    /**
     * Determine whether args 1 and 2 are xy values or point indices
     */
    public boolean argsAreXYValues(int index) {
        return ((getFlag(index) & ARGS_ARE_XY_VALUES) != 0);
    }
    
    /**
     * Determine whether to round XY values to the grid
     */
    public boolean roundXYToGrid(int index) {
        return ((getFlag(index) & ROUND_XY_TO_GRID) != 0);
    }
    
    /**
     * Determine whether there is a simple scale
     */
    public boolean hasAScale(int index) {
        return ((getFlag(index) & WE_HAVE_A_SCALE) != 0);
    }
    
    /**
     * Determine whether there are more components left to read
     */
    protected boolean moreComponents(int index) {
        return ((getFlag(index) & MORE_COMPONENTS) != 0);
    }
    
    /**
     * Determine whether there are separate scales on X and Y
     */
    protected boolean hasXYScale(int index) {
        return ((getFlag(index) & WE_HAVE_AN_X_AND_Y_SCALE) != 0);
    }
    
    /**
     * Determine whether there is a 2x2 transform
     */
    protected boolean hasTwoByTwo(int index) {
        return ((getFlag(index) & WE_HAVE_A_TWO_BY_TWO) != 0);
    }
    
    /**
     * Determine whether there are instructions
     */
    protected boolean hasInstructions(int index) {
        return ((getFlag(index) & WE_HAVE_INSTRUCTIONS) != 0);
    }
    
    /**
     * Use the metrics of this component for the compound
     */
    public boolean useMetrics(int index) {
        return ((getFlag(index) & USE_MY_METRICS) != 0);
    }
    
    /**
     * This component overlaps the existing compound
     */
    public boolean overlapCompound(int index) {
        return ((getFlag(index) & OVERLAP_COMPOUND) != 0);
    }
    
    /**
     * Set the components
     */
    void setComponents(GlyfComponent[] components) {
        this.components = components;
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
     * The record for a single component of this compound glyph
     */
    static class GlyfComponent {
        /** flags */
        short flags;
        
        /** the index of the component glyf */
        int glyphIndex;
        
        /** the points to match */
        int compoundPoint;
        int componentPoint;
        
        /** affine transform of this component */
        float a = 1.0f;
        float b = 0.0f;
        float c = 0.0f;
        float d = 1.0f;
        float e = 0.0f;
        float f = 0.0f;
    }
}