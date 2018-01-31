package com.lowagie.toolbox.plugins;

public class WatermarkerTest {

    /**
     * Adding text at absolute positions.
     *
     * @param args no arguments needed
     *
     * Before passing that test, you have to create a MyFile.pdf at the project root.
     * The generated files will be written there too.
     *
     */
    public static void main(String[] args) {
        Watermarker.main(new String[]{"MyFile.pdf", "Specimen", "120", "0.5", "MyFile-watermark.pdf"});
        Watermarker.main(new String[]{"MyFile.pdf", "Specimen", "120", "0.5", "MyFile-watermark-red.pdf", "#FF0000"});
    }

}