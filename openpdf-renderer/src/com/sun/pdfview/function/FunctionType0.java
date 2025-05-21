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
import java.nio.ByteBuffer;

import com.sun.pdfview.PDFDebugger;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;

/**
 * A sampled function maps input values to output values by interpolating
 * along a line or cubic between two known values.
 */
public class FunctionType0 extends PDFFunction {
    /** the valid interpolation methods */
    protected static final int LINEAR_INTERPOLATION = 1;
    protected static final int CUBIC_INTERPOLATION = 3;
    
    /** the size of each input dimension, as an array of <i>m</i> integers */
    private int[] size;
    
    /** the number of bits in each sample */
    private int bitsPerSample;
    
    /** the interpolation type, from the list above */
    private int order = 1;
    
    /** the optional encoding array, tells how to map input parameters to values */
    private float[] encode;
    
    /** the optional decoding array, tells how to map output parameters to values */
    private float[] decode;
    
    /** 
     * the actual samples, converted to integers.  The first index is
     * input values (from 0 to size[m - 1] * size[m - 2] * ... * size[0]), 
     * and the second is the output dimension within the sample (from 0 to n)
     */
    private int[][] samples;
    
    /** Creates a new instance of FunctionType0 */
    protected FunctionType0() {
        super (TYPE_0);
    }
    
    /** Read the function information from a PDF Object */
    @Override
	protected void parse(PDFObject obj) throws IOException {
        // read the size array (required)
        PDFObject sizeObj = obj.getDictRef("Size");
        if (sizeObj == null) {
            throw new PDFParseException("Size required for function type 0!");
        }
        PDFObject[] sizeAry = sizeObj.getArray();
        int[] size = new int[sizeAry.length];
        for (int i = 0; i < sizeAry.length; i++) {
            size[i] = sizeAry[i].getIntValue();
        }
        setSize(size);
    
        // read the # bits per sample (required)
        PDFObject bpsObj = obj.getDictRef("BitsPerSample");
        if (bpsObj == null) {
            throw new PDFParseException("BitsPerSample required for function type 0!");
        }
        setBitsPerSample(bpsObj.getIntValue());
        
        // read the order (optional)
        PDFObject orderObj = obj.getDictRef("Order");
        if (orderObj != null) {
            setOrder(orderObj.getIntValue());
        }
        
        // read the encode array (optional)
        PDFObject encodeObj = obj.getDictRef("Encode");
        if (encodeObj != null) {
            PDFObject[] encodeAry = encodeObj.getArray();
            float[] encode = new float[encodeAry.length];
            for (int i = 0; i < encodeAry.length; i++) {
                encode[i] = encodeAry[i].getFloatValue();
            }
            setEncode(encode);
        }
        
        // read the decode array (optional)
        PDFObject decodeObj = obj.getDictRef("Decode");
        if (decodeObj != null) {
            PDFObject[] decodeAry = decodeObj.getArray();
            float[] decode = new float[decodeAry.length];
            for (int i = 0; i < decodeAry.length; i++) {
                decode[i] = decodeAry[i].getFloatValue();
            }
            setDecode(decode);
        }
        
        // finally, read the samples
        setSamples(readSamples(obj.getStreamBuffer()));
    }
    
