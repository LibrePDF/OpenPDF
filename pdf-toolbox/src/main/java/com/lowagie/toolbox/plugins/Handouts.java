/*
 * $Id: Handouts.java 3271 2008-04-18 20:39:42Z xlv $
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
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.OptionArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Generates a PDF file that is usable as Handout.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Handouts extends AbstractTool {

    static {
        addVersion("$Id: Handouts.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs a Handouts object.
     */
    public Handouts() {
        arguments.add(new FileArgument(this, "srcfile", "The file you want to convert", false, new PdfFilter()));
        arguments.add(new FileArgument(this, "destfile", "The file to which the Handout has to be written", true,
                new PdfFilter()));
        OptionArgument oa = new OptionArgument(this, "pages", "The number of pages you want on one handout page");
        oa.addOption("2 pages on 1", "2");
        oa.addOption("3 pages on 1", "3");
        oa.addOption("4 pages on 1", "4");
        oa.addOption("5 pages on 1", "5");
        oa.addOption("6 pages on 1", "6");
        oa.addOption("7 pages on 1", "7");
        oa.addOption("8 pages on 1", "8");
        arguments.add(oa);
    }

    /**
     * Converts a PDF file to a PDF file usable as Handout.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        Handouts tool = new Handouts();
        if (args.length < 2) {
            System.err.println(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Handouts", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== Handouts OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        try {
            if (getValue("srcfile") == null) {
                throw new InstantiationException("You need to choose a sourcefile");
            }
            File src = (File) getValue("srcfile");
            if (getValue("destfile") == null) {
                throw new InstantiationException("You need to choose a destination file");
            }
            File dest = (File) getValue("destfile");
            int pages;
            try {
                pages = Integer.parseInt((String) getValue("pages"));
            } catch (Exception e) {
                pages = 4;
            }

            float x1 = 30f;
            float x2 = 280f;
            float x3 = 320f;
            float x4 = 565f;

            float[] y1 = new float[pages];
            float[] y2 = new float[pages];

            float height = (778f - (20f * (pages - 1))) / pages;
            y1[0] = 812f;
            y2[0] = 812f - height;

            for (int i = 1; i < pages; i++) {
                y1[i] = y2[i - 1] - 20f;
                y2[i] = y1[i] - height;
            }

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(src.getAbsolutePath());
            // we retrieve the total number of pages
            int n = reader.getNumberOfPages();
            System.out.println("There are " + n + " pages in the original file.");

            // step 1: creation of a document-object
            Document document = new Document(PageSize.A4);
            // step 2: we create a writer that listens to the document
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));
            // step 3: we open the document
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfImportedPage page;
            int rotation;
            int i = 0;
            int p = 0;
            // step 4: we add content
            while (i < n) {
                i++;
                Rectangle rect = reader.getPageSizeWithRotation(i);
                float factorx = (x2 - x1) / rect.getWidth();
                float factory = (y1[p] - y2[p]) / rect.getHeight();
                float factor = (factorx < factory ? factorx : factory);
                float dx = (factorx == factor ? 0f : ((x2 - x1) - rect.getWidth() * factor) / 2f);
                float dy = (factory == factor ? 0f : ((y1[p] - y2[p]) - rect.getHeight() * factor) / 2f);
                page = writer.getImportedPage(reader, i);
                rotation = reader.getPageRotation(i);
                if (rotation == 90 || rotation == 270) {
                    cb.addTemplate(page, 0, -factor, factor, 0, x1 + dx, y2[p] + dy + rect.getHeight() * factor);
                } else {
                    cb.addTemplate(page, factor, 0, 0, factor, x1 + dx, y2[p] + dy);
                }
                cb.setRGBColorStroke(0xC0, 0xC0, 0xC0);
                cb.rectangle(x3 - 5f, y2[p] - 5f, x4 - x3 + 10f, y1[p] - y2[p] + 10f);
                for (float l = y1[p] - 19; l > y2[p]; l -= 16) {
                    cb.moveTo(x3, l);
                    cb.lineTo(x4, l);
                }
                cb.rectangle(x1 + dx, y2[p] + dy, rect.getWidth() * factor, rect.getHeight() * factor);
                cb.stroke();
                System.out.println("Processed page " + i);
                p++;
                if (p == pages) {
                    p = 0;
                    document.newPage();
                }
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
