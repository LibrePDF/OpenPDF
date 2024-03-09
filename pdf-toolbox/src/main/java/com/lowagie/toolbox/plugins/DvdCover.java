/*
 * $Id: DvdCover.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2005-2007 Bruno Lowagie, Carsten Hammer
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * This class was originally published under the MPL by Bruno Lowagie
 * and Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.ColorArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.ImageArgument;
import com.lowagie.toolbox.arguments.StringArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * This is a simple tool that generates a cover for a DVD.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class DvdCover extends AbstractTool {

    static {
        addVersion("$Id: DvdCover.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs a DvdCover object.
     */
    public DvdCover() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW | MENU_EXECUTE_PRINT;
        arguments.add(new FileArgument(this, "destfile", "The file to which the PDF has to be written", true,
                new PdfFilter()));
        arguments.add(new StringArgument(this, "title", "The title of the DVD"));
        arguments.add(new ColorArgument(this, "backgroundcolor",
                "The backgroundcolor of the DVD Cover (for instance 0xFFFFFF)"));
        arguments.add(new ImageArgument(this, "front", "The front image of the DVD Cover"));
        arguments.add(new ImageArgument(this, "back", "The back image of the DVD Cover"));
        arguments.add(new ImageArgument(this, "side", "The side image of the DVD Cover"));
    }

    /**
     * Generates a DVD Cover in PDF.
     *
     * @param args an array containing [0] a filename [1] a title [2] a backgroundcolor [3] a front image [4] a back
     *             image [5] a side image
     */
    public static void main(String[] args) {
        DvdCover tool = new DvdCover();
        if (args.length == 0) {
            System.err.println(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Make your own DVD Cover", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== DvdCover OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        try {
            // step 1: creation of a document-object
            Rectangle pageSize = new Rectangle(780, 525);
            if (getValue("backgroundcolor") != null) {
                pageSize.setBackgroundColor((Color) getValue("backgroundcolor"));
            }
            Document document = new Document(pageSize);
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            if (getValue("destfile") == null) {
                throw new DocumentException("You must provide a destination file!");
            }
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream((File) getValue("destfile")));

            // step 3: we open the document
            document.open();

            // step 4:
            PdfContentByte cb = writer.getDirectContent();
            if (getValue("title") != null) {
                cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false), 24);
                cb.beginText();
                if (getValue("front") == null) {
                    cb.showTextAligned(Element.ALIGN_CENTER, (String) getValue("title"), 595f, 262f, 0f);
                }
                if (getValue("side") == null) {
                    cb.showTextAligned(Element.ALIGN_CENTER, (String) getValue("title"), 385f, 262f, 270f);
                }
                cb.endText();
            }
            cb.moveTo(370, 0);
            cb.lineTo(370, 525);
            cb.moveTo(410, 525);
            cb.lineTo(410, 0);
            cb.stroke();
            if (getValue("front") != null) {
                Image front = (Image) getValue("front");
                front.scaleToFit(370, 525);
                front.setAbsolutePosition(410f + (370f - front.getScaledWidth()) / 2f,
                        (525f - front.getScaledHeight()) / 2f);
                document.add(front);
            }
            if (getValue("back") != null) {
                Image back = (Image) getValue("back");
                back.scaleToFit(370, 525);
                back.setAbsolutePosition((370f - back.getScaledWidth()) / 2f, (525f - back.getScaledHeight()) / 2f);
                document.add(back);
            }
            if (getValue("side") != null) {
                Image side = (Image) getValue("side");
                side.scaleToFit(40, 525);
                side.setAbsolutePosition(370 + (40f - side.getScaledWidth()) / 2f,
                        (525f - side.getScaledHeight()) / 2f);
                document.add(side);
            }

            // step 5: we close the document
            document.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(internalFrame,
                    e.getMessage(),
                    e.getClass().getName(),
                    JOptionPane.ERROR_MESSAGE);
            System.err.println(e.getMessage());
        }
    }

    /**
     * @param arg StringArgument
     * @see com.lowagie.toolbox.AbstractTool#valueHasChanged(com.lowagie.toolbox.arguments.AbstractArgument)
     */
    public void valueHasChanged(AbstractArgument arg) {
        if (internalFrame == null) {
            // if the internal frame is null, the tool was called from the command line
            return;
        }
        // represent the changes of the argument in the internal frame
    }

    /**
     * @return File
     * @throws InstantiationException on error
     * @see com.lowagie.toolbox.AbstractTool#getDestPathPDF()
     */
    protected File getDestPathPDF() throws InstantiationException {
        return (File) getValue("destfile");
    }
}
