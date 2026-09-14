package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PdfNumberTest {

    /**
     * Some PDF producers write the encryption dictionary's {@code /P} (permissions) entry using its
     * equivalent unsigned 32-bit decimal representation instead of the negative signed value, e.g.
     * {@code 4294965956} instead of {@code -1340}. {@link PdfNumber#intValue()} must truncate such a
     * value to its low-order 32 bits rather than clamping it to {@link Integer#MAX_VALUE}, otherwise
     * the permission bits used to derive the decryption key no longer match the value the producer
     * used, and a document with an empty user password is wrongly reported as requiring a password.
     */
    @Test
    void intValueTruncatesOutOfRangeUnsignedRepresentationInsteadOfClamping() {
        assertEquals(-1340, new PdfNumber("4294965956").intValue());
        assertEquals(-1, new PdfNumber("4294967295").intValue());
        assertEquals(Integer.MIN_VALUE, new PdfNumber(String.valueOf(1L << 31)).intValue());
    }

    @Test
    void intValueRoundTripsValuesWithinIntRange() {
        assertEquals(-1340, new PdfNumber("-1340").intValue());
        assertEquals(0, new PdfNumber("0").intValue());
        assertEquals(Integer.MAX_VALUE, new PdfNumber(String.valueOf(Integer.MAX_VALUE)).intValue());
        assertEquals(Integer.MIN_VALUE, new PdfNumber(String.valueOf(Integer.MIN_VALUE)).intValue());
    }
}
