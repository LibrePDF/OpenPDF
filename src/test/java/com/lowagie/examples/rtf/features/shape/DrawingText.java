/*
 * $Id: DrawingText.java 3373 2008-05-12 16:21:24Z xlv $
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.graphic.RtfShape;
import com.lowagie.text.rtf.graphic.RtfShapePosition;


/**
 * The DrawingText example demonstrates setting text into an RTF drawing object.
 * This text cannot be styled.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class DrawingText {
    /**
     * Demonstrates setting text into an RTF drawing object.
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates setting text into an RTF drawing object");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("DrawingText.rtf"));

            document.open();

            // Create a new rectangle RtfShape.
            RtfShapePosition position = new RtfShapePosition(1000, 1000, 3000, 2000);
            RtfShape shape = new RtfShape(RtfShape.SHAPE_RECTANGLE, position);

            // Set the text to display in the drawing object
            shape.setShapeText("This text will appear in the drawing object.");
            
            document.add(shape);

            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }

}
