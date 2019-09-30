package com.lowagie.examples.directcontent.optionalcontent;

import com.lowagie.examples.AbstractRunner;

public class OptionalContentRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "optionalcontent");

        Automatic.main(argsNew);
        ContentGroups.main(argsNew);
        Layers.main(argsNew);
        NestedLayers.main(argsNew);
        OptionalContent.main(argsNew);
        OrderedLayers.main(argsNew);
    }
}
