package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;

/**
 * This will create a file which is ~14GB, then attempt to read it.
 * This will crash, because OpenPDF doesn't support reading PDF files
 * larger than 2GB now.
 *
 */
public class LargePdfTest {

    @Disabled
    @Test
    void writeLargePdf() throws Exception {
        File largeFile = File.createTempFile("largePDFFile", ".pdf");
        Document document = PdfTestBase.createPdf(
                new FileOutputStream(largeFile));

        document.open();
        document.newPage();
        String longString = "Hello Very Large World";
        for (long i = 0; i < 10; i++) {
            longString += longString;
        }

        for (long i = 0; i < 120000; i++) {
            Image jpg = Image.getInstance("../pdf-toolbox/src/test/java/com/lowagie/examples/objects/images/sunflower-back.jpg");
            document.add(jpg);
            document.add(new Paragraph(longString));
        }
        document.close();

        // This will fail now.
        PdfReader r = new PdfReader(largeFile.getCanonicalPath());


    }

}
