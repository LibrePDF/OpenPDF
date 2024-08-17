package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class True implements PostScriptOperation {
	@Override
	/**
	 * <b>true</b> <i>true</i> <p>
	 *
	 * pushes a boolean object whose value is true on the operand
	 * stack. true is not an operator; it is a name in systemdict
	 * associated with the boolean value true. <p>
	 *
	 * errors: stackoverflow
	 */
	public void eval(Stack<Object> environment) {
	    environment.push(true);
	}
}
