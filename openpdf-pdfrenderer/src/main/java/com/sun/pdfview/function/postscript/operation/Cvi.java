package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;
final class Cvi implements PostScriptOperation {
	@Override
	/**
	 * <i>num</i> <b>cvi</b> <i>int</i> <u>or</u> <i>string</i> <b>cvi</b> <i>int</i> <p>
	 *
	 * takes an integer, real, or string and produces an
	 * integer result. If the operand is an integer, cvi
	 * simply returns it. If the operand is a real number,
	 * it truncates any fractional part (that is, rounds
	 * it toward 0) and converts it to an integer.
	 * If the operand is a string, cvi invokes the equivalent
	 * of the token operator to interpret the characters
	 * of the string as a number according to the PostScript
	 * syntax rules. If that number is a real number, cvi converts
	 * it to an integer.
	 * A rangecheck error occurs if a real number is too
	 * large to convert to an integer. <p>
	 *
	 * errors: invalidaccess, rangecheck, stackunderflow,
	 *         syntaxError, typecheck,
	 */
	public void eval(Stack<Object> environment) {
	    environment.push(environment.pop());
	}
}
