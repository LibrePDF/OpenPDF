package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.openpdf.text.Element;
import org.openpdf.text.Paragraph;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DynamicTest;
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

}
