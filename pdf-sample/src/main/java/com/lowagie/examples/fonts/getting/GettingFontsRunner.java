package com.lowagie.examples.fonts.getting;

import com.lowagie.examples.AbstractRunner;

public class GettingFontsRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "getting");

        ChineseJapaneseKorean.main(argsNew);
        FontFactoryStyles.main(argsNew);
        OpenTypeFont.main(argsNew);
        RegisterFont.main(argsNew);
        TrueType.main(argsNew);
        TrueTypeCollections.main(argsNew);
        UsingFontFactory.main(argsNew);
    }
}
