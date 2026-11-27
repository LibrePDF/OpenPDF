package org.openpdf.renderer.decode;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.DeflaterOutputStream;
import org.junit.jupiter.api.Test;
import org.openpdf.renderer.PDFParseException;

/**
 * {@link FlateDecode#decode} for intact and damaged streams.
 */
class FlateDecodeTest {

    private static final byte[] CONTENT =
            "BT /F1 12 Tf 72 720 Td (Hello) Tj ET\n".repeat(200).getBytes(StandardCharsets.ISO_8859_1);

    private static byte[] deflate(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DeflaterOutputStream zip = new DeflaterOutputStream(out)) {
            zip.write(data);
        }
        return out.toByteArray();
    }

    private static byte[] toArray(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    @Test
    void intactStreamIsDecoded() throws IOException {
        ByteBuffer decoded = FlateDecode.decode(null, ByteBuffer.wrap(deflate(CONTENT)), null);
        assertArrayEquals(CONTENT, toArray(decoded));
    }

    @Test
    void readOnlyBufferIsDecoded() throws IOException {
        ByteBuffer input = ByteBuffer.wrap(deflate(CONTENT)).asReadOnlyBuffer();
        assertArrayEquals(CONTENT, toArray(FlateDecode.decode(null, input, null)));
    }

    @Test
    void dataThatIsNotDeflatedIsReported() {
        ByteBuffer garbage = ByteBuffer.wrap("this is not a zlib stream".getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(PDFParseException.class, () -> FlateDecode.decode(null, garbage, null));
    }

    @Test
    void manyStreamsCanBeDecoded() throws IOException {
        byte[] compressed = deflate(CONTENT);
        for (int i = 0; i < 2000; i++) {
            assertArrayEquals(CONTENT, toArray(FlateDecode.decode(null, ByteBuffer.wrap(compressed), null)));
        }
    }
}
