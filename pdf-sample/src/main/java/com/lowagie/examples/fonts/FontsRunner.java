package com.lowagie.examples.fonts;

import com.lowagie.examples.AbstractRunner;
import com.lowagie.examples.fonts.getting.GettingFontsRunner;
import com.lowagie.examples.fonts.styles.FontStylesRunner;

public class FontsRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "fonts");

        EncodingFont.main(argsNew);
        FontEncoding.main(argsNew);
        FontFactoryType1Fonts.main(argsNew);
        FullFontNames.main(argsNew);
        ListEncodings.main(argsNew);
        StandardType1Fonts.main(argsNew);
        UnicodeExample.main(argsNew);
        TrueType.main(argsNew);

        GettingFontsRunner.main(argsNew);
        FontStylesRunner.main(argsNew);
    }
}
