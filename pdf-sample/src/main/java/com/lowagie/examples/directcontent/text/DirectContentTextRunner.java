package com.lowagie.examples.directcontent.text;

import com.lowagie.examples.AbstractRunner;

public class DirectContentTextRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "text");

        Logo.main(argsNew);
        Text.main(argsNew);
    }
}
