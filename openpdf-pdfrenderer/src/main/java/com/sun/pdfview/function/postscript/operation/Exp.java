package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Exp implements PostScriptOperation {
	@Override
	/**
	 * <i>base exponent</i> <b>exp</b> <i>real</i> <p>
	 *
	 * raises base to the exponent power. The operands may be
	 * either integers or real numbers. If the exponent has a
	 * fractional part, the result is meaningful only if the
	 * base is nonnegative. The result is always a real number. <p>
	 *
	 * errors: stackunderflow, typecheck, undefinedresult
	 */
	public void eval(Stack<Object> environment) {
	    double exponent = (Double)environment.pop();
	    double base = (Double)environment.pop();
	    environment.push(Math.pow(exponent, base));
	}
	
}
