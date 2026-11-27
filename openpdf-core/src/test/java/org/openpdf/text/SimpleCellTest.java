package org.openpdf.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfPCell;

class SimpleCellTest {

    @Test
    void createPdfPCellAppliesDefaultZeroForNaNSpacingAndPadding() {
        SimpleCell row = new SimpleCell(SimpleCell.ROW);
        SimpleCell cell = new SimpleCell(SimpleCell.CELL);
        cell.setPadding_left(5f);
        cell.setSpacing_left(3f);
        cell.setPadding(2f);

        PdfPCell pdfPCell = cell.createPdfPCell(row);

        assertThat(pdfPCell.getPaddingLeft()).isEqualTo(8f);
        assertThat(pdfPCell.getPaddingRight()).isEqualTo(2f);
        assertThat(pdfPCell.getPaddingTop()).isEqualTo(2f);
        assertThat(pdfPCell.getPaddingBottom()).isEqualTo(2f);
    }

    @Test
    void addElementAcceptsValidContentAndRejectsInvalidElement() throws BadElementException {
        SimpleCell cell = new SimpleCell(SimpleCell.CELL);
        cell.addElement(new Paragraph("valid paragraph"));

        assertThat(cell.getContent()).hasSize(1);
        Table invalidElement = new Table(2);
        assertThatThrownBy(() -> cell.addElement(invalidElement))
                .isInstanceOf(BadElementException.class);
    }
}
