/*
 * $Id: NUp.java 3427 2008-05-24 18:32:31Z xlv $
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

/**
 * This tool lets you generate a PDF that shows N pages on 1.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class NUp extends AbstractTool {

    static {
        addVersion("$Id: NUp.java 3427 2008-05-24 18:32:31Z xlv $");
    }

    /**
     * Constructs an NUp object.
     */
    public NUp() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        arguments.add(new FileArgument(this, "srcfile", "The file you want to N-up", false, new PdfFilter()));
        arguments.add(new FileArgument(this, "destfile", "The resulting PDF", true, new PdfFilter()));
        OptionArgument oa = new OptionArgument(this, "pow2", "The number of pages you want to copy to 1 page");
        oa.addOption("2", "1");
        oa.addOption("4", "2");
        oa.addOption("8", "3");
        oa.addOption("16", "4");
        oa.addOption("32", "5");
        oa.addOption("64", "6");
        arguments.add(oa);
    }

    /**
     * Generates an NUp version of an existing PDF file.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        NUp tool = new NUp();
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
        internalFrame = new JInternalFrame("N-up", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== N-up OPENED ===");
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
            int pow2;
            try {
                pow2 = Integer.parseInt((String) getValue("pow2"));
            } catch (Exception e) {
                pow2 = 1;
            }
            // we create a reader for a certain document
            PdfReader reader = new PdfReader(src.getAbsolutePath());
            // we retrieve the total number of pages and the page size
            int total = reader.getNumberOfPages();
            System.out.println("There are " + total + " pages in the original file.");
            Rectangle pageSize = reader.getPageSize(1);
            Rectangle newSize = (pow2 % 2) == 0 ? new Rectangle(pageSize.getWidth(), pageSize.getHeight())
                    : new Rectangle(pageSize.getHeight(), pageSize.getWidth());
            Rectangle unitSize = new Rectangle(pageSize.getWidth(), pageSize.getHeight());
            Rectangle currentSize;
            for (int i = 0; i < pow2; i++) {
                unitSize = new Rectangle(unitSize.getHeight() / 2, unitSize.getWidth());
            }
            int n = (int) Math.pow(2, pow2);
            int r = (int) Math.pow(2, pow2 / 2);
            int c = n / r;
            // step 1: creation of a document-object
            Document document = new Document(newSize, 0, 0, 0, 0);
            // step 2: we create a writer that listens to the document
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));
            // step 3: we open the document
            document.open();
            // step 4: adding the content
            PdfContentByte cb = writer.getDirectContent();
            PdfImportedPage page;
            float offsetX, offsetY, factor;
            int p;
            for (int i = 0; i < total; i++) {
                if (i % n == 0) {
                    document.newPage();
                }
                p = i + 1;
                offsetX = unitSize.getWidth() * ((i % n) % c);
                offsetY = newSize.getHeight() - (unitSize.getHeight() * (((i % n) / c) + 1));
                currentSize = reader.getPageSize(p);
                factor = Math.min(unitSize.getWidth() / currentSize.getWidth(),
                        unitSize.getHeight() / currentSize.getHeight());
                offsetX += (unitSize.getWidth() - (currentSize.getWidth() * factor)) / 2f;
                offsetY += (unitSize.getHeight() - (currentSize.getHeight() * factor)) / 2f;
                page = writer.getImportedPage(reader, p);
                cb.addTemplate(page, factor, 0, 0, factor, offsetX, offsetY);
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
