package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Atan implements PostScriptOperation {
	@Override
	/**
	 * <i>num den</i> <b>atan</b> <i>angle</i> <p>
	 *
	 * returns the angle (in degress between
	 * 0 and 360) whose tangent is num divided by den.
	 * Either num or den may be 0, but not both. The signs
	 * of num and den determine the quadrant in which the
	 * result will lie: positive num yeilds a result in the
	 * positive y plane, while a positive den yeilds a result in
	 * the positive x plane. The result is a real number.<p>
	 *
	 * errors: stackunderflow, typecheck, undefinedresult
	 */
	public void eval(Stack<Object> environment) {
	    double den = (Double)environment.pop();
	    double num = (Double)environment.pop();
	    if (den == 0.0) {
	        environment.push(90.0);
	    } else {
	        environment.push(Math.toDegrees(Math.atan(num / den)));
	    }
	}
}
