package com.lowagie.text;

import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FooterTableTest {

    @Test
    public void imageLeftAlignmentPositionTest() throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new ByteArrayOutputStream());

        Image jpg = Image.getInstance(
                Objects.requireNonNull(getClass().getClassLoader().getResource("GitHub-Mark-32px.png")));
        jpg.setAlignment(Image.RIGHT);

        PdfPTable table = new PdfPTable(3);
        table.getDefaultCell().setBorder(Table.NO_BORDER);
        table.addCell("1.1");
        table.addCell("1.2");
        table.addCell("1.3");
        table.addCell("2.1");
        table.addCell(new Phrase("center"));
        table.addCell("2.3");
        table.addCell("3.1");
        table.addCell("3.2");
        table.addCell("3.3");

        Paragraph footerParagraph = new Paragraph();
        ;
        footerParagraph.add(jpg);
        footerParagraph.add(table);

        HeaderFooter footer = new HeaderFooter(footerParagraph, false);
        document.setFooter(footer);

        document.open();
        document.add(new Paragraph("This is a test line."));
        document.add(new Paragraph("Second line"));
        document.newPage();
        document.add(new Paragraph("second"));
        document.newPage();
        document.add(new Paragraph("third"));
        document.close();

        float tableHeight = table.getTotalHeight();
        float footerTop = footer.getTop();
        float tableBottom = footerTop - table.getTotalHeight();
        Assertions.assertEquals(48.0, tableHeight);
        Assertions.assertEquals(76.0, footerTop);
        Assertions.assertEquals(28.0, tableBottom);
    }
}
