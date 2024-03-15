/*
 * $Id: Images.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */
package com.lowagie.examples.html;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.HtmlWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * General Images example.
 */
public class Images {

    /**
     * General Images example that generates an HTML file with wrong paths to the images where the paths are right.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Images in HTML");

        // step 1: creation of a document-object
        Document document = new Document();

        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            HtmlWriter.getInstance(document, new FileOutputStream("images_wrong.html"));
            HtmlWriter writer = HtmlWriter.getInstance(document, new FileOutputStream("images_right.html"));
            writer.setImagepath("./");

            // step 3: we open the document
            document.open();

            // step 4:
            document.add(new Paragraph("A picture of my dog: otsoe.jpg"));
            Image jpg = Image.getInstance("otsoe.jpg");
            document.add(jpg);
            document.add(new Paragraph("getacro.gif"));
            Image gif = Image.getInstance("getacro.gif");
            document.add(gif);
            document.add(new Paragraph("pngnow.png"));
            Image png = Image.getInstance("pngnow.png");
            document.add(png);
            document.add(new Paragraph("iText.bmp"));
            Image bmp = Image.getInstance("iText.bmp");
            document.add(bmp);
            document.add(new Paragraph("iText.wmf"));
            Image wmf = Image.getInstance("iText.wmf");
            document.add(wmf);
            document.add(new Paragraph("iText.tif"));
            Image tiff = Image.getInstance("iText.tif");
            document.add(tiff);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}