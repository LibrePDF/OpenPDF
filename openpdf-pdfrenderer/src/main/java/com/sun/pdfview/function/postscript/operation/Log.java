package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Log implements PostScriptOperation {
	@Override
	/**
	 * <i>num</i> <b>log</b> <i>real</i> <p>
	 *
	 * returns the common logarithm (base 10) of num.
	 * The result is a real number. <p>
	 *
	 * errors:  rangecheck, stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    environment.push(Math.log10((Double)environment.pop()));
	}
}
