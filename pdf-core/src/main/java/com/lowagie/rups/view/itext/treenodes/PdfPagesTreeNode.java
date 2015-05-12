/*
 * $Id: PdfPagesTreeNode.java 3117 2008-01-31 05:53:22Z xlv $
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

package com.lowagie.rups.view.itext.treenodes;

import com.lowagie.text.pdf.PdfDictionary;

/**
 * A tree node corresponding with a Pages dictionary in a PDF file.
 */
public class PdfPagesTreeNode extends PdfObjectTreeNode {
	/**
	 * Creates a tree node for a Pages dictionary.
	 * @param	object	a PdfDictionary of type pages.
	 */
	public PdfPagesTreeNode(PdfDictionary object) {
		super("pages.png", object);
	}

	/** a serial version uid */
	private static final long serialVersionUID = 4527774449030791503L;
}
