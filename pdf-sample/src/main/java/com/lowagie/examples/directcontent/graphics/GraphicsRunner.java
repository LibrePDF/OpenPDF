package com.lowagie.examples.directcontent.graphics;

import com.lowagie.examples.AbstractRunner;

public class GraphicsRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "graphics");

        Circles.main(argsNew);
        GState.main(argsNew);
        Literal.main(argsNew);
        Shapes.main(argsNew);
        State.main(argsNew);
    }
}
