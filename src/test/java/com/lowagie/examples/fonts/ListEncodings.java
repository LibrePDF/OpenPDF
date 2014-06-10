/*
 * $Id: ListEncodings.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.fonts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

/**
 * Listing the encodings of font comic
 */
public class ListEncodings {

	/**
	 * Listing the encodings of font comic.
	 * @param args no arguments needed
	 */
	public static void main(String[] args) {
		System.out.println("Listing Font properties");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("encodings.txt"));
			BaseFont bfComic = BaseFont.createFont("c:\\windows\\fonts\\comicbd.ttf", BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			out.write("postscriptname: " + bfComic.getPostscriptFontName());
			out.write("\r\n\r\n");
	        String[] codePages = bfComic.getCodePagesSupported();
	        out.write("All available encodings:\n\n");
	        for (int i = 0; i < codePages.length; i++) {
	        	out.write(codePages[i]);
	        	out.write("\r\n");
	        }
	        out.flush();
	        out.close();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
