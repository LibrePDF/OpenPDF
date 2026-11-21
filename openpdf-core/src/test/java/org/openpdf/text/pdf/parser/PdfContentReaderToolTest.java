package org.openpdf.text.pdf.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PRIndirectReference;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

class PdfContentReaderToolTest {

    private static final PdfName POPUP = new PdfName("Popup");

    private static PdfReader readerWithCircularDictionaries() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("Hello"));
        document.close();

        PdfReader reader = new PdfReader(baos.toByteArray());
        // An annotation and its popup refer to each other, as in the file of issue #893
        PdfDictionary annotation = new PdfDictionary(PdfName.ANNOT);
        PdfDictionary popup = new PdfDictionary(PdfName.ANNOT);
        PRIndirectReference annotationRef = reader.addPdfObject(annotation);
        PRIndirectReference popupRef = reader.addPdfObject(popup);
        annotation.put(POPUP, popupRef);
        popup.put(PdfName.PARENT, annotationRef);
        reader.getPageN(1).put(new PdfName("Annot"), annotationRef);
        return reader;
    }

    @Test
    void getDictionaryDetailStopsAtCircularReference() throws Exception {
        PdfReader reader = readerWithCircularDictionaries();

        String detail = PdfContentReaderTool.getDictionaryDetail(reader.getPageN(1));

        assertTrue(detail.contains("Subdictionary /Popup = "), detail);
        assertTrue(detail.contains("Subdictionary /Parent = "), detail);
        assertTrue(detail.contains("(circular reference)"), detail);
    }

    @Test
    void listContentStreamForPageHandlesCircularReference() throws Exception {
        PdfReader reader = readerWithCircularDictionaries();
        StringWriter out = new StringWriter();

        PdfContentReaderTool.listContentStreamForPage(reader, 1, new PrintWriter(out));

        assertTrue(out.toString().contains("Hello"), out.toString());
    }
}
