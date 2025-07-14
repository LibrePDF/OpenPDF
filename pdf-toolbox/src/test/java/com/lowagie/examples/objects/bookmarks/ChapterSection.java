/*
 * $Id: ChapterSection.java 3373 2008-05-12 16:21:24Z xlv $
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

import com.lowagie.text.Chapter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Section;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import org.librepdf.openpdf.examples.content.Constants;

/**
 * Creates a document with outlines (bookmarks) using the Chapter and Section object.
 *
 * @author blowagie
 */

public class ChapterSection {

    /**
     * Creates a document with outlines.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Chapters and Sections");

        // step 1: creation of a document-object
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            // step 2: we create a writer that listens to the document
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("ChapterSection.pdf"));
            // step 3: we open the document
            writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
            document.open();
            // step 4: we add content to the document
            // we define some fonts
            Font chapterFont = FontFactory.getFont(FontFactory.HELVETICA, 24, Font.NORMAL, new Color(255, 0, 0));
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA, 20, Font.NORMAL, new Color(0, 0, 255));
            Font subsectionFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, new Color(0, 64, 64));
            // we create some paragraphs
            Paragraph blahblah = new Paragraph(Constants.BLAH_BLAH);
            Paragraph blahblahblah = new Paragraph(Constants.BLAH_BLAH + Constants.BLAH_BLAH);
            // this loop will create 7 chapters
            for (int i = 1; i < 8; i++) {
                Paragraph cTitle = new Paragraph("This is chapter " + i, chapterFont);
                Chapter chapter = new Chapter(cTitle, i);
                // in chapter 4 we change the alignment to ALIGN_JUSTIFIED
                if (i == 4) {
                    blahblahblah.setAlignment(Element.ALIGN_JUSTIFIED);
                    blahblah.setAlignment(Element.ALIGN_JUSTIFIED);
                    chapter.add(blahblah);
                }
                // in chapter 5, the alignment is changed again
                if (i == 5) {
                    blahblahblah.setAlignment(Element.ALIGN_CENTER);
                    blahblah.setAlignment(Element.ALIGN_RIGHT);
                    chapter.add(blahblah);
                }
                // the alignment is changed to ALIGN_JUSTIFIED again
                if (i == 6) {
                    blahblahblah.setAlignment(Element.ALIGN_JUSTIFIED);
                    blahblah.setAlignment(Element.ALIGN_JUSTIFIED);
                }
                // in every chapter 3 sections will be added
                for (int j = 1; j < 4; j++) {
                    Paragraph sTitle = new Paragraph("This is section " + j + " in chapter " + i, sectionFont);
                    Section section = chapter.addSection(sTitle, 1);
                    // for chapters > 2, the outine isn't open by default
                    if (i > 2) {
                        section.setBookmarkOpen(false);
                    }
                    // in all chapters except the 1st one, some extra text is added to section 3
                    if (j == 3 && i > 1) {
                        section.setIndentationLeft(72);
                        section.add(blahblah);
                        section.add(new Paragraph("test"));
                    }
                    // in every section 3 subsections are added
                    for (int k = 1; k < 4; k++) {
                        Paragraph subTitle = new Paragraph("This is subsection " + k + " of section " + j,
                                subsectionFont);
                        Section subsection = section.addSection(subTitle, 3);
                        // in the first subsection of section 3, extra text is added
                        if (k == 1 && j == 3) {
                            subsection.add(blahblahblah);
                        }
                        subsection.add(blahblah);
                    }
                    // in the section section of every chapter > 2 extra text is added
                    if (j == 2 && i > 2) {
                        section.add(blahblahblah);
                    }
                    // a new page is added after the second section in Chapter 1
                    if (j == 2 && i == 1) {
                        section.add(Chunk.NEXTPAGE);
                    }
                }
                document.add(chapter);
            }
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step 5: we close the document
        document.close();
    }
}