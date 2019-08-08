package com.lowagie.text.pdf;

import java.io.File;
import java.io.FileOutputStream;

import com.lowagie.text.Annotation;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import org.junit.jupiter.api.Test;

public class SimplePdfTest {

    @Test
    void testSimplePdf() throws Exception {
        // create document
        Document document = PdfTestBase.createPdf("testSimplePdf.pdf");
        try {
            // new page with a rectangle
            document.open();
            document.newPage();
            Annotation ann = new Annotation("Title", "Text");
            Rectangle rect = new Rectangle(100, 100);
            document.add(ann);
            document.add(rect);
        } finally {
            // close document
            if (document != null)
                document.close();
        }

    }

    @Test
    void testTryWithResources() throws Exception {
        try (PdfReader reader = new PdfReader("./src/test/resources/HelloWorldMeta.pdf");
            Document document = new Document();
            FileOutputStream os = new FileOutputStream(File.createTempFile("temp-file-name", ".pdf"));
            PdfWriter writer = PdfWriter.getInstance(document, os)
        ) {
            document.open();
            final PdfContentByte cb = writer.getDirectContent();

            document.newPage();
            PdfImportedPage page = writer.getImportedPage(reader, 1);
            cb.addTemplate(page, 1, 0, 0, 1, 0, 0);
        }
    }
}
