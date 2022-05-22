package com.lowagie.text;

import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ChunkTest {
    @Test
    void shouldReturnChunkMCID() throws Exception {
        PdfDocument  document = new PdfDocument();
        document.open();
        PdfAction action = new PdfAction("https://www.google.com");
        Chunk chunk=new Chunk("https://www.google.com");
        chunk.setAction(action);
        chunk.setGenericTag("LINK");
        document.add(chunk);
        document.close();
        assertNotNull(chunk.getMCID());
    }

}
