package com.lowagie.text.pdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

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

        final PdfPTable result = PdfDocument.createInOneCell(mainParagraph.iterator());
        return Arrays.asList(
                DynamicTest.dynamicTest("row size should be 1", new Executable() {
                    @Override
                    public void execute() {
                        assertThat(result.getRows().size(), equalTo(1));
                    }
                }),
                DynamicTest.dynamicTest("cell size should be 1", new Executable() {
                    @Override
                    public void execute() {
                        final PdfPCell[] cells = result.getRows().get(0).getCells();
                        assertThat(cells.length, equalTo(1));
                    }
                }),
                DynamicTest.dynamicTest("elements in cell should be 5", new Executable() {
                    @Override
                    public void execute() {
                        assertThat(PdfDocumentTest.this.getCellElements(result).size(), equalTo(5));
                    }
                }),
                DynamicTest.dynamicTest("element text should be '" + PARAGRAPH_TEXT_1 + "'",
                        new Executable() {
                            @Override
                            public void execute() {
                                assertThat(PdfDocumentTest.this.getCellElements(result).get(0)
                                        .getChunks().toString(), equalTo(paragraph1.toString()));
                            }
                        }),
                DynamicTest.dynamicTest("element should be table", new Executable() {
                    @Override
                    public void execute() {
                        assertThat(PdfDocumentTest.this.getCellElements(result).get(2),
                                Matchers.<Element>equalTo(table));
                    }
                }),
                DynamicTest.dynamicTest("element text should be '" + PARAGRAPH_TEXT_2 + "'",
                        new Executable() {
                            @Override
                            public void execute() {
                                assertThat(PdfDocumentTest.this.getCellElements(result).get(3)
                                        .getChunks().toString(), equalTo(paragraph2.toString()));
                            }
                        }));
    }

    private List<Element> getCellElements(PdfPTable result) {
        PdfPCell firstCell = result.getRows().get(0).getCells()[0];
        return firstCell.getColumn().compositeElements;
    }

}
