package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openpdf.text.Rectangle;

public class PdfReaderPageSizeTest {

    @Test
    void getPageSizeShouldWorkWithFullReadWhenPageHasNoMediaBox() throws Exception {
        byte[] minimalPdfWithoutPageMediaBox = createMinimalPdfWithoutPageMediaBox();

        try (PdfReader fullReader = new PdfReader(minimalPdfWithoutPageMediaBox)) {
            Rectangle pageSize = fullReader.getPageSize(1);

            assertNotNull(pageSize);
            assertEquals(595f, pageSize.getWidth(), 0.01f);
            assertEquals(842f, pageSize.getHeight(), 0.01f);
        }
    }

    @Test
    void getPageSizeShouldWorkWithPartialReadWhenPageHasNoMediaBox() throws Exception {
        byte[] minimalPdfWithoutPageMediaBox = createMinimalPdfWithoutPageMediaBox();

        Path tempPdf = Files.createTempFile("openpdf-no-mediabox-page-", ".pdf");
        Files.write(tempPdf, minimalPdfWithoutPageMediaBox);

        try {
            try (RandomAccessFileOrArray raf = new RandomAccessFileOrArray(tempPdf
                    .toString()); PdfReader partialReader = new PdfReader(raf, null)) {
                Rectangle pageSize = partialReader.getPageSize(1);

                assertNotNull(pageSize);
                assertEquals(595f, pageSize.getWidth(), 0.01f);
                assertEquals(842f, pageSize.getHeight(), 0.01f);
            }
        } finally {
            Files.deleteIfExists(tempPdf);
        }
    }

    @Test
    void getPageSizeShouldFallbackToLetterWithFullReadWhenNoMediaBoxInHierarchy() throws Exception {
        byte[] minimalPdfWithoutAnyMediaBox = createMinimalPdfWithoutAnyMediaBox();

        try (PdfReader fullReader = new PdfReader(minimalPdfWithoutAnyMediaBox)) {
            Rectangle pageSize = fullReader.getPageSize(1);

            assertNotNull(pageSize);
            assertEquals(612f, pageSize.getWidth(), 0.01f);
            assertEquals(792f, pageSize.getHeight(), 0.01f);
        }
    }

    @Test
    void getPageSizeShouldFallbackToLetterWithPartialReadWhenNoMediaBoxInHierarchy() throws Exception {
        byte[] minimalPdfWithoutAnyMediaBox = createMinimalPdfWithoutAnyMediaBox();

        Path tempPdf = Files.createTempFile("openpdf-no-mediabox-any-level-", ".pdf");
        Files.write(tempPdf, minimalPdfWithoutAnyMediaBox);

        try {
            try (RandomAccessFileOrArray raf = new RandomAccessFileOrArray(tempPdf
                    .toString()); PdfReader partialReader = new PdfReader(raf, null)) {
                Rectangle pageSize = partialReader.getPageSize(1);

                assertNotNull(pageSize);
                assertEquals(612f, pageSize.getWidth(), 0.01f);
                assertEquals(792f, pageSize.getHeight(), 0.01f);
            }
        } finally {
            Files.deleteIfExists(tempPdf);
        }
    }

    private static byte[] createMinimalPdfWithoutPageMediaBox() throws IOException {
        List<String> objects = new ArrayList<>();
        objects.add("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        objects.add("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 /MediaBox [0 0 595 842] >>\nendobj\n");
        objects.add("3 0 obj\n<< /Type /Page /Parent 2 0 R /Count 1 /Resources << >> /Contents 4 0 R >>\nendobj\n");
        objects.add("4 0 obj\n<< /Length 0 >>\nstream\n\nendstream\nendobj\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));

        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        for (String object : objects) {
            offsets.add(out.size());
            out.write(object.getBytes(StandardCharsets.ISO_8859_1));
        }

        int xrefOffset = out.size();
        out.write(("xref\n0 " + (objects.size() + 1) + "\n").getBytes(StandardCharsets.ISO_8859_1));
        out.write("0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));

        for (int i = 1; i <= objects.size(); i++) {
            out.write(String.format("%010d 00000 n \n", offsets.get(i)).getBytes(StandardCharsets.ISO_8859_1));
        }

        out.write(("trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n").getBytes(
                StandardCharsets.ISO_8859_1));
        out.write(("startxref\n" + xrefOffset + "\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));
        return out.toByteArray();
    }

    private static byte[] createMinimalPdfWithoutAnyMediaBox() throws IOException {
        List<String> objects = new ArrayList<>();
        objects.add("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        objects.add("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        objects.add("3 0 obj\n<< /Type /Page /Parent 2 0 R /Resources << >> /Contents 4 0 R >>\nendobj\n");
        objects.add("4 0 obj\n<< /Length 0 >>\nstream\n\nendstream\nendobj\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));

        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        for (String object : objects) {
            offsets.add(out.size());
            out.write(object.getBytes(StandardCharsets.ISO_8859_1));
        }

        int xrefOffset = out.size();
        out.write(("xref\n0 " + (objects.size() + 1) + "\n").getBytes(StandardCharsets.ISO_8859_1));
        out.write("0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));

        for (int i = 1; i <= objects.size(); i++) {
            out.write(String.format("%010d 00000 n \n", offsets.get(i)).getBytes(StandardCharsets.ISO_8859_1));
        }

        out.write(("trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n").getBytes(
                StandardCharsets.ISO_8859_1));
        out.write(("startxref\n" + xrefOffset + "\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));
        return out.toByteArray();
    }
}
