package com.lowagie.examples.forms.create;

import com.lowagie.examples.AbstractRunner;

public class CreateFormsRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "create");

        StudentCard.main(argsNew);
        StudentCardForm.main(argsNew);
    }
}
