package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lowagie.text.DocumentException;
import com.lowagie.text.error_messages.MessageLocalization;
import java.io.IOException;
import org.junit.jupiter.api.Test;

// Deprecated: use org.openpdf package (openpdf-core-modern)
@Deprecated
class PdfPTableTest {

    @Test
    void whenAddCellWithSelf_shouldThrowException() {
        PdfPTable table = new PdfPTable(1);
        assertThatThrownBy(() -> table.addCell(table))
                .isInstanceOf(DocumentException.class)
                .hasMessage("Unable to add self to table contents.");
    }

    @Test
    void whenAddCellWithSelf_shouldThrowException_pt() throws IOException {
        MessageLocalization.setLanguage("pt", null);
        PdfPTable table = new PdfPTable(1);
        assertThatThrownBy(() -> table.addCell(table))
                .isInstanceOf(DocumentException.class)
                .hasMessage("Não é possível adicionar a si mesmo ao conteúdo da tabela.");
        MessageLocalization.setLanguage("en", null);
    }


}