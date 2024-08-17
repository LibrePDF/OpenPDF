package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class If implements PostScriptOperation {
	@Override
	/**
	 * <i>bool {proc}</i> <b>if</b> - <p>
	 *
	 * removes both operands from the stack, then executes proc
	 * if bool is true. The if operator pushes no results of
	 * its own on the operand stack, but proc may do so (see
	 * Section 3.5, "Execution"). <p>
	 *
	 * Examples <p>
	 * 3 4 lt {(3 is less than 4)} if <p>
	 *
	 * errors: stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    if ((Boolean)environment.pop()) {
	        environment.push(environment.pop());
	    } else {
	    	environment.pop();
	    }
	}
}
