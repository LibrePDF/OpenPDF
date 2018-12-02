/*
 * $Id: HelloWorldMultiple.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.general;

import com.lowagie.text.Anchor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Generates a simple 'Hello World' PDF.
 * 
 * @author blowagie
 */

class HelloWorldPdf {

	/**
	 * Generates simple PDF, RTF and HTML files using only one Document object.
	 */
    @Test
	void testHelloWorld() throws Exception {

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // step 1: creation of a document-object
            Document document = new Document();
			// step 2:
			// we create 3 different writers that listen to the document
			PdfWriter pdf = PdfWriter.getInstance(document, baos);

			// step 3: we open the document
			document.open();
			// step 4: we add a paragraph to the document
			document.add(new Paragraph("Hello World"));
			// we make references
			Anchor pdfRef = new Anchor("see Hello World in PDF.");
			pdfRef.setReference("./HelloWorldPdf.pdf");
			Anchor rtfRef = new Anchor("see Hello World in RTF.");
			rtfRef.setReference("./HelloWorldRtf.rtf");
			
			// we add the references, but only to the HTML page:
			
			pdf.pause();
			document.add(pdfRef);
			document.add(Chunk.NEWLINE);
			document.add(rtfRef);
			pdf.resume();

            // step 5: we close the document
            document.close();

            assertFalse(baos.size() == 0);
		}

	}
}