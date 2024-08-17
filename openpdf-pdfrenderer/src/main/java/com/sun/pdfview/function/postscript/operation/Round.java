package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Round implements PostScriptOperation {
	@Override
	/**
	 * <i>num1</i> <b>round</b> <i>num2</i> <p>
	 *
	 * returns the integer value nearest to num1.
	 * If num1 is equally close to its two nearest
	 * integers, round returns the greater of the two.
	 * The type of the result is the same as
	 * the type of the operand. <p>
	 *
	 * errors: stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    environment.push(Math.round((Double)environment.pop()));
	}
}
