package org.librepdf.openpdf.fonts;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class UnicodePdfTest {

    private static final String INPUT = "Symbol: '\u25b2' Latin: 'äöüÄÖÜß'";

    @Test
    void testSimplePdf() throws IOException {
        // Probably a good idea to write the document to a byte array, so you can read the result and make some checks.
        // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // create document
        Document document = FontsTestUtil.createPdf("target/unicode.pdf");
        // new page with a rectangle
        document.open();
        document.add(new Paragraph(INPUT, Liberation.SANS.create()));
        document.close();
    }
}
