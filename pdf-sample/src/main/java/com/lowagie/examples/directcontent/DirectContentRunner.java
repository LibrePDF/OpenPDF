package com.lowagie.examples.directcontent;

import com.lowagie.examples.AbstractRunner;
import com.lowagie.examples.directcontent.colors.ColorsRunner;
import com.lowagie.examples.directcontent.coordinates.CoordinatesRunner;
import com.lowagie.examples.directcontent.graphics.GraphicsRunner;
import com.lowagie.examples.directcontent.graphics2D.Graphics2DRunner;
import com.lowagie.examples.directcontent.optionalcontent.OptionalContentRunner;
import com.lowagie.examples.directcontent.pageevents.PageEventsRunner;
import com.lowagie.examples.directcontent.text.DirectContentTextRunner;

public class DirectContentRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "directcontent");

        Layers.main(argsNew);
        TemplateImages.main(argsNew);
        Templates.main(argsNew);

        ColorsRunner.main(argsNew);
        CoordinatesRunner.main(argsNew);
        GraphicsRunner.main(argsNew);
        Graphics2DRunner.main(argsNew);
        OptionalContentRunner.main(argsNew);
        PageEventsRunner.main(argsNew);
        DirectContentTextRunner.main(argsNew);
    }
}