    /**
     * Map from <i>m</i> input values to <i>n</i> output values.
     * The number of inputs <i>m</i> must be exactly one half the size of the
     * domain.  The number of outputs should match one half the size of the
     * range.
     *
     * @param inputs an array of <i>m</i> input values
     * @param outputs an array of size <i>n</i> which will be filled
     *                with the output values, or null to return a new array
     */
    @Override
	protected void doFunction(float[] inputs, int inputOffset, 
                              float[] outputs, int outputOffset) 
    {      
        // calculate the encoded values for each input
        float[] encoded = new float[getNumInputs()];
        for (int i = 0; i < getNumInputs(); i++) {
            // encode -- interpolate(x<i>, domain<2i>, domain<2i + 1>,
            //                       encode<2i>, encode<2i + 1>)
            encoded[i] = interpolate(inputs[i + inputOffset],
                                     getDomain(2 * i),
                                     getDomain((2 * i) + 1),
                                     getEncode(2 * i),
                                     getEncode((2 * i) + 1));
        
            // clip to size of sample table -- min(max(e<i>, 0), size<i> - 1)
            encoded[i] = Math.max(encoded[i], 0);
            encoded[i] = Math.min(encoded[i], this.size[i] - 1);
        }
        
        // do some magic
	for (int i = 0; i < getNumOutputs(); i++) {
            if (getOrder() == 1) {
                outputs[i + outputOffset] = multilinearInterpolate(encoded, i);
            } else {
                outputs[i + outputOffset] = multicubicInterpolate(encoded, i);
            }
        }
        
        // now adjust the output to be within range
        for (int i = 0; i < outputs.length; i++) {
            // decode -- interpolate(r<i>, 0, 2^bps - 1, 
            //                       decode<2i>, decode<2i + 1>)
            outputs[i + outputOffset] = interpolate(outputs[i + outputOffset], 
                                     0,
                                     (float) Math.pow(2, getBitsPerSample()) - 1,
                                     getDecode(2 * i),
                                     getDecode((2 * i) + 1));
        }
    }
    
    /**
     * Get the size of a given input dimension
     *
     * @param dimension the input dimension to get the size of
     * @return the number of samples in the given dimension
     */
    protected int getSize(int dimension) {
        return this.size[dimension];
    }
    
    /** 
     * Set the size of all input dimensions
     */
    protected void setSize(int[] size) {
        this.size = size;
    }
    
    
    /** 
     * Get the number of bits per sample
     */
    protected int getBitsPerSample() {
        return this.bitsPerSample;
    }
    
    /**
     * Set the number of bits per sample
     */
    protected void setBitsPerSample(int bits) {
        this.bitsPerSample = bits;
    }
    
    /**
     * Get the interpolation type
     */
    protected int getOrder() {
        return this.order;
    }
    
    /**
     * Set the interpolation type
     */
    protected void setOrder(int order) {
        this.order = order;
    }
    
    /**
     * Get the encoding for a particular input parameter
     *
     * @param i the index into the encoding array, which has size 2 * <i>m</i>.
     *          the <i>i</i>th entry in the array has index 2<i>i</i>, 
     *           2<i>i</i> + 1
     * @return the encoding value if the encoding array is set, or the default 
     */
    protected float getEncode(int i) {
        if (this.encode != null) {
            return this.encode[i];
        } else if ((i % 2) == 0) {
            return 0f;
        } else {
            return (getSize(i / 2) - 1);
        }  
    }
    
    /**
     * Set the encode array
     */
    protected void setEncode(float[] encode) {
        this.encode = encode;
    }
    
    /**
     * Get the decoding for a particular input parameter
     *
     * @param i the index into the decoding array, which has size 2 * <i>n</i>.
     *          the <i>i</i>th entry in the array has index 2<i>i</i>, 
     *           2<i>i</i> + 1
     * @return the decoding value if the decoding array is set, or the default 
     */
    protected float getDecode(int i) {
        if (this.decode != null) {
            return this.decode[i];
        } else {
            return getRange(i);
        }  
    }
    
    /**
     * Set the decode array
     */
    protected void setDecode(float[] decode) {
        this.decode = decode;
    }
    
    /**
     * Get a component for a sample given <i>m</i> indices and output
     * dimension. 
     *
     * @param values an array of <i>m</i> values determining which sample
     *               to select
     * @param od the output dimension (0 - <i>n</i>) to get the sample in
     * @return the sample for the given values and index
     */
    protected int getSample(int[] values, int od) {
        int mult = 1;
        int index = 0;
        for (int i = 0; i < values.length; i++) {
            index += mult * values[i];
            mult *= getSize(i);
        }
        
        return this.samples[index][od];
    }
    
    /**
     * Set the table of samples
     */
    protected void setSamples(int[][] samples) {
        this.samples = samples;
    }
    
