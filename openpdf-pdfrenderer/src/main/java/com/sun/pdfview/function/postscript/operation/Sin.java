package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Sin implements PostScriptOperation {
	@Override
	/**
	 * <i>angle</i> <b>sin</b> <i>real</i> <p>
	 *
	 * returns the sine of angle, which is interpreted as an
	 * angle in degrees. The result is a real number. <p>
	 *
	 * errors: stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    double radians = Math.toRadians((Double)environment.pop());
	    environment.push(Math.toDegrees(Math.sin(radians)));
	}
}
