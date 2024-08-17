package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;


final class Pop implements PostScriptOperation {
	@Override
	public void eval(Stack<Object> environment) {   // discard top element
	    environment.pop();
	}
}

