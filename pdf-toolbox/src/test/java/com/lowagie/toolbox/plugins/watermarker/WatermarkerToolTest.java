package com.lowagie.toolbox.plugins.watermarker;

public class WatermarkerToolTest {

    /**
     * Adding text at absolute positions.
     *
     * @param args no arguments needed
     *             <p>
     *             Before passing that test, you have to create a MyFile.pdf at the project root. The generated files
     *             will be written there too.
     */
    public static void main(String[] args) {
        WatermarkerTool.main(new String[]{"MyFile.pdf", "Specimen", "120", "0.5", "MyFile-watermark.pdf"});
        WatermarkerTool.main(
                new String[]{"MyFile.pdf", "Specimen", "120", "0.5", "MyFile-watermark-red.pdf", "#FF0000"});
    }
}