    /**
     * Read the samples from the input stream.  Each sample is made up
     * of <i>n</i> components, each of which has length <i>bitsPerSample</i>
     * bits.  The samples are arranged by dimension, then range
     */
    private int[][] readSamples(ByteBuffer buf) {
        // calculate the number of samples in the table
        int size = 1;
        for (int i = 0; i < getNumInputs(); i++) {
            size *= getSize(i);
        }
        
        // create the samples table
        int[][] samples = new int[size][getNumOutputs()];
        
        
        // the current location in the buffer, in bits from byteLoc
        int bitLoc = 0;
          
        // the current location in the buffer, in bytes
        int byteLoc = 0;
      
        // the current index in the samples array
        int index = 0;
        
        for (int i = 0; i < getNumInputs(); i++) {
            for (int j = 0; j < getSize(i); j++) {
                for (int k = 0; k < getNumOutputs(); k++) {
                    /** [JK FIXME one bit at a time is really inefficient */
                    int value = 0;
                    
                    int toRead = getBitsPerSample();
                    byte curByte = buf.get(byteLoc);
                    
                    while (toRead > 0) {
                        int nextBit = ((curByte >> (7 - bitLoc)) & 0x1);
                        value |= nextBit << (toRead - 1);
                        
                        if (++bitLoc == 8) {
                            bitLoc = 0;
                            byteLoc++;
                            
                            if (toRead > 1) {
                                curByte = buf.get(byteLoc);
                            }
                        }
                        
                        toRead--;
                    }
                    
                    samples[index][k] = value;
                }
                
                index++;
            }
        }
        
        return samples;
    }
    
    /**
     * Perform a piecewise multilinear interpolation.  The provides a
     * close approximation to the standard linear interpolation, at
     * a far lower cost, since every element is not evaluated at every
     * iteration.  Instead, a walk of the most significant axes is performed,
     * following the algorithm desribed at:
     *     http://osl.iu.edu/~tveldhui/papers/MAScThesis/node33.html
     *
     * @param encoded the encoded input values
     * @param od the output dimension
     */
    private float multilinearInterpolate(float[] encoded, int od) {
        // first calculate the distances -- the differences between
        // each encoded value and the integer below it.
        float[] dists = new float[encoded.length];
        
        for (int i = 0; i < dists.length; i++) {
            dists[i] = (float) (encoded[i] - Math.floor(encoded[i]));
        }
        
        // initialize the map of axes.  Each bit in this map represents
        // whether the control value in that dimension should be the integer
        // above or below encoded[i]
        int map = 0;
        
        // the initial values
        float val = getSample(encoded, map, od);
        float prev = val;
        
        // walk the axes
        for (int i = 0; i < dists.length; i++) {
            // find the largest value of dist remaining
            int idx = 0;
            float largest = -1;
            for (int c = 0; c < dists.length; c++) {
                if (dists[c] > largest) {
                    largest = dists[c];
                    idx = c;
                }
            }
            
            // now find the sample with that axis set to 1
            map |= (0x1 << idx);
            float cur = getSample(encoded, map, od);
            
            // calculate the value and remember it
            val += dists[idx] * (cur - prev);
            prev = val;
            
            // make sure we won't find this distance again
            dists[idx] = -1;
        }
        
        // voila
        return val;
    }
    
    /**
     * Perform a multicubic interpolation
     *
     * @param encoded the encoded input values
     * @param od the output dimension
     */
    private float multicubicInterpolate(float[] encoded, int od) {
        PDFDebugger.debug("Cubic interpolation not supported!");
        return multilinearInterpolate(encoded, od);
    }
    
    /**
     * Get a sample based on an array of encoded values and a control
     * map.  For each bit in the map, if that bit is 0 the integer below 
     * the encoded value is selected, or if the bit is 1, the interger
     * above is selected.
     * 
     * @param encoded the encoded values
     * @param map the bit map of control values
     * @param od the output dimension to read the sample for
     */
    private float getSample(float[] encoded, int map, int od) {
        int[] controls = new int[encoded.length];
        
        // fill in the controls array with appropriate ints
        for (int i = 0; i < controls.length; i++) {
            if ((map & (0x1 << i)) == 0) {
                controls[i] = (int) Math.floor(encoded[i]);
            } else {
                controls[i] = (int) Math.ceil(encoded[i]);
            }
        }
        
        // now return the actual sample
        return getSample(controls, od);
    }
}