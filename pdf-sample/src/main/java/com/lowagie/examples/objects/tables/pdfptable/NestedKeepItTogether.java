package com.lowagie.examples.objects.tables.pdfptable;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;

public class NestedKeepItTogether {

    public static void main(String[] args) {
        System.out.println("Objects :: Tables ::Nested Table Keeping it together");

        try (Document document = new Document()) {
            PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(args[0] + "/nestedTableKeepItTogether.pdf"));
            document.open();

            PdfPTable table = new PdfPTable(2);
            table.addCell("Lorem");
            table.addCell("ipsum");
            table.addCell("dolor");
            table.addCell("sit");

            Paragraph mainParagraph = new Paragraph();
            Paragraph paragraph1 = new Paragraph("Text above table");
            Paragraph paragraph2 = new Paragraph("Text below table");

            mainParagraph.add(paragraph1);
            mainParagraph.add(table);
            mainParagraph.add(paragraph2);

            // if keeptogether is true, table is not visible
            mainParagraph.setKeepTogether(true);

            document.add(mainParagraph);
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}
