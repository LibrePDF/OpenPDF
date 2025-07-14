package com.lowagie.text.pdf.metadata;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProducerTest {

    private static final String PRODUCER = "Producer";

    @Test
    public void changeProducerLineTest() throws IOException {
        String expected = "New Producer.";

        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.addProducer(expected);
        document.open();
        document.add(new Paragraph("Hello World!"));
        document.close();

        byte[] pdfBytes = baos.toByteArray();
        baos.close();

        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));

        Map<String, String> infoDictionary = reader.getInfo();
        String actual = infoDictionary.get(PRODUCER);

        Assertions.assertEquals(expected, actual);

        reader.close();
    }

    @Test
    public void testMetadataProducerStamperIssue254() throws IOException {
        File origin = new File("src/test/resources/pdf_form_metadata_issue_254.pdf");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(origin.getAbsolutePath());
        PdfStamper stamp = new PdfStamper(reader, baos);
        stamp.close();
        String sData = baos.toString();
        Assertions.assertTrue(sData.contains("(LibreOffice 6.0; modified using OpenPDF"));


    }
}