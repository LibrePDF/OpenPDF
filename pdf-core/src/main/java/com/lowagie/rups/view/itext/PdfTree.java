/*
 * $Id: PdfTree.java 3242 2008-04-13 23:00:20Z xlv $
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

package com.lowagie.rups.view.itext;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.lowagie.rups.view.icons.IconTreeCellRenderer;
import com.lowagie.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.lowagie.rups.view.itext.treenodes.PdfTrailerTreeNode;

/**
 * A JTree that shows the object hierarchy of a PDF document.
 */
public class PdfTree extends JTree implements Observer {

	/** The root of the PDF tree. */
	protected PdfTrailerTreeNode root;
	
	/**
	 * Constructs a PDF tree.
	 */
	public PdfTree() {
		super();
		root = new PdfTrailerTreeNode();
		setCellRenderer(new IconTreeCellRenderer());
		update(null, null);
	}
	
	/**
	 * Getter for the root node
	 * @return	the PDF Trailer node
	 */
	public PdfTrailerTreeNode getRoot() {
		return root;
	}

	/**
	 * Updates the PdfTree when a file is closed or when a ObjectLoader
	 * has finished loading objects.
	 * @param observable	the Observable class that started the update
	 * @param obj			the object that has all the updates
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable observable, Object obj) {
		if (obj == null) {
			root = new PdfTrailerTreeNode();
		}
		setModel(new DefaultTreeModel(root));
		repaint();
	}

	/**
	 * Select a specific node in the tree.
	 * Typically this method will be called from a different tree,
	 * such as the pages, outlines or form tree.
	 * @param	node	the node that has to be selected
	 */
	public void selectNode(PdfObjectTreeNode node) {
		TreePath path = new TreePath(node.getPath());
		setSelectionPath(path);
		scrollPathToVisible(path);
	}

	/** a serial version UID */
	private static final long serialVersionUID = 7545804447512085734L;
	
}