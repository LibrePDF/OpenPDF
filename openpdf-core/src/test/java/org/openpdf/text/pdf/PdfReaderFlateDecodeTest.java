package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import org.junit.jupiter.api.Test;

/**
 * {@link PdfReader#FlateDecode(byte[], boolean)} for intact and damaged streams. The damaged cases are the ones where
 * the decoder leaves its loop with an exception.
 */
class PdfReaderFlateDecodeTest {

    private static final byte[] CONTENT =
            "BT /F1 12 Tf 72 720 Td (Hello) Tj ET\n".repeat(200).getBytes(StandardCharsets.ISO_8859_1);

    private static byte[] deflate(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DeflaterOutputStream zip = new DeflaterOutputStream(out)) {
            zip.write(data);
        }
        return out.toByteArray();
    }

    @Test
    void intactStreamIsDecodedInBothModes() throws IOException {
        byte[] compressed = deflate(CONTENT);
        assertArrayEquals(CONTENT, PdfReader.FlateDecode(compressed, true));
        assertArrayEquals(CONTENT, PdfReader.FlateDecode(compressed, false));
        assertArrayEquals(CONTENT, PdfReader.FlateDecode(compressed));
    }

    @Test
    void truncatedStreamGivesNullInStrictMode() throws IOException {
        byte[] compressed = deflate(CONTENT);
        byte[] truncated = Arrays.copyOf(compressed, compressed.length / 2);
        assertNull(PdfReader.FlateDecode(truncated, true));
    }

    @Test
    void truncatedStreamGivesTheReadablePartInLenientMode() throws IOException {
        byte[] compressed = deflate(CONTENT);
        byte[] truncated = Arrays.copyOf(compressed, compressed.length / 2);

        byte[] decoded = PdfReader.FlateDecode(truncated, false);

        assertNotNull(decoded);
        assertTrue(decoded.length > 0 && decoded.length < CONTENT.length, "length " + decoded.length);
        assertArrayEquals(Arrays.copyOf(CONTENT, decoded.length), decoded);
        // the one argument method falls back to the lenient mode
        assertArrayEquals(decoded, PdfReader.FlateDecode(truncated));
    }

    @Test
    void dataThatIsNotDeflatedGivesNullInStrictModeAndNothingInLenientMode() {
        byte[] garbage = "this is not a zlib stream".getBytes(StandardCharsets.ISO_8859_1);
        assertNull(PdfReader.FlateDecode(garbage, true));
        assertArrayEquals(new byte[0], PdfReader.FlateDecode(garbage, false));
    }

    @Test
    void manyDamagedStreamsCanBeDecoded() throws IOException {
        byte[] compressed = deflate(CONTENT);
        byte[] truncated = Arrays.copyOf(compressed, compressed.length / 2);
        for (int i = 0; i < 2000; i++) {
            assertNull(PdfReader.FlateDecode(truncated, true));
        }
    }
}
