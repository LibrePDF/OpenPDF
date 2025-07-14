/*
 * $Id: LocalGoto.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.conformance;

import static com.lowagie.text.pdf.PdfWriter.PDFA1B;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.xml.xmp.XmpWriter;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Creates a document validating PDF/A-1b conformance
 *
 * @author raodienne
 */

public class PdfA1B {

    /**
     * Creates a document validating PDF/A-1b conformance
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {
        System.out.println("PDF/A-1b");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2: create writer and set conformance
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("PdfA1b.pdf"));
            writer.setPDFXConformance(PDFA1B);

            // step 3: open the document
            document.open();

            // create pdf dictionary with required entry
            PdfDictionary pdfDictionary = new PdfDictionary();
            pdfDictionary.put(PdfName.CREATIONDATE, writer.getInfo().get(PdfName.CREATIONDATE));
            pdfDictionary.put(PdfName.MODDATE, writer.getInfo().get(PdfName.MODDATE));

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                // embed dictionary in XmpWriter
                XmpWriter xmpWriter = new XmpWriter(baos, pdfDictionary, PdfWriter.PDFA1B);
                xmpWriter.close();

                byte[] xmpMetadata = baos.toByteArray();

                // set xmp metadata and output intents
                writer.setXmpMetadata(xmpMetadata);
                writer.setOutputIntents("Custom", "", null, "sRGB IEC61966-2.1",
                        ICC_Profile.getInstance(ColorSpace.CS_sRGB));
            }

            // step 4:

            // we make some content
            String fontDir = "com/lowagie/examples/fonts/";

            BaseFont notoBase = BaseFont.createFont(
                    Objects.requireNonNull(
                            PdfA1B.class.getClassLoader().getResource(fontDir + "noto/NotoSans-Regular.ttf")).getFile(),
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font noto = new Font(notoBase, 10, Font.NORMAL);

            // a paragraph
            Paragraph p1 = new Paragraph("This document is compliant with PDF-A/1b requirements", noto);

            // some paragraph
            Paragraph p2 = new Paragraph("blah, blah, blah", noto);

            // we add the content
            document.add(p1);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
            document.add(p2);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}