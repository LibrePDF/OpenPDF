package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Ge implements PostScriptOperation {
	@Override
	/**
	 * <i>num1 num2</i> <b>ge</b> <i>bool</i> <p>
	 *
	 * pops two objects from the operand stack and pushes true
	 * if the first operand is greater than or equal to the second,
	 * or false otherwise. If both operands are numbers,
	 * ge compares their mathematical values. If both operands
	 * are strings, ge compares them element by element, treating
	 * the elements as integers in the range 0 to 255, to determine
	 * whether the first string is lexically greater than or equal
	 * to the second. If the operands are of other types or one
	 * is a string and the other is a number, a typecheck
	 * error occurs. <p>
	 *
	 * errors: invalidaccess, stackunderflow, typecheck
	 */
	public void eval(Stack<Object> environment) {
	    double num2 = (Double)environment.pop();
	    double num1 = (Double)environment.pop();
	    environment.push(num1 >= num2);
	}
}

