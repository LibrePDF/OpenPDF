package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Sub implements PostScriptOperation {
	@Override
	/**
	 * <i>num1 num2</i> <b>sub</b> <i>difference</i> <p>
	 *
	 * returns the result of subtracting num2 from num1.
	 * If both operands are integers and the result is within
	 * integer range, the result is an integer; otherwise,
	 * the result is a real number. <p>
	 *
	 * errors: stackunderflow, typecheck, undefinedresult
	 */
	public void eval(Stack<Object> environment) {
	    double num2 = (Double)environment.pop();
	    double num1 = (Double)environment.pop();
	    environment.push(num1 - num2);
	}
}
