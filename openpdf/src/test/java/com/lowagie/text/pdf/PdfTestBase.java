package com.lowagie.text.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;

public class PdfTestBase {

	public static Document createPdf(String filename)
			throws FileNotFoundException, DocumentException {
		// create a new document
		Rectangle rec = new Rectangle(PageSize.A4.getWidth(),
				PageSize.A4.getHeight());
		Document document = new Document(rec);

		// create a new file
		FileOutputStream fileOs = new FileOutputStream(new File(filename));

		// generate file
		PdfWriter.getInstance(document, fileOs);
		return document;
	}

}
