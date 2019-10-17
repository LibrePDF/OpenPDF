/*
 * $Id: TransformImage.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent.coordinates;

import com.lowagie.examples.AbstractSample;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Add an image using different transformation matrices.
 */
public class TransformImage  extends AbstractSample {

    @Override
    public String getFileName() {
        return "/transform_image";
    }

    public static void main(String[] args) {
        TransformImage templates = new TransformImage();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("DirectContent :: Coordinates :: Transforming an Image");

        // tag::generation[]
        Document.compress = false;
        // step 1: creation of a document-object
        try (Document document = new Document(PageSize.A4)) {
            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path + getFileName() + ".pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            PdfContentByte cb = writer.getDirectContent();
            Image img = Image.getInstance(TransformImage.class.getClassLoader().getResource("hitchcock.png"));
            cb.addImage(img, 271, -50, -30, 550, 100, 100);
            cb.sanityCheck();

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        // end::generation[]
    }
}
