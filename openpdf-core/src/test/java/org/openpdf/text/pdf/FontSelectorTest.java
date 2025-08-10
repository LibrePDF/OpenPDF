package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class FontSelectorTest {

    public static final String STRING_TO_CHECK = "Some Χαίρετε Greek -";

    @Test
    void testDefaultFont() throws IOException {
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, stream);
        writer.setRunDirection(PdfWriter.RUN_DIRECTION_LTR);
        document.open();

        FontSelector selector = new FontSelector();
        selector.addFont(new Font(Font.HELVETICA));
        document.add(selector.process(STRING_TO_CHECK));
        document.close();

        PdfReader rd = new PdfReader(stream.toByteArray());
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(rd);
        assertEquals(STRING_TO_CHECK, pdfTextExtractor.getTextFromPage(1));
    }
}
