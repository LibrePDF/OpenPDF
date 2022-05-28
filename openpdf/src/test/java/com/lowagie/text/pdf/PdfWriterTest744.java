package com.lowagie.text.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfWriterTest744 {
    @Test
    public void pdfDocumentTest1() throws IOException {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("myTest.pdf"));
            writer.setPdfVersion(PdfWriter.VERSION_1_5);
            document.open();
            PdfLayer layer = new PdfLayer("test", writer);
            BaseFont font = BaseFont.createFont(BaseFont.HELVETICA,
                    BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            PdfContentByte bytes = writer.getDirectContent();
            bytes.beginText();
            bytes.setTextMatrix(40, 800);
            bytes.setLeading(16);
            bytes.setFontAndSize(font, 16);
            bytes.showText("test");
            bytes.beginLayer(layer);
            bytes.newlineShowText("gg");
            bytes.endLayer();
            bytes.endText();
            bytes.sanityCheck();
            document.close();

        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        try {
            PdfReader reader;
            PdfStamper pdfstamper;
            reader = new PdfReader("myTest.pdf");
            pdfstamper = new PdfStamper(reader, new FileOutputStream("myTest2.pdf"));
            System.out.println(pdfstamper.getPdfLayers());
            pdfstamper.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
