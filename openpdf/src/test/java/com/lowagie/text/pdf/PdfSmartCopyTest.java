package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
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

}
