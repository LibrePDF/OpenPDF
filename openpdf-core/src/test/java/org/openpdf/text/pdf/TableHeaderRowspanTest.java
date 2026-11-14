package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

/**
 * A cell in the header rows whose rowspan reaches into the body must not be split at the end of the header rows, see
 * issue #1529.
 */
class TableHeaderRowspanTest {

    @ParameterizedTest
    @ValueSource(strings = {"rowspan", "rowspanrowspanrowspanrowspanrowspanrowspanrowspanrowspanrowspanrowspan"})
    void headerRowWithRowspanIntoBodyIsDrawnAsWithoutHeader(String text) throws IOException {
        byte[] withHeader = createTable(1, text);
        byte[] withoutHeader = createTable(0, text);

        try (PdfReader readerWithHeader = new PdfReader(withHeader);
                PdfReader readerWithoutHeader = new PdfReader(withoutHeader)) {
            String extracted = new PdfTextExtractor(readerWithHeader).getTextFromPage(1).replaceAll("\\s", "");
            assertTrue(extracted.contains(text), extracted);
            // on the first page, the header rows directly precede the body, so they must be drawn the same way
            assertArrayEquals(readerWithoutHeader.getPageContent(1), readerWithHeader.getPageContent(1));
        }
    }

    private static byte[] createTable(int headerRows, String text) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4, 10, 10, 10, 10)) {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            PdfPTable table = new PdfPTable(new float[] {50f, 50f});
            table.setTotalWidth(document.getPageSize().getWidth() - 20);
            table.setLockedWidth(true);
            table.setHeaderRows(headerRows);
            PdfPCell cell = new PdfPCell();
            cell.setRowspan(2);
            cell.addElement(new Chunk(text));
            table.addCell(cell);
            table.addCell("row1");
            table.addCell("row2");
            document.add(table);
        }
        return outputStream.toByteArray();
    }
}
