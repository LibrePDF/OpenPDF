package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
/**
 * @author  SE_SUSTech, group: Lanrand
 * test issue #687
 * <p>This file is to test the code for fix the bugs in the issue #687
 * and there are 2 test cases
 */
public class ParagraphTest {
    /**
     * This bug occurs when the user stores text and images using a Paragraph object,
     * the first stored text and images overlap.
     * After I fix this bug,
     * I use Paragraph again to store the words and images, and there is no overlap.
     */
    @Test
    public void testParagraphAdd() throws IOException {
        Document document = new Document(PageSize.A4);
        //!Important!The name of test.png should be your file's name.
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("E://test.pdf"));
        //!Important!The name of test.png should be your file's name.
        Image jpg = Image.getInstance("E://test.png");

        String test = "test";
        Paragraph paragraph = new Paragraph();
        paragraph.add(test);
        paragraph.add(jpg);

        document.open();
        document.add(paragraph);
        document.close();
    }

    @Test
    public void testParagraphAdd2() throws IOException {
        Document document = new Document(PageSize.A4);
        //!Important!The name of test.png should be your file's name.
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("E://test.pdf"));
        //!Important!The name of test.png should be your file's name.
        Image jpg = Image.getInstance("E://test.png");

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
