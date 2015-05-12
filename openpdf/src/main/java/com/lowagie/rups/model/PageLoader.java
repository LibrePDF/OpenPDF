/*
 * $Id: PageLoader.java 3117 2008-01-31 05:53:22Z xlv $
 *
 * Copyright 2007 Bruno Lowagie.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.lowagie.rups.model;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/**
 * Loads all the PDFPage objects for SUN's PDF Renderer
 * in Background.
 */
public class PageLoader extends BackgroundTask {

	/** The PDFFile (SUN's PDF Renderer class) */
	protected PDFFile file;
	/** The total number of pages. */
	protected int numberOfPages;
	/** True for pages with page number equal to index + 1 that are being loaded. */
	protected boolean[] busy;
	/** True for pages with page number equal to index + 1 that have already been loaded.  */
	protected boolean[] done;
	
	/**
	 * Creates a new page loader.
	 * @param	file	the PDFFile (SUN's PDF Renderer)
	 */
	public PageLoader(PDFFile file) {
		super();
		this.file = file;
		numberOfPages = file.getNumPages();
		busy = new boolean[numberOfPages];
		done = new boolean[numberOfPages];
		start();
	}

	/**
	 * Getter for the number of pages.
	 * @return	the number of pages in the PDF file.
	 */
	public int getNumberOfPages() {
		return numberOfPages;
	}

	/**
	 * Loads a page.
	 * @param	pageNumber	the number of the page that has to be loaded.
	 * @return	the PDFPage that has been loaded.
	 */
	public synchronized PDFPage loadPage(int pageNumber) {
		pageNumber--;
		if (busy[pageNumber]) return null;
		busy[pageNumber] = true;
		PDFPage page = file.getPage(pageNumber + 1, true);
		if (!done[pageNumber]) {
			System.out.println("Loading page " + (pageNumber + 1));
		}
		done[pageNumber] = true;
		busy[pageNumber] = false;
		return page;
	}

	/**
	 * @see com.lowagie.rups.model.BackgroundTask#doTask()
	 */
	@Override
	public void doTask() {
		for (int i = 0; i < numberOfPages; i++) {
			loadPage(i + 1);
		}
	}
}
