/*
 * $Id: XRefTable.java 3117 2008-01-31 05:53:22Z xlv $
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

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;

import com.lowagie.rups.controller.PdfReaderController;
import com.lowagie.rups.model.IndirectObjectFactory;
import com.lowagie.rups.model.ObjectLoader;
import com.lowagie.rups.view.models.JTableAutoModel;
import com.lowagie.rups.view.models.JTableAutoModelInterface;
import com.lowagie.text.pdf.PdfNull;
import com.lowagie.text.pdf.PdfObject;

/**
 * A JTable that shows the indirect objects of a PDF xref table.
 */
public class XRefTable extends JTable implements JTableAutoModelInterface, Observer {

	/** The factory that can produce all the indirect objects. */
	protected IndirectObjectFactory objects;
	/** The renderer that will render an object when selected in the table. */
	protected PdfReaderController controller;
	
	/** Creates a JTable visualizing xref table. */
	public XRefTable(PdfReaderController controller) {
		super();
		this.controller = controller;
	}
	
	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable observable, Object obj) {
		if (obj == null) {
			objects = null;
			repaint();
			return;
		}
		if (observable instanceof PdfReaderController
				&& obj instanceof ObjectLoader) {
			ObjectLoader loader = (ObjectLoader)obj;
			objects = loader.getObjects();
			setModel(new JTableAutoModel(this));
			TableColumn col= getColumnModel().getColumn(0);
			col.setPreferredWidth(5);
		}
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
		if (objects == null) return 0;
		return objects.size();
	}

    /**
     * @see javax.swing.JTable#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return getObjectReferenceByRow(rowIndex);
		case 1:
			return getObjectDescriptionByRow(rowIndex);
		default:
			return null;
		}
	}
	
	/**
	 * Gets the reference number of an indirect object
	 * based on the row index.
	 * @param rowIndex	a row number
	 * @return	a reference number
	 */
	protected int getObjectReferenceByRow(int rowIndex) {
		return objects.getRefByIndex(rowIndex);
	}
	
	/**
	 * Gets the object that is shown in a row.
	 * @param rowIndex	the row number containing the object
	 * @return	a PDF object
	 */
	protected String getObjectDescriptionByRow(int rowIndex) {
		PdfObject object = objects.getObjectByIndex(rowIndex);
		if (object instanceof PdfNull)
			return "Indirect object";
		return object.toString();
	}
	
	/**
	 * @see javax.swing.JTable#getColumnName(int)
	 */
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Number";
		case 1:
			return "Object";
		default:
			return null;
		}
	}
	
	/**
	 * Gets the object that is shown in a row.
	 * @param rowIndex	the row number containing the object
	 * @return	a PDF object
	 */
	protected PdfObject getObjectByRow(int rowIndex) {
		return objects.loadObjectByReference(getObjectReferenceByRow(rowIndex));
	}
	
	/**
	 * Selects a row containing information about an indirect object.
	 * @param ref	the reference number of the indirect object
	 */
	public void selectRowByReference(int ref) {
		int row = objects.getIndexByRef(ref);
		setRowSelectionInterval(row, row);
		scrollRectToVisible(getCellRect(row, 1, true));
		valueChanged(null);
	}

	/**
	 * @see javax.swing.JTable#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if (evt != null)
			super.valueChanged(evt);
		if (controller != null && objects != null) {
			controller.render(getObjectByRow(this.getSelectedRow()));
			controller.selectNode(getObjectReferenceByRow(getSelectedRow()));
		}
	}
	
	/** A serial version UID. */
	private static final long serialVersionUID = -382184619041375537L;

}