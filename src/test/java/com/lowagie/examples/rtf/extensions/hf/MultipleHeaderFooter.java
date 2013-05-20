/*
 * $Id: MultipleHeaderFooter.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.rtf.extensions.hf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.field.RtfPageNumber;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooter;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooterGroup;

/**
 * The MultipleHeaderFooter example demonstrates the use of the
 * RtfHeaderFooterGroup to place different headers and footers
 * on different pages.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class MultipleHeaderFooter {
    /**
     * Extended font example.
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates use of the RtfHeaderFooterGroup for multiple headers and footers");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("MultipleHeaderFooter.rtf"));

            // Create the Paragraph that will be used in the header.
            Paragraph date = new Paragraph("01.01.2010");
            date.setAlignment(Element.ALIGN_CENTER);

            // Create the RtfHeaderFooterGroup for the header.
            // To display the same header on both pages, but not the
            // title page set them to left and right pages explicitly.
            RtfHeaderFooterGroup header = new RtfHeaderFooterGroup();
            header.setHeaderFooter(new RtfHeaderFooter(date), RtfHeaderFooter.DISPLAY_LEFT_PAGES);
            header.setHeaderFooter(new RtfHeaderFooter(date), RtfHeaderFooter.DISPLAY_RIGHT_PAGES);
            
            // Set the header
            document.setHeader(header);

            // Create the paragraphs that will be used as footers
            Paragraph titleFooter = new Paragraph("Multiple headers / footers example");
            titleFooter.setAlignment(Element.ALIGN_CENTER);
            Paragraph leftFooter = new Paragraph("Page ");
            leftFooter.add(new RtfPageNumber());
            Paragraph rightFooter = new Paragraph("Page ");
            rightFooter.add(new RtfPageNumber());
            rightFooter.setAlignment(Element.ALIGN_RIGHT);
            
            // Create the RtfHeaderGroup for the footer and set the footers
            // at the desired positions
            RtfHeaderFooterGroup footer = new RtfHeaderFooterGroup();
            footer.setHeaderFooter(new RtfHeaderFooter(titleFooter), RtfHeaderFooter.DISPLAY_FIRST_PAGE);
            footer.setHeaderFooter(new RtfHeaderFooter(leftFooter), RtfHeaderFooter.DISPLAY_LEFT_PAGES);
            footer.setHeaderFooter(new RtfHeaderFooter(rightFooter), RtfHeaderFooter.DISPLAY_RIGHT_PAGES);
            
            // Set the document footer
            document.setFooter(footer);
            
            document.open();
            
            document.add(new Paragraph("This document has headers and footers created" +
                    " using the RtfHeaderFooterGroup class.\n\n"));
            
            // Add some content, so that the different headers / footers show up.
            for(int i = 0; i < 300; i++) {
                document.add(new Paragraph("Just a bit of content so that the headers become visible."));
            }

            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }
}
