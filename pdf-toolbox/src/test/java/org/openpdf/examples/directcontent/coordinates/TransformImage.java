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
package org.openpdf.examples.directcontent.coordinates;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Add an image using different transformation matrices.
 */
public class TransformImage {

    /**
     * Add an image using different transformation matrices.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        Document.compress = false;
        System.out.println("Transforming an Image");
        // step 1: creation of a document-object
        Document document = new Document(PageSize.A4);

        try {
            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get("transformImage.pdf")));

            // step 3: we open the document
            document.open();

            // step 4:
            PdfContentByte cb = writer.getDirectContent();
            Image img = Image.getInstance("hitchcock.png");
            cb.addImage(img, 271, -50, -30, 550, 100, 100);
            cb.sanityCheck();

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
        Document.compress = true;
    }
}
