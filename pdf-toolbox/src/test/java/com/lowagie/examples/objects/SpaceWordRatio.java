/*
 * $Id: SpaceWordRatio.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Space Word Ratio.
 */
public class SpaceWordRatio {

    /**
     * Space Word Ratio.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Space Word Ratio");
        // step 1
        Document document = new Document(PageSize.A4, 50, 350, 50, 50);
        try {
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("spacewordratio.pdf"));
            // step 3
            document.open();
            // step 4
            String text = "Flanders International Filmfestival Ghent - Internationaal Filmfestival van Vlaanderen Gent";
            Paragraph p = new Paragraph(text);
            p.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(p);
            document.newPage();
            writer.setSpaceCharRatio(PdfWriter.NO_SPACE_CHAR_RATIO);
            document.add(p);
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step 5
        document.close();
    }
}