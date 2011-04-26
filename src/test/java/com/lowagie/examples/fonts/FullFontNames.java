/*
 * $Id: FullFontNames.java 3373 2008-05-12 16:21:24Z xlv $
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

import com.lowagie.text.pdf.BaseFont;

/**
 * Retrieving the full font name
 */
public class FullFontNames {

	/**
	 * Retrieving the full font name
	 * @param args no arguments needed
	 */
	public static void main(String[] args) {
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter("fullfontname_arialbi.txt"));
	        BaseFont bf = BaseFont.createFont("c:\\windows\\fonts\\arialbi.ttf", "winansi", BaseFont.NOT_EMBEDDED);
			out.write("postscriptname: " + bf.getPostscriptFontName());
			out.write("\r\n\r\n");
	        String names[][] = bf.getFullFontName();
	        out.write("\n\nListing the full font name:\n\n");
	        for (int k = 0; k < names.length; ++k) {
	            if (names[k][0].equals("3") && names[k][1].equals("1")) // Microsoft encoding
	                out.write(names[k][3] + "\r\n");
	        }
	        out.flush();
	        out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
