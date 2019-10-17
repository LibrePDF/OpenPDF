package com.lowagie.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class AbstractSample {
    public boolean isPdfProducer() { return true; }
    public int getExpectedPageCount() {
        return 1;
    };

    public abstract String getFileName();
    public abstract void render(String path);

    public void run(String[] args) {
        render(args[0]);
        boolean renderImages = false;
        boolean removePdf = false;
        if (args.length > 1) {
            renderImages = Boolean.parseBoolean(args[1]);
            removePdf = Boolean.parseBoolean(args[2]);
        }

        if (!isPdfProducer()){
            renderImages = false;
        }

        if(renderImages) {
            try {
                for (int i = 0; i < getExpectedPageCount(); i++) {
                    final com.hotwire.imageassert.Image image = com.hotwire.imageassert.Image.toPng(new FileInputStream(args[0] + getFileName() + ".pdf"), i, 150, 90, "white");
                    image.save(new File(args[0] + getFileName() + "-" + i + ".png"));
                }
            } catch (IOException | RuntimeException de) {
                System.err.println(de.getMessage());
            }
        }

        if (removePdf) {
            try {
                Files.delete(Paths.get(args[0] + getFileName() + ".pdf"));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
