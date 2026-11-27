package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for issue #1585: some PDF producers (e.g. mPDF) write the /P permissions entry of the
 * encryption dictionary as an unsigned 32-bit integer (4294963412) instead of the signed value
 * required by ISO 32000 (-3884). {@link PdfNumber#intValue()} used a direct double-to-int cast,
 * which clamps values above {@code Integer.MAX_VALUE} (JLS 5.1.3) instead of preserving the bit
 * pattern, so the RC4/AES key derivation used a wrong /P and threw BadPasswordException even for
 * documents with an empty user password.
 */
class PdfNumberTest {

    @Test
    void intValueWrapsUnsignedInt32() {
        // 4294963412 == 0xFFFFF0D4 == -3884 as signed int32 (the mPDF example from the issue)
        assertEquals(-3884, new PdfNumber("4294963412").intValue(),
                "Unsigned int32 must wrap to its two's complement value, not clamp to Integer.MAX_VALUE");
        assertEquals(-4, new PdfNumber("4294967292").intValue());
        assertEquals(Integer.MIN_VALUE, new PdfNumber("2147483648").intValue());
    }

    @Test
    void intValueUnchangedForSignedRange() {
        assertEquals(-3884, new PdfNumber("-3884").intValue());
        assertEquals(0, new PdfNumber("0").intValue());
        assertEquals(42, new PdfNumber(42).intValue());
        assertEquals(Integer.MAX_VALUE, new PdfNumber(Integer.MAX_VALUE).intValue());
        assertEquals(Integer.MIN_VALUE, new PdfNumber(Integer.MIN_VALUE).intValue());
        assertEquals(3, new PdfNumber(3.9).intValue(), "Fractional values still truncate toward zero");
        assertEquals(-3, new PdfNumber(-3.9).intValue(), "Fractional values still truncate toward zero");
    }
}
