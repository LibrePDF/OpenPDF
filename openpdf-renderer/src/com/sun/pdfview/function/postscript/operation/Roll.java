package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

final class Roll implements PostScriptOperation {
	
	public static int popAsInteger(Stack<Object> st) {
		Object e = st.pop();
		if (e instanceof Double) {
			double doubleVal = (Double) e;
			return (int) doubleVal;
		} else {
			// error
			return 0;
		}
	}

	@Override
	public void eval(Stack<Object> environment) {
	    // <i>anyn-1 ... any0 n j</i> <b>roll</b> <i>any(j-1)mod n ... anyn-1 ... any</i>
	    // Roll n elements up j times
		int j = popAsInteger(environment);
		int n = popAsInteger(environment);
		Object[] temp = new Object[n];
		
		if (environment.size() < n) {
			// error, cause by non-standard PS cmd, do nothing for compatibility
			return;
		}
		
		if (j >= 0) {
			j %= n;
		} else {
			j = -j % n;
			if (j != 0)
				j = n - j;
		}
		for (int i = 0; i < n; ++i) {
			temp[i] = environment.pop();
		}

		for (int i = j - 1; i > -1; --i) {
			environment.push(temp[i]);
		}
		for (int i = n - 1; i > j - 1; --i) {
			environment.push(temp[i]);
		}
	}

}
