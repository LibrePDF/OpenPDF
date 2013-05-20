/*
 * $Id: BasicTabs.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.rtf.features.tabs;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.text.RtfTab;

/**
 * The BasicTabs example demonstrates the basic concepts of using the
 * RtfTab class to define tab stops.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class BasicTabs {
    /**
     * Tab stops in paragraphs.
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates creating Paragraphs with tab stops");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("BasicTabs.rtf"));

            document.open();
            
            // Define the Paragraph to add tab stops to
            Paragraph par = new Paragraph();
            
            // Add the tab stops to the paragraph
            par.add(new RtfTab(70, RtfTab.TAB_LEFT_ALIGN));
            par.add(new RtfTab(400, RtfTab.TAB_RIGHT_ALIGN));
            
            // Add the text to the paragraph, placing the tab stops with \t
            par.add("\tFirst the text on the left-hand side\tThis text is right aligned.");
            
            document.add(par);

            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }
}
