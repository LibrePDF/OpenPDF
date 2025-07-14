package org.openpdf.text.pdf;

import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TabTest {

    @Test
    void TabTest1() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String stringWithTab = "data\ttable";
        try (Document document
                = new Document(PageSize.A4.rotate(), 10, 10, 10, 10)) {
            Document.compress = false;
            PdfWriter.getInstance(document, stream);
            document.open();
            Chunk a = new Chunk(stringWithTab);
            document.add(a);
        }
        PdfReader rd = new PdfReader(stream.toByteArray());
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(rd);
        Assertions.assertEquals(stringWithTab, pdfTextExtractor.getTextFromPage(1));
        Document.compress = true;
    }
}
