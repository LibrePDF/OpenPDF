package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Anchor;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

/**
 * Paragraphs kept together must accept anchors, see issue #1228.
 */
class KeepTogetherAnchorTest {

    @Test
    void keepTogetherParagraphWithAnchor() throws IOException {
        Anchor anchor = new Anchor("this is a link");
        anchor.setName("LINK");
        anchor.setReference("http://www.example.com");

        Paragraph mainParagraph = new Paragraph();
        mainParagraph.add(new Paragraph("paragraph 1"));
        mainParagraph.add(new Paragraph("paragraph 2"));
        mainParagraph.add(anchor);
        mainParagraph.add(new Paragraph("paragraph 3"));
        mainParagraph.setKeepTogether(true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (Document document = new Document()) {
            PdfWriter.getInstance(document, stream);
            document.open();
            document.add(mainParagraph);
        }

        PdfReader reader = new PdfReader(stream.toByteArray());
        String text = new PdfTextExtractor(reader).getTextFromPage(1);
        assertTrue(text.contains("paragraph 1"), text);
        assertTrue(text.contains("paragraph 2"), text);
        // the anchor is inline text, so it stays on the same line as the following paragraph
        assertTrue(text.contains("this is a linkparagraph 3"), text);

        PdfArray annotations = reader.getPageN(1).getAsArray(PdfName.ANNOTS);
        assertNotNull(annotations, "the anchor's link annotation is missing");
        assertEquals(1, annotations.size());
        PdfDictionary action = annotations.getAsDict(0).getAsDict(PdfName.A);
        assertEquals("http://www.example.com", action.getAsString(PdfName.URI).toString());
        reader.close();
    }
}
