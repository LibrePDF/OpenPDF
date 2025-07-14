/*
 * $Id: RawData.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.images;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Using raw data to construct an Image object.
 */
public class RawData {

    /**
     * Raw data.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("bytes[] / raw image");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file

            PdfWriter.getInstance(document, new FileOutputStream("rawdata.pdf"));

            // step 3: we open the document
            document.open();

            // step 4: we add content (example by Paulo Soares)

            // creation a jpeg passed as an array of bytes to the Image
            RandomAccessFile rf = new RandomAccessFile("otsoe.jpg", "r");
            int size = (int) rf.length();
            byte[] imext = new byte[size];
            rf.readFully(imext);
            rf.close();
            Image img1 = Image.getInstance(imext);
            img1.setAbsolutePosition(50, 500);
            document.add(img1);

            // creation of an image of 100 x 100 pixels (x 3 bytes for the Red, Green and Blue value)
            byte[] data = new byte[100 * 100 * 3];
            for (int k = 0; k < 100; ++k) {
                for (int j = 0; j < 300; j += 3) {
                    data[k * 300 + j] = (byte) (255 * Math.sin(j * .5 * Math.PI / 300));
                    data[k * 300 + j + 1] = (byte) (256 - j * 256 / 300);
                    data[k * 300 + j + 2] = (byte) (255 * Math.cos(k * .5 * Math.PI / 100));
                }
            }
            Image img2 = Image.getInstance(100, 100, 3, 8, data);
            img2.setAbsolutePosition(200, 200);
            document.add(img2);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }


}
