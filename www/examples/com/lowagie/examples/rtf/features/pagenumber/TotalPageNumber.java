/*
 * $Id: TotalPageNumber.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.field.RtfPageNumber;
import com.lowagie.text.rtf.field.RtfTotalPageNumber;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooter;


/**
 * The TotalPageNumber example demonstrates how to create a header of the
 * "Page X of Y" form. Unfortunately Word sometimes takes some time until the
 * total number of pages is updated.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class TotalPageNumber {
    /**
     * Demonstrates creating a header with page number and total number of pages
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates creating a header with page number and total page count");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("TotalPageNumber.rtf"));

            // Create a new Paragraph for the footer
            Paragraph par = new Paragraph("Page ");

            // Add the RtfPageNumber to the Paragraph
            par.add(new RtfPageNumber());
            
            // Add the RtfTotalPageNumber to the Paragraph
            par.add(" of ");
            par.add(new RtfTotalPageNumber());
            
            // Create an RtfHeaderFooter with the Paragraph and set it
            // as a header for the document
            RtfHeaderFooter header = new RtfHeaderFooter(par);
            document.setHeader(header);
            
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
