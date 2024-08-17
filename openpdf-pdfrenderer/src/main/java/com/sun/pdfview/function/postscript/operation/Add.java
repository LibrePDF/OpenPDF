package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Add implements PostScriptOperation {
	@Override
	/**
	 * <i>num1 num2</i> <b>add</b> <i>sum</i> <p>
	 *
	 * If both operands are integers and the result is
	 * within integer range, the result is an integer;
	 * otherwise, the result is a real number.<p>
	 *
	 * errors: stackunderflow, typecheck, undefinedresult
	 */
	public void eval(Stack<Object> environment) {
	    environment.push((Double)environment.pop() + (Double)environment.pop());
	}
}
