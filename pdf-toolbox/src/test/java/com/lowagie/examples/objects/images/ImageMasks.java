/*
 * $Id: ImageMasks.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Applying a mask to an Image.
 */
public class ImageMasks {

    /**
     * Applying masks to images.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("masked images");

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("maskedImages.pdf"));

            document.open();
            Paragraph p = new Paragraph("Some text behind a masked image.");
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);

            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            document.add(p);
            PdfContentByte cb = writer.getDirectContent();
            byte[] maskr = {(byte) 0x3c, (byte) 0x7e, (byte) 0xe7, (byte) 0xc3, (byte) 0xc3, (byte) 0xe7, (byte) 0x7e,
                    (byte) 0x3c};
            Image mask = Image.getInstance(8, 8, 1, 1, maskr);
            mask.makeMask();
            mask.setInverted(true);
            Image image = Image.getInstance("otsoe.jpg");
            image.setImageMask(mask);
            image.setAbsolutePosition(60, 550);
            // explicit masking
            cb.addImage(image);
            // stencil masking
            cb.setRGBColorFill(255, 0, 0);
            cb.addImage(mask, mask.getScaledWidth() * 8, 0, 0, mask.getScaledHeight() * 8, 100, 450);
            cb.setRGBColorFill(0, 255, 0);
            cb.addImage(mask, mask.getScaledWidth() * 8, 0, 0, mask.getScaledHeight() * 8, 100, 400);
            cb.setRGBColorFill(0, 0, 255);
            cb.addImage(mask, mask.getScaledWidth() * 8, 0, 0, mask.getScaledHeight() * 8, 100, 350);
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }

}