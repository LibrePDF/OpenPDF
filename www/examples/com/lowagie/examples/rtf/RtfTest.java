/*
 * $Id: RtfTest.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is free software. It may only be copied or modified
 * if you include the following copyright notice:
 *
 * --> Copyright 2004 by Mark Hall <--
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

package com.lowagie.examples.rtf;

import java.awt.Color;
import java.io.FileOutputStream;

import com.lowagie.text.Anchor;
import com.lowagie.text.Annotation;
import com.lowagie.text.Cell;
import com.lowagie.text.Chapter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Section;
import com.lowagie.text.Table;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.field.RtfTableOfContents;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooter;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooterGroup;
import com.lowagie.text.rtf.style.RtfFont;
import com.lowagie.text.rtf.table.RtfBorder;
import com.lowagie.text.rtf.table.RtfBorderGroup;
import com.lowagie.text.rtf.table.RtfCell;

/**
 * This is a test suite in which all kinds of RTF functionality are tested
 * with the RtfWriter2.
 * 
 * @author Mark Hall
 */
public class RtfTest {

    /**
     * Creates 1 RTF file.
     * 
     * @param args no arguments needed
     */

    public static void main(String[] args) {
        System.out.println("Test Suite");

        try {
            Document doc = new Document();
            RtfWriter2 writer2 = RtfWriter2.getInstance(doc,
                    new FileOutputStream("testNew.rtf"));

            writer2.setAutogenerateTOCEntries(true);

            Table headerTable = new Table(3);
            headerTable.addCell("Test Cell 1");
            headerTable.addCell("Test Cell 2");
            headerTable.addCell("Test Cell 3");
            HeaderFooter header = new RtfHeaderFooter(headerTable);
            RtfHeaderFooterGroup footer = new RtfHeaderFooterGroup();
            footer
                    .setHeaderFooter(
                            new RtfHeaderFooter(new Phrase(
                                    "This is the footer on the title page")),
                            com.lowagie.text.rtf.headerfooter.RtfHeaderFooter.DISPLAY_FIRST_PAGE);
            footer
                    .setHeaderFooter(
                            new RtfHeaderFooter(new Phrase(
                                    "This is a left side page")),
                            com.lowagie.text.rtf.headerfooter.RtfHeaderFooter.DISPLAY_LEFT_PAGES);
            footer
                    .setHeaderFooter(
                            new RtfHeaderFooter(new Phrase(
                                    "This is a right side page")),
                            com.lowagie.text.rtf.headerfooter.RtfHeaderFooter.DISPLAY_RIGHT_PAGES);

            doc.setHeader(header);
            doc.setFooter(footer);

            doc.open();

            Paragraph p = new Paragraph();
            p.add(new RtfTableOfContents("UPDATE ME!"));
            doc.add(p);

            p = new Paragraph("", new RtfFont("Staccato222 BT"));
            p.add(new Chunk("Hello! "));
            p.add(new Chunk("How do you do?"));
            doc.add(p);
            p.setAlignment(Element.ALIGN_RIGHT);
            doc.add(p);

            Anchor a = new Anchor("http://www.uni-klu.ac.at");
            a.setReference("http://www.uni-klu.ac.at");
            doc.add(a);

            Image img = Image.getInstance("pngnow.png");
            doc.add(new Chunk(img, 0, 0));
            doc.add(new Annotation("Mark", "This works!"));

            Chunk c = new Chunk("");
            c.setNewPage();
            doc.add(c);

            List subList = new List(true, 40);
            subList.add(new ListItem("Sub list 1"));
            subList.add(new ListItem("Sub list 2"));

            List list = new List(true, 20);
            list.add(new ListItem("Test line 1"));
            list
                    .add(new ListItem(
                            "Test line 2 - This is a really long test line to test that linebreaks are working the way they are supposed to work so a really really long line of drivel is required"));
            list.add(subList);
            list.add(new ListItem("Test line 3 - \t\u20ac\t 60,-"));
            doc.add(list);

            list = new List(false, 20);
            list.add(new ListItem("Bullet"));
            list.add(new ListItem("Another one"));
            doc.add(list);

            doc.newPage();

            Chapter chapter = new Chapter(new Paragraph("This is a Chapter"), 1);
            chapter.add(new Paragraph(
                    "This is some text that belongs to this chapter."));
            chapter.add(new Paragraph("A second paragraph. What a surprise."));

            Section section = chapter.addSection(new Paragraph(
                    "This is a subsection"));
            section.add(new Paragraph("Text in the subsection."));

            doc.add(chapter);

            com.lowagie.text.rtf.field.RtfTOCEntry rtfTOC = new com.lowagie.text.rtf.field.RtfTOCEntry(
                    "Table Test");
            doc.add(rtfTOC);

            Table table = new Table(3);
            table.setPadding(2);
            table.setAlignment(Element.ALIGN_LEFT);
            table.setSpacing(2);

            Cell emptyCell = new Cell("");

            Cell cellLeft = new Cell("Left Alignment");
            cellLeft.setHorizontalAlignment(Element.ALIGN_LEFT);
            Cell cellCenter = new Cell("Center Alignment");
            cellCenter.setHorizontalAlignment(Element.ALIGN_CENTER);
            Cell cellRight = new Cell("Right Alignment");
            cellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);

            table.addCell(cellLeft);
            table.addCell(cellCenter);
            table.addCell(cellRight);

            Cell cellSpanHoriz = new Cell("This Cell spans two columns");
            cellSpanHoriz.setColspan(2);
            table.addCell(cellSpanHoriz);
            table.addCell(emptyCell);

            Cell cellSpanVert = new Cell("This Cell spans two rows");
            cellSpanVert.setRowspan(2);
            table.addCell(emptyCell);
            table.addCell(cellSpanVert);
            table.addCell(emptyCell);
            table.addCell(emptyCell);
            table.addCell(emptyCell);

            Cell cellSpanHorizVert = new Cell(
                    "This Cell spans both two columns and two rows");
            cellSpanHorizVert.setColspan(2);
            cellSpanHorizVert.setRowspan(2);
            table.addCell(emptyCell);
            table.addCell(cellSpanHorizVert);
            table.addCell(emptyCell);

            RtfCell cellDotted = new RtfCell("Dotted border");
            cellDotted.setBorders(new RtfBorderGroup(Rectangle.BOX,
                    RtfBorder.BORDER_DOTTED, 1, new Color(0, 0, 0)));
            RtfCell cellEmbossed = new RtfCell("Embossed border");
            cellEmbossed.setBorders(new RtfBorderGroup(Rectangle.BOX,
                    RtfBorder.BORDER_EMBOSS, 1, new Color(0, 0, 0)));
            RtfCell cellNoBorder = new RtfCell("No border");
            cellNoBorder.setBorders(new RtfBorderGroup());

            table.addCell(cellDotted);
            table.addCell(cellEmbossed);
            table.addCell(cellNoBorder);

            doc.add(table);

            for (int i = 0; i < 300; i++) {
                doc.add(new Paragraph(
                        "Dummy line to get multi-page document. Line "
                                + (i + 1)));
            }

            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}