package org.openpdf.text.pdf.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.CMapAwareDocumentFont;
import org.openpdf.text.pdf.PRIndirectReference;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfString;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

/**
 * Tests for issue #1268: every Tf operator in a content stream constructed a fresh
 * {@link CMapAwareDocumentFont}, re-parsing the embedded font program and ToUnicode CMap. A page
 * that selects fonts hundreds of times allocated gigabytes while being processed. The handler now
 * caches parsed fonts by the object number of the font dictionary.
 */
class PdfContentStreamHandlerFontCacheTest {

    @Test
    void fontIsParsedOnlyOncePerReference() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph("Hello font cache"));
        document.close();

        PdfReader reader = new PdfReader(out.toByteArray());
        try {
            PdfDictionary fonts = reader.getPageN(1).getAsDict(PdfName.RESOURCES).getAsDict(PdfName.FONT);
            PRIndirectReference fontRef = (PRIndirectReference) fonts.get(fonts.getKeys().iterator().next());

            PdfContentStreamHandler handler = new PdfContentStreamHandler(new MarkedUpTextAssembler(reader)) {
                @Override
                void popContext() {
                    // not needed for the font cache test
                }

                @Override
                void pushContext(String newContextName) {
                    // not needed for the font cache test
                }

                @Override
                public void reset() {
                    // not needed for the font cache test
                }

                @Override
                void displayPdfString(PdfString string) {
                    // not needed for the font cache test
                }

                @Override
                public String getResultantText() {
                    return "";
                }
            };
            assertSame(handler.getFont(fontRef), handler.getFont(fontRef),
                    "The handler must reuse the parsed font for repeated Tf operators");

            assertEquals("Hello font cache", new PdfTextExtractor(reader).getTextFromPage(1),
                    "Text extraction must still work with the font cache");
        } finally {
            reader.close();
        }
    }
}
