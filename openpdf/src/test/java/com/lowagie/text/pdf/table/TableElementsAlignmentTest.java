package com.lowagie.text.pdf.table;

import com.lowagie.text.Cell;
import com.lowagie.text.Row;
import com.lowagie.text.Table;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.alignment.VerticalAlignment;
import com.lowagie.text.alignment.WithHorizontalAlignment;
import com.lowagie.text.alignment.WithVerticalAlignment;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for setting alignment through {@link WithHorizontalAlignment} and {@link WithVerticalAlignment} interfaces.
 * Testing classes: {@link Table} and {@link Cell}. {@link Row} cannot be tested because of its package-private access.
 *
 * @author noavarice
 */
public class TableElementsAlignmentTest {

    @Test
    public void testSettingTableAlignment() {
        final Table table = new Table(1);
        for (final HorizontalAlignment alignment: HorizontalAlignment.values()) {
            table.setHorizontalAlignment(alignment);
            final int alignmentId = table.getAlignment();
            assertEquals(alignmentId, alignment.getId());
        }
    }

    @Test
    public void testSettingCellHorizontalAlignment() {
        final Cell cell = new Cell();
        for (final HorizontalAlignment alignment: HorizontalAlignment.values()) {
            cell.setHorizontalAlignment(alignment);
            final int alignmentId = cell.getHorizontalAlignment();
            assertEquals(alignmentId, alignment.getId());
        }
    }

    @Test
    public void testSettingCellVerticalAlignment() {
        final Cell cell = new Cell();
        for (final VerticalAlignment alignment: VerticalAlignment.values()) {
            cell.setVerticalAlignment(alignment);
            final int alignmentId = cell.getVerticalAlignment();
            assertEquals(alignmentId, alignment.getId());
        }
    }
}
