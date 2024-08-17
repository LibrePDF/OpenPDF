package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Exch implements PostScriptOperation {
	@Override
	public void eval(Stack<Object> environment) {   // <i>any1 any2</i> <b>exch</b> <i>any2 any1</i> - exchange top of stack
	    Object any1 = environment.pop();
	    Object any2 = environment.pop();
	    environment.push(any1);
	    environment.push(any2);
	}
}
