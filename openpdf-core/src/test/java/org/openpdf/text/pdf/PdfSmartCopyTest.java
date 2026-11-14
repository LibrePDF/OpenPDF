package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    void copiesNestedSharedFormXObjectsQuickly() throws IOException {
        // see issue #1636: every level references all forms of the next level, so the number of paths through
        // the resource graph grows exponentially, while the number of distinct objects stays small
        byte[] pdf = createPdfWithNestedFormXObjects(8, 8);
        byte[] copied = Assertions.assertTimeoutPreemptively(Duration.ofSeconds(20), () -> copyWithSmartCopy(pdf));
        try (PdfReader reader = new PdfReader(copied)) {
            assertEquals(1, reader.getNumberOfPages());
        }
    }

    @Test
    void stillDeduplicatesNestedFormXObjects() throws IOException {
        byte[] pdf = createPdfWithNestedFormXObjects(3, 3);
        byte[] once = copyWithSmartCopy(pdf);
        byte[] twice = copyWithSmartCopy(pdf, pdf);
        try (PdfReader readerOnce = new PdfReader(once); PdfReader readerTwice = new PdfReader(twice)) {
            assertEquals(2, readerTwice.getNumberOfPages());
            // the second copy shares all form XObjects with the first one, only the page objects are new
            assertTrue(readerTwice.getXrefSize() - readerOnce.getXrefSize() <= 3,
                    "form XObjects were not deduplicated: " + readerOnce.getXrefSize() + " vs. "
                            + readerTwice.getXrefSize() + " objects");
        }
    }

    @Test
    void doesNotMergeFormXObjectsWithDifferentNestedContent() throws IOException {
        // the PDFs only differ in the innermost forms, so the forms of all levels must be copied again
        byte[] pdf = createPdfWithNestedFormXObjects(3, 3, 10);
        byte[] otherPdf = createPdfWithNestedFormXObjects(3, 3, 11);
        byte[] sameTwice = copyWithSmartCopy(pdf, pdf);
        byte[] different = copyWithSmartCopy(pdf, otherPdf);
        try (PdfReader readerSame = new PdfReader(sameTwice); PdfReader readerDifferent = new PdfReader(different)) {
            // the forms within a level are identical and merged, so there is one additional form per level
            assertEquals(3, readerDifferent.getXrefSize() - readerSame.getXrefSize(),
                    "form XObjects with different nested content were merged");
        }
    }

    private static byte[] copyWithSmartCopy(byte[]... pdfs) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Document document = new Document()) {
            try (PdfCopy copy = new PdfSmartCopy(document, outputStream)) {
                document.open();
                for (byte[] pdf : pdfs) {
                    try (PdfReader reader = new PdfReader(pdf)) {
                        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                            copy.addPage(copy.getImportedPage(reader, page));
                        }
                        copy.freeReader(reader);
                    }
                }
            }
        }
        return outputStream.toByteArray();
    }

    private static byte[] createPdfWithNestedFormXObjects(int nestedLevels, int fanout) throws IOException {
        return createPdfWithNestedFormXObjects(nestedLevels, fanout, 10);
    }

    private static byte[] createPdfWithNestedFormXObjects(int nestedLevels, int fanout, float leafSize)
            throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4)) {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("Test PDF, nested Form XObjects"));

            List<PdfTemplate> layer = new ArrayList<>();
            for (int index = 0; index < fanout; index++) {
                PdfTemplate leaf = PdfTemplate.createTemplate(writer, 20, 20);
                leaf.rectangle(0, 0, leafSize, leafSize);
                leaf.stroke();
                layer.add(leaf);
            }
            for (int level = 1; level < nestedLevels; level++) {
                List<PdfTemplate> parents = new ArrayList<>();
                for (int index = 0; index < fanout; index++) {
                    PdfTemplate parent = PdfTemplate.createTemplate(writer, 20, 20);
                    for (PdfTemplate nested : layer) {
                        parent.addTemplate(nested, 0, 0);
                    }
                    parents.add(parent);
                }
                layer = parents;
            }
            for (PdfTemplate form : layer) {
                writer.getDirectContent().addTemplate(form, 0, 0);
            }
        }
        return outputStream.toByteArray();
    }
}
