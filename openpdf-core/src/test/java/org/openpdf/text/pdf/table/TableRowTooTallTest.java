package org.openpdf.text.pdf.table;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.PageSize;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

/**
 * Tests for issue #1163: adding a table with a row that can never fit on a page (e.g. a cell with
 * a fixed height larger than the page) silently wrote the text "ERROR: Infinite table loop" into
 * the generated document instead of failing. It now throws a descriptive DocumentException.
 */
class TableRowTooTallTest {

    @Test
    void rowTallerThanPageThrowsInsteadOfWritingErrorText() {
        assertTimeoutPreemptively(ofSeconds(10), () -> {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new ByteArrayOutputStream());
            document.open();

            PdfPTable table = new PdfPTable(1);
            table.setTotalWidth(new float[]{100});
            table.setLockedWidth(true);
            PdfPCell cell = new PdfPCell(new Phrase("This row can never fit on a page"));
            cell.setFixedHeight(PageSize.A4.getHeight() + 100);
            table.addCell(cell);

            DocumentException e = assertThrows(DocumentException.class, () -> document.add(table),
                    "A table row that can never fit must fail instead of corrupting the document");
            assertNotNull(e.getMessage());
        });
    }

    @Test
    void fittingTableIsUnaffected() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        PdfPTable table = new PdfPTable(1);
        table.setTotalWidth(new float[]{100});
        table.setLockedWidth(true);
        PdfPCell cell = new PdfPCell(new Phrase("A perfectly reasonable row"));
        cell.setFixedHeight(100);
        table.addCell(cell);
        document.add(table);
        document.close();

        PdfReader reader = new PdfReader(out.toByteArray());
        try {
            String text = new PdfTextExtractor(reader).getTextFromPage(1);
            assertFalse(text.contains("Infinite table loop"), "No error text may leak into the document");
        } finally {
            reader.close();
        }
    }
}
