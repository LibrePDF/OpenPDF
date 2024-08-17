package com.sun.pdfview.function.postscript.operation;

import java.util.Stack;

public interface PostScriptOperation {

    /**
     * evaluate the function, popping the stack as needed and pushing results.
     */
    public void eval(Stack<Object> environment);

}

