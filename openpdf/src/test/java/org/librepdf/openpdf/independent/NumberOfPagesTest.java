package org.librepdf.openpdf.independent;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class NumberOfPagesTest {

    @Test
    void whenWritingHelloWorld_thenOnlyOnePageShouldBeCreated() throws IOException {
        // GIVEN
        // buffer for reading the document afterwards
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        // step 1: create a writer that listens to the document and writes to outputBuffer
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, outputBuffer);
        // step 2: open the document
        document.open();
        // step 3: add a paragraph to the document
        document.add(new Paragraph("Hello World"));
        // step 4: we close the document
        int pagesWritten = writer.getCurrentPageNumber(); // get page "count" just before the closed
        document.close();
        // WHEN
        // step 5 Read it back and count pages
        PdfReader reader = new PdfReader(outputBuffer.toByteArray());
        final int pagesRead = reader.getNumberOfPages();
        // THEN
        Assertions.assertThat(pagesWritten)
                .isEqualTo(1)
                .isEqualTo(pagesRead);
    }
}
