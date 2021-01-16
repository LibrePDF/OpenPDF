/*
 * $Id: ColumnIrregular.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.columns;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import org.librepdf.openpdf.examples.content.Constants;

/**
 * Writes text in irregular columns that can be added at an absolute position.
 */
public class ColumnIrregular {

    /**
     * Demonstrates the use of ColumnText.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Irregular Columns");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("columnirregular.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            // we grab the contentbyte and do some stuff with it
            PdfContentByte cb = writer.getDirectContent();

            PdfTemplate t = cb.createTemplate(600, 800);
            Image caesar = Image.getInstance("caesar_coin.jpg");
            cb.addImage(caesar, 100, 0, 0, 100, 260, 595);
            t.setGrayFill(0.75f);
            t.moveTo(310, 112);
            t.lineTo(280, 60);
            t.lineTo(340, 60);
            t.closePath();
            t.moveTo(310, 790);
            t.lineTo(310, 710);
            t.moveTo(310, 580);
            t.lineTo(310, 122);
            t.stroke();
            cb.addTemplate(t, 0, 0);

            ColumnText ct = new ColumnText(cb);
            ct.addText(new Phrase(Constants.GALLIA_EST, FontFactory.getFont(FontFactory.HELVETICA, 12)));
            ct.addText(new Phrase(Constants.EORUM_UNA, FontFactory.getFont(FontFactory.HELVETICA, 12)));
            ct.addText(new Phrase(Constants.APUD_HELVETIOS, FontFactory.getFont(FontFactory.HELVETICA, 12)));
            ct.addText(new Phrase(Constants.HIS_REBUS, FontFactory.getFont(FontFactory.HELVETICA, 12)));
            ct.addText(new Phrase(Constants.EA_RES, FontFactory.getFont(FontFactory.HELVETICA, 12)));

            float[] left1 = {70, 790, 70, 60};
            float[] right1 = {300, 790, 300, 700, 240, 700, 240, 590, 300, 590, 300, 106, 270, 60};
            float[] left2 = {320, 790, 320, 700, 380, 700, 380, 590, 320, 590, 320, 106, 350, 60};
            float[] right2 = {550, 790, 550, 60};

            int status = 0;
            int column = 0;
            while ((status & ColumnText.NO_MORE_TEXT) == 0) {
                if (column == 0) {
                    ct.setColumns(left1, right1);
                    column = 1;
                } else {
                    ct.setColumns(left2, right2);
                    column = 0;
                }
                status = ct.go();
                ct.setYLine(790);
                ct.setAlignment(Element.ALIGN_JUSTIFIED);
                status = ct.go();
                if ((column == 0) && ((status & ColumnText.NO_MORE_COLUMN) != 0)) {
                    document.newPage();
                    cb.addTemplate(t, 0, 0);
                    cb.addImage(caesar, 100, 0, 0, 100, 260, 595);
                }
            }
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}
