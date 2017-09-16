package com.lowagie.text.pdf;

import java.io.ByteArrayOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import org.junit.Assert;
import org.junit.Test;

public class PdfTestBaseTest {

    @Test
    public void testCreatePdfStream() throws DocumentException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Document pdf = null;
        try {
            pdf = PdfTestBase.createPdf(stream);
            pdf.open();
            pdf.newPage();
            pdf.add(new Paragraph("Hello World!"));
        } finally {
            // close document
            if (pdf != null)
                pdf.close();
        }
        final byte[] bytes = stream.toByteArray();
        Assert.assertNotNull("There should be some PDF-bytes there.", bytes);
        String header = new String(bytes, 0, 5);
        Assert.assertEquals("This is no PDF.", "%PDF-", header);
    }

}