/*
 * $Id: DictionaryTableModel.java 3117 2008-01-31 05:53:22Z xlv $
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

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;

/**
 * A TableModel in case we want to show a PDF dictionary in a JTable.
 */
public class DictionaryTableModel extends AbstractTableModel {

	/** A serial version UID. */
	private static final long serialVersionUID = -8835275996639701776L;
	/** The PDF dictionary. */
	protected PdfDictionary dictionary;
	/** An ArrayList with the dictionary keys. */
	protected ArrayList<PdfName> keys = new ArrayList<PdfName>();

	/**
	 * Creates the TableModel.
	 * @param dictionary the dictionary we want to show
	 */
	public DictionaryTableModel(PdfDictionary dictionary) {
		this.dictionary = dictionary;
		for (Iterator i = dictionary.getKeys().iterator(); i.hasNext(); )
			this.keys.add((PdfName) i.next());
	}
	
	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 2;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return dictionary.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return keys.get(rowIndex);
		case 1:
			return dictionary.get(keys.get(rowIndex));
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
			return "Key";
		case 1:
			return "Value";
		default:
			return null;
		}
	}

}
