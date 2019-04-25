package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class PdfTestBase {

    static Document createPdf(String filename) throws DocumentException, IOException {
        // create a new file in target dir
        return createPdf(
                new FileOutputStream(
                        File.createTempFile(filename, ".pdf")));
    }

    static Document createPdf(OutputStream outputStream) throws DocumentException {
        // create a new document
        Document document = new Document(PageSize.A4);

        // generate file
        PdfWriter.getInstance(document, outputStream);
        return document;
    }

}
