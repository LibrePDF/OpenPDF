package com.lowagie.text.pdf.parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class PdfTextExtractorTest {

	
	@Test
	void testPageExceeded() {
		Assertions.assertThrows(IOException.class, () -> getString("HelloWorldMeta.pdf", 5));
	}
	@Test
	void testInvalidPageNumber() {
		Assertions.assertThrows(IOException.class, () -> getString("HelloWorldMeta.pdf", 0));
	}
	
	
	@Test
	void testConcatenateWatermark() throws Exception {
		String result = getString("merge-acroforms.pdf", 5);
		Assertions.assertNotNull(result);
		// html??
		result = result.replaceAll("\\<.*?>","");		
		// Multiple spaces betwen words??
		Assertions.assertTrue(result.contains("2.  This  is  chapter  2"));
		Assertions.assertTrue(result.contains("watermark-concatenate"));
	}
		
	
	private String getString(String fileName, int pageNumber) throws Exception {
		return getString(new File("src/test/resources", fileName), pageNumber);
	}
	private String getString(File file, int pageNumber) throws Exception {
		byte[] pdfBytes = readDocument(file);
		final PdfReader pdfReader = new PdfReader(pdfBytes);
		
		return new PdfTextExtractor(pdfReader).getTextFromPage( pageNumber);		
	}
	
	protected static byte[] readDocument(final File file) throws IOException {

		try (ByteArrayOutputStream fileBytes = new ByteArrayOutputStream();
				InputStream inputStream = new FileInputStream(file)) {
			final byte[] buffer = new byte[8192];
			while (true) {
				final int bytesRead = inputStream.read(buffer);
				if (bytesRead == -1) {
					break;
				}
				fileBytes.write(buffer, 0, bytesRead);
			}
			return fileBytes.toByteArray();
		}

	}
}
