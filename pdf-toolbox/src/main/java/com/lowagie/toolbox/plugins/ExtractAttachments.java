/*
 * $Id: ExtractAttachments.java 3712 2009-02-20 20:11:31Z xlv $
 * Copyright (c) 2005-2007 Paulo Soares, Bruno Lowagie, Carsten Hammer
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
 * Paulo Soares and Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNameTree;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import com.lowagie.toolbox.swing.PdfInformationPanel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.JInternalFrame;

/**
 * This tool lets you extract the attachments of a PDF.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class ExtractAttachments extends AbstractTool {

    static {
        addVersion("$Id: ExtractAttachments.java 3712 2009-02-20 20:11:31Z xlv $");
    }

    /**
     * Constructs a ExtractAttachements object.
     */
    public ExtractAttachments() {
        FileArgument f = new FileArgument(this, "srcfile",
                "The file you want to operate on", false, new PdfFilter());
        f.setLabel(new PdfInformationPanel());
        arguments.add(f);
    }

    /**
     * Extract the attachments of a PDF.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        ExtractAttachments tool = new ExtractAttachments();
        if (args.length < 1) {
            System.err.println(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * Unpacks a file attachment.
     *
     * @param reader   The object that reads the PDF document
     * @param filespec The dictionary containing the file specifications
     * @param outPath  The path where the attachment has to be written
     * @throws IOException on error
     */
    public static void unpackFile(PdfReader reader, PdfDictionary filespec,
            String outPath) throws IOException {
        if (filespec == null) {
            return;
        }
        PdfName type = filespec.getAsName(PdfName.TYPE);
        if (!PdfName.F.equals(type) && !PdfName.FILESPEC.equals(type)) {
            return;
        }
        PdfDictionary ef = filespec.getAsDict(PdfName.EF);
        if (ef == null) {
            return;
        }
        PdfString fn = filespec.getAsString(PdfName.F);
        System.out.println("Unpacking file '" + fn + "' to " + outPath);
        if (fn == null) {
            return;
        }
        File fLast = new File(fn.toUnicodeString());
        File fullPath = new File(outPath, fLast.getName());
        if (fullPath.exists()) {
            return;
        }
        PRStream prs = (PRStream) PdfReader.getPdfObject(ef.get(PdfName.F));
        if (prs == null) {
            return;
        }
        byte[] b = PdfReader.getStreamBytes(prs);
        FileOutputStream fout = new FileOutputStream(fullPath);
        fout.write(b);
        fout.close();
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("ExtractAttachments", true, false,
                true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== ExtractAttachments OPENED ===");
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

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(src.getAbsolutePath());
            final File parentFile = src.getParentFile();
            final String outPath;
            if (parentFile != null) {
                outPath = parentFile.getAbsolutePath();
            } else {
                outPath = "";
            }
            PdfDictionary catalog = reader.getCatalog();
            PdfDictionary names = catalog.getAsDict(PdfName.NAMES);
            if (names != null) {
                PdfDictionary embFiles = names.getAsDict(new PdfName("EmbeddedFiles"));
                if (embFiles != null) {
                    HashMap<String, PdfObject> embMap = PdfNameTree.readTree(embFiles);
                    for (PdfObject pdfObject : embMap.values()) {
                        PdfDictionary filespec = (PdfDictionary) PdfReader
                                .getPdfObject(pdfObject);
                        unpackFile(reader, filespec, outPath);
                    }
                }
            }
            for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
                PdfArray annots = reader.getPageN(k).getAsArray(PdfName.ANNOTS);
                if (annots == null) {
                    continue;
                }
                for (PdfObject pdfObject : annots.getElements()) {
                    PdfDictionary annot = (PdfDictionary) PdfReader.getPdfObject(pdfObject);
                    PdfName subType = annot.getAsName(PdfName.SUBTYPE);
                    if (!PdfName.FILEATTACHMENT.equals(subType)) {
                        continue;
                    }
                    PdfDictionary filespec = annot.getAsDict(PdfName.FS);
                    unpackFile(reader, filespec, outPath);
                }
            }

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
        throw new InstantiationException("There is more than one destfile.");
    }

}
