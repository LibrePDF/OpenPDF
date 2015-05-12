/*
 * $Id: PdfObjectPanel.java 3117 2008-01-31 05:53:22Z xlv $
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
import java.awt.CardLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import com.lowagie.rups.view.models.DictionaryTableModel;
import com.lowagie.rups.view.models.PdfArrayTableModel;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfObject;

public class PdfObjectPanel extends JPanel implements Observer {

	/** Name of a panel in the CardLayout. */
	private static final String TEXT = "text";
	/** Name of a panel in the CardLayout. */
	private static final String TABLE = "table";
	
	/** The layout that will show the info about the PDF object that is being analyzed. */
	protected CardLayout layout = new CardLayout();

	/** Table with dictionary entries. */
	JTable table = new JTable();
	/** The text pane with the info about a PDF object in the bottom panel. */
	JTextArea text = new JTextArea();
	
	/** Creates a PDF object panel. */
	public PdfObjectPanel() {
		// layout
		setLayout(layout);

		// dictionary / array / stream
		JScrollPane dict_scrollpane = new JScrollPane();
		dict_scrollpane.setViewportView(table);
		add(dict_scrollpane, TABLE);
		
		// number / string / ...
		JScrollPane text_scrollpane = new JScrollPane();
		text_scrollpane.setViewportView(text);
		add(text_scrollpane, TEXT);
	}
	
	/**
	 * Clear the object panel.
	 */
	public void clear() {
		text.setText(null);
		layout.show(this, TEXT);
	}

	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable observable, Object obj) {
		clear();
	}
	
	/**
	 * Shows a PdfObject as text or in a table.
	 * @param object	the object that needs to be shown.
	 */
	public void render(PdfObject object) {
		if (object == null) {
			text.setText(null);
			layout.show(this, TEXT);
			this.repaint();
			text.repaint();
			return;
		}
		switch(object.type()) {
		case PdfObject.DICTIONARY:
		case PdfObject.STREAM:
			table.setModel(new DictionaryTableModel((PdfDictionary)object));
			layout.show(this, TABLE);
			this.repaint();
			break;
		case PdfObject.ARRAY:
			table.setModel(new PdfArrayTableModel((PdfArray)object));
			layout.show(this, TABLE);
			this.repaint();
			break;
		default:
			text.setText(object.toString());
			layout.show(this, TEXT);
			break;
		}
	}
	
	/** a serial version id. */
	private static final long serialVersionUID = 1302283071087762494L;

}
