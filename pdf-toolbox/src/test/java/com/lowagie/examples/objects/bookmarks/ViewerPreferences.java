/*
 * $Id: ViewerPreferences.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects.bookmarks;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import org.librepdf.openpdf.examples.content.Constants;

/**
 * Creates a document with different viewerpreferences.
 *
 * @author blowagie
 */

public class ViewerPreferences {

    /**
     * Creates documents with different viewerpreferences.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Viewerpreferences");

        // step 1: creation of a document-object
        Document document = new Document(PageSize.A6);
        try {

            // step 2:
            PdfWriter writer1 = PdfWriter.getInstance(document,
                    new FileOutputStream("TwoColumnLeft.pdf"));
            PdfWriter writer2 = PdfWriter.getInstance(document,
                    new FileOutputStream("HideMenuToolbar.pdf"));
            PdfWriter writer3 = PdfWriter.getInstance(document,
                    new FileOutputStream("FullScreen.pdf"));
            PdfWriter writer4 = PdfWriter.getInstance(document,
                    new FileOutputStream("WithTitle.pdf"));
            PdfWriter writer5 = PdfWriter.getInstance(document,
                    new FileOutputStream("NoScaling.pdf"));
            // step 3:
            writer1.setViewerPreferences(PdfWriter.PageLayoutTwoColumnLeft);
            writer2.setViewerPreferences(PdfWriter.HideMenubar);
            writer3.setViewerPreferences(PdfWriter.PageLayoutTwoColumnRight
                    | PdfWriter.PageModeFullScreen
                    | PdfWriter.NonFullScreenPageModeUseThumbs);
            writer4.setViewerPreferences(PdfWriter.DisplayDocTitle);
            writer5.setViewerPreferences(PdfWriter.PrintScalingNone);
            document.addTitle("Julius Caesar");
            document.open();
            // step 4: we grab the ContentByte and do some stuff with it
            document.add(new Paragraph(Constants.GALLIA_EST, new Font(Font.HELVETICA, 12)));
            document.add(new Paragraph(Constants.EORUM_UNA, new Font(Font.HELVETICA, 12)));
            document.add(new Paragraph(Constants.APUD_HELVETIOS, new Font(Font.HELVETICA, 12)));
            document.add(new Paragraph(Constants.HIS_REBUS, new Font(Font.HELVETICA, 12)));
            document.add(new Paragraph(Constants.EA_RES, new Font(Font.HELVETICA, 12)));
        } catch (Exception de) {
            de.printStackTrace();
        }

        // step 5: we close the document
        document.close();
    }
}