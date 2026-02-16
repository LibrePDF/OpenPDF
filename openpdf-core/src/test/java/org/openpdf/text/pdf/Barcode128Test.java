package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class Barcode128Test {

    /**
     * Test that AI(11) Production date has correct length of 8 (2 for AI + 6 for YYMMDD).
     * This validates the fix for issue #1450.
     */
    @Test
    void testAI11ProductionDateLength() {
        Barcode128 barcode = new Barcode128();
        barcode.setCodeType(Barcode128.CODE128_UCC);
        
        // Valid AI(11) with 6 digits for date (YYMMDD format)
        // Total length should be 8: 2 (AI identifier "11") + 6 (date "240315")
        String validCode = "(11)240315";
        barcode.setCode(validCode);
        assertThat(barcode.getCode()).isNotNull();
        
        // Invalid AI(11) with incorrect length should throw exception
        String invalidCode = "(11)2403151"; // 7 digits instead of 6
        assertThrows(IllegalArgumentException.class, () -> {
            Barcode128 invalidBarcode = new Barcode128();
            invalidBarcode.setCodeType(Barcode128.CODE128_UCC);
            invalidBarcode.setCode(invalidCode);
        });
    }

    /**
     * Test that AI(12) Due date also has correct length of 8 (similar to AI(11)).
     */
    @Test
    void testAI12DueDateLength() {
        Barcode128 barcode = new Barcode128();
        barcode.setCodeType(Barcode128.CODE128_UCC);
        
        // Valid AI(12) with 6 digits for date (YYMMDD format)
        String validCode = "(12)240315";
        barcode.setCode(validCode);
        assertThat(barcode.getCode()).isNotNull();
    }
}
