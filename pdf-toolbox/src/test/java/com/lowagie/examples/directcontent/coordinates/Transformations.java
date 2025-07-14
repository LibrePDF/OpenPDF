/*
 * $Id: Transformations.java 3838 2009-04-07 18:34:15Z mstorer $
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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Adding a template using different transformation matrices.
 */
public class Transformations {

    /**
     * Adding a template using different transformation matrices.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Transformations");
        // step 1: creation of a document-object
        Document document = new Document(PageSize.A4);

        try {
            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("transformations.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            PdfContentByte cb = writer.getDirectContent();

            // we create a PdfTemplate
            PdfTemplate template = cb.createTemplate(120, 120);

            // we add some graphics
            template.moveTo(30, 10);
            template.lineTo(90, 10);
            template.lineTo(90, 80);
            template.lineTo(110, 80);
            template.lineTo(60, 110);
            template.lineTo(10, 80);
            template.lineTo(30, 80);
            template.closePath();
            template.stroke();
            template.sanityCheck();

            // we add the template on different positions
            cb.addTemplate(template, 0, 0);
            cb.addTemplate(template, 0, 1, -1, 0, 200, 600);
            cb.addTemplate(template, .5f, 0, 0, .5f, 100, 400);
            cb.sanityCheck();

            // we go to a new page
            document.newPage();
            cb.addTemplate(template, 0, 500);
            cb.addTemplate(template, 2, 0, -1, 2, 200, 300);
            cb.sanityCheck();
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}
