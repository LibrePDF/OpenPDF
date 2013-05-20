/*
 * $Id: PageLabels.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects.bookmarks;

import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPageLabels;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Demonstrates how pagelabels work.
 * 
 * @author blowagie
 */

public class PageLabels {

	/**
	 * Demonstrates some PageLabel functionality.
	 * 
	 * @param args no arguments needed here
	 */
	public static void main(String[] args) {

		System.out.println("pagelabels");

		// step 1: creation of a document-object
		Document document = new Document();
		try {
            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("PageLabels.pdf"));
            // step 3:
            writer.setViewerPreferences(PdfWriter.PageModeUseThumbs);
            document.open();
            // step 4:
            // we add some content
            for (int k = 1; k <= 10; ++k) {
                document.add(new Paragraph("This document has the logical page numbers: i,ii,iii,iv,1,2,3,A-8,A-9,A-10\nReal page " + k));
                document.newPage();
            }
            PdfPageLabels pageLabels = new PdfPageLabels();
            pageLabels.addPageLabel(1, PdfPageLabels.LOWERCASE_ROMAN_NUMERALS);
            pageLabels.addPageLabel(5, PdfPageLabels.DECIMAL_ARABIC_NUMERALS);
            pageLabels.addPageLabel(8, PdfPageLabels.DECIMAL_ARABIC_NUMERALS, "A-", 8);
            writer.setPageLabels(pageLabels);
		} catch (DocumentException de) {
			System.err.println(de.getMessage());
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}

		// step 5: we close the document
		document.close();
	}
}