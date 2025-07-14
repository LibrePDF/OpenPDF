package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class PdfDocument536Test {

    @Test
    void whenSkipFirstHeader_thenHeaderIsNotPresentOnFirstPage() throws IOException {
        // given
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        Document.compress = false;
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        document.open();

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSplitLate(false);
        table.setHeaderRows(3);
        table.setSkipFirstHeader(true);
        addCell(table, "HEADER 1");
        addCell(table, "HEADER 2");
        addCell(table, "HEADER 3");

        for (int i = 0; i < 100; i++) {
            addCell(table, "ROW " + i);
        }
        document.add(table);
        document.close();
        // when
        final PdfReader reader = new PdfReader(pdfOut.toByteArray());
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        // then
        assertThat(extractor.getTextFromPage(1)).as("First Page")
                .doesNotContain("HEADER 1", "HEADER 2", "HEADER 3");
        assertThat(extractor.getTextFromPage(2)).as("SecondPage")
                .contains("HEADER 1", "HEADER 2", "HEADER 3");
        Document.compress = true;
    }

    private void addCell(PdfPTable table, String cellText) {
        PdfPCell cell;
        cell = new PdfPCell(new Phrase(cellText));
        cell.setBorder(Rectangle.BOX);
        table.addCell(cell);
    }
}
