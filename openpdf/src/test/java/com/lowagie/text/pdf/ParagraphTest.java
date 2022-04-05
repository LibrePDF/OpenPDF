package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
//The test is test adding text and pictures to paragraph.
public class ParagraphTest {
    @Test
    public void testParagraphAdd() throws IOException {
        Document document = new Document(PageSize.A4);
        //!Important!The name of test.png should be your file's name.
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("test.pdf"));
        //!Important!The name of test.png should be your file's name.
        Image jpg = Image.getInstance("test.png");

        String test = "test";
        Paragraph paragraph = new Paragraph();
        paragraph.add(test);
        paragraph.add(jpg);
        paragraph.add(test);
        paragraph.add(jpg);

        document.open();
        document.add(paragraph);
        document.close();
    }
}
