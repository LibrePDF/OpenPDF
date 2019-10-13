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
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

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
        final HorizontalAlignment[] values = HorizontalAlignment.values();
        List<DynamicTest> tests = new ArrayList<>();
        for (final HorizontalAlignment alignment : values) {
            final DynamicTest dynamicTest = dynamicTest(TEST_TITLE + alignment,
                    new Executable() {
                        @Override
                        public void execute() {
                            table.setHorizontalAlignment(alignment);
                            final int alignmentId = table.getAlignment();
                            assertEquals(alignmentId, alignment.getId());
                        }
                    });
            tests.add(dynamicTest);
        }
        return tests;
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
