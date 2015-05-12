/*
 * $Id: KnitTiff.java 3271 2008-04-18 20:39:42Z xlv $
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

import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JInternalFrame;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.codec.TiffImage;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.filters.ImageFilter;
import com.lowagie.toolbox.arguments.filters.PdfFilter;

/**
 * Knits two TIFF files, one with the even pages and another with the odd pages, together.
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class KnitTiff extends AbstractTool {

	static {
		addVersion("$Id: KnitTiff.java 3271 2008-04-18 20:39:42Z xlv $");
	}
	/**
	 * Constructs a KnitTiff object.
	 */
	public KnitTiff() {
		menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
		arguments.add(new FileArgument(this, "odd", "The tiff file with the odd pages", false, new ImageFilter(false, false, false, false, false, true)));
		arguments.add(new FileArgument(this, "even", "The tiff file with the even pages", false, new ImageFilter(false, false, false, false, false, true)));
		arguments.add(new FileArgument(this, "destfile", "The file to which the converted TIFF has to be written", true, new PdfFilter()));
	}

	/**
	 * @see com.lowagie.toolbox.AbstractTool#createFrame()
	 */
	protected void createFrame() {
		internalFrame = new JInternalFrame("KnitTiff", true, false, true);
		internalFrame.setSize(300, 80);
		internalFrame.setJMenuBar(getMenubar());
		System.out.println("=== KnitTiff OPENED ===");
	}

	/**
	 * @see com.lowagie.toolbox.AbstractTool#execute()
	 */
	public void execute() {
		try {
			if (getValue("odd") == null) throw new InstantiationException("You need to choose a sourcefile for the odd pages");
			File odd_file = (File)getValue("odd");
			if (getValue("even") == null) throw new InstantiationException("You need to choose a sourcefile for the even pages");
			File even_file = (File)getValue("even");
			if (getValue("destfile") == null) throw new InstantiationException("You need to choose a destination file");
			File pdf_file = (File)getValue("destfile");
			RandomAccessFileOrArray odd = new RandomAccessFileOrArray(odd_file.getAbsolutePath());
			RandomAccessFileOrArray even = new RandomAccessFileOrArray(even_file.getAbsolutePath());
			Image img = TiffImage.getTiffImage(odd, 1);
			Document document = new Document(new Rectangle(img.getScaledWidth(),
					img.getScaledHeight()));
			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream(pdf_file));
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			int count = Math.max(TiffImage.getNumberOfPages(odd), TiffImage
					.getNumberOfPages(even));
			for (int c = 0; c < count; ++c) {
				try {
					Image imgOdd = TiffImage.getTiffImage(odd, c + 1);
					Image imgEven = TiffImage.getTiffImage(even, count - c);
					document.setPageSize(new Rectangle(imgOdd.getScaledWidth(),
							imgOdd.getScaledHeight()));
					document.newPage();
					imgOdd.setAbsolutePosition(0, 0);
					cb.addImage(imgOdd);
					document.setPageSize(new Rectangle(imgEven.getScaledWidth(),
							imgEven.getScaledHeight()));
					document.newPage();
					imgEven.setAbsolutePosition(0, 0);
					cb.addImage(imgEven);

				} catch (Exception e) {
					System.out.println("Exception page " + (c + 1) + " "
							+ e.getMessage());
				}
			}
			odd.close();
			even.close();
			document.close();
		} catch (Exception e) {
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
		// represent the changes of the argument in the internal frame
	}


    /**
     * Converts a tiff file to PDF.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
    	KnitTiff tool = new KnitTiff();
    	if (args.length < 3) {
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
		return (File)getValue("destfile");
	}
}
