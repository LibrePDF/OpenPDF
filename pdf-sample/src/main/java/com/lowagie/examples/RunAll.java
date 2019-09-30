package com.lowagie.examples;

import com.lowagie.examples.directcontent.DirectContentRunner;
import com.lowagie.examples.fonts.FontsRunner;
import com.lowagie.examples.forms.FormsRunner;
import com.lowagie.examples.general.GeneralRunner;
import com.lowagie.examples.objects.ObjectsRunner;

public class RunAll extends AbstractRunner {

    public static void main(String[] args) {

        String[] argsNew = getOutputDirectory(args, "examples");

        // directcontent
        DirectContentRunner.main(argsNew);

        // fonts
        FontsRunner.main(argsNew);

        // forms
        FormsRunner.main(argsNew);

        // objects
        ObjectsRunner.main(argsNew);

        // general
        GeneralRunner.main(argsNew);
    }
}
