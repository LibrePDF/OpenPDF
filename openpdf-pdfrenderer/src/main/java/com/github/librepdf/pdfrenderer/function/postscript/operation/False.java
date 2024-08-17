package com.github.librepdf.pdfrenderer.function.postscript.operation;

import java.util.Stack;


final class False implements PostScriptOperation {
	@Override
	/**
	 * <b>false</b> <i>false</i> <p>
	 *
	 * pushes a boolean object whose value is false on the
	 * operand stack. false is not an operator; it is a name in
	 * systemdict associated with the boolean value false. <p>
	 *
	 * errors: stackoverflow
	 */
	public void eval(Stack<Object> environment) {   
	    environment.push(false);
	}
}
