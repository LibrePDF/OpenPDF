package com.lowagie.examples.general;

import com.lowagie.examples.AbstractRunner;
import com.lowagie.examples.general.copystamp.CopyStampRunner;
import com.lowagie.examples.general.faq.FaqRunner;
import com.lowagie.examples.general.read.ReadRunner;

public class GeneralRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "general");

        CustomPageSize.main(argsNew);
        DefaultPageSize.main(argsNew);
        HelloEncrypted.main(argsNew);
//        HelloSystemOut.main(args);
        HelloWorld.main(argsNew);
        HelloWorldMeta.main(argsNew);
        LandscapePortrait.main(argsNew);
        Margins.main(argsNew);

        FaqRunner.main(argsNew);
        CopyStampRunner.main(argsNew);
        ReadRunner.main(argsNew);
    }
}
