package com.lowagie.examples.general.copystamp;

import com.lowagie.examples.AbstractRunner;

public class CopyStampRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "copystamp");

        AddWatermarkPageNumbers.main(argsNew);
        Concatenate.main(argsNew);
        ConcatenateForms.main(argsNew);
        EncryptorExample.main(argsNew);
        Register.main(argsNew);
        TwoOnOne.main(argsNew);
    }
}
