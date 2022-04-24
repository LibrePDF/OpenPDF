package com.lowagie.examples.objects.images;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author SE_SUSTech, group: Lanrand
 * test issue #130
 * <p> General Images example. </p>
 */
public class ImageHeicTest {

    @Test
    /**
     * ImageHeicTest1: Just test the .HEIC image
     */
    public void ImageHeicTest1() {

        // step 1: creation of a document-object
        Document document = new Document();

        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter.getInstance(document, new FileOutputStream("Images.pdf"));
            // step 3: we open the document
            document.open();

            // step 4:
            document.add(new Paragraph("doll.HEIC"));
            Image heic1 = Image.getInstance("doll.HEIC");
            document.add(heic1);
            document.close();
        } catch (DocumentException | IOException de) {
            document.close();
            throw new ExceptionConverter(de);
        }

        // step 5: we close the document
        document.close();
    }
    @Test
    /**
     * ImageHeicTest2: Test export PDF of multiple images
     */
    public void ImageHeicTest2(final String[] args) {

        // step 1: creation of a document-object
        final Document document = new Document();

        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter.getInstance(document, new FileOutputStream("Images.pdf"));
            // step 3: we open the document
            document.open();

            // step 4:
            document.add(new Paragraph("A picture of my dog: otsoe.jpg"));
            Image jpg = Image.getInstance("otsoe.jpg");
            document.add(jpg);
            document.add(new Paragraph("getacro.gif"));
            Image gif= Image.getInstance("getacro.gif");
            document.add(gif);
            document.add(new Paragraph("pngnow.png"));
            Image png = Image.getInstance("pngnow.png");
            document.add(png);
            document.add(new Paragraph("doll.HEIC"));
            Image heic1 = Image.getInstance("doll.HEIC");
            document.add(heic1);
            document.add(new Paragraph("scene.HEIC"));
            Image heic2 = Image.getInstance("scene.HEIC");
            document.add(heic2);
            document.add(new Paragraph("food.HEIC"));
            Image heic3 = Image.getInstance("food.HEIC");
            document.add(heic3);
            document.close();
        } catch (DocumentException | IOException de) {
            document.close();
            throw new ExceptionConverter(de);
        }

        // step 5: we close the document
        document.close();
    }

}