/*
 * $Id: AddWatermarkPageNumbers.java 3373 2008-05-12 16:21:24Z xlv $
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

import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads the pages of an existing PDF file, adds pagenumbers and a watermark.
 */
public class AddWatermarkPageNumbers {

    /**
     * Reads the pages of an existing PDF file, adds pagenumbers and a watermark.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Add watermarks and pagenumbers");
        try {
            // we create a reader for a certain document
            PdfReader reader = new PdfReader("ChapterSection.pdf");
            int n = reader.getNumberOfPages();
            // we create a stamper that will copy the document to a new file
            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream("watermark_pagenumbers.pdf"));
            // adding some metadata
            Map<String, String> moreInfo = new HashMap<>();
            moreInfo.put("Author", "Bruno Lowagie");
            stamp.setInfoDictionary(moreInfo);
            // adding content to each page
            int i = 0;
            PdfContentByte under;
            PdfContentByte over;
            Image img = Image.getInstance("watermark.jpg");
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
            img.setAbsolutePosition(200, 400);
            while (i < n) {
                i++;
                // watermark under the existing page
                under = stamp.getUnderContent(i);
                under.addImage(img);
                // text over the existing page
                over = stamp.getOverContent(i);
                over.beginText();
                over.setFontAndSize(bf, 18);
                over.setTextMatrix(30, 30);
                over.showText("page " + i);
                over.setFontAndSize(bf, 32);
                over.showTextAligned(Element.ALIGN_LEFT, "DUPLICATE", 230, 430, 45);
                over.endText();
            }
            // adding an extra page
            stamp.insertPage(1, PageSize.A4);
            over = stamp.getOverContent(1);
            over.beginText();
            over.setFontAndSize(bf, 18);
            over.showTextAligned(Element.ALIGN_LEFT, "DUPLICATE OF AN EXISTING PDF DOCUMENT", 30, 600, 0);
            over.endText();
            // adding a page from another document
            PdfReader reader2 = new PdfReader("SimpleAnnotations1.pdf");
            under = stamp.getUnderContent(1);
            under.addTemplate(stamp.getImportedPage(reader2, 3), 1, 0, 0, 1, 0, 0);
            // closing PdfStamper will generate the new PDF file
            stamp.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}
