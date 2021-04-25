package com.lowagie.text;

import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.stream.IntStream;

public class PdfPTableTest {
    @Test
    void writePDF() throws FileNotFoundException {
        File file = new File("output.pdf");
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));

        document.open();
        IntStream.range(0, 30)
                .mapToObj(idx -> new Paragraph("Fill line"))
                .forEach(document::add);

        PdfPTable table = new PdfPTable(1);
        table.setSplitLate(false);

        IntStream.range(0, 3)
                .mapToObj(idx -> new Paragraph("Row " + idx + " Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi eu cursus purus, id efficitur arcu. " +
                        "Pellentesque eu arcu elit. Phasellus in mattis diam. Donec in augue eget ligula consectetur pretium. Maecenas scelerisque non massa ut faucibus. " +
                        "Nam sit amet malesuada nisl. Duis varius vitae leo venenatis ullamcorper. Morbi at vestibulum tortor, vel blandit erat."))
                .forEach(table::addCell);

        table.addCell("Line 1\nLine 2\nThis line should be in the PDF\nLine 4");
        document.add(table);
        document.close();
    }
}
