package com.lowagie.examples;

import com.lowagie.examples.directcontent.Layers;
import com.lowagie.examples.directcontent.TemplateImages;
import com.lowagie.examples.directcontent.Templates;
import com.lowagie.examples.directcontent.colors.Groups;
import com.lowagie.examples.directcontent.colors.Pattern;
import com.lowagie.examples.directcontent.colors.Patterns;
import com.lowagie.examples.directcontent.colors.Shading;
import com.lowagie.examples.directcontent.colors.ShadingPattern;
import com.lowagie.examples.directcontent.colors.SoftMask;
import com.lowagie.examples.directcontent.colors.SpotColors;
import com.lowagie.examples.directcontent.colors.Transparency;
import com.lowagie.examples.directcontent.coordinates.AffineTransformation;
import com.lowagie.examples.directcontent.coordinates.TransformImage;
import com.lowagie.examples.directcontent.coordinates.Transformations;
import com.lowagie.examples.directcontent.coordinates.UpsideDown;
import com.lowagie.examples.directcontent.coordinates.XandYcoordinates;
import com.lowagie.examples.directcontent.graphics.Circles;
import com.lowagie.examples.directcontent.graphics.GState;
import com.lowagie.examples.directcontent.graphics.Literal;
import com.lowagie.examples.directcontent.graphics.Shapes;
import com.lowagie.examples.directcontent.graphics.State;
import com.lowagie.examples.directcontent.graphics2d.ArabicText;
import com.lowagie.examples.directcontent.graphics2d.G2D;
import com.lowagie.examples.directcontent.graphics2d.JFreeChartExample;
import com.lowagie.examples.directcontent.optionalcontent.Automatic;
import com.lowagie.examples.directcontent.optionalcontent.ContentGroups;
import com.lowagie.examples.directcontent.optionalcontent.NestedLayers;
import com.lowagie.examples.directcontent.optionalcontent.OptionalContent;
import com.lowagie.examples.directcontent.optionalcontent.OrderedLayers;
import com.lowagie.examples.directcontent.pageevents.Bookmarks;
import com.lowagie.examples.directcontent.pageevents.EndPage;
import com.lowagie.examples.directcontent.pageevents.Events;
import com.lowagie.examples.directcontent.pageevents.PageNumbersWatermark;
import com.lowagie.examples.directcontent.text.Logo;
import com.lowagie.examples.directcontent.text.Text;

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
        com.lowagie.examples.directcontent.optionalcontent.Layers.main(args);
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
