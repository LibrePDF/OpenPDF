/*
 * $Id: ExtendingStylesheets.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.rtf.features.styles;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.style.RtfParagraphStyle;


/**
 * The ExtendingStylesheets example shows how to create new paragraph stylesheets
 * that (if desired) inherit from existing stylesheets.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class ExtendingStylesheets {
    /**
     * Creation of new paragraph stylesheets.
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates creating new, user-defined paragraph stylesheets");
        try {
            Document document = new Document();
            RtfWriter2 writer = RtfWriter2.getInstance(document, new FileOutputStream("ExtendingStylesheets.rtf"));

            // Create the new RtfParagraphStyle. The second parameter is the name of
            // the RtfParagraphStyle that this style will inherit default properties from.
            RtfParagraphStyle incorrectStyle = new RtfParagraphStyle("Incorrect", "Normal");
            // Change the desired properties
            incorrectStyle.setColor(Color.RED);
            incorrectStyle.setStyle(Font.STRIKETHRU);
            // Register the new paragraph stylesheet with the RtfWriter2.
            writer.getDocumentSettings().registerParagraphStyle(incorrectStyle);
            
            // Create a new RtfParagraphStyle that does not inherit from any other style.
            RtfParagraphStyle correctStyle = new RtfParagraphStyle("Correct", "Arial",
                    12, Font.NORMAL, Color.GREEN);
            // Register the new paragraph stylesheet with the RtfWriter2.
            writer.getDocumentSettings().registerParagraphStyle(correctStyle);

            // Change the default font name. This will propagate to the paragraph stylesheet
            // that inherits, but not the other one.
            RtfParagraphStyle.STYLE_NORMAL.setFontName("Times New Roman");
            
            document.open();
            
            // Simply set the stylesheet you wish to use as the Font
            // of the Paragraph
            document.add(new Paragraph("This is a heading level 1",
                    RtfParagraphStyle.STYLE_HEADING_1));
            document.add(new Paragraph("This is a heading level 2",
                    RtfParagraphStyle.STYLE_HEADING_2));
            document.add(new Paragraph("Just some text that is formatted " +
                    "in the default style.", RtfParagraphStyle.STYLE_NORMAL));
            document.add(new Paragraph("This paragraph should be removed.",
                    incorrectStyle));
            document.add(new Paragraph("It should be replaced with this.",
                    correctStyle));

            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }

}
