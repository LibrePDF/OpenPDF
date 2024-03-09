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
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Generates a simple 'Hello World' HTML page.
 *
 * @author blowagie
 */

public class ParseHelloHtml {

    /**
     * Generates an HTML page with the text 'Hello World'
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {
        System.out.println("Parse Hello World");

        // step 1: creation of a document-object
        try (Document document = new Document()) {
            PdfWriter.getInstance(document, new FileOutputStream("parseHelloWorld.pdf"));
            // step 2: we open the document
            document.open();
            // step 3: parsing the HTML document to convert it in PDF
            HtmlParser.parse(document, ParseHelloHtml.class.getClassLoader()
                    .getResourceAsStream("com/lowagie/examples/html/parseHelloWorld.html"));
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
    }
}