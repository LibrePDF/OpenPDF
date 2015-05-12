/*
 * $Id: TableOfContents.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.rtf.features.toc;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.field.RtfTableOfContents;
import com.lowagie.text.rtf.style.RtfParagraphStyle;


/**
 * The TableOfContents example demonstrates the use of the RtfTableOfContents
 * for creating tables of contents. It also shows how to use the RtfParagraphStyle
 * to define table of contents entries and how to style them.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class TableOfContents {
    /**
     * Demonstrates creating and styling a table of contents
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates using the RTF table of contents.");
        try {
            Document document = new Document();
            RtfWriter2 rtfWriter2 = RtfWriter2.getInstance(document, new FileOutputStream("TableOfContents.rtf"));

            // Create paragraph stylesheets for each heading level. They must be named
            // "toc N" for each heading level you are using
            RtfParagraphStyle tocLevel1Style = new RtfParagraphStyle("toc 1",
                    "Times New Roman", 11, Font.NORMAL, Color.BLACK);
            RtfParagraphStyle tocLevel2Style = new RtfParagraphStyle("toc 2",
                    "Times New Roman", 10, Font.NORMAL, Color.BLACK);
            tocLevel2Style.setIndentLeft(10);
            
            // Register the paragraph stylesheets with the RtfWriter2
            rtfWriter2.getDocumentSettings().registerParagraphStyle(tocLevel1Style);
            rtfWriter2.getDocumentSettings().registerParagraphStyle(tocLevel2Style);
            
            document.open();

            // Create a Paragraph and add the table of contents to it
            Paragraph par = new Paragraph();
            par.add(new RtfTableOfContents("Right-click here and select \"Update\" " +
                    "to see the table of contents."));
            document.add(par);
            
            for(int i = 1; i <= 5; i++) {
                // Create a level 1 heading
                document.add(new Paragraph("Heading " + i, RtfParagraphStyle.STYLE_HEADING_1));
                for(int j = 1; j <= 3; j++) {
                    // Create a level 2 heading
                    document.add(new Paragraph("Heading " + i + "." + j, RtfParagraphStyle.STYLE_HEADING_2));
                    for(int k = 1; k <= 20; k++) {
                        document.add(new Paragraph("Line " + k + " in section " + i + "." + k));
                    }
                }
            }
            
            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }

}
