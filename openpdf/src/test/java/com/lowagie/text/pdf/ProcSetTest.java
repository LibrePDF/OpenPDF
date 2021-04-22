package com.lowagie.text.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ProcSetTest {
    @Test
    public void procSetTest1() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, stream);
        document.open();
        document.add(Chunk.NEWLINE);
        document.close();
        PdfReader reader = new PdfReader(stream.toByteArray());
        Assertions.assertNull(reader.getPageN(1).getAsDict(PdfName.RESOURCES).get(PdfName.PROCSET));
    }
}
