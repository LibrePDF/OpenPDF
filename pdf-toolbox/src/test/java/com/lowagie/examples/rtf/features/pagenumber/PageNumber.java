/*
 * $Id: PageNumber.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.rtf.features.pagenumber;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.field.RtfPageNumber;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooter;


/**
 * The PageNumber example demonstrates creating a footer with the
 * current page number.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class PageNumber {
    /**
     * Demonstrates creating a footer with the current page number
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates creating a footer with a page number");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("PageNumber.rtf"));

            // Create a new Paragraph for the footer
            Paragraph par = new Paragraph("Page ");
            par.setAlignment(Element.ALIGN_RIGHT);

            // Add the RtfPageNumber to the Paragraph
            par.add(new RtfPageNumber());
            
            // Create an RtfHeaderFooter with the Paragraph and set it
            // as a footer for the document
            RtfHeaderFooter footer = new RtfHeaderFooter(par);
            document.setFooter(footer);
            
            document.open();

            for(int i = 1; i <= 300; i++) {
                document.add(new Paragraph("Line " + i + "."));
            }

            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }

}
