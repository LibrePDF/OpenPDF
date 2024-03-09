/*
 * $Id: DvdCover.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.images;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This is a tool that allows you to make a DVD Cover.
 *
 * @author blowagie
 */
public class DvdCover {

    /**
     * the name of the file that has to be generated.
     */
    protected String filename;
    /**
     * the title that has to be on the side of the cover.
     */
    protected String title;
    /**
     * the backgroundcolor of the cover.
     */
    protected Color backgroundcolor;
    /**
     * the front image of the cover.
     */
    protected Image front;
    /**
     * the side image of the cover.
     */
    protected Image side;
    /**
     * the back image of the cover.
     */
    protected Image back;

    /**
     * Creates a DVD Cover object.
     */
    public DvdCover() {
    }

    /**
     * Example that generates a DVD Cover in PDF.
     *
     * @param args an array containing [0] a filename [1] a title [2] a backgroundcolor [3] a front image [4] a back
     *             image [5] a side image
     */
    public static void main(String[] args) {
        System.out.println("DVD Cover");
        DvdCover cover = new DvdCover();
        if (args.length > 0) {
            cover.setFilename(args[0]);
        }
        if (args.length > 1) {
            cover.setTitle(args[1]);
        }
        if (args.length > 2) {
            cover.setBackgroundcolor(Color.decode(args[2]));
        }
        if (args.length > 3) {
            try {
                cover.setFront(Image.getInstance(args[3]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (args.length > 4) {
            try {
                cover.setBack(Image.getInstance(args[4]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (args.length > 5) {
            try {
                cover.setSide(Image.getInstance(args[5]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cover.generatePdf();
    }

    /**
     * Sets the Image that has to be on the back of the cover.
     *
     * @param back an Image object.
     */
    public void setBack(Image back) {
        this.back = back;
    }

    /**
     * Sets the backgroundcolor of the cover.
     *
     * @param backgroundcolor a Color object.
     */
    public void setBackgroundcolor(Color backgroundcolor) {
        this.backgroundcolor = backgroundcolor;
    }

    /**
     * Sets the Image that has to be on the front of the cover.
     *
     * @param front an Image object.
     */
    public void setFront(Image front) {
        this.front = front;
    }

    /**
     * Sets the Image that has to be on the back of the cover.
     *
     * @param side an Image object.
     */
    public void setSide(Image side) {
        this.side = side;
    }

    /**
     * Sets the title of the movie (will be printed on the side if there is no side image)
     *
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the filename of the resulting PDF
     *
     * @param filename The filename to set.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Method that generates the actual PDF file.
     */
    public void generatePdf() {

        // step 1: creation of a document-object
        Rectangle pageSize = new Rectangle(780, 525);
        if (backgroundcolor != null) {
            pageSize.setBackgroundColor(backgroundcolor);
        }
        Document document = new Document(pageSize);

        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            if (filename == null) {
                filename = "dvdcover.pdf";
            }
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));

            // step 3: we open the document
            document.open();

            // step 4:
            PdfContentByte cb = writer.getDirectContent();
            if (title != null) {
                cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false), 24);
                cb.beginText();
                if (front == null) {
                    cb.showTextAligned(Element.ALIGN_CENTER, title, 595f, 262f, 0f);
                }
                if (side == null) {
                    cb.showTextAligned(Element.ALIGN_CENTER, title, 385f, 262f, 270f);
                }
                cb.endText();
            }
            cb.moveTo(370, 0);
            cb.lineTo(370, 525);
            cb.moveTo(410, 525);
            cb.lineTo(410, 0);
            cb.stroke();
            if (front != null) {
                front.scaleToFit(370, 525);
                front.setAbsolutePosition(410f + (370f - front.getScaledWidth()) / 2f,
                        (525f - front.getScaledHeight()) / 2f);
                document.add(front);
            }
            if (back != null) {
                back.scaleToFit(370, 525);
                back.setAbsolutePosition((370f - back.getScaledWidth()) / 2f, (525f - back.getScaledHeight()) / 2f);
                document.add(back);
            }
            if (side != null) {
                side.scaleToFit(40, 525);
                side.setAbsolutePosition(370 + (40f - side.getScaledWidth()) / 2f,
                        (525f - side.getScaledHeight()) / 2f);
                document.add(side);
            }
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}