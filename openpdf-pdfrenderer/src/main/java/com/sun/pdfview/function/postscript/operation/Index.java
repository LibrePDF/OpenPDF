package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Index implements PostScriptOperation {
	@Override
	public void eval(Stack<Object> environment) {   // <i>anyn ... any0 n</i> <b>index</b> <i>anyn ... any0 anyn</i>
		long n = Math.round((Double)environment.pop());
		environment.push(environment.get((int)(environment.size() - n - 1)));
	}
}
