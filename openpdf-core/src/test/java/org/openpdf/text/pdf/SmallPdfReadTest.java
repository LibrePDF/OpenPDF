package org.openpdf.text.pdf;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Paragraph;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Test for small PDF files in MappedRandomAccessFile
 */
public class SmallPdfReadTest {

    @Test
    public void testEOFHandlingInMappedRandomAccessFile(@TempDir File tempDir) throws IOException, DocumentException {
        File pdfFile = new File(tempDir, "tiny.pdf");
        pdfFile.deleteOnExit();

        // 1. Create a tiny PDF file
        try (OutputStream outputStream = new FileOutputStream(pdfFile)) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("Minimal content"));
            document.close();
        }

        Assertions.assertTrue(pdfFile.length() < 1024, "PDF should be under 1KiB");

        // 2. Open with MappedRandomAccessFile and seek to EOF
        try (MappedRandomAccessFile raf = new MappedRandomAccessFile(pdfFile.getAbsolutePath(), "r")) {
            long length = pdfFile.length();
            raf.seek(length); // Seek to exactly EOF

            // 3. Try to read past EOF — should return -1, not throw
            int result = raf.read();

            // BUG: This currently throws IndexOutOfBoundsException instead of returning -1
            Assertions.assertEquals(-1, result, "Expected -1 at EOF, but read returned: " + result);
        }

        System.gc();
        try {
            Thread.sleep(100); // give GC time to release mapped file
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // best practice: restore interrupt flag
            throw new IllegalStateException("Sleep was interrupted", e);
        }
    }
}
