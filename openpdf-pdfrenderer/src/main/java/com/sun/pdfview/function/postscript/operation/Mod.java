package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Mod implements PostScriptOperation {
	@Override
	/**
	 * <i>int1 int2</i> <b>mod</b> <i>remainder</i> <p>
	 *
	 * returns the remainder that results from
	 * dividing int1 by int2. The sign of the result
	 * is the same as the sign of the dividend int1.
	 * Both operands must be integers and the result
	 * is an integer. <p>
	 *
	 * errors: stackunderflow, typecheck, undefinedresult
	 */
	public void eval(Stack<Object> environment) {
	    long int2 = (Long)environment.pop();
	    long int1 = (Long)environment.pop();
	    environment.push(int1 % int2);
	}
}
