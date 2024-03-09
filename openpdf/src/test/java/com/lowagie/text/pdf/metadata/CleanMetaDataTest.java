package com.lowagie.text.pdf.metadata;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.xml.xmp.XmpWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CleanMetaDataTest {

    public CleanMetaDataTest() {
        super();
    }

    private HashMap<String, String> createCleanerMoreInfo() {
        HashMap<String, String> moreInfo = new HashMap<String, String>();
        moreInfo.put("Title", null);
        moreInfo.put("Author", null);
        moreInfo.put("Subject", null);
        moreInfo.put("Producer", null);
        moreInfo.put("Keywords", null);
        moreInfo.put("Creator", null);
        moreInfo.put("ModDate", null);
        return moreInfo;
    }

    @Test
    public void testProducer() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("Hello World"));
        document.close();

        try (PdfReader r = new PdfReader(baos.toByteArray())) {
            final String producer = r.getInfo().get("Producer");
            org.assertj.core.api.Assertions.assertThat(producer).startsWith("OpenPDF ");
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
        byte[] data = addWatermark(new File("src/test/resources/HelloWorldMeta.pdf"), false, createCleanerMoreInfo());
        PdfReader r = new PdfReader(data);
        Assertions.assertNull(r.getInfo().get("Producer"));
        Assertions.assertNull(r.getInfo().get("Author"));
        Assertions.assertNull(r.getInfo().get("Title"));
        Assertions.assertNull(r.getInfo().get("Subject"));
        r.close();
        String dataString = new String(data);
        Assertions.assertFalse(dataString.contains("This example explains how to add metadata."));
    }

    @Test
    public void testStamperEncryptMetadata() throws Exception {
        byte[] data = addWatermark(new File("src/test/resources/HelloWorldMeta.pdf"), true, createCleanerMoreInfo());
        PdfReader r = new PdfReader(data);
        Assertions.assertNull(r.getInfo().get("Producer"));
        Assertions.assertNull(r.getInfo().get("Author"));
        Assertions.assertNull(r.getInfo().get("Title"));
        Assertions.assertNull(r.getInfo().get("Subject"));
        r.close();
    }


    @Test
    public void testStamperExtraMetadata() throws Exception {
        HashMap<String, String> moreInfo = createCleanerMoreInfo();
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

    @Test
    public void testCleanMetadataMethodInStamper() throws Exception {
        byte[] data = cleanMetadata(new File("src/test/resources/HelloWorldMeta.pdf"));
        PdfReader r = new PdfReader(data);
        Assertions.assertNull(r.getInfo().get("Producer"));
        Assertions.assertNull(r.getInfo().get("Author"));
        Assertions.assertNull(r.getInfo().get("Title"));
        Assertions.assertNull(r.getInfo().get("Subject"));
        r.close();
        String dataString = new String(data);
        Assertions.assertFalse(dataString.contains("This example explains how to add metadata."));
    }

    @Test
    public void skipMetaDataUpdateTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(new File("src/test/resources/HelloWorldMeta.pdf").getAbsolutePath());
        PdfStamper stamp = new PdfStamper(reader, baos, '\0', true);
        stamp.setUpdateMetadata(false);
        stamp.cleanMetadata();
        stamp.close();

        String dataString = baos.toString();
        Assertions.assertTrue(dataString.contains("This example explains how to add metadata."));
    }

    @Test
    public void skipMetaDataUpdateFirstRevisionTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(new File("src/test/resources/HelloWorldMeta.pdf").getAbsolutePath());
        PdfStamper stamp = new PdfStamper(reader, baos, '\0', false);
        stamp.setUpdateMetadata(false);
        stamp.cleanMetadata();
        stamp.close();

        String dataString = baos.toString();
        Assertions.assertFalse(dataString.contains("This example explains how to add metadata."));
    }

    @Test
    public void skipInfoUpdateTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(new File("src/test/resources/HelloWorldMeta.pdf").getAbsolutePath());
        PdfStamper stamp = new PdfStamper(reader, baos, '\0', true);

        HashMap<String, String> moreInfo = createCleanerMoreInfo();
        moreInfo.put("Producer", Document.getVersion());
        moreInfo.put("Author", "Author1");
        moreInfo.put("Title", "Title2");
        moreInfo.put("Subject", "Subject3");
        stamp.setInfoDictionary(moreInfo);

        stamp.setUpdateDocInfo(false);
        stamp.close();

        PdfReader r = new PdfReader(baos.toByteArray());
        Assertions.assertNull(r.getInfo().get("Producer"));
        Assertions.assertNull(r.getInfo().get("Author"));
        Assertions.assertNull(r.getInfo().get("Title"));
        Assertions.assertNull(r.getInfo().get("Subject"));
        r.close();
    }

    @Test
    public void skipInfoUpdateFirstRevisionTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(new File("src/test/resources/HelloWorldMeta.pdf").getAbsolutePath());
        PdfStamper stamp = new PdfStamper(reader, baos, '\0', false);

        HashMap<String, String> moreInfo = createCleanerMoreInfo();
        moreInfo.put("Producer", Document.getVersion());
        moreInfo.put("Author", "Author1");
        moreInfo.put("Title", "Title2");
        moreInfo.put("Subject", "Subject3");
        stamp.setInfoDictionary(moreInfo);

        stamp.setUpdateDocInfo(false);
        stamp.close();

        PdfReader r = new PdfReader(baos.toByteArray());
        Assertions.assertNotNull(r.getInfo().get("Producer"));
        Assertions.assertNotNull(r.getInfo().get("Author"));
        Assertions.assertNotNull(r.getInfo().get("Title"));
        Assertions.assertNotNull(r.getInfo().get("Subject"));
        r.close();
    }

    @Test
    public void testXMPMetadata() throws Exception {
        File file = new File("src/test/resources/HelloWorldMeta.pdf");
        PdfReader reader = new PdfReader(file.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfStamper stamp = new PdfStamper(reader, baos);
        Map<String, String> moreInfo = createCleanerMoreInfo();
        ByteArrayOutputStream meta = new ByteArrayOutputStream();
        XmpWriter writer = new XmpWriter(meta, moreInfo);
        writer.close();
        // Manually set clean metadata
        stamp.setInfoDictionary(moreInfo);
        stamp.setXmpMetadata(meta.toByteArray());

        stamp.close();

        byte[] data = baos.toByteArray();
        PdfReader r = new PdfReader(data);
        Assertions.assertNull(r.getInfo().get("Producer"));
        Assertions.assertNull(r.getInfo().get("Author"));
        Assertions.assertNull(r.getInfo().get("Title"));
        Assertions.assertNull(r.getInfo().get("Subject"));
        byte[] metadata = r.getMetadata();
        r.close();
        String dataString = new String(data);

        Assertions.assertFalse(dataString.contains("Bruno Lowagie"));
        Assertions.assertFalse(dataString.contains(" 1.2.12.SNAPSHOT"));
        if (metadata != null) {
            String metadataString = new String(metadata);
            Assertions.assertFalse(metadataString.contains("Bruno Lowagie"));
            Assertions.assertFalse(metadataString.contains(" 1.2.12.SNAPSHOT"));
            Assertions.assertTrue(metadataString.contains("<pdf:Producer></pdf:Producer>"));
        }
    }

    private byte[] cleanMetadata(File origin) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(origin.getAbsolutePath());
        PdfStamper stamp = new PdfStamper(reader, baos);
        stamp.cleanMetadata();
        stamp.close();
        return baos.toByteArray();
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
