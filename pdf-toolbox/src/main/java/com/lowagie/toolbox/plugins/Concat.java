/*
 * $Id: Concat.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2005-2007 Bruno Lowagie, Carsten Hammer, Paulo Soares
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
 * This class was originally published under the MPL by Bruno Lowagie,
 * Paulo Soares and Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JInternalFrame;

/**
 * Concatenates two PDF files
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Concat extends AbstractTool {

    static {
        addVersion("$Id: Concat.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs a Concat object.
     */
    public Concat() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        arguments.add(new FileArgument(this, "srcfile1", "The first PDF file", false, new PdfFilter()));
        arguments.add(new FileArgument(this, "srcfile2", "The second PDF file", false, new PdfFilter()));
        arguments.add(
                new FileArgument(this, "destfile", "The file to which the concatenated PDF has to be written", true,
                        new PdfFilter()));
    }

    /**
     * Concatenates two PDF files.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        Concat tool = new Concat();
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
        internalFrame = new JInternalFrame("Concatenate 2 PDF files", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== Concat OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        try {
            String[] files = new String[2];
            if (getValue("srcfile1") == null) {
                throw new InstantiationException("You need to choose a first sourcefile");
            }
            files[0] = ((File) getValue("srcfile1")).getAbsolutePath();
            if (getValue("srcfile2") == null) {
                throw new InstantiationException("You need to choose a second sourcefile");
            }
            files[1] = ((File) getValue("srcfile2")).getAbsolutePath();
            if (getValue("destfile") == null) {
                throw new InstantiationException("You need to choose a destination file");
            }
            File pdf_file = (File) getValue("destfile");
            int pageOffset = 0;
            List<Map<String, Object>> master = new ArrayList<>();
            Document document = null;
            PdfCopy writer = null;
            for (int i = 0; i < 2; i++) {
                // we create a reader for a certain document
                PdfReader reader = new PdfReader(files[i]);
                reader.consolidateNamedDestinations();
                // we retrieve the total number of pages
                int n = reader.getNumberOfPages();
                List<Map<String, Object>> bookmarks = SimpleBookmark.getBookmarkList(reader);
                if (bookmarks != null) {
                    if (pageOffset != 0) {
                        SimpleBookmark.shiftPageNumbersInRange(bookmarks, pageOffset, null);
                    }
                    master.addAll(bookmarks);
                }
                pageOffset += n;
                System.out.println("There are " + n + " pages in " + files[i]);
                if (i == 0) {
                    // step 1: creation of a document-object
                    document = new Document(reader.getPageSizeWithRotation(1));
                    // step 2: we create a writer that listens to the document
                    writer = new PdfCopy(document, new FileOutputStream(pdf_file));
                    // step 3: we open the document
                    document.open();
                }
                // step 4: we add content
                PdfImportedPage page;
                for (int p = 0; p < n; ) {
                    ++p;
                    page = writer.getImportedPage(reader, p);
                    writer.addPage(page);
                    System.out.println("Processed page " + p);
                }
            }
            if (!master.isEmpty()) {
                writer.setOutlines(master);
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
