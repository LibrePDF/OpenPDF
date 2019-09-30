package com.lowagie.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractRunner {
    protected static String[] getOutputDirectory(String[] args, String outputDirectory) {
        String[] argsNew = new String[1];
        argsNew[0] = args[0] + "/" + outputDirectory;

        Path outputPath = Paths.get(args[0]).resolve(outputDirectory);
        try {
            createDir(outputPath);
        } catch (IOException e) {
            argsNew[0] = "";
        }
        return argsNew;
    }

    private static void createDir(Path outputPath) throws IOException {
        if (!Files.exists(outputPath)) {
            if (!outputPath.getParent().toFile().exists()) {
                createDir(outputPath.getParent());
            }
            Files.createDirectory(outputPath);
        }
    }
}
