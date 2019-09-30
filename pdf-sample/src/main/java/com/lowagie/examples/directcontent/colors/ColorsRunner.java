package com.lowagie.examples.directcontent.colors;

import com.lowagie.examples.AbstractRunner;

public class ColorsRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "colors");

        Groups.main(argsNew);
        Pattern.main(argsNew);
        Patterns.main(argsNew);
        Shading.main(argsNew);
        ShadingPattern.main(argsNew);
        SoftMask.main(argsNew);
        SpotColors.main(argsNew);
        Transparency.main(argsNew);
    }
}
