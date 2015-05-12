/*
 * $Id: FormTreeNode.java 3117 2008-01-31 05:53:22Z xlv $
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
import com.lowagie.text.pdf.PdfObject;

/**
 * A FormTreeNode is a standard node in a FormTree.
 */
public class FormTreeNode extends IconTreeNode {
	
	/** The corresponding tree node in the PdfTree. */
	protected PdfObjectTreeNode object_node;
	
	/**
	 * Creates the root node of the FormTree.
	 */
	public FormTreeNode() {
		super("form.png", "Form");
	}
	
	/**
	 * Creates a node corresponding with a node in the PdfTree.
	 * @param	node	a corresponding node
	 */
	public FormTreeNode(PdfObjectTreeNode node) {
		super("form.png");
		this.object_node = node;
		if (node.isDictionary()) {
			PdfDictionary dict = (PdfDictionary)node.getPdfObject();
			PdfObject fieldname = dict.get(PdfName.T);
			if (fieldname != null) {
				this.setUserObject(fieldname);
			}
			else {
				this.setUserObject("unnamed field");
			}
		}
	}

    /**
     * Gets the node in the PdfTree that corresponds with this
     * FormTreeNode.
     * @return	a PdfObjectTreeNode in the PdfTree
     */
	public PdfObjectTreeNode getCorrespondingPdfObjectNode() {
		return object_node;
	}

	/** A serial version UID. */
	private static final long serialVersionUID = 7800080437550790989L;
}