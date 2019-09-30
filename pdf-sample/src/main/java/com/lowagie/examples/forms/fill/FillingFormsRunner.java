package com.lowagie.examples.forms.fill;

import com.lowagie.examples.AbstractRunner;

public class FillingFormsRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "filling");

        Register.main(argsNew);
        FdfExample.main(argsNew);
        XfdfExample.main(argsNew);
    }
}
