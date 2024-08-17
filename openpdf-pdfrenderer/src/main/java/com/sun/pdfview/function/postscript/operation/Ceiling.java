package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Ceiling implements PostScriptOperation {
	@Override
	/**
	 * <i>num1</i> <b>ceiling</b> <i>num2</i> <p>
	 *
	 * returns the least integer value greater than or equal
	 * to num1. The type of the result is the same as the type
	 * of the operand. <p>
	 *
	 * errors: stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    environment.push(Math.ceil((Double)environment.pop()));
	}
}
