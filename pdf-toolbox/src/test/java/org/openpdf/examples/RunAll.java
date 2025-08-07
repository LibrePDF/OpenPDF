package org.openpdf.examples;

import org.openpdf.examples.directcontent.Layers;
import org.openpdf.examples.directcontent.TemplateImages;
import org.openpdf.examples.directcontent.Templates;
import org.openpdf.examples.directcontent.colors.Groups;
import org.openpdf.examples.directcontent.colors.Pattern;
import org.openpdf.examples.directcontent.colors.Patterns;
import org.openpdf.examples.directcontent.colors.Shading;
import org.openpdf.examples.directcontent.colors.ShadingPattern;
import org.openpdf.examples.directcontent.colors.SoftMask;
import org.openpdf.examples.directcontent.colors.SpotColors;
import org.openpdf.examples.directcontent.colors.Transparency;
import org.openpdf.examples.directcontent.coordinates.AffineTransformation;
import org.openpdf.examples.directcontent.coordinates.TransformImage;
import org.openpdf.examples.directcontent.coordinates.Transformations;
import org.openpdf.examples.directcontent.coordinates.UpsideDown;
import org.openpdf.examples.directcontent.coordinates.XandYcoordinates;
import org.openpdf.examples.directcontent.graphics.Circles;
import org.openpdf.examples.directcontent.graphics.GState;
import org.openpdf.examples.directcontent.graphics.Literal;
import org.openpdf.examples.directcontent.graphics.Shapes;
import org.openpdf.examples.directcontent.graphics.State;
import org.openpdf.examples.directcontent.graphics2d.ArabicText;
import org.openpdf.examples.directcontent.graphics2d.G2D;
import org.openpdf.examples.directcontent.graphics2d.JFreeChartExample;
import org.openpdf.examples.directcontent.optionalcontent.Automatic;
import org.openpdf.examples.directcontent.optionalcontent.ContentGroups;
import org.openpdf.examples.directcontent.optionalcontent.NestedLayers;
import org.openpdf.examples.directcontent.optionalcontent.OptionalContent;
import org.openpdf.examples.directcontent.optionalcontent.OrderedLayers;
import org.openpdf.examples.directcontent.pageevents.Bookmarks;
import org.openpdf.examples.directcontent.pageevents.EndPage;
import org.openpdf.examples.directcontent.pageevents.Events;
import org.openpdf.examples.directcontent.pageevents.PageNumbersWatermark;
import org.openpdf.examples.directcontent.text.Logo;
import org.openpdf.examples.directcontent.text.Text;

public class RunAll {

    public static void main(String[] args) {
        // directcontent
        Layers.main(args);
        TemplateImages.main(args);
        Templates.main(args);
        // colors
        Groups.main(args);
        Pattern.main(args);
        Patterns.main(args);
        Shading.main(args);
        ShadingPattern.main(args);
        SoftMask.main(args);
        SpotColors.main(args);
        Transparency.main(args);
        // coordinates
        AffineTransformation.main(args);
        Transformations.main(args);
        TransformImage.main(args);
        UpsideDown.main(args);
        XandYcoordinates.main(args);
        // graphics
        Circles.main(args);
        GState.main(args);
        Literal.main(args);
        Shapes.main(args);
        State.main(args);
        // graphics2D
        ArabicText.main(args);
        G2D.main(args);
        JFreeChartExample.main(args);
        // optionalcontent
        Automatic.main(args);
        ContentGroups.main(args);
        org.openpdf.examples.directcontent.optionalcontent.Layers.main(args);
        NestedLayers.main(args);
        OptionalContent.main(args);
        OrderedLayers.main(args);
        // pageevents
        Bookmarks.main(args);
        EndPage.main(args);
        Events.main(args);
        PageNumbersWatermark.main(args);
        // text
        Logo.main(args);
        Text.main(args);
    }
}
