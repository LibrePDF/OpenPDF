package com.lowagie.text.pdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
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
                DynamicTest.dynamicTest("row size should be 1", () -> assertThat(result.getRows().size(), equalTo(1))),
                DynamicTest.dynamicTest("cell size should be 1", () -> {
                    final PdfPCell[] cells = result.getRows().get(0).getCells();
                    assertThat(cells.length, equalTo(1));
                }),
                DynamicTest.dynamicTest("elements in cell should be 5",
                        () -> assertThat(getCellElements(result).size(), equalTo(5))),
                DynamicTest.dynamicTest("element text should be '" + PARAGRAPH_TEXT_1 + "'",
                        () -> assertThat(getCellElements(result).get(0).getChunks().toString(),
                                equalTo(paragraph1.toString()))),
                DynamicTest.dynamicTest("element should be table",
                        () -> assertThat(getCellElements(result).get(2), equalTo(table))),
                DynamicTest.dynamicTest("element text should be '" + PARAGRAPH_TEXT_2 + "'",
                        () -> assertThat(getCellElements(result).get(3).getChunks().toString(),
                                equalTo(paragraph2.toString()))));
    }

    private List<Element> getCellElements(PdfPTable result) {
        PdfPCell firstCell = result.getRows().get(0).getCells()[0];
        return firstCell.getColumn().compositeElements;
    }

}
