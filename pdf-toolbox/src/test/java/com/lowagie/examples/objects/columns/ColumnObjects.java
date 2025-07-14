/*
 * $Id: ColumnObjects.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.columns;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * A more complex example demonstrating the use of ColumnText.
 */
public class ColumnObjects {

    /**
     * Some data we want to output.
     */
    static public String[] headings = {
            "Book/Product Model:",
            "Sales Handle:",
            "Why We Published this Book/Product Model:",
            "Key benefits:",
            "About the Author(s):",
            "Technology/Topic Overview: ",
            "Book/Product Content Summary:",
            "Audience:",
            "What's on the CD/DVD/Web:"
    };

    /**
     * Some text we want to output.
     */
    static public String[] texts = {
            "Ideally, choose one title (2-3 if absolutely necessary) that this book should perform like. Include full "
                    + "title, ISBN, author, and any sell through numbers if possible.",
            "One line description about the sales.",
            "Brief description (one-two lines) on the importance of this book to the audience.",
            "What benefit does this book provide to the consumer? (expert advice, speed, fun, productivity). Why "
                    + "should the Retailer/Wholesaler select this book over its competition? What are the unique "
                    + "features about this book should be highlighted? What makes this book different, better? From "
                    + "other books and the previous edition?",
            "What makes this person so special?  Is she/he an expert, creator of the technology, educational leader, "
                    + "etc.? What is their background, and what relevant experiences do they have to make them the "
                    + "BEST choice? Have he/she/they won awards or been recognized in any way. Other books poublished "
                    + "by the author.\n1. Book one.\n2. Book two.",
            "In brief two to five line description of the technology, topic or relevant information. Please keep "
                    + "descriptions succinct.",
            "Ideal describe the contents of this book. What will this book do for the reader? Will this book help them "
                    + "optimize their system? Increase productivity? offer tips and stragegies?",
            "Who is your intended customer? Experts? Power users? Business professionals? Programmers? What are the "
                    + "demographics?",
            "What is included on the Cd or Web site? Why is it necessary and what will it do for the purchaser (source "
                    + "code, examples, case studies)?\nIs there a value that can be associated with what is on the "
                    + "CD/DVD or Web?"
    };

