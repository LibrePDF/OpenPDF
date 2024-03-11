package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import java.io.File;
import java.io.FileOutputStream;
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

}
