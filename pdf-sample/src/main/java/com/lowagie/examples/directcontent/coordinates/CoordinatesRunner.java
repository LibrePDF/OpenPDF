package com.lowagie.examples.directcontent.coordinates;

import com.lowagie.examples.AbstractRunner;

public class CoordinatesRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "coordinates");

        AffineTransformation.main(argsNew);
        Transformations.main(argsNew);
        TransformImage.main(argsNew);
        UpsideDown.main(argsNew);
        XandYCoordinates.main(argsNew);
    }
}
