package org.openpdf.text.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Behavior of {@link LongMappedByteBuffer} that the Java 21 implementation and the Java 22+ multi-release variant
 * must have in common. Surefire runs this test against the Java 21 classes; on JDK 22+, failsafe runs it again
 * against the packaged multi-release jar.
 */
class LongMappedByteBufferTest {

    private static final byte[] CONTENT = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, (byte) 0xff};

    @TempDir
    Path tempDir;

    @Test
    void readsWholeFileByteByByte() throws IOException {
        try (FileChannel channel = open(write(CONTENT));
                LongMappedByteBuffer buffer = new LongMappedByteBuffer(channel, FileChannel.MapMode.READ_ONLY)) {
            assertEquals(CONTENT.length, buffer.size());
            assertEquals(CONTENT.length, buffer.limit());
            for (byte expected : CONTENT) {
                assertEquals(expected, buffer.get());
            }
            assertEquals(CONTENT.length, buffer.position());
        }
    }

    @Test
    void getAtEndThrowsBufferUnderflowException() throws IOException {
        try (FileChannel channel = open(write(CONTENT));
                LongMappedByteBuffer buffer = new LongMappedByteBuffer(channel, FileChannel.MapMode.READ_ONLY)) {
            assertThrows(BufferUnderflowException.class, () -> buffer.get(CONTENT.length));
        }
    }

    @Test
    void readReturnsAvailableBytesAndMinusOneAtEnd() throws IOException {
        try (FileChannel channel = open(write(CONTENT));
                LongMappedByteBuffer buffer = new LongMappedByteBuffer(channel, FileChannel.MapMode.READ_ONLY)) {
            buffer.position(CONTENT.length - 3);
            byte[] dst = new byte[10];
            assertEquals(3, buffer.read(dst, 2, 10 - 2));
            assertArrayEquals(new byte[]{0, 0, 8, 9, (byte) 0xff, 0, 0, 0, 0, 0}, dst);
            assertEquals(CONTENT.length, buffer.position());
            assertEquals(-1, buffer.read(dst, 0, dst.length));
        }
    }

    @Test
    void bulkGetCopiesRangeAndRejectsInvalidArguments() throws IOException {
        try (FileChannel channel = open(write(CONTENT));
                LongMappedByteBuffer buffer = new LongMappedByteBuffer(channel, FileChannel.MapMode.READ_ONLY)) {
            byte[] dst = new byte[4];
            buffer.get(3, dst, 0, 4);
            assertArrayEquals(new byte[]{3, 4, 5, 6}, dst);
            assertDoesNotThrow(() -> buffer.get(CONTENT.length, dst, 0, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(0, dst, 1, 4));
        }
    }

    @Test
    void positionOutsideOfFileIsRejected() throws IOException {
        try (FileChannel channel = open(write(CONTENT));
                LongMappedByteBuffer buffer = new LongMappedByteBuffer(channel, FileChannel.MapMode.READ_ONLY)) {
            assertThrows(IllegalArgumentException.class, () -> buffer.position(-1));
            assertThrows(IllegalArgumentException.class, () -> buffer.position(CONTENT.length + 1));
            assertDoesNotThrow(() -> buffer.position(CONTENT.length));
        }
    }

    @Test
    void emptyFileCanBeMapped() throws IOException {
        try (FileChannel channel = open(write(new byte[0]));
                LongMappedByteBuffer buffer = new LongMappedByteBuffer(channel, FileChannel.MapMode.READ_ONLY)) {
            assertEquals(0, buffer.size());
            buffer.load();
            assertEquals(-1, buffer.read(new byte[1], 0, 1));
        }
    }

    @Test
    void writingToReadOnlyMappingThrowsReadOnlyBufferException() throws IOException {
        try (FileChannel channel = open(write(CONTENT));
                LongMappedByteBuffer buffer = new LongMappedByteBuffer(channel, FileChannel.MapMode.READ_ONLY)) {
            assertThrows(ReadOnlyBufferException.class, () -> buffer.put((byte) 1));
            assertThrows(ReadOnlyBufferException.class, () -> buffer.put(0, (byte) 1));
            assertThrows(ReadOnlyBufferException.class, () -> buffer.put(new byte[]{1}, 0, 1));
            assertDoesNotThrow(buffer::force);
        }
    }

    @Test
    void writesToReadWriteMappingReachTheFile() throws IOException {
        Path file = write(CONTENT);
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE);
                LongMappedByteBuffer buffer = new LongMappedByteBuffer(channel, FileChannel.MapMode.READ_WRITE)) {
            buffer.put(0, (byte) 42);
            buffer.position(1);
            buffer.put(new byte[]{43, 44}, 0, 2);
            buffer.put((byte) 45);
            assertEquals(4, buffer.position());
            buffer.force();
        }
        byte[] expected = CONTENT.clone();
        expected[0] = 42;
        expected[1] = 43;
        expected[2] = 44;
        expected[3] = 45;
        assertArrayEquals(expected, Files.readAllBytes(file));
    }

    @Test
    void closeCanBeCalledMoreThanOnce() throws IOException {
        try (FileChannel channel = open(write(CONTENT))) {
            LongMappedByteBuffer buffer = new LongMappedByteBuffer(channel, FileChannel.MapMode.READ_ONLY);
            buffer.close();
            assertDoesNotThrow(buffer::close);
        }
    }

    private Path write(byte[] content) throws IOException {
        return Files.write(Files.createTempFile(tempDir, "mapped", ".bin"), content);
    }

    private static FileChannel open(Path file) throws IOException {
        return FileChannel.open(file, StandardOpenOption.READ);
    }
}
