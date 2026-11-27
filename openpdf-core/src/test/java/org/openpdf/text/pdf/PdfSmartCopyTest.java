package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PdfSmartCopyTest {

    public PdfSmartCopyTest() {
        super();
    }

    @Test
    public void test1() {
        File orig = new File("src/test/resources/pdfsmartcopy_bec.pdf");
        check(orig, 1);
    }

    @SuppressWarnings("SameParameterValue")
    private void check(File orig, int counter) {
        Assertions.assertTimeout(Duration.ofSeconds(20), () -> {
            File out = new File("target/test-classes/pdfsmartocy-" + counter + ".pdf");
            Files.createDirectories(out.getParentFile().toPath());
            Document document = new Document();
            FileOutputStream outputStream = new FileOutputStream(out);
            PdfCopy copy = new PdfSmartCopy(document, outputStream);
            document.open();

            PdfReader reader = new PdfReader(orig.getAbsolutePath());
            int n = reader.getNumberOfPages();
            for (int currentPage = 1; currentPage <= n; currentPage++) {
                PdfImportedPage page = copy.getImportedPage(reader, currentPage);
                copy.addPage(page);
            }
            copy.freeReader(reader);
            reader.close();
            document.close();
            copy.close();
        });
    }

    @Test
    void canWriteAndCopy() throws IOException {
        try (PdfReader reader = new PdfReader("src/test/resources/pdfsmartcopy_bec.pdf")) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (Document document = new Document()) {
                try (PdfCopy copy = new PdfSmartCopy(document, outputStream)) {
                    document.open();

                    document.add(new Paragraph("Front page"));
                    document.newPage();

                    int n = reader.getNumberOfPages();
                    for (int currentPage = 1; currentPage <= n; currentPage++) {
                        PdfImportedPage page = copy.getImportedPage(reader, currentPage);
                        copy.addPage(page);
                    }
                    copy.freeReader(reader);

                    document.newPage();
                    document.add(new Paragraph("Last page"));
                }
            }
            try (PdfReader reader2 = new PdfReader(outputStream.toByteArray())) {
                assertEquals(2 + reader.getNumberOfPages(), reader2.getNumberOfPages());
            }
        }
    }

    @Test
    void canInterleaveTwoFilesWithCustomPages() throws IOException {
        // Interleaves two PDF files with a custom page in front of each
        try (PdfReader reader1 = new PdfReader("src/test/resources/pdfsmartcopy_bec.pdf");
             PdfReader reader2 = new PdfReader("src/test/resources/HelloWorldMeta.pdf")
        ) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (Document document = new Document()) {
                try (PdfCopy copy = new PdfSmartCopy(document, outputStream)) {
                    document.open();

                    for (int i = 1; i <= 10; i++) {
                        document.add(new Paragraph("Page " + i + " from document 1"));
                        document.newPage();
                        PdfImportedPage page1 = copy.getImportedPage(reader1, ((i - 1) % reader1.getNumberOfPages()) + 1);
                        copy.addPage(page1);
                        document.add(new Paragraph("Page " + i + " from document 2"));
                        document.newPage();
                        PdfImportedPage page2 = copy.getImportedPage(reader2, ((i - 1) % reader2.getNumberOfPages()) + 1);
                        copy.addPage(page2);
                    }

                    copy.freeReader(reader1);
                    copy.freeReader(reader2);
                }
            }
            try (PdfReader resultReader = new PdfReader(outputStream.toByteArray())) {
                assertEquals(40, resultReader.getNumberOfPages());
            }
        }
    }

    @Test
    void copiesStreamThatReachesTheSameObjectsAlongManyPaths() throws IOException {
        // each level is referenced twice by the level above it, which gives 2^60 paths to the last object
        List<String> objects = new ArrayList<>(List.of(
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] /Contents 4 0 R >>",
                "<< /Length 0 /A 5 0 R /B 5 0 R >>\nstream\n\nendstream"));
        int levels = 60;
        for (int k = 0; k < levels; k++) {
            int next = 6 + k;
            objects.add(k == levels - 1 ? "<< >>" : "<< /A " + next + " 0 R /B " + next + " 0 R >>");
        }
        byte[] pdf = pdf(objects);

        byte[] result = Assertions.assertTimeoutPreemptively(Duration.ofSeconds(20), () -> smartCopy(pdf));

        try (PdfReader reader = new PdfReader(result)) {
            assertEquals(1, reader.getNumberOfPages());
        }
    }

    @Test
    void mergesEqualStreamsThatShareAnObject() throws IOException {
        byte[] pdf = pdf(List.of(
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R 5 0 R] /Count 2 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] /Contents 4 0 R >>",
                "<< /Length 1 /A 7 0 R /B 7 0 R >>\nstream\nn\nendstream",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] /Contents 6 0 R >>",
                "<< /Length 1 /A 7 0 R /B 7 0 R >>\nstream\nn\nendstream",
                "<< /C 8 0 R /D 8 0 R >>",
                "<< >>"));

        try (PdfReader reader = new PdfReader(smartCopy(pdf))) {
            PdfIndirectReference first = (PdfIndirectReference) reader.getPageN(1).get(PdfName.CONTENTS);
            PdfIndirectReference second = (PdfIndirectReference) reader.getPageN(2).get(PdfName.CONTENTS);
            assertEquals(first.getNumber(), second.getNumber());
        }
    }

    @Test
    void keepsStreamsApartThatDifferBehindASharedObject() throws IOException {
        byte[] pdf = pdf(List.of(
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R 5 0 R] /Count 2 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] /Contents 4 0 R >>",
                "<< /Length 1 /A 7 0 R /B 7 0 R >>\nstream\nn\nendstream",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] /Contents 6 0 R >>",
                "<< /Length 1 /A 7 0 R /B 8 0 R >>\nstream\nn\nendstream",
                "<< /C 1 >>",
                "<< /C 2 >>"));

        try (PdfReader reader = new PdfReader(smartCopy(pdf))) {
            PdfIndirectReference first = (PdfIndirectReference) reader.getPageN(1).get(PdfName.CONTENTS);
            PdfIndirectReference second = (PdfIndirectReference) reader.getPageN(2).get(PdfName.CONTENTS);
            assertNotEquals(first.getNumber(), second.getNumber());
        }
    }

    private static byte[] smartCopy(byte[] pdf) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PdfReader reader = new PdfReader(pdf); Document document = new Document()) {
            PdfCopy copy = new PdfSmartCopy(document, outputStream);
            document.open();
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                copy.addPage(copy.getImportedPage(reader, page));
            }
            copy.freeReader(reader);
        }
        return outputStream.toByteArray();
    }

    /**
     * Builds a PDF file from the bodies of its objects, which are numbered from 1.
     */
    private static byte[] pdf(List<String> objects) {
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int k = 0; k < objects.size(); k++) {
            offsets.add(pdf.length());
            pdf.append(k + 1).append(" 0 obj\n").append(objects.get(k)).append("\nendobj\n");
        }
        int xref = pdf.length();
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n0000000000 65535 f \n");
        for (int offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xref).append("\n%%EOF\n");
        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }
}
