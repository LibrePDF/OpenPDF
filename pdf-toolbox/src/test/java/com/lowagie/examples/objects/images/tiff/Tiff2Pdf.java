/*
 * $Id: Tiff2Pdf.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.codec.TiffImage;

/**
 * Demonstrates how the Tiff to PDF conversion works.
 * 
 * @author psoares
 * @author blowagie
 */

public class Tiff2Pdf {

	/**
	 * Demonstrates some TIFF functionality.
	 * 
	 * @param args
	 *            a list of tiff files to convert
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Tiff2Pdf needs an argument.");
			System.out.println("Usage: com.lowagie.examples.objects.images.tiff.Tiff2Pdf file1.tif [file2.tif ... fileN.tif]");
			System.exit(1);
		}
		String tiff_file;
		String pdf_file;
		for (int i = 0; i < args.length; i++) {
			tiff_file = args[i];
			pdf_file = tiff_file.substring(0, tiff_file.lastIndexOf('.') + 1) + "pdf";
			Document document = new Document();
			try {
				PdfWriter writer = PdfWriter.getInstance(document,
						new FileOutputStream(pdf_file));
				int pages = 0;
				document.open();
				PdfContentByte cb = writer.getDirectContent();
                RandomAccessFileOrArray ra = null;
                int comps = 0;
                try {
                    ra = new RandomAccessFileOrArray(tiff_file);
                    comps = TiffImage.getNumberOfPages(ra);
                }
                catch (Throwable e) {
                    System.out.println("Exception in " + tiff_file + " " + e.getMessage());
                    continue;
                }
                System.out.println("Processing: " + tiff_file);
                for (int c = 0; c < comps; ++c) {
                    try {
                        Image img = TiffImage.getTiffImage(ra, c + 1);
                        if (img != null) {
                            System.out.println("page " + (c + 1));
                            if (img.getScaledWidth() > 500 || img.getScaledHeight() > 700) {
                                img.scaleToFit(500, 700);
                            }
                            img.setAbsolutePosition(20, 20);
                            document.add(new Paragraph(tiff_file + " - page " + (c + 1)));
                            cb.addImage(img);
                            document.newPage();
                            ++pages;
                        }
                    }
                    catch (Throwable e) {
                        System.out.println("Exception " + tiff_file + " page " + (c + 1) + " " + e.getMessage());
                    }
				}
				ra.close();
				document.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}    
}