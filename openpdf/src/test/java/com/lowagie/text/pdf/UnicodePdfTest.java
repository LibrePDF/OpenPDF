package com.lowagie.text.pdf;

import java.io.IOException;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.StandardFonts;
import org.junit.jupiter.api.Test;

class UnicodePdfTest {

    private static final String INPUT = "Symbol: '\u25b2' Latin: 'äöüÄÖÜß'";

    @Test
    void testSimplePdf() throws IOException {
        // Probably a good idea to write the document to a byte array, so you can read the result and make some checks.
        // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // create document
        Document document = PdfTestBase.createPdf("target/unicode.pdf");
        // new page with a rectangle
        document.open();
        document.add(new Paragraph(INPUT, StandardFonts.LIBERATION_SANS.create()));
        document.close();
    }
}
