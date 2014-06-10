/*
 * $Id: TabGroups.java 3373 2008-05-12 16:21:24Z xlv $
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
import java.text.DecimalFormat;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.text.RtfTab;
import com.lowagie.text.rtf.text.RtfTabGroup;

/**
 * The TabGroups example demonstrates how using the RtfTabGroup class
 * simplifies the repeated use of a set of tab stops.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class TabGroups {
    /**
     * Using the RtfTabGroup to simplify adding a set of tab stops.
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates how to use the RtfTabGroup to repeatedly add a set of tab stops");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("TabGroups.rtf"));

            document.open();
            
            // Construct the RtfTabGroup object
            RtfTabGroup tabGroup = new RtfTabGroup();
            // Add RtfTab tab stops at the desired positions
            tabGroup.add(new RtfTab(400, RtfTab.TAB_RIGHT_ALIGN));
            tabGroup.add(new RtfTab(500, RtfTab.TAB_DECIMAL_ALIGN));
            
            // Create a Paragraph object
            Paragraph par = new Paragraph();
            // Add the tab group to the paragraph
            par.add(tabGroup);
            // Specify the tab positions using "\t"
            par.add("Description\tDate\tAmount");
            document.add(par);
            
            DecimalFormat nf = new DecimalFormat("#.00");
            double sum = 0;
            for(int i = 0; i < 10; i++) {
                double value = Math.random() * 100;
                sum = sum + value;

                par = new Paragraph();
                // The RtfTabGroup can be reused for further paragraphs
                par.add(tabGroup);
                par.add("Item " + (i + 1) + "\t" + (12 + i) + ".03.2007\t" + nf.format(value));
                document.add(par);
            }

            par = new Paragraph("", new Font(Font.TIMES_ROMAN, 12, Font.BOLD));
            par.add(tabGroup);
            // If a tab in the RtfTabGroup is not needed, just add an empty tab stop in the text
            par.add("Total\t\t" + nf.format(sum));
            document.add(par);
            
            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }
}
