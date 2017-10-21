package com.lowagie.text.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;

class PdfTestBase {

	static Document createPdf(String filename) throws FileNotFoundException, DocumentException {
		// create a new file
		return createPdf(new FileOutputStream(new File(filename)));
	}

	static Document createPdf(OutputStream outputStream) throws DocumentException {
		// create a new document
		Document document = new Document(PageSize.A4);

		// generate file
		PdfWriter.getInstance(document, outputStream);
		return document;
	}

}
