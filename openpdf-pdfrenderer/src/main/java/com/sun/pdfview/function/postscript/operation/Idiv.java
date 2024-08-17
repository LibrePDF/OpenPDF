package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Idiv implements PostScriptOperation {
	@Override
	/**
	 * <i>int1 int2</i> <b>idiv</b> <i>quotient</i> <p>
	 *
	 * divides int1 by int2 and returns the integer part
	 * of the quotient, with any fractional part discarded.
	 * Both operands of idiv must be integers and the result
	 * is an integer. <p>
	 *
	 * stackunderflow, typecheck, undefinedresult
	 */
	public void eval(Stack<Object> environment) {
	    long int2 = (Long)environment.pop();
	    long int1 = (Long)environment.pop();
	    environment.push(int1 / int2);
	}
}
