package com.lowagie.text.pdf.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lowagie.text.pdf.PdfReader;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * Regression test for a document that has no user password but is nevertheless reported by
 * {@link PdfReader} as requiring one, because its {@code /Encrypt} dictionary writes the {@code /P}
 * (permissions) entry using the unsigned 32-bit decimal representation of a negative value (e.g.
 * {@code 4294965956} instead of {@code -1340}) - a convention some PDF producers use.
 * <p>
 * {@link com.lowagie.text.pdf.PdfNumber#intValue()} used a plain {@code (int)} narrowing cast on the
 * underlying <code>double</code>, which clamps such an out-of-range value to
 * {@link Integer#MAX_VALUE} instead of truncating it to its low-order 32 bits. The corrupted
 * permission value then fed into the standard security handler's key derivation, producing a user key
 * that did not match {@code /U}, so {@link PdfReader} threw a {@link com.lowagie.text.exceptions.BadPasswordException}
 * even though the correct (empty) password was supplied.
 */
class DecryptUnsignedPermissionsTest {

    @Test
    void opensWithEmptyPasswordDespiteUnsignedPermissionsValue() throws IOException {
        try (InputStream resource = getClass()
                .getResourceAsStream("/permissions/empty-user-password-unsigned-p-value.pdf")) {
            PdfReader pdfReader = new PdfReader(resource);
            assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            assertFalse(pdfReader.isOwnerPasswordUsed(), "PdfReader fails to report limited permissions.");
            assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            pdfReader.close();
        }
    }
}
