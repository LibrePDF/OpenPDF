/*
 * $Id: CompressDecompressPageContent.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2005-2007 Bruno Lowagie
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
 * This class was originally published under the MPL by Bruno Lowagie.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.OptionArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import com.lowagie.toolbox.swing.PdfInformationPanel;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class CompressDecompressPageContent extends AbstractTool {

    static {
        addVersion("$Id: CompressDecompressPageContent.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs a Burst object.
     */
    public CompressDecompressPageContent() {
        FileArgument f = new FileArgument(this, "srcfile", "The file you want to compress/decompress", false,
                new PdfFilter());
        f.setLabel(new PdfInformationPanel());
        arguments.add(f);
        arguments.add(new FileArgument(this, "destfile",
                "The file to which the compressed/decompressed PDF has to be written", true, new PdfFilter()));
        OptionArgument oa = new OptionArgument(this, "compress", "compress");
        oa.addOption("Compress page content", "true");
        oa.addOption("Decompress page content", "false");
        arguments.add(oa);
    }

    /**
     * Compresses/decompresses the page content streams in a PDF file.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        CompressDecompressPageContent tool = new CompressDecompressPageContent();
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
        internalFrame = new JInternalFrame("Compress/Decompress", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== Compress/Decompress OPENED ===");
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

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        try {
            if (getValue("srcfile") == null) {
                throw new InstantiationException("You need to choose a sourcefile");
            }
            if (getValue("destfile") == null) {
                throw new InstantiationException("You need to choose a destination file");
            }
            boolean compress = "true".equals(getValue("compress"));
            PdfReader reader = new PdfReader(((File) getValue("srcfile")).getAbsolutePath());
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(getDestPathPDF()));
            synchronized (arguments) {
                Document.compress = compress;
                int total = reader.getNumberOfPages() + 1;
                for (int i = 1; i < total; i++) {
                    reader.setPageContent(i, reader.getPageContent(i));
                }
                stamper.close();
                Document.compress = true;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(internalFrame,
                    e.getMessage(),
                    e.getClass().getName(),
                    JOptionPane.ERROR_MESSAGE);
            System.err.println(e.getMessage());
        }
    }
}
