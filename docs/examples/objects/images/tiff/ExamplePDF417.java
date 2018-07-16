/*
 * $Id: ExamplePDF417.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'iText Tutorial'.
 * You can find the complete tutorial at the following address:
 * http://itextdocs.lowagie.com/tutorial/
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * itext-questions@lists.sourceforge.net
 */
package com.lowagie.examples.objects.images.tiff;

import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BarcodePDF417;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Example Barcode PDF417.
 */
public class ExamplePDF417 {
	/**
	 * Example Barcode PDF417.
	 * @param args no arguments needed
	 */

	public static void main(String[] args) {
        try {
            BarcodePDF417 pdf417 = new BarcodePDF417();
            String text = "It was the best of times, it was the worst of times, " + 
                "it was the age of wisdom, it was the age of foolishness, " +
                "it was the epoch of belief, it was the epoch of incredulity, " +
                "it was the season of Light, it was the season of Darkness, " +
                "it was the spring of hope, it was the winter of despair, " +
                "we had everything before us, we had nothing before us, " +
                "we were all going direct to Heaven, we were all going direct " +
                "the other way - in short, the period was so far like the present " +
                "period, that some of its noisiest authorities insisted on its " +
                "being received, for good or for evil, in the superlative degree " +
                "of comparison only.";
            pdf417.setText(text);
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("pdf417.pdf"));
            document.open();
            Image img = pdf417.getImage();
            img.scalePercent(50, 50 * pdf417.getYHeight());
            document.add(img);
            document.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}
}
