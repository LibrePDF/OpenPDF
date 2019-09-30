package com.lowagie.examples.general.read;

import com.lowagie.examples.AbstractRunner;

public class ReadRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "read");

        Info.main(argsNew);
        ReadEncrypted.main(argsNew);
    }
}
