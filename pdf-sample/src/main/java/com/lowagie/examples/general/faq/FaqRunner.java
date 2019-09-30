package com.lowagie.examples.general.faq;

import com.lowagie.examples.AbstractRunner;

public class FaqRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "faq");

        iTextVersion.main(argsNew);
        Measurements.main(argsNew);
        NewPage.main(argsNew);
        PdfVersion.main(argsNew);
    }
}
