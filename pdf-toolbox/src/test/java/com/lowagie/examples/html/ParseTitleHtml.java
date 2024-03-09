/*
 * $Id: HelloHtml.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.html;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.html.HtmlParser;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Generates a simple HTML page which contains a title.
 *
 * @author chappyer
 */

public class ParseTitleHtml {

    /**
     * Generates an HTML page which contains a title
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        // step 1: creation of a document-object
        try (Document document = new Document()) {
            PdfWriter.getInstance(document, Files.newOutputStream(Paths.get("parseTitle.pdf")));
            // step 2: we open the document
            document.open();
            // step 3: parsing the HTML document to convert it in PDF
            HtmlParser.parse(document, ParseHelloHtml.class.getClassLoader()
                    .getResourceAsStream("com/lowagie/examples/html/parseTitle.html"));
        } catch (DocumentException | IOException de) {
            de.printStackTrace();
        }
    }
}