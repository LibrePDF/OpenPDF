package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import java.io.ByteArrayOutputStream;

public class DocumentProducerHelper {

    public static byte[] createHelloWorldDocumentBytes() {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, stream);
        document.open();
        document.add(new Paragraph("Hello World"));
        document.close();
        return stream.toByteArray();
    }
}
