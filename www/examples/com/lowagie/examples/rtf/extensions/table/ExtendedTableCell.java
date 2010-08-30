/*
 * $Id: ExtendedTableCell.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.rtf.extensions.table;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.table.RtfBorder;
import com.lowagie.text.rtf.table.RtfBorderGroup;
import com.lowagie.text.rtf.table.RtfCell;

/**
 * The ExtendedTableCell example demonstrates the use of the RtfCell and
 * RtfBorderGroup classes to create Table cells with extended and complex
 * borders.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class ExtendedTableCell {
    /**
     * Extended borders for Table Cells.
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates creating Table Cells with complex borders");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("ExtendedTableCell.rtf"));

            document.open();
            
            Table table = new Table(3);

            // Create a simple RtfCell with a dotted border.
            RtfCell cellDotted = new RtfCell("Dotted border");
            cellDotted.setBorders(new RtfBorderGroup(Rectangle.BOX,
                    RtfBorder.BORDER_DOTTED, 1, new Color(0, 0, 0)));
            
            // Create a simple RtfCell with an embossed border.
            RtfCell cellEmbossed = new RtfCell("Embossed border");
            cellEmbossed.setBorders(new RtfBorderGroup(Rectangle.BOX,
                    RtfBorder.BORDER_EMBOSS, 1, new Color(0, 0, 0)));
            
            // Create a simple RtfCell with no border.
            RtfCell cellNoBorder = new RtfCell("No border");
            cellNoBorder.setBorders(new RtfBorderGroup());
            cellNoBorder.setRowspan(2);
            
            // Create a simple RtfCell that only has a border
            // on the bottom side.
            RtfCell bottomBorder = new RtfCell("Bottom border");
            bottomBorder.setBorders(new RtfBorderGroup(Rectangle.BOTTOM,
                    RtfBorder.BORDER_SINGLE, 2, new Color(255, 0, 0)));
            
            // Create a simple RtfCell that has different borders
            // on the left and bottom sides.
            RtfCell mixedBorder = new RtfCell("Mixed border");
            RtfBorderGroup mixedBorders = new RtfBorderGroup();
            mixedBorders.addBorder(Rectangle.RIGHT,
                    RtfBorder.BORDER_DOUBLE_WAVY, 2, Color.GREEN);
            mixedBorders.addBorder(Rectangle.BOTTOM,
                    RtfBorder.BORDER_DOT_DASH, 1, Color.BLUE);
            mixedBorder.setBorders(mixedBorders);
            
            // Add the cells to the table
            table.addCell(cellDotted);
            table.addCell(cellEmbossed);
            table.addCell(cellNoBorder);
            table.addCell(bottomBorder);
            table.addCell(mixedBorder);
            
            document.add(table);

            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }
}
