/*
 * $Id: TwoOnOne.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.general.copystamp;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Reads the pages of an existing PDF file and puts 2 pages from the existing doc into one of the new doc.
 */
public class TwoOnOne {

    /**
     * Reads the pages of an existing PDF file and puts 2 pages from the existing doc into one of the new doc.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Import pages as images");
        try {
            // we create a reader for a certain document
            PdfReader reader = new PdfReader("ChapterSection.pdf");
            // we retrieve the total number of pages
            int n = reader.getNumberOfPages();
            // we retrieve the size of the first page
            Rectangle psize = reader.getPageSize(1);
            float width = psize.getHeight();
            float height = psize.getWidth();

            // step 1: creation of a document-object
            Document document = new Document(new Rectangle(width, height));
            // step 2: we create a writer that listens to the document
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("2on1.pdf"));
            // step 3: we open the document
            document.open();
            // step 4: we add content
            PdfContentByte cb = writer.getDirectContent();
            int i = 0;
            int p = 0;
            while (i < n) {
                document.newPage();
                p++;
                i++;
                PdfImportedPage page1 = writer.getImportedPage(reader, i);
                cb.addTemplate(page1, .5f, 0, 0, .5f, 60, 120);
                if (i < n) {
                    i++;
                    PdfImportedPage page2 = writer.getImportedPage(reader, i);
                    cb.addTemplate(page2, .5f, 0, 0, .5f, width / 2 + 60, 120);
                }
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                cb.beginText();
                cb.setFontAndSize(bf, 14);
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "page " + p + " of " + ((n / 2) + (n % 2 > 0 ? 1 : 0)),
                        width / 2, 40, 0);
                cb.endText();
            }
            // step 5: we close the document
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}
