package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Ln implements PostScriptOperation {
	@Override
	/**
	 * <i>num</i> <b>ln</b> <i>real</i> <p>
	 *
	 * returns the natural logarithm (base e) of num.
	 * The result is a real number. <p>
	 *
	 * errors: rangecheck, stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    environment.push(Math.log((Double)environment.pop()));
	}
}
