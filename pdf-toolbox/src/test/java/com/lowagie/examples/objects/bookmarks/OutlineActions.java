/*
 * $Id: OutlineActions.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects.bookmarks;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demonstrates how pagelabels work.
 *
 * @author blowagie
 */

public class OutlineActions {

    /**
     * Demonstrates some PageLabel functionality.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Outlines with actions");

        // step 1: creation of a document-object
        Document document = new Document();
        Document remote = new Document();
        try {
            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("OutlineActions.pdf"));
            PdfWriter.getInstance(remote, new FileOutputStream("remote.pdf"));
            // step 3:
            writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
            document.open();
            remote.open();
            // step 4:
            // we add some content
            document.add(new Paragraph("Outline action example"));
            // we add the outline
            PdfContentByte cb = writer.getDirectContent();
            PdfOutline root = cb.getRootOutline();
            PdfOutline links = new PdfOutline(root, new PdfAction("https://github.com/LibrePDF/OpenPDFlinks.html"),
                    "Useful links");
            links.setColor(new Color(0x00, 0x80, 0x80));
            links.setStyle(Font.BOLD);
            new PdfOutline(links, new PdfAction("http://www.lowagie.com/iText"), "Bruno's iText site");
            new PdfOutline(links, new PdfAction("http://itextpdf.sourceforge.net/"), "Paulo's iText site");
            new PdfOutline(links, new PdfAction("http://sourceforge.net/projects/itext/"), "iText @ SourceForge");
            PdfOutline other = new PdfOutline(root, new PdfDestination(PdfDestination.FIT), "other actions", false);
            other.setStyle(Font.ITALIC);
            new PdfOutline(other, new PdfAction("remote.pdf", 1), "Go to yhe first page of a remote file");
            new PdfOutline(other, new PdfAction("remote.pdf", "test"), "Go to a local destination in a remote file");
            new PdfOutline(other, PdfAction.javaScript("app.alert('Hello');\r", writer), "Say Hello");

            remote.add(new Paragraph("Some remote document"));
            remote.newPage();
            Paragraph p = new Paragraph("This paragraph contains a ");
            p.add(new Chunk("local destination").setLocalDestination("test"));
            remote.add(p);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
        remote.close();
    }
}