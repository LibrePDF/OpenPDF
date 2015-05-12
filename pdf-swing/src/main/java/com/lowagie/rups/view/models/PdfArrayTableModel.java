/*
 * $Id: PdfArrayTableModel.java 3117 2008-01-31 05:53:22Z xlv $
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

package com.lowagie.rups.view.models;

import javax.swing.table.AbstractTableModel;

import com.lowagie.text.pdf.PdfArray;

/**
 * A TableModel in case we want to show a PDF array in a JTable.
 */
public class PdfArrayTableModel extends AbstractTableModel {
	
	/** A serial version UID. */
	private static final long serialVersionUID = 4665485782853993708L;
	/** The PDF array. */
	protected PdfArray array;

	/**
	 * Creates the TableModel.
	 * @param array a PDF array
	 */
	public PdfArrayTableModel(PdfArray array) {
		this.array = array;
	}
	
	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 1;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return array.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return array.getPdfObject(rowIndex);
		default:
			return null;
		}
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Array";
		default:
			return null;
		}
	}

}

