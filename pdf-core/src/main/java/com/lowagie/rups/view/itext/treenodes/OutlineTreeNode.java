/*
 * $Id: OutlineTreeNode.java 3117 2008-01-31 05:53:22Z xlv $
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

import com.lowagie.rups.view.icons.IconTreeNode;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;

/**
 * A node in the OutlineTree.
 */
public class OutlineTreeNode extends IconTreeNode {

	/** The corresponding tree node in the PdfTree. */
	protected PdfObjectTreeNode object_node;
	
	/** Creates the root node for the OutlineTree. */
	public OutlineTreeNode() {
		super("outline.png", "Bookmarks");
	}

	/**
	 * Creates a node corresponding with a node in the PdfTree.
	 * @param	node	a corresponding node
	 */
	public OutlineTreeNode(PdfObjectTreeNode node) {
		super("outline.png");
		this.object_node = node;
		PdfDictionary dict = (PdfDictionary)node.getPdfObject();
		this.setUserObject(dict.get(PdfName.TITLE));
	}

    /**
     * Gets the node in the PdfTree that corresponds with this
     * OutlineTreeNode.
     * @return	a PdfObjectTreeNode in the PdfTree
     */
	public PdfObjectTreeNode getCorrespondingPdfObjectNode() {
		return object_node;
	}

	/** A serial version uid */
	private static final long serialVersionUID = 5437651809665762952L;
}