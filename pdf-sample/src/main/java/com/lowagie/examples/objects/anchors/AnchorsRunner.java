package com.lowagie.examples.objects.anchors;

import com.lowagie.examples.AbstractRunner;

public class AnchorsRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "anchors");

        Actions.main(argsNew);
        AHref.main(argsNew);
        Annotations.main(argsNew);
        ChainedActions.main(argsNew);
        JavaScriptAction.main(argsNew);
        LocalDestination.main(argsNew);
        LocalGoto.main(argsNew);
        NamedActions.main(argsNew);
        OpenApplication.main(argsNew);
        RemoteGoto.main(argsNew);
        SimpleAnnotations.main(argsNew);
    }
}
