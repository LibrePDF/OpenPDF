/*
 * $Id: FormTree.java 3146 2008-02-20 18:10:07Z blowagie $
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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;

import org.dom4j.DocumentException;

import com.lowagie.rups.controller.PdfReaderController;
import com.lowagie.rups.model.ObjectLoader;
import com.lowagie.rups.model.TreeNodeFactory;
import com.lowagie.rups.model.XfaFile;
import com.lowagie.rups.view.icons.IconTreeCellRenderer;
import com.lowagie.rups.view.itext.treenodes.FormTreeNode;
import com.lowagie.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.lowagie.rups.view.itext.treenodes.PdfTrailerTreeNode;
import com.lowagie.rups.view.itext.treenodes.XfaTreeNode;
import com.lowagie.text.pdf.PdfName;

/**
 * A JTree visualizing information about the Interactive Form of the
 * PDF file (if any). Normally shows a tree view of the field hierarchy
 * and individual XDP packets. 
 */
public class FormTree extends JTree implements TreeSelectionListener, Observer {

	/** Nodes in the FormTree correspond with nodes in the main PdfTree. */
	protected PdfReaderController controller;
	
	/** If the form is an XFA form, the XML file is stored in this object. */
	protected XfaFile xfaFile;
	/** Treeview of the XFA file. */
	protected XfaTree xfaTree;
	/** Textview of the XFA file. */
	protected XfaTextArea xfaTextArea;
	
	/**
	 * Creates a new FormTree.
	 */
	public FormTree(PdfReaderController controller) {
		super();
		this.controller = controller;
		setCellRenderer(new IconTreeCellRenderer());
		setModel(new DefaultTreeModel(new FormTreeNode()));
		addTreeSelectionListener(this);
		xfaTree = new XfaTree();
		xfaTextArea = new XfaTextArea();
	}

	/**
	 * Loads the fields of a PDF document into the FormTree.
	 * @param	observable	the observable object
	 * @param	obj			the object
	 */
	public void update(Observable observable, Object obj) {
		if (obj == null) {
			setModel(new DefaultTreeModel(new FormTreeNode()));
			xfaFile = null;
			xfaTree.clear();
			xfaTextArea.clear();
			repaint();
			return;
		}
		if (obj instanceof ObjectLoader) {
			ObjectLoader loader = (ObjectLoader)obj;
			TreeNodeFactory factory = loader.getNodes();
			PdfTrailerTreeNode trailer = controller.getPdfTree().getRoot();
			PdfObjectTreeNode catalog = factory.getChildNode(trailer, PdfName.ROOT);
			PdfObjectTreeNode form = factory.getChildNode(catalog, PdfName.ACROFORM);
			if (form == null) {
				return;
			}
			PdfObjectTreeNode fields = factory.getChildNode(form, PdfName.FIELDS);
			FormTreeNode root = new FormTreeNode();
			if (fields != null) {
				FormTreeNode node = new FormTreeNode(fields);
				node.setUserObject("Fields");
				loadFields(factory, node, fields);
				root.add(node);
			}
			PdfObjectTreeNode xfa = factory.getChildNode(form, PdfName.XFA);
			if (xfa != null) {
				XfaTreeNode node = new XfaTreeNode(xfa);
				node.setUserObject("XFA");
				loadXfa(factory, node, xfa);
				root.add(node);
				try {
					xfaFile = new XfaFile(node);
					xfaTree.load(xfaFile);
					xfaTextArea.load(xfaFile);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (DocumentException e) {
					e.printStackTrace();
				}
			}
			setModel(new DefaultTreeModel(root));
		}
	}

	/**
	 * Method that can be used recursively to load the fields hierarchy into the tree.
	 * @param	factory		a factory that can produce new PDF object nodes
	 * @param	form_node	the parent node in the form tree
	 * @param	object_node	the object node that will be used to create a child node
	 */
	private void loadFields(TreeNodeFactory factory, FormTreeNode form_node, PdfObjectTreeNode object_node) {
		if (object_node == null)
			return;
		factory.expandNode(object_node);
		if (object_node.isIndirectReference()) {
			loadFields(factory, form_node, (PdfObjectTreeNode)object_node.getFirstChild());
		}
		else if (object_node.isArray()) {
			Enumeration children = object_node.children();
			while (children.hasMoreElements()) {
				loadFields(factory, form_node, (PdfObjectTreeNode)children.nextElement());
			}
		}
		else if (object_node.isDictionary()) {
			FormTreeNode leaf = new FormTreeNode(object_node);
			form_node.add(leaf);
			PdfObjectTreeNode kids = factory.getChildNode(object_node, PdfName.KIDS);
			loadFields(factory, leaf, kids);
		}
	}

	/**
	 * Method that will load the nodes that refer to XFA streams.
	 * @param	form_node	the parent node in the form tree
	 * @param	object_node	the object node that will be used to create a child node
	 */
	private void loadXfa(TreeNodeFactory factory, XfaTreeNode form_node, PdfObjectTreeNode object_node) {
		if (object_node == null)
			return;
		factory.expandNode(object_node);
		if (object_node.isIndirectReference()) {
			loadXfa(factory, form_node, (PdfObjectTreeNode)object_node.getFirstChild());
		}
		else if (object_node.isArray()) {
			Enumeration children = object_node.children();
			PdfObjectTreeNode key;
			PdfObjectTreeNode value;
			while (children.hasMoreElements()) {
				key = (PdfObjectTreeNode)children.nextElement();
				value = (PdfObjectTreeNode)children.nextElement();
				if (value.isIndirectReference()) {
					factory.expandNode(value);
					value = (PdfObjectTreeNode)value.getFirstChild();
				}
				form_node.addPacket(key.getPdfObject().toString(), value);
			}
		}
		else if (object_node.isStream()) {
			form_node.addPacket("xdp", object_node);
		}
	}

	/**
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent evt) {
		if (controller == null)
			return;
		FormTreeNode selectednode = (FormTreeNode)this.getLastSelectedPathComponent();
		if (selectednode == null)
			return;
		PdfObjectTreeNode node = selectednode.getCorrespondingPdfObjectNode();
		if (node != null)
			controller.selectNode(node);
	}

	public XfaTree getXfaTree() {
		return xfaTree;
	}
	
	public XfaTextArea getXfaTextArea() {
		return xfaTextArea;
	}
	
	/** A serial version UID. */
	private static final long serialVersionUID = -3584003547303700407L;

}
