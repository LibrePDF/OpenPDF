package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The buffer returned by {@link RandomAccessFileOrArray#getNioByteBuffer()} for a file is a mapping that is released
 * by {@link RandomAccessFileOrArray#close()}, without waiting for the garbage collector.
 * <p>
 * On Windows, a mapped file cannot be deleted, so deleting the file verifies the release there.
 */
class RandomAccessFileOrArrayNioBufferTest {

    private static final byte[] CONTENT = {10, 20, 30, 40, 50};

    @TempDir
    Path tempDir;

    @Test
    void mappedAccessReturnsFileContent() throws IOException {
        assertFileContent(false);
    }

    @Test
    void plainAccessReturnsFileContent() throws IOException {
        assertFileContent(true);
    }

    private void assertFileContent(boolean plainRandomAccess) throws IOException {
        Path file = Files.write(tempDir.resolve("data.bin"), CONTENT);
        RandomAccessFileOrArray raf = new RandomAccessFileOrArray(file.toString(), false, plainRandomAccess);
        try {
            ByteBuffer buffer = raf.getNioByteBuffer();
            assertTrue(buffer.isReadOnly());
            byte[] read = new byte[buffer.remaining()];
            buffer.get(read);
            assertArrayEquals(CONTENT, read);
        } finally {
            raf.close();
        }
        Files.delete(file);
    }

    @Test
    void closeReleasesTheMapping() throws IOException {
        Path file = Files.write(tempDir.resolve("data.bin"), CONTENT);
        RandomAccessFileOrArray raf = new RandomAccessFileOrArray(file.toString(), false, true);
        ByteBuffer first = raf.getNioByteBuffer();
        ByteBuffer second = raf.getNioByteBuffer();
        assertEquals(10, first.get(0));
        assertEquals(20, second.get(1));

        raf.close();

        assertThrows(IllegalStateException.class, () -> first.get(0));
        assertThrows(IllegalStateException.class, () -> second.get(0));
        Files.delete(file);
    }

    @Test
    void bufferCanBeRequestedAgainAfterReopening() throws IOException {
        Path file = Files.write(tempDir.resolve("data.bin"), CONTENT);
        RandomAccessFileOrArray raf = new RandomAccessFileOrArray(file.toString(), false, true);
        assertEquals(10, raf.getNioByteBuffer().get(0));
        raf.close();

        ByteBuffer reopened = raf.getNioByteBuffer();
        assertEquals(50, reopened.get(4));
        raf.close();
        assertThrows(IllegalStateException.class, () -> reopened.get(4));
        Files.delete(file);
    }

    @Test
    void emptyFileGivesEmptyBuffer() throws IOException {
        Path file = Files.write(tempDir.resolve("empty.bin"), new byte[0]);
        RandomAccessFileOrArray raf = new RandomAccessFileOrArray(file.toString(), false, true);
        try {
            assertEquals(0, raf.getNioByteBuffer().remaining());
        } finally {
            raf.close();
        }
        Files.delete(file);
    }

    @Test
    void arrayBackedBufferIsNotAffectedByClose() throws IOException {
        RandomAccessFileOrArray raf = new RandomAccessFileOrArray(CONTENT);
        ByteBuffer buffer = raf.getNioByteBuffer();
        raf.close();
        assertEquals(30, buffer.get(2));
    }
}
