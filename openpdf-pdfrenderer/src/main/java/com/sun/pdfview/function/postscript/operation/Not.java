package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Not implements PostScriptOperation {
	@Override
	/**
	 * <i>bool1|int1</i> <b>not</b> <i>bool2|int2</i> <p>
	 *
	 * returns the logical negation of the operand if it is
	 * boolean. If the operand is an integer, not returns the
	 * bitwise complement (ones complement) of its binary
	 * representation. <p>
	 *
	 * errors: stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    environment.push(~(Long)environment.pop());
	}
}
