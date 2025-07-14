package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * @author SE_SUSTech, group: Lanrand test issue #620
 * <p>This file is to test the code for fix the bugs in the issue #620
 * and there are 2 test cases
 */

public class PdfDocument620Test {

    /**
     * According to the issue, there will be different when you use setKeepTogether and not use it in the leading.
     * <p>First, we will use three paragraph to write a document and see the pdf file.
     * We can see that after fix the issue, the leading will be same whenever you use setKeepTogether or not. When we
     * see the pdf file, the two should be same.
     */
    @Test
    public void generate2DocumentsWithShortLine() throws IOException {
        createPdf("shortLine", "Test Paragraph", true);
        createPdf("shortLine", "Test Paragraph", false);
        // the 2 documents should look the same
    }

    @Test
    public void generate2DocumentsWithLongLine() throws IOException {
        String s = "sagdageafedddddd dddddddddddddd dddddddddddddddddd ddddddddddd dddddddd" +
                "sdaffffff ffffffffffffff ffffffffffffff ffffffffffff" +
                "dsaffffffffff ffffffffffffffffff ffffffffffffff fffffffffffff" +
                "dsaffff ffffffffff fffffffffffffffff";
        createPdf("longLine", s, true);
        createPdf("longLine", s, false);
    }

    private void createPdf(String baseFileName, String lineToTest, boolean keepTogether) throws IOException {
        String pathname = baseFileName + "KeepTogether" + Boolean.toString(keepTogether).toUpperCase() + ".pdf";
        OutputStream outputStream = Files.newOutputStream(Paths.get("target", pathname));
        try (Document document = new Document()) {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("Paragraph 1", FontFactory.getFont(BaseFont.COURIER, 10)));
            Paragraph par2 = new Paragraph(lineToTest, FontFactory.getFont(BaseFont.COURIER, 10));
            par2.setLeading(24);
            par2.setKeepTogether(keepTogether);
            document.add(par2);
            Paragraph par3 = new Paragraph("Paragraph 3", FontFactory.getFont(BaseFont.COURIER, 10));
            par3.setLeading(12);
            document.add(par3);
        } catch (DocumentException ignored) {
        }
    }
}
