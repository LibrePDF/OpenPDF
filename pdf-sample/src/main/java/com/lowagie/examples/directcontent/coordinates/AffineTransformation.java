/*
 * $Id: AffineTransformation.java 3838 2009-04-07 18:34:15Z mstorer $
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
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.geom.AffineTransform;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Uses the AffineTransform class to change the transformation matrix.
 */
public class AffineTransformation  extends AbstractSample {

    @Override
    public String getFileName() {
        return "/affine_transformation";
    }

    public static void main(String[] args) {
        AffineTransformation templates = new AffineTransformation();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("DirectContent :: Coordinates :: Affine Transformation");

        // tag::generation[]
        // step 1: creation of a document-object
        try (Document document = new Document(PageSize.A4)) {
            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path + getFileName() + ".pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            PdfContentByte cb = writer.getDirectContent();
            cb.transform(AffineTransform.getScaleInstance(1.2, 0.75));

            // we create a PdfTemplate
            PdfTemplate template = cb.createTemplate(25, 25);

            // we add some crosses to visualize the coordinates
            template.moveTo(13, 0);
            template.lineTo(13, 25);
            template.moveTo(0, 13);
            template.lineTo(50, 13);
            template.stroke();
            template.sanityCheck();

            // we add the template on different positions
            cb.addTemplate(template, 216 - 13, 720 - 13);
            cb.addTemplate(template, 360 - 13, 360 - 13);
            cb.addTemplate(template, 360 - 13, 504 - 13);
            cb.addTemplate(template, 72 - 13, 144 - 13);
            cb.addTemplate(template, 144 - 13, 288 - 13);

            cb.moveTo(216, 720);
            cb.lineTo(360, 360);
            cb.lineTo(360, 504);
            cb.lineTo(72, 144);
            cb.lineTo(144, 288);
            cb.stroke();

            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            cb.beginText();
            cb.setFontAndSize(bf, 12);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "(3\" * 1.2, 10\" * .75)", 216 + 25, 720 + 5, 0);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "(5\" * 1.2, 5\" * .75)", 360 + 25, 360 + 5, 0);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "(5\" * 1.2, 7\" * .75)", 360 + 25, 504 + 5, 0);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "(1\" * 1.2, 2\" * .75)", 72 + 25, 144 + 5, 0);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "(2\" * 1.2, 4\" * .75)", 144 + 25, 288 + 5, 0);
            cb.endText();

            cb.sanityCheck();
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        // end::generation[]
    }
}
