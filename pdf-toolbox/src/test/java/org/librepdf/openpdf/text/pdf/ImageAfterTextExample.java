package org.librepdf.openpdf.text.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class ImageAfterTextExample {

    public static void main(String[] args) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, Files.newOutputStream(Paths.get("ImageAfterTextExample.pdf")));

        document.open();
        document.add(new Paragraph("----"));
        // 1st way: Put text and image in a table
        Paragraph p = imageInTableInParagraph();
        document.add(p);
        document.add(new Paragraph("----"));

        // 2nd way: Put the image underlying, and create some space for the before and after the image
        Paragraph p2 = imageInParagraphWithIndentation();
        document.add(p2);
        document.add(new Paragraph("----"));

        // 3rd way: Image in Chunk
        Paragraph p3 = imageInChunkInParagraph();
        document.add(p3);
        document.add(new Paragraph("----"));

        document.close();
    }

    private static Paragraph imageInTableInParagraph() throws IOException {
        Paragraph p = new Paragraph("Text before table.");

        // setup table
        PdfPTable table = new PdfPTable(new float[]{82, 15});
        table.setTotalWidth(128);
        table.setLockedWidth(true);
        PdfPCell defaultCell = table.getDefaultCell();
        defaultCell.setBorder(0);
        // create table content
        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(0);
        textCell.setPaddingTop(-5);
        textCell.addElement(new Paragraph("Text before image."));
        table.addCell(textCell);
        table.addCell(getImage());
        p.add(table);

        p.add("Text after table in a new line.");
        return p;
    }

    private static Paragraph imageInParagraphWithIndentation() throws IOException {
        Paragraph p2 = new Paragraph("Text before image ");

        Image image = getImage();
        image.setAlignment(Image.UNDERLYING);
        image.scalePercent(25f);
        image.setIndentationLeft(100); // this will put the image to the right of the previous text

        p2.add(image);
        System.out.printf("p2 leadind: " + p2.getTotalLeading());

        String spaces = "    ";
        p2.add(spaces); // this is necessary, so the following text is not written over the image.
        p2.add(" text after image");
        return p2;
    }

    private static Paragraph imageInChunkInParagraph() throws IOException {
        Paragraph p3 = new Paragraph("Words before chunk ");
        Image image = getImage();
        image.scalePercent(25f);
        p3.add(new Chunk(image, 0, -5, true));
        p3.add(" words after chunk.");
        return p3;
    }

    private static Image getImage() throws IOException {
        URL resource = ImageAfterTextExample.class.getResource("/cc-test-64x64.png");
        return Image.getInstance(Objects.requireNonNull(resource));
    }
}
