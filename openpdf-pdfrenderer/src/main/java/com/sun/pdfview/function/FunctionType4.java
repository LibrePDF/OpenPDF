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
import java.util.*;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.function.postscript.PostScriptParser;
import com.sun.pdfview.function.postscript.operation.OperationSet;
import com.sun.pdfview.function.postscript.operation.PostScriptOperation;

/**
 * <p>A PostScript function is represented as a stream containing code
 * written in a small subset of the PostScript language. 
 * This reference is taken from the (3200-1:2008:7.10.5)<p>
 *
 * http://www.adobe.com/devnet/acrobat/pdfs/adobe_supplement_iso32000.pdf
 * </p>
 */
public class FunctionType4 extends PDFFunction {

    /** the list of tokens and sub-expressions. */
    private List<String> tokens;
    
    /** the stack of operations. The stack contents should all be Comparable. */
    private Stack<Object> stack;

    /** Creates a new instance of FunctionType4 */
    protected FunctionType4() {
        super(TYPE_4);
    }

    /** Read the function information from a PDF Object */
    @Override
	protected void parse(PDFObject obj) throws IOException {
    	ByteBuffer buf = obj.getStreamBuffer();
    	
    	byte[] byteA = new byte[buf.remaining()];
    	buf.get(byteA);
    	String scriptContent = new String(byteA, "UTF-8");
    	this.tokens = new PostScriptParser().parse(scriptContent);
    }

    /**
     * Map from <i>m</i> input values to <i>n</i> output values.
     * The number of inputs <i>m</i> must be exactly one half the size of the
     * domain.  The number of outputs should match one half the size of the
     * range.
     *
     * @param inputs an array of <i>m</i> input values
     * @param inputOffset the offset into the input array to read from
     * @param outputs an array of size >= <i>n</i> which will be filled
     *                with the output values
     * @param outputOffset the offset into the output array to write to
     */
    @Override
	protected void doFunction(float[] inputs, int inputOffset, float[] outputs, int outputOffset) {
    	prepareInitialStack(inputs, inputOffset);
    	for (Iterator<String> iterator = this.tokens.iterator(); iterator.hasNext(); ) {
			String token = iterator.next();
			PostScriptOperation op = OperationSet.getInstance().getOperation(token);
			op.eval(this.stack);
		}
    	assertResultIsCorrect(outputs, outputOffset);
    	prepareResult(outputs, outputOffset);
    }

	/*************************************************************************
	 * @param outputs
	 * @param outputOffset
	 ************************************************************************/
	private void prepareResult(float[] outputs, int outputOffset) {
		for (int i = outputOffset; i < outputs.length; i++) {
    		outputs[outputs.length-i-1] = ((Double)this.stack.pop()).floatValue();
		}
	}

	/*************************************************************************
	 * Put all input values on the initial stack.
	 * All values are pushed as Double because we calculate internally with double.
	 * @param inputs
	 * @param inputOffset
	 ************************************************************************/
	
	private void prepareInitialStack(float[] inputs, int inputOffset) {
		this.stack = new Stack<Object>();
    	for (int i = inputOffset; i < inputs.length; i++) {
    		this.stack.push(new Double(inputs[i]));
		}
	}

	/*************************************************************************
	 * @param outputs
	 * @param outputOffset
	 ************************************************************************/
	
	private void assertResultIsCorrect(float[] outputs, int outputOffset) {
		int expectedResults = outputs.length-outputOffset;
		if (this.stack.size() != expectedResults) {
        	throw new IllegalStateException("Output does not match result "+expectedResults+"/"+this.stack);
    	}
	}
}