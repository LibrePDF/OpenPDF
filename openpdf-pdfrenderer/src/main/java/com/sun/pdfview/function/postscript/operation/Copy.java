package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;
	final class Copy implements PostScriptOperation {
		@Override
		/**
		 * <i>any1 ... anyn n</i> <b>copy</b> <i>any1 ... anyn any1 ... anyn</i>
		 * <i>array1 array2</i> <b>copy</b> <i>subarray2</i> <br>
		 * <i>string1 string2</i> <b>copy</b> <i>substring2</i> <p>
		 *
		 * performs two entirely different functions, depending on the
		 * type of the topmost operand.
		 * In the first form, where the top element on the operand
		 * stack is a nonnegative integer n, copy pops n from the
		 * stack and duplicates the top n elements on the stack
		 * as shown above. This form of copy operates only on the
		 * objects themselves, not on the values of composite objects. <p>
		 *
		 * Examples<br>
		 * (a) (b) (c) 2 copy Þ (a) (b) (c) (b) (c) <br>
		 * (a) (b) (c) 0 copy Þ (a) (b) (c) <p>
		 *
		 * In the other forms, copy copies all the elements of the 
		 * first composite object into the second. The composite
		 * object operands must be of the same type, except that
		 * a packed array can be copied into an array (and only into
		 * an array—copy cannot copy into packed arrays, because
		 * they are read-only). This form of copy copies the value of
		 * a composite object. This is quite different from dup and
		 * other operators that copy only the objects themselves
		 * (see Section 3.3.1, "Simple and Composite Objects").
		 * However, copy performs only one level of copying.
		 * It does not apply recursively to elements that are
		 * themselves composite objects; instead, the values
		 * of those elements become shared. In the case of arrays or
		 * strings, the length of the second object must be at least as
		 * great as the first; copy returns the initial subarray or
		 * substring of the second operand into which the elements
		 * were copied. Any remaining elements of array2 or
		 * string2 are unaffected. <p>
		 * 
		 * Example: <br>
		 * /a1 [1 2 3] def<br>
		 * a1 dup length array copy Þ [1 2 3] <p>
		 *
		 * errors: invalidaccess, rangecheck, stackoverflow,
		 * stackunderflow, typecheck
		 */
		public void eval(Stack<Object> environment) {
		    Number count = (Number) environment.pop();
// ????
		    Object[] buffer = new Object[count.intValue()];
		    for (int i = 0; i < buffer.length; i++) {
		    	buffer[i] = environment.pop();
			}
		    for (int i = 0; i < buffer.length; i++) {
		    	environment.push(buffer[buffer.length-i-1]);
			}
		    for (int i = 0; i < buffer.length; i++) {
		    	environment.push(buffer[buffer.length-i-1]);
			}
		}
	}
	