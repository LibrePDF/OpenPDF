package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Bitshift implements PostScriptOperation {
	@Override
	/**
	 * <i>int1 <i>shift</i> <b>bitshift</b> <i>int2</i> <p>
	 *
	 * shifts the binary representation of int1 left by
	 * shift bits and returns the result. Bits shifted out
	 * are lost; bits shifted in are 0. If shift is negative,
	 * a right shift by –shift bits is performed.
	 * This PostScriptOperation produces an arithmetically correct
	 * result only for positive values of int1.
	 * Both int1 and shift must be integers. <p>
	 *
	 * errors: stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    long shift = (Long)environment.pop();
	    long int1 = (Long)environment.pop();
	    environment.push(int1 << shift);
	}
}