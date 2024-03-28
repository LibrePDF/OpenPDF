package com.lowagie.examples.objects;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates some PdfTextExtractor functionality
 */
public class TextExtraction {
    public static void main(String[] args) {

        System.out.println("Text extraction");

        // step 1: create a document object
        Document document = new Document();

        // step 2: write some text to the document
        ByteArrayOutputStream baos = writeTextToDocument(document);

        try {
            // step 3: extract the text
            PdfReader reader = new PdfReader(baos.toByteArray());
            PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);
            System.out.println("Page 1 text: " + pdfTextExtractor.getTextFromPage(1));
            System.out.println("Page 2 text: " + pdfTextExtractor.getTextFromPage(2));
            System.out.println("Page 3 table cell text: " + pdfTextExtractor.getTextFromPage(3));

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
    }

    private static ByteArrayOutputStream writeTextToDocument(Document document) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();
            writer.getInfo().put(PdfName.CREATOR, new PdfString(Document.getVersion()));
            document.add(new Paragraph("Text to extract"));

            document.newPage();
            document.add(new Paragraph("Text on page 2"));

            document.newPage();
            PdfPTable table = new PdfPTable(3);
            table.addCell("Cell 1");
            table.addCell("Cell 2");
            table.addCell("Cell 3");
            document.add(table);

            document.close();

            FileOutputStream fos = new FileOutputStream("TextExtraction.pdf");
            fos.write(baos.toByteArray());
            fos.close();

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        return baos;
    }
}