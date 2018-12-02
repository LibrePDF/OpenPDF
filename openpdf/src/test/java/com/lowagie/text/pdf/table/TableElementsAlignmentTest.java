package com.lowagie.text.pdf.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.lowagie.text.Cell;
import com.lowagie.text.Row;
import com.lowagie.text.Table;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.alignment.VerticalAlignment;
import com.lowagie.text.alignment.WithHorizontalAlignment;
import com.lowagie.text.alignment.WithVerticalAlignment;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests for setting alignment through {@link WithHorizontalAlignment} and {@link WithVerticalAlignment} interfaces.
 * Testing classes: {@link Table} and {@link Cell}. {@link Row} cannot be tested because of its package-private access.
 *
 * @author noavarice
 */
public class TableElementsAlignmentTest {

    private static final String TEST_TITLE = "Testing alignment=";

    @TestFactory
    Iterable<DynamicTest> testSettingTableAlignment() {
        final Table table = new Table(1);
        return Arrays.stream(HorizontalAlignment.values())
            .map(alignment -> dynamicTest(TEST_TITLE + alignment, () -> {
                table.setHorizontalAlignment(alignment);
                final int alignmentId = table.getAlignment();
                assertEquals(alignmentId, alignment.getId());
            }))
            .collect(Collectors.toList());
    }

    @Test
    void testSettingCellHorizontalAlignment() {
        final Cell cell = new Cell();
        for (final HorizontalAlignment alignment: HorizontalAlignment.values()) {
            cell.setHorizontalAlignment(alignment);
            final int alignmentId = cell.getHorizontalAlignment();
            assertEquals(alignmentId, alignment.getId());
        }
    }

    @Test
    void testSettingCellVerticalAlignment() {
        final Cell cell = new Cell();
        for (final VerticalAlignment alignment: VerticalAlignment.values()) {
            cell.setVerticalAlignment(alignment);
            final int alignmentId = cell.getVerticalAlignment();
            assertEquals(alignmentId, alignment.getId());
        }
    }
}
