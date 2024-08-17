package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Xor implements PostScriptOperation {
	@Override
	/**
	 * <i>bool1|int1 bool2|int2</i> <b>xor</b> <i>bool3|int3</i> <p>
	 *
	 * returns the logical "exclusive or" of the operands if they
	 * are boolean. If the operands are integers, xor returns the
	 * bitwise "exclusive or" of their binary representations. <p>
	 *
	 * errors: stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    environment.push((Long)environment.pop() ^ (Long)environment.pop());
	}
}
