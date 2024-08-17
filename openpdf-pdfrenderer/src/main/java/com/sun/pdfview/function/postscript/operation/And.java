package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class And implements PostScriptOperation {
	@Override
	/**
	 * <i>bool1|int1 bool2|int2</i> <b>and</b> <i>bool3|int3</i> <p>
	 *
	 * returns the logical conjunction of the operands
	 * if they are boolean. If the operands are integers,
	 * and returns the bitwise "and" of their binary
	 * representations. <p>
	 *
	 * errors: stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    environment.push((Long)environment.pop() & (Long)environment.pop());
	}
}
