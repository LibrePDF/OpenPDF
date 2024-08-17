package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Ne implements PostScriptOperation {
	@Override
	/**
	 * <i>any1 any2</i> <b>ne</b> <i>bool</i> <p>
	 *
	 * pops two objects from the operand stack and pushes false
	 * if they are equal, or true if not. What it means for objects
	 * to be equal is presented in the description of the
	 * eq operator. <p>
	 *
	 * errors: invalidaccess, stackunderflow
	 */
	public void eval(Stack<Object> environment) {
	    environment.push(!environment.pop().equals(environment.pop()));
	}
}
