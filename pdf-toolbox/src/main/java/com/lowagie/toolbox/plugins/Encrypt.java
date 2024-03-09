/*
 * $Id: Encrypt.java 3271 2008-04-18 20:39:42Z xlv $
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
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.BitsetArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.OptionArgument;
import com.lowagie.toolbox.arguments.StringArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Allows you to encrypt an existing PDF file.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Encrypt extends AbstractTool {

    private final static int[] PERMISSIONS = {
            PdfWriter.ALLOW_PRINTING,
            PdfWriter.ALLOW_MODIFY_CONTENTS,
            PdfWriter.ALLOW_COPY,
            PdfWriter.ALLOW_MODIFY_ANNOTATIONS,
            PdfWriter.ALLOW_FILL_IN,
            PdfWriter.ALLOW_SCREENREADERS,
            PdfWriter.ALLOW_ASSEMBLY,
            PdfWriter.ALLOW_DEGRADED_PRINTING};
    private final static String[] PERMISSION_OPTIONS = {
            "AllowPrinting",
            "AllowModifyContents",
            "AllowCopy",
            "AllowModifyAnnotations",
            "AllowFillIn (128 bit only)",
            "AllowScreenReaders (128 bit only)",
            "AllowAssembly (128 bit only)",
            "AllowDegradedPrinting (128 bit only)"
    };

    static {
        addVersion("$Id: Encrypt.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs an Encrypt object.
     */
    public Encrypt() {
        arguments.add(new FileArgument(this, "srcfile", "The file you want to encrypt", false, new PdfFilter()));
        arguments.add(new FileArgument(this, "destfile", "The file to which the encrypted PDF has to be written", true,
                new PdfFilter()));
        arguments.add(new StringArgument(this, "ownerpassword", "The ownerpassword you want to add to the PDF file"));
        arguments.add(new StringArgument(this, "userpassword", "The userpassword you want to add to the PDF file"));
        arguments.add(new BitsetArgument(this, "permissions", "Permissions on the file", PERMISSION_OPTIONS));
        OptionArgument oa = new OptionArgument(this, "strength", "Strength of the encryption");
        oa.addOption("40 bit encryption", "40");
        oa.addOption("128 bit encryption", "128");
        arguments.add(oa);
    }

    /**
     * Encrypts an existing PDF file.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        Encrypt tool = new Encrypt();
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
        internalFrame = new JInternalFrame("Encrypt", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== Encrypt OPENED ===");
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
            int permissions = 0;
            String p = (String) getValue("permissions");
            if (p != null) {
                for (int k = 0; k < p.length(); ++k) {
                    permissions |= (p.charAt(k) == '0' ? 0 : PERMISSIONS[k]);
                }
            }
            byte[] userpassword = null;
            if (getValue("userpassword") != null) {
                userpassword = ((String) getValue("userpassword")).getBytes();
            }
            byte[] ownerpassword = null;
            if (getValue("ownerpassword") != null) {
                ownerpassword = ((String) getValue("ownerpassword")).getBytes();
            }
            PdfReader reader = new PdfReader(((File) getValue("srcfile")).getAbsolutePath());
            PdfEncryptor.encrypt(
                    reader,
                    new FileOutputStream((File) getValue("destfile")),
                    userpassword,
                    ownerpassword,
                    permissions,
                    "128".equals(getValue("strength"))
            );
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
