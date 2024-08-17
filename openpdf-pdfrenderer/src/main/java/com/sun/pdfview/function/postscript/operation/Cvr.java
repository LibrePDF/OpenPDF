package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Cvr implements PostScriptOperation {
	@Override
	/**
	 * <i>num</i> <b>cvr</b> <i>real</i> <u>or</u> <i>string</i> <b>cvr</b> <i>real</i> <p>
	 *
	 * (convert to real) takes an integer, real, or string
	 * object and produces a real result. If the operand
	 * is an integer, cvr converts it to a real number.
	 * If the operand is a real number, cvr simply returns it.
	 * If the operand is a string, cvr invokes the equivalent
	 * of the token operator to interpret the characters of
	 * the string as a number according to the PostScript
	 * syntax rules. If that number is an integer, cvr converts
	 * it to a real number. <p>
	 *
	 * errors: invalidaccess, limitcheck, stackunderflow,
	 *         syntaxerror, typecheck, undefinedresult
	 */
	public void eval(Stack<Object> environment) {
	    // YOUR CODE IN THIS SPACE
	}
}
