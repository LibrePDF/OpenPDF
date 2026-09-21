package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.utils.LongMappedByteBuffer;

/**
 * On Java 22+, memory-mapped files must be unmapped as soon as they are closed, without waiting for the garbage
 * collector (issues #1112, #1517). Runs with failsafe against the multi-release jar.
 * <p>
 * On Windows, a mapped file cannot be deleted, so deleting the file verifies the release there. On Linux, a mapped
 * file can always be deleted, so the test checks /proc/self/maps instead.
 * <p>
 * There is intentionally no {@code System.gc()} in this test.
 */
class MappedFileReleaseIT {

    @TempDir
    Path tempDir;

    @BeforeEach
    void requireJava22() {
        assumeTrue(Runtime.version().feature() >= 22, "deterministic unmapping requires Java 22+");
    }

    @Test
    void bufferCannotBeAccessedAfterClose() throws IOException {
        Path file = Files.write(tempDir.resolve("data.bin"), new byte[]{1, 2, 3});
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            LongMappedByteBuffer buffer = new LongMappedByteBuffer(channel, FileChannel.MapMode.READ_ONLY);
            assertEquals(1, buffer.get(0));
            buffer.close();
            // the FFM variant is in use: the mapping is gone instead of just unreferenced
            assertThrows(IllegalStateException.class, () -> buffer.get(0));
        }
    }

    @Test
    void mappedRandomAccessFileReleasesFileOnClose() throws IOException {
        Path file = Files.write(tempDir.resolve("data.bin"), new byte[]{1, 2, 3});
        String realPath = file.toRealPath().toString();

        MappedRandomAccessFile raf = new MappedRandomAccessFile(file.toString(), "r");
        assertEquals(1, raf.read());
        assertMappedIfDetectable(realPath, true);
        raf.close();

        assertMappedIfDetectable(realPath, false);
        Files.delete(file);
    }

    @Test
    void pdfReaderReleasesFileOnClose() throws IOException {
        Path pdf = createPdf();
        String realPath = pdf.toRealPath().toString();
        assertFalse(Document.plainRandomAccess, "test requires memory-mapped access");

        try (PdfReader reader = new PdfReader(pdf.toString())) {
            assertEquals(1, reader.getNumberOfPages());
        }

        assertMappedIfDetectable(realPath, false);
        Files.delete(pdf);
    }

    @Test
    void partialPdfReaderReleasesFileOnClose() throws IOException {
        Path pdf = createPdf();
        String realPath = pdf.toRealPath().toString();

        try (PdfReader reader = new PdfReader(new RandomAccessFileOrArray(pdf.toString()), null)) {
            assertEquals(1, reader.getNumberOfPages());
            assertMappedIfDetectable(realPath, true);
        }

        assertMappedIfDetectable(realPath, false);
        Files.delete(pdf);
    }

    private Path createPdf() throws IOException {
        Path pdf = tempDir.resolve("document.pdf");
        try (OutputStream out = Files.newOutputStream(pdf)) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Hello"));
            document.close();
        }
        return pdf;
    }

    private static void assertMappedIfDetectable(String realPath, boolean expected) throws IOException {
        Path maps = Path.of("/proc/self/maps");
        if (!Files.isReadable(maps)) {
            return; // not Linux; on Windows, Files.delete() verifies the release
        }
        try (Stream<String> lines = Files.lines(maps)) {
            assertEquals(expected, lines.anyMatch(line -> line.endsWith(realPath)),
                    () -> realPath + (expected ? " should be mapped" : " should no longer be mapped"));
        }
    }
}
