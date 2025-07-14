/*
 * $Id: Bookmarks.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import org.librepdf.openpdf.examples.content.Constants;

/**
 * Creates a document with outlines (bookmarks) for each paragraph.
 *
 * @author blowagie
 */

public class ParagraphBookmarks extends PdfPageEventHelper {

    /**
     * Keeps the number of the current paragraph.
     */
    private int n = 0;

    /**
     * Creates a document with outlines for each paragraph.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Bookmarks for Paragraphs");

        // step 1: creation of a document-object
        Document document = new Document(PageSize.A6);
        try {

            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("ParagraphBookmarks.pdf"));
            // step 3:
            writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
            document.open();
            // step 4: we grab the ContentByte and do some stuff with it
            writer.setPageEvent(new ParagraphBookmarks());

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

    /**
     * Adds an outline for every new Paragraph
     *
     * @param writer   the PdfWriter that link to the pdf
     * @param document the Document to outline
     * @param position the position in the document
     */
    public void onParagraph(PdfWriter writer, Document document, float position) {
        n++;
        PdfContentByte cb = writer.getDirectContent();
        PdfDestination destination = new PdfDestination(PdfDestination.FITH, position);
        new PdfOutline(cb.getRootOutline(), destination, "paragraph " + n);
    }
}
