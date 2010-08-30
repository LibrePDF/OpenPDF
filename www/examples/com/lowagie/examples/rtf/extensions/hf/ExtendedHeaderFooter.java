/*
 * $Id: ExtendedHeaderFooter.java 3373 2008-05-12 16:21:24Z xlv $
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

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.field.RtfPageNumber;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooter;

/**
 * The ExtendedHeaderFooter example demonstrates the use of the
 * RtfHeaderFooter object to create more complex headers or footers
 * using more complex elements such as multiple paragraphs or tables.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class ExtendedHeaderFooter {
    /**
     * Extended headers / footers example
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates use of the RtfHeaderFooter for extended headers and footers");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("ExtendedHeaderFooter.rtf"));

            // Create the Paragraphs that will be used in the header.
            Paragraph date = new Paragraph("01.01.2010");
            date.setAlignment(Paragraph.ALIGN_RIGHT);
            Paragraph address = new Paragraph("TheFirm\nTheRoad 24, TheCity\n" +
                    "+00 99 11 22 33 44");

            // Create the RtfHeaderFooter with an array containing the Paragraphs to add
            RtfHeaderFooter header = new RtfHeaderFooter(new Element[]{date, address});
            
            // Set the header
            document.setHeader(header);

            // Create the table that will be used as the footer
            Table footer = new Table(2);
            footer.setBorder(0);
            footer.getDefaultCell().setBorder(0);
            footer.setWidth(100);
            footer.addCell(new Cell("(c) Mark Hall"));
            Paragraph pageNumber = new Paragraph("Page ");
            
            // The RtfPageNumber is an RTF specific element that adds a page number field
            pageNumber.add(new RtfPageNumber());
            pageNumber.setAlignment(Paragraph.ALIGN_RIGHT);
            footer.addCell(new Cell(pageNumber));
            
            // Create the RtfHeaderFooter and set it as the footer to use
            document.setFooter(new RtfHeaderFooter(footer));
            
            document.open();
            
            document.add(new Paragraph("This document has headers and footers created" +
                    " using the RtfHeaderFooter class."));

            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }
}
