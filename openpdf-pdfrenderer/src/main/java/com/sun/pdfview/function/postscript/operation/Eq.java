package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Eq implements PostScriptOperation {
	@Override
	/**
	 * <i>any1 <i>any2</i> <b>eq</b> <i>bool</i> <p>
	 *
	 * pops two objects from the operand stack and pushes\
	 * true if they are equal, or false if not.
	 * The definition of equality depends on the types of
	 * the objects being compared.
	 * Simple objects are equal if their types and values
	 * are the same. Strings are equal if their lengths and
	 * individual elements are equal.
	 *  Other composite objects
	 * (arrays and dictionaries) are equal only if they share
	 * the same value. Separate values are considered unequal,
	 * even if all the components of those values are the
	 * same.
	 * This operator performs some type conversions.
	 * Integers and real numbers can be compared freely:
	 * an integer and a real number representing the same
	 * mathematical value are considered equal by eq.
	 * Strings and names can likewise be compared freely:
	 * a name defined by some sequence of characters is equal
	 * to a string whose elements are the same sequence of
	 * characters.
	 * The literal/executable and access attributes of
	 * objects are not considered in comparisons
	 * between objects. <p>
	 *
	 * errors: invalidaccess, stackunderflow
	 */
	public void eval(Stack<Object> environment) {
	    environment.push(environment.pop().equals(environment.pop()));
	}
}
