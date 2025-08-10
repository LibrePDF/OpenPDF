package org.openpdf.text.pdf.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.FontSelector;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;


class PdfTextExtractorTest {

    private static final String LOREM_IPSUM =
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
                    + "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed "
                    + "diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum.";

    static byte[] createSimpleDocumentWithElements(Element... elements) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        for (Element element : elements) {
            document.add(element);
        }
        document.close();

        return baos.toByteArray();
    }

    protected static byte[] readDocument(final File file) throws IOException {
        try (ByteArrayOutputStream fileBytes = new ByteArrayOutputStream();
                InputStream inputStream = Files.newInputStream(file.toPath())) {
            final byte[] buffer = new byte[8192];
            while (true) {
                final int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                fileBytes.write(buffer, 0, bytesRead);
            }
            return fileBytes.toByteArray();
        }
    }

    @Test
    void testPageExceeded() throws Exception {
        assertEquals("", getString("HelloWorldMeta.pdf", 5));
    }

    @Test
    void testInvalidPageNumber() throws Exception {
        assertEquals("", getString("HelloWorldMeta.pdf", 0));
    }

    @Test
    void testZapfDingbatsFont() throws Exception {
        Document document = new Document();
        Document.compress = false;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();

        //There has some problem to write Greek with Font.ZAPFDINGBATS, but show in html it is "✧❒❅❅❋"
        document.add(new Chunk("Greek", new Font(Font.ZAPFDINGBATS)));
        document.close();
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(new PdfReader(byteArrayOutputStream.toByteArray()));
        assertEquals("✧❒❅❅❋", pdfTextExtractor.getTextFromPage(1));
        Document.compress = true;
    }

    @Test
    void testSymbolFont() throws Exception {
        Document document = new Document();
        Document.compress = false;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();

        FontSelector selector = new FontSelector();
        selector.addFont(new Font(Font.SYMBOL));
        document.add(selector.process("ετε"));
        document.close();
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(new PdfReader(byteArrayOutputStream.toByteArray()));
        assertEquals("ετε", pdfTextExtractor.getTextFromPage(1));
        Document.compress = true;
    }

    @Test
    void testConcatenateWatermark() throws Exception {
        String result = getString("merge-acroforms.pdf", 5);
        assertNotNull(result);
        // html??
        result = result.replaceAll("<.*?>", "");
        // Multiple spaces between words??
        assertTrue(result.contains("2. This is chapter 2"));
        assertTrue(result.contains("watermark-concatenate"));
    }

    @Test
    void whenTrunkedWordsInChunks_expectsFullWordAsExtraction() throws IOException {
        // given
        byte[] pdfBytes = createSimpleDocumentWithElements(
                new Chunk("trun"),
                new Chunk("ked"));
        // when
        final String extracted = new PdfTextExtractor(new PdfReader(pdfBytes)).getTextFromPage(1);
        // then
        assertEquals("trunked", extracted);
    }

    @Test
    void getTextFromPageWithPhrases_expectsNoAddedSpace() throws IOException {
        // given
        byte[] pdfBytes = createSimpleDocumentWithElements(
                new Phrase("Phrase begin. "),
                new Phrase("Phrase End.")
        );
        // when
        final String extracted = new PdfTextExtractor(new PdfReader(pdfBytes)).getTextFromPage(1);
        // then
        assertEquals("Phrase begin. Phrase End.", extracted);
    }

    @Test
    void getTextFromPageWithParagraphs_expectsTextHasNoMultipleSpaces() throws IOException {
        // given
        final Paragraph loremIpsumParagraph = new Paragraph(LOREM_IPSUM);
        loremIpsumParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
        byte[] pdfBytes = createSimpleDocumentWithElements(
                loremIpsumParagraph,
                Chunk.NEWLINE,
                loremIpsumParagraph
        );
        final String expected = LOREM_IPSUM + " " + LOREM_IPSUM;
        // when
        final String extracted = new PdfTextExtractor(new PdfReader(pdfBytes)).getTextFromPage(1);
        // then
        assertFalse(extracted.contains("  "));
        // ignore all white spaces
        assertEquals(expected.replaceAll("\\s+", ""), extracted.replaceAll("\\s+", ""));
    }

    @Test
    void getTextFromPageInTablesWithSingleWords_expectsWordsAreSeparatedBySpaces()
            throws IOException {
        // given
        PdfPTable table = new PdfPTable(3);
        table.addCell("One");
        table.addCell("Two");
        table.addCell("Three");
        byte[] pdfBytes = createSimpleDocumentWithElements(table);
        // when
        final String extracted = new PdfTextExtractor(new PdfReader(pdfBytes)).getTextFromPage(1);
        // then
        assertEquals("One Two Three", extracted);
    }

    private String getString(String fileName, int pageNumber) throws Exception {
        URL resource = getClass().getResource("/" + fileName);
        return getString(new File(resource.toURI()), pageNumber);
    }

    private String getString(File file, int pageNumber) throws Exception {
        byte[] pdfBytes = readDocument(file);
        final PdfReader pdfReader = new PdfReader(pdfBytes);

        return new PdfTextExtractor(pdfReader).getTextFromPage(pageNumber);
    }
}
