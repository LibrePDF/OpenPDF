/*
 * $Id: AwtImage.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package com.lowagie.examples.objects.images;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import org.junit.jupiter.api.Test;

/**
 * Using the java.awt.Image object.
 */
class AwtImage {

    /**
     * Uses a java.awt.Image object to construct a com.lowagie.text.Image object.
     */
    @Test
    void testAwtImage() throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // step 1: creation of a document-object
            Document document = new Document();

            // step 2: we create a writer that listens to the document
            // and directs a PDF-stream to a byte array
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            // step 3: we open the document
            document.open();

            // step 4: we add content to the document
            for (int i = 0; i < 300; i++) {
                document.add(new Phrase("Who is this? "));
            }
            PdfContentByte cb = writer.getDirectContent();
            final URL systemResource = ClassLoader.getSystemResource("H.gif");
            final String path = systemResource.getPath();
            java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(path);
            Image image = Image.getInstance(awtImage, null);
            image.setAbsolutePosition(100, 500);
            cb.addImage(image);
            Image gif = Image.getInstance(awtImage, new Color(0x00, 0xFF, 0xFF), true);
            gif.setAbsolutePosition(300, 500);
            cb.addImage(gif);
            Image img1 = Image.getInstance(awtImage, null, true);
            img1.setAbsolutePosition(100, 200);
            cb.addImage(img1);
            Image img2 = Image.getInstance(awtImage, new Color(0xFF, 0xFF, 0x00), false);
            img2.setAbsolutePosition(300, 200);
            cb.addImage(img2);

            // step 5: we close the document
            document.close();

            // Files.write(Paths.get("AwtImage.pdf"), outputStream.toByteArray()); // Use to write the PDF to file
            assertNotEquals(0, outputStream.size());
        }
    }
}
