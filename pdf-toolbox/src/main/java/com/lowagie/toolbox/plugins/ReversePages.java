/*
 * $Id: ReversePages.java 3271 2008-04-18 20:39:42Z xlv $
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

import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.toolbox.*;
import com.lowagie.toolbox.arguments.*;
import com.lowagie.toolbox.arguments.filters.*;

/**
 * This tool lets you take pages from an existing PDF and copy them in reverse order into a new PDF.
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class ReversePages
    extends AbstractTool {

  static {
    addVersion(
        "$Id: ReversePages.java 3271 2008-04-18 20:39:42Z xlv $");
  }

  FileArgument destfile = null;
  /**
   * Constructs a ReversePages object.
   */
  public ReversePages() {
    menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
    FileArgument inputfile = null;
    inputfile = new FileArgument(this, "srcfile",
                                 "The file you want to reorder", false,
                                 new PdfFilter());
    arguments.add(inputfile);
    destfile = new FileArgument(this, "destfile",
                                "The file to which the reordered version of the original PDF has to be written", true,
                                new PdfFilter());
    arguments.add(destfile);
    inputfile.addPropertyChangeListener(destfile);
  }

  /**
   * @see com.lowagie.toolbox.AbstractTool#createFrame()
   */
  protected void createFrame() {
    internalFrame = new JInternalFrame("ReversePages", true, false, true);
    internalFrame.setSize(300, 80);
    internalFrame.setJMenuBar(getMenubar());
    System.out.println("=== ReversePages OPENED ===");
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
      System.out.println("The original file had " + reader.getNumberOfPages() +
                         " pages.");
      int pages = reader.getNumberOfPages();
      ArrayList<Integer> li = new ArrayList<Integer>();
      for (int i = pages; i > 0; i--) {
        li.add(Integer.valueOf(i));
      }
      reader.selectPages(li);

      System.err.println("The new file has " + pages + " pages.");
      Document document = new Document(reader.getPageSizeWithRotation(1));
      PdfCopy copy = new PdfCopy(document,
                                 new FileOutputStream(dest.getAbsolutePath()));
      document.open();
      PdfImportedPage page;
      for (int i = 0; i < pages; ) {
        ++i;
        System.out.println("Processed page " + i);
        page = copy.getImportedPage(reader, i);
        copy.addPage(page);
      }

      PRAcroForm form = reader.getAcroForm();
      if (form != null) {
        copy.copyAcroForm(reader);
      }
      document.close();

    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

    /**
     *
     * @see com.lowagie.toolbox.AbstractTool#valueHasChanged(com.lowagie.toolbox.arguments.AbstractArgument)
     * @param arg StringArgument
     */
    public void valueHasChanged(AbstractArgument arg) {
    if (internalFrame == null) {
      // if the internal frame is null, the tool was called from the command line
      return;
    }

    if (destfile.getValue() == null && arg.getName().equalsIgnoreCase("srcfile")) {
      String filename = arg.getValue().toString();
      String filenameout = filename.substring(0, filename.indexOf(".",
          filename.length() - 4)) + "_out.pdf";
      destfile.setValue(filenameout);
    }
  }

    /**
     * Take pages from an existing PDF and copy them in reverse order into a new PDF.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
    ReversePages tool = new ReversePages();
    if (args.length < 2) {
      System.err.println(tool.getUsage());
    }
    tool.setMainArguments(args);
    tool.execute();
  }

    /**
     *
     * @see com.lowagie.toolbox.AbstractTool#getDestPathPDF()
     * @throws InstantiationException
     * @return File
     */
    protected File getDestPathPDF() throws InstantiationException {
    return (File) getValue("destfile");
  }

}
