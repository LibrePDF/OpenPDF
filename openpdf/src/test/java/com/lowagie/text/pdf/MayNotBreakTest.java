package com.lowagie.text.pdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MayNotBreakTest {
    @Test
    void shouldReturnWhetherBreak() throws Exception {
        PdfPCell[] cells = new PdfPCell[3];
        PdfPRow row = new PdfPRow(cells);
        row.setMayNotBreak(true);
        assertTrue(row.isMayNotBreak());
    }
}
