/*
 * $Id: ChapterHeaderFooter.java 3373 2008-05-12 16:21:24Z xlv $
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

import com.lowagie.text.Chapter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooter;

/**
 * The ChapterHeaderFooter example shows how by using Chapters it is
 * possible to have different headers and footers for different
 * parts of the document.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class ChapterHeaderFooter {
    /**
     * Different headers and footers per Chapter example.
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates creating different headers and footers per Chapter");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("ChapterHeaderFooter.rtf"));

            // Create the header identifying the current chapter. The first
            // chapter has to be set before the document is opened.
            Paragraph header = new Paragraph("Chapter 1");
            header.setAlignment(Element.ALIGN_CENTER);
            document.setHeader(new RtfHeaderFooter(header));
            
            // If the footer (or header) is to be the same for all Chapters
            // then it has to be set before the document is opened and is
            // then automatically set for all Chapters.
            document.setFooter(new HeaderFooter(new Phrase("This is page "), new Phrase(".")));
            
            document.open();
            
            Chapter chapter1 = new Chapter("Chapter 1", 1);
            chapter1.add(new Paragraph("This document has different headers and footers " +
                    " for each chapter."));
            document.add(chapter1);

            // After adding the first chapter set the header for the second chapter.
            header = new Paragraph("Chapter 2");
            header.setAlignment(Element.ALIGN_CENTER);
            document.setHeader(new RtfHeaderFooter(header));

            Chapter chapter2 = new Chapter("Chapter 2", 2);
            chapter2.add(new Paragraph("This is the content of chapter 2."));
            document.add(chapter2);
            
            // After adding the second chapter set the header for the third chapter.
            header = new Paragraph("Chapter 3");
            header.setAlignment(Element.ALIGN_CENTER);
            document.setHeader(new RtfHeaderFooter(header));

            Chapter chapter3 = new Chapter("Chapter 3", 3);
            chapter3.add(new Paragraph("Chapter 3 is very boring."));
            document.add(chapter3);

            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }
}
