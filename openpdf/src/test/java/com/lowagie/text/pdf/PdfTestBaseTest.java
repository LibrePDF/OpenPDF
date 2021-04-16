package com.lowagie.text.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;

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
    
    @Test
    void nullpointerExceptionTest() {
        //given when
        Assertions.assertDoesNotThrow(this::pdfCopyTest);
    }

    private void pdfCopyTest() throws IOException {
        InputStream stream = getClass().getResourceAsStream("/openpdf_bug_test.pdf");

        PdfReader reader = new PdfReader(stream);

        byte[] bytes;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfCopyFields pdfCopyFields = new PdfCopyFields(baos);

            pdfCopyFields.addDocument(reader, "1"); // <-- just the table of contents

            pdfCopyFields.close(); // <-- bang

            baos.flush();

            bytes = baos.toByteArray();
        }

        try (OutputStream os = new FileOutputStream(new File("output.pdf"))) {
            os.write(bytes);
        }
    }

}
