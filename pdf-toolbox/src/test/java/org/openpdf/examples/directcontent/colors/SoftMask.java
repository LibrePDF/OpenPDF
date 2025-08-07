/*
 * $Id: SoftMask.java 3838 2009-04-07 18:34:15Z mstorer $
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
package org.openpdf.examples.directcontent.colors;

import java.io.FileOutputStream;
import org.openpdf.text.Document;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Demonstrates transparency and images.
 */
public class SoftMask {

    /**
     * Demonstrates the Transparency functionality.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("SoftMask");
        // step 1: creation of a document-object
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            // step 2: creation of a writer 
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("softmask.pdf"));
            // step 3: we open the document
            document.open();
            // step 4: content
            PdfContentByte cb = writer.getDirectContent();
            String text = "text ";
            text += text;
            text += text;
            text += text;
            text += text;
            text += text;
            text += text;
            text += text;
            text += text;
            document.add(new Paragraph(text));
            Image img = Image.getInstance("otsoe.jpg");
            img.setAbsolutePosition(100, 550);
            byte[] gradient = new byte[256];
            for (int k = 0; k < 256; ++k) {
                gradient[k] = (byte) k;
            }
            Image smask = Image.getInstance(256, 1, 1, 8, gradient);
            smask.makeMask();
            img.setImageMask(smask);
            cb.addImage(img);
            cb.sanityCheck();
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step 5: we close the document
        document.close();
    }
}