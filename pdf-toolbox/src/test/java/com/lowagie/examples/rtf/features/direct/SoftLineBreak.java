/*
 * $Id: SoftLineBreak.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is free software. It may only be copied or modified
 * if you include the following copyright notice:
 *
 * --> Copyright 2007 by Mark Hall <--
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
package com.lowagie.examples.rtf.features.direct;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.direct.RtfDirectContent;


/**
 * The SoftLineBreak example demonstrates using the RtfDirectContent
 * to add a soft linebreak to a Paragraph. 
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class SoftLineBreak {
    /**
     * Demonstrates adding a soft linebreak to a Paragraph
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates adding a soft linebreak to a Paragraph");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("SoftLineBreak.rtf"));

            document.open();

            document.add(new Paragraph("This is just a paragraph."));
            
            Paragraph par = new Paragraph();

            // Set the spacings just to demonstrate that the soft linebreak
            // does not cause spacing before or after
            par.setSpacingBefore(10);
            par.setSpacingAfter(10);
            
            // Add the contents before the linebreak
            par.add("This paragraph contains a soft linebreak");

            // Add the soft linebreak
            par.add(RtfDirectContent.DIRECT_SOFT_LINEBREAK);

            // Add the contents after the linebreak
            par.add("just before the just.");
            
            // Add the paragraph to the document
            document.add(par);

            document.add(new Paragraph("This is just a paragraph."));
            
            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }

}
