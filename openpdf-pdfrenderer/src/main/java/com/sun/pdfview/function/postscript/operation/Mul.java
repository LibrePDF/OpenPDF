package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Mul implements PostScriptOperation {
	@Override
	/**
	 * <i>num1 num2</i> <b>mul</b> <i>product</i> <p>
	 *
	 * returns the product of num1 and num2.
	 * If both operands are integers and the result
	 * is within integer range, the result is an integer;
	 * otherwise, the result is a real number. <p>
	 *
	 * errors: stackunderflow, typecheck, undefinedresult
	 */
	public void eval(Stack<Object> environment) {
	    environment.push((Double)environment.pop() * (Double)environment.pop());
	}
}
