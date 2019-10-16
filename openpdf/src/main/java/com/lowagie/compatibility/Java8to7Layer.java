package com.lowagie.compatibility;

import com.lowagie.text.DocListener;
import com.lowagie.text.Element;
import java.util.Stack;

public class Java8to7Layer {

    public static boolean isFinite(float value) {
        return !(Float.isNaN(value) || Float.isInfinite(value));
    }

    public static void addStackContentToDocument(Stack<Object> stack, DocListener document) {
        for (Object o : stack) {
            document.add((Element) o);
        }
    }
}
