/*
 * $Id: PagesTable.java 4033 2009-07-23 13:08:02Z blowagie $
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;

import com.lowagie.rups.controller.PdfReaderController;
import com.lowagie.rups.model.ObjectLoader;
import com.lowagie.rups.model.TreeNodeFactory;
import com.lowagie.rups.view.PageSelectionListener;
import com.lowagie.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.lowagie.rups.view.itext.treenodes.PdfPageTreeNode;
import com.lowagie.rups.view.itext.treenodes.PdfPagesTreeNode;
import com.lowagie.rups.view.itext.treenodes.PdfTrailerTreeNode;
import com.lowagie.rups.view.models.JTableAutoModel;
import com.lowagie.rups.view.models.JTableAutoModelInterface;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfPageLabels;

/**
 * A JTable listing all the pages in a PDF file: the object number of each
 * page dictionary and the page numbers (with label information if present).
 */
public class PagesTable extends JTable implements JTableAutoModelInterface, Observer {

	/** A list with page nodes. */
	protected ArrayList<PdfPageTreeNode> list = new ArrayList<PdfPageTreeNode>();
	/** Nodes in the FormTree correspond with nodes in the main PdfTree. */
	protected PdfReaderController controller;
	/***/
	protected PageSelectionListener listener;

	/**
	 * Constructs a PagesTable.
	 * @param	listener	the page navigation listener.
	 */
	public PagesTable(PdfReaderController controller, PageSelectionListener listener) {
		this.controller = controller;
		this.listener = listener;
	}
	
	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable observable, Object obj) {
		if (obj == null) {
			list = new ArrayList<PdfPageTreeNode>();
			repaint();
			return;
		}
		if (obj instanceof ObjectLoader) {
			ObjectLoader loader = (ObjectLoader)obj;
			String[] pagelabels = PdfPageLabels.getPageLabels(loader.getReader());
			int i = 0;
			TreeNodeFactory factory = loader.getNodes();
			PdfTrailerTreeNode trailer = controller.getPdfTree().getRoot();
			PdfObjectTreeNode catalog = factory.getChildNode(trailer, PdfName.ROOT);
			PdfPagesTreeNode pages = (PdfPagesTreeNode)factory.getChildNode(catalog, PdfName.PAGES);
			if (pages == null) {
				return;
			}
			Enumeration p = pages.depthFirstEnumeration();
			PdfObjectTreeNode  child;
			StringBuffer buf;
			while (p.hasMoreElements()) {
				child = (PdfObjectTreeNode)p.nextElement();
				if (child instanceof PdfPageTreeNode) {
					buf = new StringBuffer("Page ");
					buf.append(++i);
					if (pagelabels != null) {
						buf.append(" ( ");
						buf.append(pagelabels[i - 1]);
						buf.append(" )");
					}
					child.setUserObject(buf.toString());
					list.add((PdfPageTreeNode)child);
				}
			}
		}
		setModel(new JTableAutoModel(this));
	}
	
	/**
	 * @see javax.swing.JTable#getColumnCount()
	 */
	public int getColumnCount() {
		return 2;
	}
	
	/**
	 * @see javax.swing.JTable#getRowCount()
	 */
	public int getRowCount() {
		return list.size();
	}

    /**
     * @see javax.swing.JTable#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
    	if (getRowCount() == 0) return null;
		switch (columnIndex) {
		case 0:
			return "Object " + list.get(rowIndex).getNumber();
		case 1:
			return list.get(rowIndex);
		}
		return null;
	}
	/**
	 * @see javax.swing.JTable#getColumnName(int)
	 */
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Object";
		case 1:
			return "Page";
		default:
			return null;
		}
	}

	/**
	 * @see javax.swing.JTable#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if (evt != null)
			super.valueChanged(evt);
		if (controller == null)
			return;
		if (getRowCount() > 0) {
			controller.selectNode(list.get(getSelectedRow()));
			if (listener != null)
				listener.gotoPage(getSelectedRow() + 1);
		}
	}

	/** A serial version UID. */
	private static final long serialVersionUID = -6523261089453886508L;

}