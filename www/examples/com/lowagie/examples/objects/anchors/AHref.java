/*
 * $Id: AHref.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects.anchors;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.Anchor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.HtmlWriter;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Demonstrates how the Anchor object works.
 * 
 * @author blowagie
 */

public class AHref {

	/**
	 * Demonstrates some Anchor functionality.
	 * 
	 * @param args no arguments needed here
	 */
	public static void main(String[] args) {

		System.out.println("the Anchor object");

		// step 1: creation of a document-object
		Document document = new Document();
		try {
            // step 2:
            PdfWriter.getInstance(document, new FileOutputStream("AHref.pdf"));
            HtmlWriter.getInstance(document, new FileOutputStream("AHref.html"));
            
            // step 3: we open the document
            document.open();
            
            // step 4:
            Paragraph paragraph = new Paragraph("Please visit my ");
            Anchor anchor1 = new Anchor("website (external reference)", FontFactory.getFont(FontFactory.HELVETICA, 12, Font.UNDERLINE, new Color(0, 0, 255)));
            anchor1.setReference("http://www.lowagie.com/iText/");
            anchor1.setName("top");
            paragraph.add(anchor1);
            paragraph.add(new Chunk(".\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
            document.add(paragraph);
            Anchor anchor2 = new Anchor("please jump to a local destination", FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 255)));
            anchor2.setReference("#top");
            document.add(anchor2);
		} catch (DocumentException de) {
			System.err.println(de.getMessage());
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}

		// step 5: we close the document
		document.close();
	}
}