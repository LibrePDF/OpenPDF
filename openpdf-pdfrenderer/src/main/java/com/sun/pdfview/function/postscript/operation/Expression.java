package com.sun.pdfview.function.postscript.operation;

import java.util.LinkedList;



public class Expression extends LinkedList<Object> {

    @Override
	public boolean equals(Object obj) {
        if (obj instanceof Expression) {
            // actually validate the list contents are the same expressions
            return true;
        }
        return false;
    }
}
