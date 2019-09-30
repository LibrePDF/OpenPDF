package com.lowagie.examples.objects.images;

import com.lowagie.examples.AbstractRunner;

public class ImagesRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "images");

        AbsolutePositions.main(argsNew);
        Alignment.main(argsNew);
        AnnotatedImage.main(argsNew);
        DvdCover.main(argsNew);
        ImageChunks.main(argsNew);
        Images.main(argsNew);
        ImagesAlignment.main(argsNew);
        ImageSequence.main(argsNew);
        RawData.main(argsNew);
        Rotating.main(argsNew);
        Scaling.main(argsNew);
    }
}
