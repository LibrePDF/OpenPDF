package com.lowagie.examples.fonts.styles;

import com.lowagie.examples.AbstractRunner;

public class FontStylesRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "styles");

        ComplexText.main(argsNew);
        FixedFontWidth.main(argsNew);
        FontColor.main(argsNew);
        FontStylePropagation.main(argsNew);
        RightToLeft.main(argsNew);
        Vertical.main(argsNew);
        WidthHeight.main(argsNew);
    }
}
