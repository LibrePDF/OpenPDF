package com.lowagie.text.pdf;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.lowagie.text.Annotation;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;

public class SimplePdfTest {

    @Test
    void testSimplePdf() throws IOException, DocumentException {
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
}
