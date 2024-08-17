package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Div implements PostScriptOperation {
	@Override
	/**
	 * <i>num1 num2</i> <b>div</b> <i>quotient</i> <p>
	 *
	 * divides num1 by num2, producing a result that is
	 * always a real number even if both operands are integers.
	 * Use idiv instead if the operands are integers and an
	 * integer result is desired. <p>
	 *
	 * errors: stackunderflow, typecheck, undefinedresult
	 */
	public void eval(Stack<Object> environment) {
	    double num2 = (Double)environment.pop();
	    double num1 = (Double)environment.pop();
	    environment.push(num1 / num2);
	}
}
