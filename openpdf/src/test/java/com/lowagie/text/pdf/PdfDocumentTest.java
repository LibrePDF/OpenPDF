package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

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
}
