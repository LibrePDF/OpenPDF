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
package com.lowagie.examples.directcontent.colors;

import com.lowagie.examples.AbstractSample;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;

/**
 * Demonstrates transparency and images.
 */
public class SoftMask  extends AbstractSample {

    @Override
    public String getFileName() {
        return "/softmask";
    }

    public static void main(String[] args) {
        SoftMask templates = new SoftMask();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("DirectContent :: Colors :: SoftMask");

        // tag::generation[]
        // step 1: creation of a document-object
        try (Document document = new Document(PageSize.A4, 50, 50, 50, 50)) {
            // step 2: creation of a writer 
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path + getFileName() + ".pdf"));
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
            Image img = Image.getInstance(SoftMask.class.getClassLoader().getResource("otsoe.jpg"));
            img.setAbsolutePosition(100, 550);
            byte[] gradient = new byte[256];
            for (int k = 0; k < 256; ++k)
                gradient[k] = (byte) k;
            Image smask = Image.getInstance(256, 1, 1, 8, gradient);
            smask.makeMask();
            img.setImageMask(smask);
            cb.addImage(img);
            cb.sanityCheck();
        } catch (Exception de) {
            de.printStackTrace();
        }
        // end::generation[]
    }
}