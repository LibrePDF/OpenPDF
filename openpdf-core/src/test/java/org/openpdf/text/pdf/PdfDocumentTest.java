package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.HeaderFooter;
import org.openpdf.text.ListItem;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;

import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

class PdfDocumentTest {

    private static final String PARAGRAPH_TEXT_1 = "Text above table";

    private static final String PARAGRAPH_TEXT_2 = "Text below table";

    @TestFactory
    List<DynamicTest> testCreateWithAllElementsInOneCell() {
        final PdfPTable table = new PdfPTable(2);
        table.addCell("Lorem");
        table.addCell("ipsum");
        table.addCell("dolor");
        table.addCell("sit");

        final Paragraph mainParagraph = new Paragraph();
        final Paragraph paragraph1 = new Paragraph(PARAGRAPH_TEXT_1);
        final Paragraph paragraph2 = new Paragraph(PARAGRAPH_TEXT_2);

        mainParagraph.add(paragraph1);
        mainParagraph.add(table);
        mainParagraph.add(paragraph2);

        PdfPTable result = PdfDocument.createInOneCell(mainParagraph);
        return Arrays.asList(
                DynamicTest.dynamicTest("row size should be 1",
                        () -> assertEquals(1, result.getRows().size())),
                DynamicTest.dynamicTest("cell size should be 1", () -> {
                    final PdfPCell[] cells = result.getRows().get(0).getCells();
                    assertEquals(1, cells.length);
                }),
                DynamicTest.dynamicTest("elements in cell should be 5",
                        () -> assertEquals(5, getCellElements(result).size())),
                DynamicTest.dynamicTest("element text should be '" + PARAGRAPH_TEXT_1 + "'",
                        () -> assertEquals(paragraph1.toString(),
                                getCellElements(result).get(0).getChunks().toString())),
                DynamicTest.dynamicTest("element should be table",
                        () -> assertEquals(table, getCellElements(result).get(2))),
                DynamicTest.dynamicTest("element text should be '" + PARAGRAPH_TEXT_2 + "'",
                        () -> assertEquals(paragraph2.toString(),
                                getCellElements(result).get(3).getChunks().toString()
                                )));
    }

    private List<Element> getCellElements(PdfPTable result) {
        PdfPCell firstCell = result.getRows().get(0).getCells()[0];
        return firstCell.getColumn().compositeElements;
    }

    @Test
    void createPdfFileWithAutoPageBreak() throws Exception {
        Path output = Path.of("openpdf-test.pdf");
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(
                document,
                new FileOutputStream(output.toFile())
        );
        document.setHeader(new HeaderFooter(false, new Phrase("Header")));
        document.setFooter(new HeaderFooter(false, new Phrase("Footer")));
        document.open();
        Font font = new Font(Font.HELVETICA, 12);

        for (int i = 0; i < 50; i++) {
            if (i == 37) {
                document.newPage();
            }
            var pdf = writer.getPdfDocument();
            var headerFielt = PdfDocument.class.getDeclaredField("text");
            headerFielt.setAccessible(true);
            var text = (PdfContentByte) headerFielt.get(pdf);
            assertTrue(String.valueOf(text.getInternalBuffer()).contains("Header"),
                    "Header not found: %d".formatted(i));
            assertTrue(String.valueOf(text.getInternalBuffer()).contains("Footer"),
                    "Footer not found: %d".formatted(i));
            document.add(new Paragraph(
                    "This is line " + i + " of a long text to force automatic page breaks.",
                    font
            ));
        }
        document.close();
    }

    @Test
    void listItemWithKeepTogetherShouldMoveToNextPage() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A6, 36, 36, 36, 36);
        PdfWriter.getInstance(document, output);

        document.open();

        // Fill most of the first page.
        Paragraph filler = new Paragraph(
                String.join("\n", java.util.Collections.nCopies(23, "Filler line")));
        filler.setLeading(12f);
        document.add(filler);

        // Create a ListItem that cannot fit in the remaining space,
        // but can fit completely on a fresh page.
        String itemText = "KEEP_TOGETHER_START "
                + String.join(" ", java.util.Collections.nCopies(40, "KeepTogetherToken"))
                + " KEEP_TOGETHER_END";

        ListItem listItem = new ListItem(itemText);
        listItem.setLeading(12f);
        listItem.setKeepTogether(true);

        document.add(listItem);
        document.close();

        PdfReader reader = new PdfReader(output.toByteArray());

        try {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);

            String page1Text = extractor.getTextFromPage(1);
            String page2Text = extractor.getTextFromPage(2);

            // The PDF should contain exactly two pages.
            assertEquals(2, reader.getNumberOfPages());

            // The ListItem must not start on the first page.
            assertTrue(
                    !page1Text.contains("KEEP_TOGETHER_START"),
                    "The ListItem should not start on the first page"
            );

            // The complete ListItem must appear on the second page.
            assertTrue(
                    page2Text.contains("KEEP_TOGETHER_START"),
                    "The beginning of the ListItem should appear on the second page"
            );

            assertTrue(
                    page2Text.contains("KEEP_TOGETHER_END"),
                    "The end of the ListItem should appear on the second page"
            );
        } finally {
            reader.close();
        }
    }


    @Test
    void listItemWithoutKeepTogetherShouldRetainExistingPageBreakingBehavior() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A6, 36, 36, 36, 36);
        PdfWriter.getInstance(document, output);

        document.open();

        // Fill most of the first page.
        Paragraph filler = new Paragraph(
                String.join("\n", java.util.Collections.nCopies(23, "Filler line")));
        filler.setLeading(12f);
        document.add(filler);

        // This ListItem deliberately does not enable KeepTogether.
        String itemText = "NO_KEEP_TOGETHER_START "
                + String.join(" ", java.util.Collections.nCopies(40, "NormalItemToken"))
                + " NO_KEEP_TOGETHER_END";

        ListItem listItem = new ListItem(itemText);
        listItem.setLeading(12f);
        listItem.setKeepTogether(false);

        document.add(listItem);
        document.close();

        PdfReader reader = new PdfReader(output.toByteArray());

        try {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);

            String page1Text = extractor.getTextFromPage(1);
            String page2Text = extractor.getTextFromPage(2);

            // The item should begin on the first page when KeepTogether is disabled.
            assertTrue(
                    page1Text.contains("NO_KEEP_TOGETHER_START"),
                    "The ListItem should start on the first page when KeepTogether is disabled"
            );

            // The item should continue onto the second page.
            assertTrue(
                    page2Text.contains("NO_KEEP_TOGETHER_END"),
                    "The ListItem should continue onto the second page"
            );
        } finally {
            reader.close();
        }
    }


}
