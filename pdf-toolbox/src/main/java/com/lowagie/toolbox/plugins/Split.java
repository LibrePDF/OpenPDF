/*
 * $Id: Split.java 3271 2008-04-18 20:39:42Z xlv $
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
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.IntegerArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import com.lowagie.toolbox.swing.PdfInformationPanel;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JInternalFrame;

/**
 * This tool lets you split a PDF in two separate PDF files.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Split extends AbstractTool {

    static {
        addVersion("$Id: Split.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs an Split object.
     */
    public Split() {
        FileArgument f = new FileArgument(this, "srcfile", "The file you want to split", false, new PdfFilter());
        f.setLabel(new PdfInformationPanel());
        arguments.add(f);
        arguments.add(new FileArgument(this, "destfile1",
                "The file to which the first part of the original PDF has to be written", true, new PdfFilter()));
        arguments.add(new FileArgument(this, "destfile2",
                "The file to which the second part of the original PDF has to be written", true, new PdfFilter()));
        arguments.add(new IntegerArgument(this, "pagenumber", "The pagenumber where you want to split"));
    }

    /**
     * Split a PDF in two separate PDF files.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        Split tool = new Split();
        if (args.length < 4) {
            System.err.println(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Split", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== Split OPENED ===");
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
            if (getValue("destfile1") == null) {
                throw new InstantiationException("You need to choose a destination file for the first part of the PDF");
            }
            File file1 = (File) getValue("destfile1");
            if (getValue("destfile2") == null) {
                throw new InstantiationException(
                        "You need to choose a destination file for the second part of the PDF");
            }
            File file2 = (File) getValue("destfile2");
            int pagenumber = Integer.parseInt((String) getValue("pagenumber"));

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(src.getAbsolutePath());
            // we retrieve the total number of pages
            int n = reader.getNumberOfPages();
            System.out.println("There are " + n + " pages in the original file.");

            if (pagenumber < 2 || pagenumber > n) {
                throw new DocumentException(
                        "You can't split this document at page " + pagenumber + "; there is no such page.");
            }

            // step 1: creation of a document-object
            Document document1 = new Document(reader.getPageSizeWithRotation(1));
            Document document2 = new Document(reader.getPageSizeWithRotation(pagenumber));
            // step 2: we create a writer that listens to the document
            PdfWriter writer1 = PdfWriter.getInstance(document1, new FileOutputStream(file1));
            PdfWriter writer2 = PdfWriter.getInstance(document2, new FileOutputStream(file2));
            // step 3: we open the document
            document1.open();
            PdfContentByte cb1 = writer1.getDirectContent();
            document2.open();
            PdfContentByte cb2 = writer2.getDirectContent();
            PdfImportedPage page;
            int rotation;
            int i = 0;
            // step 4: we add content
            while (i < pagenumber - 1) {
                i++;
                document1.setPageSize(reader.getPageSizeWithRotation(i));
                document1.newPage();
                page = writer1.getImportedPage(reader, i);
                rotation = reader.getPageRotation(i);
                if (rotation == 90 || rotation == 270) {
                    cb1.addTemplate(page, 0, -1f, 1f, 0, 0, reader.getPageSizeWithRotation(i).getHeight());
                } else {
                    cb1.addTemplate(page, 1f, 0, 0, 1f, 0, 0);
                }
            }
            while (i < n) {
                i++;
                document2.setPageSize(reader.getPageSizeWithRotation(i));
                document2.newPage();
                page = writer2.getImportedPage(reader, i);
                rotation = reader.getPageRotation(i);
                if (rotation == 90 || rotation == 270) {
                    cb2.addTemplate(page, 0, -1f, 1f, 0, 0, reader.getPageSizeWithRotation(i).getHeight());
                } else {
                    cb2.addTemplate(page, 1f, 0, 0, 1f, 0, 0);
                }
            }
            // step 5: we close the document
            document1.close();
            document2.close();
        } catch (Exception e) {
            e.printStackTrace();
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
        return (File) getValue("destfile1");
    }
}
