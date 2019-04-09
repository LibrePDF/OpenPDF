package com.lowagie.text.pdf.metadata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

public class CleanMetaDataTest {

	@Test
	public void testProducer() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document document = new Document();

		PdfWriter.getInstance(document, baos);
		document.open();
		document.add(new Paragraph("Hello World"));
		document.close();

		try (PdfReader r = new PdfReader(baos.toByteArray())) {
			Assertions.assertNull(r.getInfo().get("Producer"));
		}
		
	}

	@Test
	public void testAddedMetadata() throws Exception {
		String AUTHOR_NAME = "Mr Bean";
		String TITLE = "The title";

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document document = new Document();

		PdfWriter.getInstance(document, baos);

		document.open();
		document.addProducer();
		document.addAuthor(AUTHOR_NAME);
		document.addTitle(TITLE);
		document.add(new Paragraph("Hello World"));
		document.close();

		PdfReader r = new PdfReader(baos.toByteArray());

		// Metadata generated only on demand
		Assertions.assertEquals(Document.getVersion(), r.getInfo().get("Producer"));

		Assertions.assertEquals(AUTHOR_NAME, r.getInfo().get("Author"));
		Assertions.assertEquals(TITLE, r.getInfo().get("Title"));

		r.close();
	}
	

	@Test
	public void testStamperMetadata() throws Exception {
		byte[] data = addWatermark(new File("src/test/resources/HelloWorldMeta.pdf"), false, null);
		PdfReader r = new PdfReader(data);
		Assertions.assertNull(r.getInfo().get("Producer"));
		Assertions.assertNull(r.getInfo().get("Author"));
		Assertions.assertNull(r.getInfo().get("Title"));
		Assertions.assertNull(r.getInfo().get("Subject"));	
		r.close();
	}
	
	@Test
	public void testStamperEncryptMetadata() throws Exception {
		byte[] data = addWatermark(new File("src/test/resources/HelloWorldMeta.pdf"), true, null);
		PdfReader r = new PdfReader(data);
		Assertions.assertNull(r.getInfo().get("Producer"));
		Assertions.assertNull(r.getInfo().get("Author"));
		Assertions.assertNull(r.getInfo().get("Title"));
		Assertions.assertNull(r.getInfo().get("Subject"));		
		r.close();
	}
	
	
	@Test
	public void testStamperExtraMetadata() throws Exception {
		HashMap<String, String> moreInfo = new HashMap<String, String>();
		moreInfo.put("Producer", Document.getVersion());
		moreInfo.put("Author", "Author1");
		moreInfo.put("Title", "Title2");
		moreInfo.put("Subject", "Subject3");
		byte[] data = addWatermark(new File("src/test/resources/HelloWorldMeta.pdf"), false, moreInfo);
		PdfReader r = new PdfReader(data);
		Assertions.assertEquals(Document.getVersion(), r.getInfo().get("Producer"));
		Assertions.assertEquals("Author1", r.getInfo().get("Author"));
		Assertions.assertEquals("Title2", r.getInfo().get("Title"));
		Assertions.assertEquals("Subject3", r.getInfo().get("Subject"));	
		r.close();
	}
	

	private byte[] addWatermark(File origin, boolean encrypt, HashMap<String, String> moreInfo) throws Exception {
		int text_angle = 45;
		int text1_pos_x = 300;
		int text_1_pos_y = 430;
		int text_2_pos_x = 330;
		int text_2_pos_y = 410;
		String text1 = "NOT VALID";
		String text2 = "DRAFT";
		int font_size = 32;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfReader reader = new PdfReader(origin.getAbsolutePath());
		int n = reader.getNumberOfPages();
		PdfStamper stamp = new PdfStamper(reader, baos);
		stamp.setInfoDictionary(moreInfo);
		if (encrypt) {
			stamp.setEncryption(null, null, 0, PdfWriter.ENCRYPTION_AES_128);
		}
		int i = 0;
		PdfContentByte over;
		BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);

		while (i < n) {
			i++;
			over = stamp.getOverContent(i);
			over.beginText();
			over.setRGBColorFill(255, 0, 0);
			over.setFontAndSize(bf, font_size);
			over.showTextAligned(Element.ALIGN_CENTER, text1, text1_pos_x, text_1_pos_y, text_angle);
			over.showTextAligned(Element.ALIGN_CENTER, text2, text_2_pos_x, text_2_pos_y, text_angle);
			over.endText();
		}
		
		stamp.close();

		return baos.toByteArray();
	}

}
