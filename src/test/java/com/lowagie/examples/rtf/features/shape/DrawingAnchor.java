/*
 * $Id: DrawingAnchor.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.graphic.RtfShape;
import com.lowagie.text.rtf.graphic.RtfShapePosition;


/**
 * The DrawingAnchor example demonstrates how a drawing object can
 * be anchored to a Paragraph and the page margin, instead of the
 * page to create a horizontal line.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class DrawingAnchor {
    /**
     * Demonstrates setting the horizontal and vertical anchors for a drawing object
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates setting horizontal and vertical anchors");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("DrawingAnchor.rtf"));

            document.open();

            document.add(new Paragraph("This text is above the horizontal rule"));
            
            // Construct a new RtfShapePosition that covers the whole page horizontally 
            RtfShapePosition position = new RtfShapePosition(150, 0, 10400, 150);

            // The horizontal position is relative to the margins of the page
            position.setXRelativePos(RtfShapePosition.POSITION_X_RELATIVE_MARGIN);

            // The vertical position is relative to the paragraph
            position.setYRelativePos(RtfShapePosition.POSITION_Y_RELATIVE_PARAGRAPH);
            
            // Create a new line drawing object
            RtfShape shape = new RtfShape(RtfShape.SHAPE_LINE, position);
            
            // Add the shape to the paragraph, so that it is anchored to the
            // correct paragraph
            Paragraph par = new Paragraph();
            par.add(shape);
            document.add(par);
            
            document.add(new Paragraph("This text is below the horizontal rule"));

            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }

}
