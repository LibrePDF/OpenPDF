/*
 * $Id: MultiColumnIrregular.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.
 *
 *
 */
package com.lowagie.examples.objects.columns;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.MultiColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.librepdf.openpdf.examples.content.Constants;

/**
 * An example using MultiColumnText with irregular columns.
 */
public class MultiColumnIrregular {

    /**
     * An example using MultiColumnText with irregular columns.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        try {
            // step 1
            Document document = new Document();
            OutputStream out = new FileOutputStream("multicolumnirregular.pdf");
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // calculate diamond shaped hole
            float diamondHeight = 400;
            float diamondWidth = 400;
            float gutter = 10;
            float bodyHeight = document.top() - document.bottom();
            float colMaxWidth = (document.right() - document.left() - (gutter * 2)) / 2f;
            float diamondTop = document.top() - ((bodyHeight - diamondHeight) / 2f);
            float diamondInset = colMaxWidth - (diamondWidth / 2f);
            float centerX = (document.right() - document.left()) / 2 + document.left();
            // draw stuff
            PdfContentByte cb = writer.getDirectContentUnder();

            MultiColumnText mct = new MultiColumnText(document.top() - document.bottom());

            // setup column 1
            float[] left = {document.left(), document.top(), document.left(), document.bottom()};
            float[] right = {document.left() + colMaxWidth, document.top(),
                    document.left() + colMaxWidth, diamondTop,
                    document.left() + diamondInset, diamondTop - diamondHeight / 2,
                    document.left() + colMaxWidth, diamondTop - diamondHeight,
                    document.left() + colMaxWidth, document.bottom()
            };
            mct.addColumn(left, right);

            // setup column 2
            left = new float[]{document.right() - colMaxWidth, document.top(),
                    document.right() - colMaxWidth, diamondTop,
                    document.right() - diamondInset, diamondTop - diamondHeight / 2,
                    document.right() - colMaxWidth, diamondTop - diamondHeight,
                    document.right() - colMaxWidth, document.bottom()
            };
            right = new float[]{document.right(), document.top(), document.right(), document.bottom()};
            mct.addColumn(left, right);

            // add text
            for (int i = 0; i < 8; i++) {
                mct.addElement(new Paragraph(Constants.GALLIA_EST, FontFactory.getFont(FontFactory.HELVETICA, 12)));
                mct.addElement(new Paragraph(Constants.EORUM_UNA, FontFactory.getFont(FontFactory.HELVETICA, 12)));
                mct.addElement(new Paragraph(Constants.APUD_HELVETIOS, FontFactory.getFont(FontFactory.HELVETICA, 12)));
                mct.addElement(new Paragraph(Constants.HIS_REBUS, FontFactory.getFont(FontFactory.HELVETICA, 12)));
                mct.addElement(new Paragraph(Constants.EA_RES, FontFactory.getFont(FontFactory.HELVETICA, 12)));
            }
            do {
                cb.setLineWidth(5);
                cb.setColorStroke(Color.GRAY);
                cb.moveTo(centerX, document.top());
                cb.lineTo(centerX, document.bottom());
                cb.stroke();
                cb.moveTo(centerX, diamondTop);
                cb.lineTo(centerX - (diamondWidth / 2), diamondTop - (diamondHeight / 2));
                cb.lineTo(centerX, diamondTop - diamondHeight);
                cb.lineTo(centerX + (diamondWidth / 2), diamondTop - (diamondHeight / 2));
                cb.lineTo(centerX, diamondTop);
                cb.setColorFill(Color.GRAY);
                cb.fill();
                document.add(mct);
                mct.nextColumn();
            } while (mct.isOverflow());
            document.close();
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}