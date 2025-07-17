package org.librepdf.openpdf.fonts;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FontsTestUtil {

    static Document createPdf(OutputStream outputStream) throws DocumentException {
        // create a new document
        Document document = new Document(PageSize.A4);
        // generate file
        PdfWriter.getInstance(document, outputStream);
        return document;
    }

    static Document createPdf(String filename) throws FileNotFoundException {
        FileOutputStream outputStream = new FileOutputStream(filename);
        return createPdf(outputStream);
    }

}
