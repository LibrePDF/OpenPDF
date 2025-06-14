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

package com.sun.pdfview.function;

import java.io.IOException;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;

/**
 * A type 2 function is an exponential interpolation function, which maps
 * from one input value to n output values using a simple exponential
 * formula.
 */
public class FunctionType2 extends PDFFunction {
    /** the function's value at zero for the n outputs */
    private float[] c0 = new float[] { 0f };
    
    /** the function's value at one for the n outputs */
    private float[] c1 = new float[] { 1f };
    
    /** the exponent */
    private float n;
    
    /** Creates a new instance of FunctionType2 */
    public FunctionType2() {
        super(TYPE_2);
    }
    
    /**
     * Read the zeros, ones and exponent
     */
    @Override
	protected void parse(PDFObject obj) throws IOException 
    {
        // read the exponent (required)
        PDFObject nObj = obj.getDictRef("N");
        if (nObj == null) {
            throw new PDFParseException("Exponent required for function type 2!");
        }
        setN(nObj.getFloatValue());
        
        // read the zeros array (optional)
        PDFObject cZeroObj = obj.getDictRef("C0");
        if (cZeroObj != null) {
            PDFObject[] cZeroAry = cZeroObj.getArray();
            float[] cZero = new float[cZeroAry.length];
            for (int i = 0; i < cZeroAry.length; i++) {
                cZero[i] = cZeroAry[i].getFloatValue();
            }
            setC0(cZero);
        }
        
        // read the ones array (optional)
        PDFObject cOneObj = obj.getDictRef("C1");
        if (cOneObj != null) {
            PDFObject[] cOneAry = cOneObj.getArray();
            float[] cOne = new float[cOneAry.length];
            for (int i = 0; i < cOneAry.length; i++) {
                cOne[i] = cOneAry[i].getFloatValue();
            }
            setC1(cOne);
        }
    }
    
    /**
     * Calculate the function value for the input.  For each output (j),
     * the function value is:
     * C0(j) + x^N * (C1(j) - C0(j))
     */
    @Override
	protected void doFunction(float[] inputs, int inputOffset, 
                              float[] outputs, int outputOffset)
    {
        // read the input value
        float input = inputs[inputOffset];
        
        // calculate the output values
        for (int i = 0; i < getNumOutputs(); i++) {
            outputs[i + outputOffset] = getC0(i) + 
                (float) (Math.pow(input, getN()) * (getC1(i) - getC0(i)));
        }
    }
    
    @Override
    public int getNumOutputs()
    {
        // For Type 2 functions, the number of outputs is determined by the size of C0 (or C1).
        return c0.length;
    }
    
    /**
     * Get the exponent
     */
    public float getN() {
        return this.n;
    }
    
    /**
     * Set the exponent
     */
    protected void setN(float n) {
        this.n = n;
    }
    
    /**
     * Get the values at zero
     */
    public float getC0(int index) {
        return this.c0[index];
    }
    
    /**
     * Set the values at zero
     */
    protected void setC0(float[] c0) {
        this.c0 = c0;
    }
    
    /**
     * Get the values at one
     */
    public float getC1(int index) {
        return this.c1[index];
    }
    
    /**
     * Set the values at one
     */
    protected void setC1(float[] c1) {
        this.c1 = c1;
    }  
}