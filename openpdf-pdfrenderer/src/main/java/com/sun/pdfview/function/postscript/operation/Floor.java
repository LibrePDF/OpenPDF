package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Floor implements PostScriptOperation {
	@Override
	/**
	 * <i>num1</i> <b>floor</b> <i>num2</i> <p>
	 *
	 * returns the greatest integer value less than or equal
	 * to num1. The type of the result is the same as the type
	 * of the operand. <p>
	 *
	 * errors: stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    environment.push(Math.floor((Double)environment.pop()));
	}
}
