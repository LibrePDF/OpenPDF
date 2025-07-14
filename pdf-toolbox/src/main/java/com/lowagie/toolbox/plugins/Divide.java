/*
 * $Id: Divide.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2005-2007 Carsten Hammer
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
 * This class was originally published under the MPL by Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JInternalFrame;

/**
 * This tool lets you generate a PDF that shows N pages on 1.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Divide extends AbstractTool {

    static {
        addVersion("$Id: Divide.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs an Divide object.
     */
    public Divide() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        arguments.add(new FileArgument(this, "srcfile",
                "The file you want to divide", false, new PdfFilter()));
        arguments.add(new FileArgument(this, "destfile", "The resulting PDF",
                true, new PdfFilter()));
    }

    /**
     * Generates a divided version of an NUp version of an existing PDF file.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        Divide tool = new Divide();
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
        internalFrame = new JInternalFrame("Divide", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== Divide OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        try {
            if (getValue("srcfile") == null) {
                throw new InstantiationException(
                        "You need to choose a sourcefile");
            }
            File src = (File) getValue("srcfile");
            if (getValue("destfile") == null) {
                throw new InstantiationException(
                        "You need to choose a destination file");
            }
            File dest = (File) getValue("destfile");

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(src.getAbsolutePath());
            // we retrieve the total number of pages and the page size
            int total = reader.getNumberOfPages();
            System.out.println("There are " + total
                    + " pages in the original file.");

            Rectangle pageSize = reader.getPageSize(1);
            Rectangle newSize = new Rectangle(pageSize.getWidth() / 2, pageSize
                    .getHeight());
            // step 1: creation of a document-object
            Document document = new Document(newSize, 0, 0, 0, 0);
            // step 2: we create a writer that listens to the document
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream(dest));
            // step 3: we open the document
            document.open();
            // step 4: adding the content
            PdfContentByte cb = writer.getDirectContent();
            PdfImportedPage page;
            float offsetX, offsetY;
            int p;
            for (int i = 0; i < total; i++) {
                p = i + 1;
                pageSize = reader.getPageSize(p);
                newSize = new Rectangle(pageSize.getWidth() / 2, pageSize.getHeight());

                document.newPage();
                offsetX = 0;
                offsetY = 0;
                page = writer.getImportedPage(reader, p);
                cb.addTemplate(page, 1, 0, 0, 1, offsetX, offsetY);
                document.newPage();
                offsetX = -newSize.getWidth();
                offsetY = 0;
                page = writer.getImportedPage(reader, p);
                cb.addTemplate(page, 1, 0, 0, 1, offsetX, offsetY);

            }
            // step 5: we close the document
            document.close();
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
            // if the internal frame is null, the tool was called from the
            // command line
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
