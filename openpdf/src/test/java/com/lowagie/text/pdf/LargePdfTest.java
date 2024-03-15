package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * This will create a file which is > 2 GB, then attempt to read it. This will crash, because OpenPDF doesn't support
 * reading PDF files larger than 2 GB now.
 */
class LargePdfTest {

    private File largeFile;

    @BeforeEach
    void before() throws IOException {
        largeFile = Files.createTempFile("largePDFFile", ".pdf").toFile();
    }

    @AfterEach
    void after() throws IOException {
        Files.deleteIfExists(largeFile.toPath());
    }

    @Disabled("Because it takes too long to run this test")
    @Test
    void writeLargePdf() throws Exception {
        Document document = PdfTestBase.createPdf(
                Files.newOutputStream(largeFile.toPath()));

        document.open();
        document.newPage();
        String longString = "Hello Very Large World";
        for (long i = 0; i < 10; i++) {
            longString += longString;
        }

        for (long i = 0; i < 19500; i++) {
            Image jpg = Image.getInstance(
                    "../pdf-toolbox/src/test/java/com/lowagie/examples/objects/images/sunflower-back.jpg");
            document.add(jpg);
            document.add(new Paragraph(longString));
        }
        document.close();

        String canonicalPath = largeFile.getCanonicalPath();

        // This will fail now.
        assertThatThrownBy(() -> new PdfReader(canonicalPath))
                .isInstanceOf(PdfException.class);
    }

}
