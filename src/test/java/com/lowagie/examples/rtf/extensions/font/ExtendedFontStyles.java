/*
 * $Id: ExtendedFontStyles.java 3373 2008-05-12 16:21:24Z xlv $
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.style.RtfFont;

/**
 * The ExtendedFontStyles class demonstrates the use of the extended
 * font styles provided by the RtfFont class. These styles can be used
 * in place or combined with the existing font styles in Font.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @see com.lowagie.text.rtf.style.RtfFont
 */
public class ExtendedFontStyles {
    /**
     * Extended font styles example.
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates the extended font styles");
        try {
            Document document = new Document();
            RtfWriter2.getInstance(document, new FileOutputStream("ExtendedFontStyles.rtf"));
            document.open();
            
            // Use the RtfFont.STYLE_* instead of the Font styles.
            RtfFont doubleStrikethrough = new RtfFont("Arial", RtfFont.UNDEFINED,
                    RtfFont.STYLE_DOUBLE_STRIKETHROUGH);
            RtfFont shadow = new RtfFont("Arial", RtfFont.UNDEFINED,
                    RtfFont.STYLE_SHADOW);
            
            // Or combine them with Font styles.
            RtfFont engravedItalic = new RtfFont("Arial", RtfFont.UNDEFINED,
                    RtfFont.STYLE_ENGRAVED | Font.ITALIC);
            
            // The hidden style is special since it hides text.
            RtfFont hidden = new RtfFont("Arial", RtfFont.UNDEFINED,
                    RtfFont.STYLE_HIDDEN);
            
            Paragraph paragraph = new Paragraph("This text is ", new RtfFont("Arial", 12));
            
            // Use the RtfFonts when creating the text.
            paragraph.add(new Chunk("deleted,", doubleStrikethrough));
            paragraph.add(new Chunk(" shady,", shadow));
            paragraph.add(new Chunk(" engraved and italic", engravedItalic));
            paragraph.add(" and");
            paragraph.add(new Chunk(" you won't see this", hidden));
            paragraph.add(" nothing.");
            
            document.add(paragraph);
            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }
}
