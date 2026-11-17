package org.openpdf.text.pdf.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PRStream;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;

/**
 * A page's content may be an array of streams, split between any two tokens. They are read as one stream in which
 * the last token of a stream and the first token of the next stay apart.
 */
class PdfTextExtractorContentsArrayTest {

    /** A one-page document whose content is replaced by the given streams. */
    private static PdfReader readerWithContents(String... streams) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph("Placeholder"));
        document.close();

        PdfReader reader = new PdfReader(out.toByteArray());
        PdfDictionary page = reader.getPageN(1);
        PdfArray contents = new PdfArray();
        for (String stream : streams) {
            contents.add(reader.addPdfObject(
                    new PRStream(reader, stream.replace("/Font", fontName(page)).getBytes(StandardCharsets.ISO_8859_1))));
        }
        page.put(PdfName.CONTENTS, contents);
        return reader;
    }

    /** The name the page's resources give its one font, as it is written in content. */
    private static String fontName(PdfDictionary page) {
        PdfDictionary fonts = page.getAsDict(PdfName.RESOURCES).getAsDict(PdfName.FONT);
        return fonts.getKeys().iterator().next().toString();
    }

    @Test
    void streamsSplitBetweenTwoOperators() throws Exception {
        try (PdfReader reader = readerWithContents(
                "BT /Font 12 Tf 14 TL 50 700 Td (Hello) Tj",
                "T* (World) Tj ET")) {

            assertThat(new PdfTextExtractor(reader).getTextFromPage(1)).containsSubsequence("Hello", "World");
        }
    }

    @Test
    void streamsSplitAfterSavingTheGraphicsState() throws Exception {
        try (PdfReader reader = readerWithContents(
                "q",
                "BT /Font 12 Tf 50 700 Td (Hello) Tj ET",
                "Q")) {

            assertThat(new PdfTextExtractor(reader).getTextFromPage(1)).isEqualTo("Hello");
        }
    }

    @Test
    void textLocatorReadsTheSameStreams() throws Exception {
        try (PdfReader reader = readerWithContents(
                "BT /Font 12 Tf 14 TL 50 700 Td (Hello) Tj",
                "T* (World) Tj ET")) {

            assertThat(new String(
                    PdfContentStreamHandler.getContentBytesFromPdfObjectStatic(reader.getPageN(1).get(PdfName.CONTENTS)),
                    StandardCharsets.ISO_8859_1)).contains("(Hello) Tj\nT* (World)");
        }
    }
}