    /**
     * A more complex example using ColumnText.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Columns and objects");

        // step 1: creation of a document-object
        Document document = new Document(PageSize.LETTER, 90, 54, 72, 72);
        try {
            // step 2: we create a writer that listens to the document
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("columnobjects.pdf"));

            float gutter = 20;
            int numColumns = 3;
            float fullWidth = document.right() - document.left();
            float columnWidth = (fullWidth - (numColumns - 1) * gutter) / numColumns;
            float[] allColumns = new float[numColumns]; // left
            for (int k = 0; k < numColumns; ++k) {
                allColumns[k] = document.left() + (columnWidth + gutter) * k;
            }
            // set the fonts
            Font font24B = FontFactory.getFont(FontFactory.TIMES_ROMAN, 24, Font.BOLD);
            Font font10B = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10, Font.BOLD);
            Font font14B = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, Font.BOLD, new Color(255, 0, 0));
            Font font9 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9);
            Font font11 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11);

            // step 3: we open the document
            document.open();
            // step 4:
            // get the stream content
            PdfContentByte cb = writer.getDirectContent();
            // headers
            Phrase fullTitle = new Phrase("Full Title", font24B);
            float currentY = document.top();
            ColumnText ct = new ColumnText(cb);
            ct.setSimpleColumn(fullTitle, document.left(), 0, document.right(), document.top(), 24,
                    Element.ALIGN_JUSTIFIED);
            ct.go();
            currentY = ct.getYLine();
            currentY -= 4;
            cb.setLineWidth(1);
            cb.moveTo(document.left(), currentY);
            cb.lineTo(document.right(), currentY);
            cb.stroke();
            currentY -= 4;
            ct.setYLine(currentY);
            ct.addText(new Chunk("Author: Name of the author comes here", font10B));
            ct.setLeading(10);
            ct.go();
            currentY = ct.getYLine();
            currentY -= 15;
            float topColumn = currentY;
            for (int k = 1; k < numColumns; ++k) {
                float x = allColumns[k] - gutter / 2;
                cb.moveTo(x, topColumn);
                cb.lineTo(x, document.bottom());
            }
            cb.stroke();
            Image img = Image.getInstance("cover.png");
            cb.addImage(img, img.getScaledWidth(), 0, 0, img.getScaledHeight(), document.left(),
                    currentY - img.getScaledHeight());
            currentY -= img.getScaledHeight() + 10;
            ct.setYLine(currentY);
            ct.addText(new Chunk("Key Data:", font14B));
            ct.go();
            currentY = ct.getYLine();
            currentY -= 4;
            PdfPTable ptable = new PdfPTable(2);
            ptable.getDefaultCell().setPaddingLeft(4);
            ptable.getDefaultCell().setPaddingTop(0);
            ptable.getDefaultCell().setPaddingBottom(4);
            ptable.addCell(new Phrase("Imprint Name:", font9));
            ptable.addCell(new Phrase("Prentice Hall", font9));
            ptable.addCell(new Phrase("Series Name:", font9));
            ptable.addCell(new Phrase("", font9));
            ptable.addCell(new Phrase("ISBN:", font9));
            ptable.addCell(new Phrase("Hall", font9));
            ptable.addCell(new Phrase("UPC Code:", font9));
            ptable.addCell(new Phrase("0789718103", font9));
            ptable.addCell(new Phrase("EAN #", font9));
            ptable.addCell(new Phrase("0786718103", font9));
            ptable.addCell(new Phrase("Price:", font9));
            ptable.addCell(new Phrase("49.99", font9));
            ptable.addCell(new Phrase("Page Count:", font9));
            ptable.addCell(new Phrase("500", font9));
            ptable.addCell(new Phrase("Discount:", font9));
            ptable.addCell(new Phrase("10%", font9));
            ptable.addCell(new Phrase("Trim Size:", font9));
            ptable.addCell(new Phrase("420x340", font9));
            ptable.addCell(new Phrase("Cover:", font9));
            ptable.addCell(new Phrase("Hard", font9));
            ptable.addCell(new Phrase("Interior Color:", font9));
            ptable.addCell(new Phrase("none", font9));
            ptable.addCell(new Phrase("Media with book:", font9));
            ptable.addCell(new Phrase("CD", font9));
            ptable.addCell(new Phrase("Author(s):", font9));
            ptable.addCell(new Phrase("Ben Forta", font9));
            ptable.addCell(new Phrase("Editor:", font9));
            ptable.addCell(new Phrase("Ben Forta", font9));
            ptable.addCell(new Phrase("Pub Date:", font9));
            ptable.addCell(new Phrase("06/05/1998", font9));
            ptable.setTotalWidth(columnWidth);

            currentY = ptable.writeSelectedRows(0, -1, document.left(), currentY, cb) - 20;
            for (int k = 0; k < headings.length; ++k) {
                ct.addText(new Chunk(headings[k] + "\n", font14B));
                ct.addText(new Chunk(texts[k] + "\n\n", font11));
            }

            int currentColumn = 0;
            ct.setSimpleColumn(allColumns[currentColumn], document.bottom(),
                    allColumns[currentColumn] + columnWidth, currentY, 15, Element.ALIGN_JUSTIFIED);
            ct.setLeading(2, 1);
            while (true) {
                int rc = ct.go();
                if ((rc & ColumnText.NO_MORE_TEXT) != 0) {
                    break;
                }
                // we run out of column. Let's go to another one
                ++currentColumn;
                if (currentColumn >= allColumns.length) {
                    break;
                }
                ct.setSimpleColumn(allColumns[currentColumn], document.bottom(),
                        allColumns[currentColumn] + columnWidth, topColumn, 15, Element.ALIGN_JUSTIFIED);
                ct.setLeading(2, 1);
            }
            // step 5: we close the document
            document.close();
        } catch (Exception de) {
            System.err.println(de.getMessage());
        }
    }
}