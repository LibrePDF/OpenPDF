/*
 * $Id: InspectPDF.java 3826 2009-03-31 17:46:18Z blowagie $
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

import com.lowagie.text.pdf.PdfEncryptor;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.StringArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import java.io.File;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Allows you to inspect an existing PDF file.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class InspectPDF extends AbstractTool {

    static {
        addVersion("$Id: InspectPDF.java 3826 2009-03-31 17:46:18Z blowagie $");
    }

    /**
     * Constructs an InpectPDF object.
     */
    public InspectPDF() {
        arguments.add(new FileArgument(this, "srcfile", "The file you want to inspect", false, new PdfFilter()));
        arguments.add(new StringArgument(this, "ownerpassword", "The owner password if the file is encrypt"));
    }

    /**
     * Inspects an existing PDF file.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        InspectPDF tool = new InspectPDF();
        if (args.length < 1) {
            System.err.println(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Pdf Information", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== Pdf Information OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        try {
            if (getValue("srcfile") == null) {
                throw new InstantiationException("You need to choose a sourcefile");
            }
            PdfReader reader;
            if (getValue("ownerpassword") == null) {
                reader = new PdfReader(((File) getValue("srcfile")).getAbsolutePath());
            } else {
                reader = new PdfReader(((File) getValue("srcfile")).getAbsolutePath(),
                        ((String) getValue("ownerpassword")).getBytes());
            }
            // Some general document information and page size
            System.out.println("=== Document Information ===");
            System.out.println("PDF Version: " + reader.getPdfVersion());
            System.out.println("Number of pages: " + reader.getNumberOfPages());
            System.out.println("Number of PDF objects: " + reader.getXrefSize());
            System.out.println("File length: " + reader.getFileLength());
            System.out.println("Encrypted? " + reader.isEncrypted());
            if (reader.isEncrypted()) {
                System.out.println("Permissions: " + PdfEncryptor.getPermissionsVerbose(reader.getPermissions()));
                System.out.println("128 bit? " + reader.is128Key());
            }
            System.out.println("Rebuilt? " + reader.isRebuilt());
            // Some metadata
            System.out.println("=== Metadata ===");
            Map<String, String> info = reader.getInfo();
            String key;
            String value;
            for (Map.Entry<String, String> entry : info.entrySet()) {
                key = entry.getKey();
                value = entry.getValue();
                System.out.println(key + ": " + value);
            }
            if (reader.getMetadata() == null) {
                System.out.println("There is no XML Metadata in the file");
            } else {
                System.out.println("XML Metadata: " + new String(reader.getMetadata()));
            }
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
        throw new InstantiationException("There is no file to show.");
    }

}
