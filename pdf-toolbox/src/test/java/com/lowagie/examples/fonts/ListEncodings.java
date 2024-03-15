/*
 * $Id: ListEncodings.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.fonts;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Listing the encodings of font comic
 */
public class ListEncodings {

    /**
     * Listing the encodings of font comic.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Listing Font properties");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("encodings.txt"));
            BaseFont bfComic = BaseFont.createFont("c:\\windows\\fonts\\comicbd.ttf", BaseFont.CP1252,
                    BaseFont.NOT_EMBEDDED);
            out.write("postscriptname: " + bfComic.getPostscriptFontName());
            out.write("\r\n\r\n");
            String[] codePages = bfComic.getCodePagesSupported();
            out.write("All available encodings:\n\n");
            for (String codePage : codePages) {
                out.write(codePage);
                out.write("\r\n");
            }
            out.flush();
            out.close();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }
}
