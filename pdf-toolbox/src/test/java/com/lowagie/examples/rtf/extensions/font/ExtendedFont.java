/*
 * $Id: ExtendedFont.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.rtf.extensions.font;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.style.RtfFont;

/**
 * The ExtendedFont example demonstrates using the RtfFont class
 * to generate RTF documents with fonts other than the core
 * iText fonts.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @see com.lowagie.text.rtf.style.RtfFont
 */
public class ExtendedFont {
    /**
     * Extended font example.
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates the extended font support");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("ExtendedFont.rtf"));
            document.open();
            
            // Create a RtfFont with the desired font name.
            RtfFont msComicSans = new RtfFont("Comic Sans MS");
            
            // Use the RtfFont like any other Font.
            document.add(new Paragraph("This paragraph uses the" +
                    " Comic Sans MS font.", msComicSans));
            
            // Font size, font style and font colour can also be specified.
            RtfFont bigBoldGreenArial = new RtfFont("Arial", 36, Font.BOLD, Color.GREEN);
            
            document.add(new Paragraph("This is a really big bold green Arial text", bigBoldGreenArial));
            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }
}
