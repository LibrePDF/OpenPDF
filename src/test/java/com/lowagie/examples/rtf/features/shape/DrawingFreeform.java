/*
 * $Id: DrawingFreeform.java 3373 2008-05-12 16:21:24Z xlv $
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
import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.graphic.RtfShape;
import com.lowagie.text.rtf.graphic.RtfShapePosition;
import com.lowagie.text.rtf.graphic.RtfShapeProperty;


/**
 * The DrawingFreeform example demonstrates creating freeform
 * drawing objects using an array of Point objects. The example
 * creates a table shaped drawing object.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class DrawingFreeform {
    /**
     * Demonstrates creating freeform drawing objects
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates creating freeform drawing objects");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("DrawingFreeform.rtf"));

            document.open();

            // Create a new rectangle RtfShape using the SHAPE_FREEFORM constant.
            RtfShapePosition position = new RtfShapePosition(1000, 1000, 4000, 4000);
            RtfShape shape = new RtfShape(RtfShape.SHAPE_FREEFORM, position);

            // Set the bottom and right extents of the drawing object.
            shape.setProperty(new RtfShapeProperty(RtfShapeProperty.PROPERTY_GEO_RIGHT, 3000));
            shape.setProperty(new RtfShapeProperty(RtfShapeProperty.PROPERTY_GEO_BOTTOM, 3000));

            // Define the vertices that make up the drawing object.
            // This list draws a basic table shape.
            shape.setProperty(new RtfShapeProperty(RtfShapeProperty.PROPERTY_VERTICIES,
                new Point[]{
                    new Point(100, 100),
                    new Point(2900, 100),
                    new Point(2900, 200),
                    new Point(2600, 200),
                    new Point(2600, 1500),
                    new Point(2520, 1500),
                    new Point(2520, 200),
                    new Point(480, 200),
                    new Point(480, 1500),
                    new Point(400, 1500),
                    new Point(400, 200),
                    new Point(100, 200)
                }));
            
            // A nice red Table :-)
            shape.setProperty(new RtfShapeProperty(RtfShapeProperty.PROPERTY_FILL_COLOR, Color.red));

            document.add(shape);

            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }

}
