/*
 * $Id: DrawingWrap.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is free software. It may only be copied or modified
 * if you include the following copyright notice:
 *
 * --> Copyright 2006 by Mark Hall <--
 *
 * This code is part of the 'iText Tutorial'.
 * You can find the complete tutorial at the following address:
 * http://itextdocs.lowagie.com/tutorial/
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * itext-questions@lists.sourceforge.net
 */
package com.lowagie.examples.rtf.features.shape;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.graphic.RtfShape;
import com.lowagie.text.rtf.graphic.RtfShapePosition;
import com.lowagie.text.rtf.graphic.RtfShapeProperty;


/**
 * The DrawingWrap example demonstrates the different types of text wrapping
 * modes that are supported when generating RTF drawing objects. Also shown
 * are how to use the setBelowText and setZOrder methods for defining the
 * z-order of drawing objects.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class DrawingWrap {
    /**
     * Demonstrates setting different text wrapping modes and the z-ordering.
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates using the different text wrapping modes for drawing objects");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("DrawingWrap.rtf"));

            document.open();

            Paragraph par = new Paragraph();
            for(int i = 0; i < 600; i++) {
                par.add("bla ");
            }
            document.add(par);

            // Create a new rectangle RtfShape. By default it will be above the text
            RtfShapePosition position = new RtfShapePosition(1000, 1000, 2000, 2000);
            RtfShape shape = new RtfShape(RtfShape.SHAPE_RECTANGLE, position);
            document.add(shape);

            // Create a rounded rectangle RtfShape and position it below the text
            position = new RtfShapePosition(4000, 1500, 4500, 5000);
            position.setShapeBelowText(true);
            shape = new RtfShape(RtfShape.SHAPE_ROUND_RECTANGLE, position);
            shape.setProperty(
                    new RtfShapeProperty(RtfShapeProperty.PROPERTY_ADJUST_VALUE, 4500));
            shape.setProperty(
                    new RtfShapeProperty(RtfShapeProperty.PROPERTY_FILL_COLOR, Color.GRAY));
            document.add(shape);

            // Create a triangle RtfShape, around which text will wrap on both sides
            position = new RtfShapePosition(1500, 3000, 4000, 2500);
            shape = new RtfShape(RtfShape.SHAPE_TRIANGLE_RIGHT, position);
            shape.setWrapping(RtfShape.SHAPE_WRAP_BOTH);
            document.add(shape);

            // Create an elliptical RtfShape, around which text will only wrap on the left side
            position = new RtfShapePosition(3000, 6000, 10500, 4500);
            shape = new RtfShape(RtfShape.SHAPE_ELLIPSE, position);
            shape.setWrapping(RtfShape.SHAPE_WRAP_LEFT);
            shape.setProperty(
                    new RtfShapeProperty(RtfShapeProperty.PROPERTY_FILL_COLOR, Color.BLUE));
            document.add(shape);

            // Create a circular RtfShape and set its z-order to 1
            position = new RtfShapePosition(5850, 6800, 8200, 7250);
            position.setZOrder(1);
            shape = new RtfShape(RtfShape.SHAPE_ELLIPSE, position);
            shape.setProperty(new RtfShapeProperty(RtfShapeProperty.PROPERTY_FILL_COLOR, Color.RED));
            document.add(shape);

            // Create a star RtfShape and set its z-order to 2, above the circle
            // defined above
            position = new RtfShapePosition(6000, 7000, 8000, 7000);
            position.setZOrder(2);
            shape = new RtfShape(RtfShape.SHAPE_STAR, position);
            shape.setProperty(new RtfShapeProperty(RtfShapeProperty.PROPERTY_FILL_COLOR, Color.YELLOW));
            document.add(shape);

            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }

}
