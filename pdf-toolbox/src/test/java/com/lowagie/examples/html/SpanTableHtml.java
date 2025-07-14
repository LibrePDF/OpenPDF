package com.lowagie.examples.html;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SpanTableHtml {

    public static void main(String[] args) throws IOException {
        testRowspan();
        testColspan();
    }

    /**
     * Converts an HTML page to pdf with the table containing rolspan tags
     */
    public static void testRowspan() {
        try (Document doc = new Document(PageSize.A4)) {
            PdfWriter.getInstance(doc, Files.newOutputStream(Paths.get("testRowspanOut.pdf")));
            doc.open();
            InputStream stream = SpanTableHtml.class.getResourceAsStream("example1forHTMLWorker.html");
            HTMLWorker worker = new HTMLWorker(doc);
            worker.parse(new InputStreamReader(stream, UTF_8));
            assert (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts an HTML page to pdf with the table containing colspan tags
     */
    public static void testColspan() {
        try (Document doc = new Document(PageSize.A4)) {
            PdfWriter.getInstance(doc, Files.newOutputStream(Paths.get("testColspanOut.pdf")));
            doc.open();
            InputStream stream = SpanTableHtml.class.getResourceAsStream("example2forHTMLWorker.html");
            HTMLWorker worker = new HTMLWorker(doc);
            worker.parse(new InputStreamReader(stream, UTF_8));
            assert (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}