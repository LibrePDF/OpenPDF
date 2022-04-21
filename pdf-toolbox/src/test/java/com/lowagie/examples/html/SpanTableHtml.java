package com.lowagie.examples.html;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;

import java.io.*;

public class SpanTableHtml {
    public static void main(String[] args) {
        testRowspan(args);
        testColspan(args);
    }

    public static void testRowspan(String[] args) {
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = null;
        try {
            File out = new File("testOut1.pdf");
            writer = PdfWriter.getInstance(doc, new FileOutputStream(out));
            doc.open();
            InputStream stream = SpanTableHtml.class.getResourceAsStream("example1forHTMLWorker.html");
            HTMLWorker worker = new HTMLWorker(doc);
            worker.parse(new InputStreamReader(stream));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            doc.close();
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void testColspan(String[] args) {
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = null;
        try {
            File out = new File("testOut2.pdf");
            writer = PdfWriter.getInstance(doc, new FileOutputStream(out));
            doc.open();
            InputStream stream = SpanTableHtml.class.getResourceAsStream("example2forHTMLWorker.html");
            HTMLWorker worker = new HTMLWorker(doc);
            worker.parse(new InputStreamReader(stream));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            doc.close();
            if (writer != null) {
                writer.close();
            }
        }
    }
}
