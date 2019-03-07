package com.lowagie.text.pdf;

import java.io.ByteArrayOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PdfTestBaseTest {

    @Test
    void testCreatePdfStream() throws DocumentException {
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
        Assertions.assertNotNull(bytes, "There should be some PDF-bytes there.");
        String header = new String(bytes, 0, 5);
        Assertions.assertEquals(header, "%PDF-", "This is no PDF.");
    }

}