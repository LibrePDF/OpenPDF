/*
 * $Id: PdfTrailerTreeNode.java 3117 2008-01-31 05:53:22Z xlv $
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
 * A special treenode that will be used for the trailer dictionary
 * of a PDF file.
 */
public class PdfTrailerTreeNode extends PdfObjectTreeNode {

    /**
     * Constructs a simple text tree node.
     */
    public PdfTrailerTreeNode() {
		super("pdf.png", null);
		setUserObject("Open a PDF file");
	}

	/**
	 * Sets the object for this node.
	 * @param trailer	the trailer dictionary of a PDF file.
	 */
	public void setTrailer(PdfDictionary trailer) {
		object = trailer;
	}

	/** A serial version id. */
	private static final long serialVersionUID = -3607980103983635182L;

}
