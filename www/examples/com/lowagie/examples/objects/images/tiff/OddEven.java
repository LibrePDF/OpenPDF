/*
 * $Id: OddEven.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.codec.TiffImage;

/**
 * Combines 2 tiffs, one with odd, another with even pages into 1 combined PDF
 * (inspired by a tiffmesh example at http://sourceforge.net/projects/tifftools/).
 * 
 * @author blowagie
 */

public class OddEven {

	/**
	 * Combines 2 tiff-files into 1 PDF (similar to tiffmesh).
	 * 
	 * @param args
	 *            [0] the file with the odd pages
	 *            [1] the file with the even pages
	 *            [2] the resulting file
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("OddEven needs 3 Arguments.");
			System.out.println("Usage: com.lowagie.examples.objects.images.tiff.OddEven odd_file.tif even_file.tif combined_file.pdf");
			System.exit(1);
		}
		try {
			RandomAccessFileOrArray odd = new RandomAccessFileOrArray(args[0]);
			RandomAccessFileOrArray even = new RandomAccessFileOrArray(args[1]);
			Image img = TiffImage.getTiffImage(odd, 1);
			Document document = new Document(new Rectangle(img.getScaledWidth(),
					img.getScaledHeight()));
			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream(args[2]));
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			int count = Math.max(TiffImage.getNumberOfPages(odd), TiffImage
					.getNumberOfPages(even));
			System.out.println("Combining 2 TIFF files into 1 PDF");
			for (int c = 0; c < count; ++c) {
				try {
					Image imgOdd = TiffImage.getTiffImage(odd, c + 1);
					Image imgEven = TiffImage.getTiffImage(even, count - c);
					document.setPageSize(new Rectangle(imgOdd.getScaledWidth(),
							imgOdd.getScaledHeight()));
					document.newPage();
					imgOdd.setAbsolutePosition(0, 0);
					cb.addImage(imgOdd);
					document.setPageSize(new Rectangle(imgEven.getScaledWidth(),
							imgEven.getScaledHeight()));
					document.newPage();
					imgEven.setAbsolutePosition(0, 0);
					cb.addImage(imgEven);

				} catch (Throwable e) {
					System.out.println("Exception page " + (c + 1) + " "
							+ e.getMessage());
				}
			}
			odd.close();
			even.close();
			document.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}