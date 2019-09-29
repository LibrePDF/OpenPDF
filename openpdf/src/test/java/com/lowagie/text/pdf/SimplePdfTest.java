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
        Document document = PdfTestBase.createTempPdf("testSimplePdf.pdf");
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
    void testTryWithResources_with_os_before_doc() throws Exception {
        try (PdfReader reader = new PdfReader("./src/test/resources/HelloWorldMeta.pdf");
            FileOutputStream os = new FileOutputStream(File.createTempFile("temp-file-name", ".pdf"));
             Document document = new Document()
        ) {
            PdfWriter writer = PdfWriter.getInstance(document, os);
            document.open();
            final PdfContentByte cb = writer.getDirectContent();

            document.newPage();
            PdfImportedPage page = writer.getImportedPage(reader, 1);
            cb.addTemplate(page, 1, 0, 0, 1, 0, 0);
        }
    }

    @Test
    void testTryWithResources_with_unknown_os() throws Exception {
        try (PdfReader reader = new PdfReader("./src/test/resources/HelloWorldMeta.pdf");
             Document document = new Document()
        ) {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(File.createTempFile("temp-file-name", ".pdf")));
            document.open();
            final PdfContentByte cb = writer.getDirectContent();

            document.newPage();
            PdfImportedPage page = writer.getImportedPage(reader, 1);
            cb.addTemplate(page, 1, 0, 0, 1, 0, 0);
        }
    }
}
