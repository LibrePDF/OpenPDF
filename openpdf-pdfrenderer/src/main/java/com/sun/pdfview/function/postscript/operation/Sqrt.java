package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Sqrt implements PostScriptOperation {
	@Override
	/**
	 * <i>num</i> <b>sqrt</b> <i>real</i> <p>
	 *
	 * returns the square root of num, which must be a
	 * nonnegative number. The result is a real number. <p>
	 *
	 * errors: rangecheck, stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    environment.push(Math.sqrt((Double)environment.pop()));
	}
}
