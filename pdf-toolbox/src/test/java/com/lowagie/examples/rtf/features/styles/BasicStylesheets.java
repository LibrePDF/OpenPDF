/*
 * $Id: BasicStylesheets.java 3373 2008-05-12 16:21:24Z xlv $
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.style.RtfParagraphStyle;


/**
 * The BasicStylesheets example shows how to use the predefined stylesheets
 * in an RTF document.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class BasicStylesheets {
    /**
     * Use of paragraph stylesheets.
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates using the paragraph stylesheets");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("BasicStylesheets.rtf"));

            document.open();
            
            // Simply set the stylesheet you wish to use as the Font
            // of the Paragraph
            document.add(new Paragraph("This is a heading",
                    RtfParagraphStyle.STYLE_HEADING_1));
            
            document.add(new Paragraph("Just some text that is formatted " +
                    "in the default style.", RtfParagraphStyle.STYLE_NORMAL));

            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }

}
