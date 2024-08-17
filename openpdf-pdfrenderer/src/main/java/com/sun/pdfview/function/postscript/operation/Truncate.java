package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Truncate implements PostScriptOperation {
	@Override
	/**
	 * <i>num1</i> <b>truncate</b> <i>num2</i> <p>
	 *
	 * truncates num1 toward 0 by removing its fractional part.
	 * The type of the result is the same as the type of the
	 * operand. <p>
	 *
	 * errors: stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    double num1 = (Double)environment.pop();
	    environment.push((((long) num1) - num1));
	}
}
