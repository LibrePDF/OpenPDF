package com.lowagie.examples.directcontent.graphics2D;

import com.lowagie.examples.AbstractRunner;

public class Graphics2DRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "graphics2d");

        ArabicText.main(argsNew);
        G2D.main(argsNew);
        JFreeChartExampleXY.main(argsNew);
        JFreeChartExamplePie.main(argsNew);
        JFreeChartExampleBar.main(argsNew);
    }
}
