package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for issue #1582: PDF files larger than ~2 GB could not be read because file positions
 * were tracked as {@code int} in {@link RandomAccessFileOrArray}, {@link PRTokeniser} and the
 * {@link PdfReader} xref table, overflowing for offsets beyond {@code Integer.MAX_VALUE}.
 * <p>
 * The tests build a <em>sparse</em> file whose PDF objects live above the 2 GB boundary, so only
 * a few kilobytes of real data are written while all file offsets require 64-bit arithmetic.
 * With the fix in place both tests complete in well under a second; the timeout guards against a
 * regression, where the overflowed offsets would send the reader scanning gigabytes of data.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class LargeFilePdfReaderTest {

    /** All PDF objects are placed above this offset, safely beyond Integer.MAX_VALUE (2^31-1). */
    private static final long BASE = 3_000_000_000L;

    @TempDir
    Path tempDir;

    @Test
    void filePointerAndLengthBeyond2GB() throws Exception {
        Path file = tempDir.resolve("large-raw.bin");
        long length = BASE + 16;
        try (SeekableByteChannel ch = open(file)) {
            ch.position(length - 1);
            ch.write(ByteBuffer.wrap(new byte[]{(byte) 0x42}));
        }
        assumeSparse(file);

        RandomAccessFileOrArray raf = new RandomAccessFileOrArray(file.toString(), false, true);
        try {
            assertEquals(length, raf.length(), "length() must not overflow for files > 2 GB");
            raf.seek(length - 1);
            assertEquals(length - 1, raf.getFilePointer(), "getFilePointer() must not overflow beyond 2 GB");
            assertEquals(0x42, raf.read(), "Byte at an offset beyond 2 GB must be readable");
            assertEquals(length, raf.getFilePointer());
        } finally {
            raf.close();
        }
    }

    @Test
    void readPdfLargerThan2GB() throws Exception {
        Path file = tempDir.resolve("large.pdf");

        String obj1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";
        String obj2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n";
        String obj3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\n";
        long off1 = BASE;
        long off2 = off1 + obj1.length();
        long off3 = off2 + obj2.length();
        long xrefPos = off3 + obj3.length();
        String xref = "xref\n0 4\n"
                + "0000000000 65535 f \n"
                + String.format("%010d 00000 n \n", off1)
                + String.format("%010d 00000 n \n", off2)
                + String.format("%010d 00000 n \n", off3)
                + "trailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n" + xrefPos + "\n%%EOF";

        try (SeekableByteChannel ch = open(file)) {
            ch.write(ByteBuffer.wrap("%PDF-1.5\n".getBytes(StandardCharsets.ISO_8859_1)));
            ch.position(BASE);
            ch.write(ByteBuffer.wrap((obj1 + obj2 + obj3 + xref).getBytes(StandardCharsets.ISO_8859_1)));
        }
        assumeSparse(file);

        // Partial-read path, plain random access — the combination reported in the issue.
        PdfReader reader = new PdfReader(new RandomAccessFileOrArray(file.toString(), false, true), new byte[0]);
        try {
            assertEquals(1, reader.getNumberOfPages(), "Page count of a > 2 GB PDF");
            assertEquals(612, (int) reader.getPageSize(1).getWidth(), "Page width of a > 2 GB PDF");
            assertEquals(xrefPos, reader.getLastXref(), "startxref offset beyond 2 GB");
        } finally {
            reader.close();
        }
    }

    private static SeekableByteChannel open(Path file) throws IOException {
        return Files.newByteChannel(file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE,
                StandardOpenOption.SPARSE);
    }

    /**
     * Skips the test when the filesystem materialized the sparse file (needing > 2 GB of real
     * disk), to avoid hammering machines without sparse-file support.
     */
    private static void assumeSparse(Path file) {
        try {
            long usableSpace = Files.getFileStore(file).getUsableSpace();
            assumeTrue(usableSpace > BASE, "Not enough free disk space to run large-file test");
        } catch (IOException e) {
            assumeTrue(false, "Cannot determine free disk space: " + e.getMessage());
        }
    }
}
