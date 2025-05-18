package com.lowagie.text;

import com.lowagie.text.utils.LongMappedByteBuffer;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class LongMappedByteBufferTest {

    private static final long[] FILE_SIZES = {
            1,                     // Tiny file
            1024,                 // 1KB
            (1L << 20),           // 1MB
            (1L << 31) - 1,       // Just under 2GB
            (1L << 31),           // Exactly 2GB
            (1L << 31) + 1,       // Just over 2GB
            (1L << 31) + 128,     // 2GB + 128 bytes
            (1L << 32) + 1024     // 4GB + 1KB
    };

    @TestFactory
    Stream<DynamicTest> test_read_and_write_with_various_sizes() {
        return LongStream.of(FILE_SIZES).mapToObj(size ->
                DynamicTest.dynamicTest("Buffer size: " + size + " bytes", () -> {
                    try {
                        testBuffer(size);
                    } catch (IOException e) {
                        fail("IOException for size " + size + ": " + e.getMessage(), e);
                    }
                })
        );
    }

    private void testBuffer(long size) throws IOException {
        File tempFile = Files.createTempFile("long-mapped", ".tmp").toFile();
        try {
            try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
                raf.setLength(size);
            }

            try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
                LongMappedByteBuffer buffer = new LongMappedByteBuffer(raf.getChannel(), FileChannel.MapMode.READ_WRITE);

                long posStart = 0;
                long posMid = Math.max(0, size / 2 - 2);
                long posEnd = size > 0 ? size - 1 : 0;

                // Write and verify single bytes
                buffer.position(posStart).put((byte) 0x01);
                assertEquals(posStart + 1, buffer.position());
                if (size > 1) {
                    buffer.position(posMid).put((byte) 0x02);
                    assertEquals(posMid + 1, buffer.position());
                }
                if (size > 2) {
                    buffer.position(posEnd).put((byte) 0x03);
                    assertEquals(posEnd + 1, buffer.position());
                }

                // Read and verify
                buffer.position(posStart);
                assertEquals(0x01, buffer.get() & 0xFF);
                if (size > 1) {
                    buffer.position(posMid);
                    assertEquals(0x02, buffer.get() & 0xFF);
                }
                if (size > 2) {
                    buffer.position(posEnd);
                    assertEquals(0x03, buffer.get() & 0xFF);
                }

                // Write and read byte[] if large enough
                if (size > 16) {
                    long writePos = Math.max(0, size - 16);
                    byte[] writeBytes = new byte[]{0x11, 0x22, 0x33, 0x44};
                    buffer.position(writePos).put(writeBytes, 0, writeBytes.length);
                    assertEquals(writePos + writeBytes.length, buffer.position());

                    byte[] readBytes = new byte[4];
                    buffer.position(writePos).get(readBytes, 0, 4);
                    assertArrayEquals(writeBytes, readBytes);
                }

                // Fill entire file (if < 1MB) with known value and sum
                if (size <= (1L << 20)) {
                    buffer.position(0);
                    byte fill = 0x7F;
                    byte[] bulk = new byte[(int) size];
                    for (int i = 0; i < bulk.length; i++) bulk[i] = fill;
                    buffer.position(0).put(bulk, 0, bulk.length);

                    byte[] verify = new byte[bulk.length];
                    buffer.position(0).get(verify, 0, verify.length);
                    for (byte b : verify) assertEquals(fill, b);
                }

                // Check file boundary
                assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(size));
                assertThrows(IndexOutOfBoundsException.class, () -> buffer.put(size, (byte) 0x00));

                // Size checks
                assertEquals(size, buffer.size());
                assertEquals(size, buffer.limit());

                // Optional methods
                assertDoesNotThrow(buffer::load);
                assertDoesNotThrow(buffer::force);
                assertDoesNotThrow(buffer::clean);
            }
        } finally {
            tempFile.delete();
        }
    }

    @Test
    void test_get_put_with_invalid_bounds() throws IOException {
        File tempFile = Files.createTempFile("long-mapped", ".tmp").toFile();
        try {
            try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
                raf.setLength(1024);
            }

            try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
                LongMappedByteBuffer buffer = new LongMappedByteBuffer(raf.getChannel(), FileChannel.MapMode.READ_WRITE);
                assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(2048));
                assertThrows(IndexOutOfBoundsException.class, () -> buffer.put(2048, (byte) 0x55));
                assertThrows(IllegalArgumentException.class, () -> buffer.position(-1));
                assertThrows(IllegalArgumentException.class, () -> buffer.position(2048));
            }
        } finally {
            tempFile.delete();
        }
    }

    @Test
    void test_reload_and_read_again() throws IOException {
        File tempFile = Files.createTempFile("long-mapped", ".tmp").toFile();
        long pos = 1234;
        byte value = 0x66;
        try {
            try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
                raf.setLength(2048);
                LongMappedByteBuffer buffer = new LongMappedByteBuffer(raf.getChannel(), FileChannel.MapMode.READ_WRITE);
                buffer.position(pos).put(value);
            }

            try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
                LongMappedByteBuffer buffer = new LongMappedByteBuffer(raf.getChannel(), FileChannel.MapMode.READ_WRITE);
                byte read = buffer.position(pos).get();
                assertEquals(value, read);

                // Verify other positions still return default (0)
                assertEquals(0, buffer.position(0).get());
                assertEquals(0, buffer.position(2047).get());
            }
        } finally {
            tempFile.delete();
        }
    }
}
