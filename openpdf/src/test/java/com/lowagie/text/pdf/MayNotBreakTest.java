package com.lowagie.text.pdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author  SE_SUSTech, group: Lanrand
 * test issue #659
 * <p>This file is to test the code for add the enhancement in the issue #659
 * and there are 2 test cases
 */
public class MayNotBreakTest {//NOPMD

    /**
     * It is test the feature of mayNotBreak
     * It will test when we set it True
     */
    @Test
    public static void shouldReturnWhetherBreak1(){
        final PdfPCell[] cells = new PdfPCell[2];
        final PdfPRow row = new PdfPRow(cells);
        row.setMayNotBreak(true);
        assertTrue(row.isMayNotBreak(),"It's cannot break");
    }

    /**
     * It is test the feature of mayNotBreak
     * It will test when we set it False
     */
    @Test
    public static void shouldReturnWhetherBreak2() {
        final PdfPCell[] cells = new PdfPCell[3];
        final PdfPRow row = new PdfPRow(cells);
        row.setMayNotBreak(false);
        assertFalse(row.isMayNotBreak(),"It's can break");
    }


}
