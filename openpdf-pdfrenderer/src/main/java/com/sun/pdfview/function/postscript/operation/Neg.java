package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Neg implements PostScriptOperation {
	@Override
	/**
	 * <i>num1</i> <b>neg</b> <i>num2</i> <p>
	 *
	 * returns the negative of num1. The type of the result
	 * is the same as the type of num1 unless num1 is the
	 * smallest (most negative) integer, in which case the
	 * result is a real number. <p>
	 *
	 * errors: stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    environment.push(-(Double)environment.pop());
	}
}
