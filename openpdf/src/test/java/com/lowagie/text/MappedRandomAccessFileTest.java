package com.lowagie.text;

import com.lowagie.text.pdf.MappedRandomAccessFile;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MappedRandomAccessFileTest {

    private static final long[] FILE_SIZES = {
            (1L << 31) - 1,  // Just under 2GB
            (1L << 31),      // Exactly 2GB
            (1L << 31) + 1,  // Just over 2GB
            (1L << 32) + 1024, // 4GB + 1KB
            (1L << 31) + 512  // ~2GB + 512 bytes (original)
    };

    @TestFactory
    Iterable<DynamicTest> testVariousFileSizes() {
        return java.util.Arrays.stream(FILE_SIZES)
                .mapToObj(size -> DynamicTest.dynamicTest("File size = " + size, () -> testWithFileSize(size)))
                .toList();
    }

    private void testWithFileSize(long size) throws IOException {
        File file = Files.createTempFile("mapped-size-" + size, ".tmp").toFile();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(size);
        }

        try (MappedRandomAccessFile mapped = new MappedRandomAccessFile(file.getAbsolutePath(), "rw")) {
            // Write and verify single byte
            long writePos = size - 1;
            mapped.getChannel().write(ByteBuffer.wrap(new byte[]{0x55}), writePos);
            mapped.seek(writePos);
            assertEquals(0x55, mapped.read());

            // Length check
            assertEquals(size, mapped.length());
        } finally {
            file.delete();
        }
    }

    @Test
    void testReadWriteAcross2GB() throws Exception {
        long SIZE = (1L << 31) + 512;
        File tempFile = Files.createTempFile("mapped-2gb-cross", ".tmp").toFile();
        try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
            raf.setLength(SIZE);
        }

        try (MappedRandomAccessFile mapped = new MappedRandomAccessFile(tempFile.getAbsolutePath(), "rw")) {

            long posStart = 0;
            long posNear2GB = (long) Integer.MAX_VALUE - 2;
            long posCross2GB = (long) Integer.MAX_VALUE + 1;
            long posEnd = SIZE - 1;

            // Write single bytes
            mapped.getChannel().write(ByteBuffer.wrap(new byte[]{0x01}), posStart);
            mapped.getChannel().write(ByteBuffer.wrap(new byte[]{0x02}), posNear2GB);
            mapped.getChannel().write(ByteBuffer.wrap(new byte[]{0x03}), posCross2GB);
            mapped.getChannel().write(ByteBuffer.wrap(new byte[]{0x04}), posEnd);

            // Read and verify
            mapped.seek(posStart); assertEquals(0x01, mapped.read());
            mapped.seek(posNear2GB); assertEquals(0x02, mapped.read());
            mapped.seek(posCross2GB); assertEquals(0x03, mapped.read());
            mapped.seek(posEnd); assertEquals(0x04, mapped.read());

            // Write 512 bytes
            long writePos = (long) Integer.MAX_VALUE - 256;
            byte[] data = new byte[512];
            new Random(42).nextBytes(data);
            ByteBuffer buf = ByteBuffer.wrap(data);
            while (buf.hasRemaining()) {
                int written = mapped.getChannel().write(buf, writePos + buf.position());
                if (written <= 0) break;
            }

            // Read 512 bytes
            byte[] readBack = new byte[512];
            mapped.seek(writePos);
            int totalRead = 0;
            while (totalRead < readBack.length) {
                int n = mapped.read(readBack, totalRead, readBack.length - totalRead);
                if (n < 0) break;
                totalRead += n;
            }
            assertEquals(512, totalRead);
            assertArrayEquals(data, readBack);

            // File pointer and EOF tests
            mapped.seek(0); assertEquals(0, mapped.getFilePointer());
            mapped.seek(123456); assertEquals(123456, mapped.getFilePointer());
            mapped.seek(SIZE - 1); assertTrue(mapped.read() >= 0);
            mapped.seek(SIZE); assertThrows(IndexOutOfBoundsException.class, () -> mapped.read());
            assertEquals(SIZE, mapped.length());
        } finally {
            tempFile.delete();
        }
    }

    @Test
    void testFilePointerAndSeek() throws Exception {
        File file = Files.createTempFile("pointer-test", ".tmp").toFile();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(1024);
        }

        try (MappedRandomAccessFile mapped = new MappedRandomAccessFile(file.getAbsolutePath(), "rw")) {
            mapped.seek(100);
            assertEquals(100, mapped.getFilePointer());
            mapped.seek(512);
            assertEquals(512, mapped.getFilePointer());
        } finally {
            file.delete();
        }
    }

    @Test
    void testLengthAndClose() throws Exception {
        File file = Files.createTempFile("length-test", ".tmp").toFile();
        long size = 1024;
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(size);
        }

        MappedRandomAccessFile mapped = new MappedRandomAccessFile(file.getAbsolutePath(), "rw");
        assertEquals(size, mapped.length());
        mapped.close();
        assertTrue(file.exists());
        file.delete();
    }
}
