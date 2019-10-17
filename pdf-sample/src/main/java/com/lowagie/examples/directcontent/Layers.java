/*
 * $Id: Layers.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent;

import com.lowagie.examples.AbstractSample;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Explains the concept concerning PdfContentByte layers.
 */
public class Layers extends AbstractSample {

    @Override
    public int getExpectedPageCount() {
        return 1;
    }

    @Override
    public String getFileName() {
        return "/layers";
    }

    public static void main(String[] args) {
        Layers layers = new Layers();
        layers.run(args);
    }

    /**
     * Draws different things into different layers.
     *
     * @param path no arguments needed
     */
    @Override
    public void render(String path) {

        System.out.println("DirectContent :: Layers");

        // tag::generation[]
        // step 1: creation of a document-object
        try (Document document = new Document()) {

            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path + getFileName() + ".pdf"));

            // step 3: we open the document
            document.open();

            // step 4: add element to the PDF

            // high level
            Paragraph p = new Paragraph();
            for (int i = 0; i < 100; i++) p.add(new Chunk("Blah blah blah blah blah. "));
            document.add(p); // <1>

            Image img = Image.getInstance(Layers.class.getClassLoader().getResource("hitchcock.png"));
            img.setAbsolutePosition(100, 500);
            document.add(img); // <2>

            // low level
            PdfContentByte cb = writer.getDirectContent(); // <3>
            cb.setRGBColorFill(0xFF, 0xFF, 0xFF);
            cb.circle(250.0f, 500.0f, 50.0f);
            cb.fill();
            cb.sanityCheck();

            PdfContentByte cbu = writer.getDirectContentUnder(); // <4>
            cbu.setRGBColorFill(0xFF, 0x00, 0x00);
            cbu.circle(250.0f, 500.0f, 100.0f);
            cbu.fill();
            cbu.sanityCheck();

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        // end::generation[]
    }
}
