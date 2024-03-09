/*
 * $Id: RemoveLaunchApplication.java 3271 2008-04-18 20:39:42Z xlv $
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

import com.lowagie.text.pdf.PRIndirectReference;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JInternalFrame;

/**
 * This tool copies an existing PDF and removes potentially dangerous code that launches an application.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class RemoveLaunchApplication
        extends AbstractTool {

    static {
        addVersion(
                "$Id: RemoveLaunchApplication.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs a ReversePages object.
     */
    public RemoveLaunchApplication() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        arguments.add(new FileArgument(this, "srcfile",
                "The file from which you want to remove Launch Application actions", false,
                new PdfFilter()));
        arguments.add(new FileArgument(this, "destfile",
                "The file to which the cleaned up version of the original PDF has to be written", true,
                new PdfFilter()));
    }

    /**
     * Copy an existing PDF and replace the Launch Application Action with JavaScript alerts.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        RemoveLaunchApplication tool = new RemoveLaunchApplication();
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
        internalFrame = new JInternalFrame("Remove Launch Applications", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== Remove Launch Applications OPENED ===");
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
                throw new InstantiationException(
                        "You need to choose a destination file");
            }
            File dest = (File) getValue("destfile");

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(src.getAbsolutePath());
            PdfObject o;
            PdfDictionary d;
            PdfDictionary l;
            PdfName n;
            for (int i = 1; i < reader.getXrefSize(); i++) {
                o = reader.getPdfObject(i);
                if (o instanceof PdfDictionary) {
                    d = (PdfDictionary) o;
                    o = d.get(PdfName.A);
                    if (o == null) {
                        continue;
                    }
                    if (o instanceof PdfDictionary) {
                        l = (PdfDictionary) o;
                    } else {
                        PRIndirectReference r = (PRIndirectReference) o;
                        l = (PdfDictionary) reader.getPdfObject(r.getNumber());
                    }
                    n = (PdfName) l.get(PdfName.S);
                    if (PdfName.LAUNCH.equals(n)) {
                        if (l.get(PdfName.F) != null) {
                            System.out.println("Removed: " + l.get(PdfName.F));
                            l.remove(PdfName.F);
                        }
                        if (l.get(PdfName.WIN) != null) {
                            System.out.println("Removed: " + l.get(PdfName.WIN));
                            l.remove(PdfName.WIN);
                        }
                        l.put(PdfName.S, PdfName.JAVASCRIPT);
                        l.put(PdfName.JS, new PdfString("app.alert('Launch Application Action removed by iText');\r"));
                    }
                }
            }
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
            stamper.close();
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
