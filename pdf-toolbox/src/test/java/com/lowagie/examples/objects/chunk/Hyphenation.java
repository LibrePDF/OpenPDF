/*
 * $Id: Hyphenation.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects.chunk;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.HyphenationAuto;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Make iText hyphenate your text.
 *
 * @author blowagie
 */

public class Hyphenation {

    /**
     * Using auto-hyphenation.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Hyphenation");

        // step 1: creation of a document-object
        Document document = new Document(PageSize.A6);
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter.getInstance(document, new FileOutputStream(
                    "Hyphenation.pdf"));

            // step 3: we open the document
            document.open();
            // step 4:
            String text = "It was the best of times, it was the worst of times, "
                    + "it was the age of wisdom, it was the age of foolishness, "
                    + "it was the epoch of belief, it was the epoch of incredulity, "
                    + "it was the season of Light, it was the season of Darkness, "
                    + "it was the spring of hope, it was the winter of despair, "
                    + "we had everything before us, we had nothing before us, "
                    + "we were all going direct to Heaven, we were all going direct "
                    + "the other way\u2014in short, the period was so far like the present "
                    + "period, that some of its noisiest authorities insisted on its "
                    + "being received, for good or for evil, in the superlative degree "
                    + "of comparison only.";
            Chunk ck = new Chunk(text);
            HyphenationAuto auto = new HyphenationAuto("en", "GB", 2, 2);
            ck.setHyphenation(auto);
            Paragraph p = new Paragraph(ck);
            p.setAlignment(Paragraph.ALIGN_JUSTIFIED);
            document.add(p);
            document.newPage();
            ck = new Chunk(text);
            auto = new HyphenationAuto("en", "US", 2, 2);
            ck.setHyphenation(auto);
            p = new Paragraph(ck);
            p.setAlignment(Paragraph.ALIGN_JUSTIFIED);
            document.add(p);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}