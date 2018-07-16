/*
 * $Id: DrawingObjects.java 3373 2008-05-12 16:21:24Z xlv $
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
 * The DrawingObjects example demonstrates basic use of the RtfShape
 * class and how RtfShapeProperty objects are used to define the
 * different properties of the drawing objects.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class DrawingObjects {
    /**
     * Demonstrates basic use of RTF drawing objects
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates basic use of RTF drawing objects");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("DrawingObjects.rtf"));

            document.open();

            document.add(new Paragraph("This text will wrap around the shape that\n" +
                    "we will define. Different\n" +
                    "wrapping modes are possible."));
            
            // Create a new RtfShape of the type RECTANGLE.
            // The position defines the extent of the shape in the page (in Twips)
            RtfShape shape = new RtfShape(RtfShape.SHAPE_RECTANGLE,
                    new RtfShapePosition(1000, 2000, 3000, 2000));
            
            // Set the line colour to red
            shape.setProperty(new RtfShapeProperty(RtfShapeProperty.PROPERTY_LINE_COLOR,
                    Color.RED));
            
            // Set the fill colour to cyan
            shape.setProperty(new RtfShapeProperty(RtfShapeProperty.PROPERTY_FILL_COLOR,
                    Color.CYAN));
            
            // Text wraps around the shape on both sides
            shape.setWrapping(RtfShape.SHAPE_WRAP_BOTH);
            
            // Add the shape to the document
            document.add(shape);
            
            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }

}
