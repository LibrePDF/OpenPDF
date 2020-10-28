package com.lowagie.text.pdf.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.junit.jupiter.api.Test;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;


class PdfTextExtractorTest {

    private static final String LOREM_IPSUM =
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
                    + "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed "
                    + "diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum.";

    @Test
    void testPageExceeded() throws Exception {
        assertThat(getString("HelloWorldMeta.pdf", 5), is(emptyString()));
    }

    @Test
    void testInvalidPageNumber() throws Exception {
        assertThat(getString("HelloWorldMeta.pdf", 0), is(emptyString()));
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
        assertThat(extracted, is("trunked"));
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
        assertThat(extracted, is("Phrase begin. Phrase End."));
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
        assertThat(extracted, equalToCompressingWhiteSpace(expected));
        assertThat(extracted, not(containsString("  ")));
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
        assertThat(extracted, is("One Two Three"));
    }

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

    private String getString(String fileName, int pageNumber) throws Exception {
        URL resource = getClass().getResource("/" + fileName);
        return getString(new File(resource.toURI()), pageNumber);
    }

    private String getString(File file, int pageNumber) throws Exception {
        byte[] pdfBytes = readDocument(file);
        final PdfReader pdfReader = new PdfReader(pdfBytes);

        return new PdfTextExtractor(pdfReader).getTextFromPage(pageNumber);
    }

    protected static byte[] readDocument(final File file) throws IOException {
        try (ByteArrayOutputStream fileBytes = new ByteArrayOutputStream();
                InputStream inputStream = new FileInputStream(file)) {
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
}
