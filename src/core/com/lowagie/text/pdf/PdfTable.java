/*
 * $Id: PdfTable.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 1999, 2000, 2001, 2002 Bruno Lowagie
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.pdf;

import java.util.ArrayList;
import java.util.Iterator;

import com.lowagie.text.Cell;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Row;
import com.lowagie.text.Table;

/**
 * <CODE>PdfTable</CODE> is an object that contains the graphics and text of a table.
 *
 * @see		com.lowagie.text.Table
 * @see		com.lowagie.text.Row
 * @see		com.lowagie.text.Cell
 * @see		PdfCell
 */

public class PdfTable extends Rectangle {
    
	// membervariables
    
	/** this is the number of columns in the table. */
	private int columns;
    
	/** this is the ArrayList with all the cell of the table header. */
	private ArrayList headercells;
    
	/** this is the ArrayList with all the cells in the table. */
	private ArrayList cells;
    
	/** Original table used to build this object*/
	protected Table table;
	
	/** Cached column widths. */
	protected float[] positions;
	
	// constructors

	/**
	 * Constructs a <CODE>PdfTable</CODE>-object.
	 *
	 * @param	table	a <CODE>Table</CODE>
	 * @param	left	the left border on the page
	 * @param	right	the right border on the page
	 * @param	top		the start position of the top of the table
	 * @since	a parameter of this method has been removed in iText 2.0.8
	 */
    
	PdfTable(Table table, float left, float right, float top) {
		// constructs a Rectangle (the bottom value will be changed afterwards)
		super(left, top, right, top);
		this.table = table;
        table.complete();
        
		// copying the attributes from class Table
        cloneNonPositionParameters(table);

		this.columns = table.getColumns();
		positions = table.getWidths(left, right - left);
        
		// initialization of some parameters
		setLeft(positions[0]);
		setRight(positions[positions.length - 1]);
		
		headercells = new ArrayList();
		cells = new ArrayList();

		updateRowAdditionsInternal();
	}

	// methods

	/**
	 * Updates the table row additions in the underlying table object and deletes all table rows, 
	 * in order to preserve memory and detect future row additions.
	 * <p><b>Pre-requisite</b>: the object must have been built with the parameter <code>supportUpdateRowAdditions</code> equals to true.
	 */
	
	void updateRowAdditions() {
        table.complete();
		updateRowAdditionsInternal();
		table.deleteAllRows();
	}
	
	/**
	 * Updates the table row additions in the underlying table object
	 */
	
	private void updateRowAdditionsInternal() {
		// correct table : fill empty cells/ parse table in table
		Row row;
		int prevRows = rows();
		int rowNumber = 0;
		int groupNumber = 0;
		boolean groupChange;
		int firstDataRow = table.getLastHeaderRow() + 1;
		Cell cell;
		PdfCell currentCell;
		ArrayList newCells = new ArrayList();
		int rows = table.size() + 1;
		float[] offsets = new float[rows];
		for (int i = 0; i < rows; i++) {
			offsets[i] = getBottom();
		}
        
		// loop over all the rows
		for (Iterator rowIterator = table.iterator(); rowIterator.hasNext(); ) {
			groupChange = false;
			row = (Row) rowIterator.next();
			if (row.isEmpty()) {
				if (rowNumber < rows - 1 && offsets[rowNumber + 1] > offsets[rowNumber]) offsets[rowNumber + 1] = offsets[rowNumber];
			}
			else {
				for(int i = 0; i < row.getColumns(); i++) {
					cell = (Cell) row.getCell(i);
					if (cell != null) {
						currentCell = new PdfCell(cell, rowNumber+prevRows, positions[i], positions[i + cell.getColspan()], offsets[rowNumber], cellspacing(), cellpadding());
						if (rowNumber < firstDataRow) {
							currentCell.setHeader();
							headercells.add(currentCell);
							if (!table.isNotAddedYet())
								continue;
						}
						try {
							if (offsets[rowNumber] - currentCell.getHeight() - cellpadding() < offsets[rowNumber + currentCell.rowspan()]) {
								offsets[rowNumber + currentCell.rowspan()] = offsets[rowNumber] - currentCell.getHeight() - cellpadding();
							}
						}
						catch(ArrayIndexOutOfBoundsException aioobe) {
							if (offsets[rowNumber] - currentCell.getHeight() < offsets[rows - 1]) {
								offsets[rows - 1] = offsets[rowNumber] - currentCell.getHeight();
							}
						}
						currentCell.setGroupNumber(groupNumber);
						groupChange |= cell.getGroupChange();
						newCells.add(currentCell);
					}
				}
			}
			rowNumber++;
			if( groupChange ) groupNumber++;
		}
        
		// loop over all the cells
		int n = newCells.size();
		for (int i = 0; i < n; i++) {
			currentCell = (PdfCell) newCells.get(i);
			try {
				currentCell.setBottom(offsets[currentCell.rownumber()-prevRows + currentCell.rowspan()]);
			}
			catch(ArrayIndexOutOfBoundsException aioobe) {
				currentCell.setBottom(offsets[rows - 1]);
			}
		}
		cells.addAll(newCells);
		setBottom(offsets[rows - 1]);
	}

	/**
	 * Get the number of rows
	 */
	
	int rows() {
		return cells.isEmpty() ? 0 : ((PdfCell)cells.get(cells.size()-1)).rownumber()+1; 
	}

	/** @see com.lowagie.text.Element#type() */
	public int type() {
		return Element.TABLE;
	}
    
	/**
	 * Returns the arraylist with the cells of the table header.
	 *
	 * @return	an <CODE>ArrayList</CODE>
	 */
    
	ArrayList getHeaderCells() {
		return headercells;
	}
    
	/**
	 * Checks if there is a table header.
	 *
	 * @return	an <CODE>ArrayList</CODE>
	 */
    
	boolean hasHeader() {
		return !headercells.isEmpty();
	}
    
	/**
	 * Returns the arraylist with the cells of the table.
	 *
	 * @return	an <CODE>ArrayList</CODE>
	 */
    
	ArrayList getCells() {
		return cells;
	}
    
	/**
	 * Returns the number of columns of the table.
	 *
	 * @return	the number of columns
	 */
    
	int columns() {
		return columns;
	}
    
	/**
	 * Returns the cellpadding of the table.
	 *
	 * @return	the cellpadding
	 */
    
	final float cellpadding() {
		return table.getPadding();
	}
    
	/**
	 * Returns the cellspacing of the table.
	 *
	 * @return	the cellspacing
	 */
    
	final float cellspacing() {
		return table.getSpacing();
	}
	
	/**
	 * Checks if this <CODE>Table</CODE> has to fit a page.
	 *
	 * @return  true if the table may not be split
	 */

	public final boolean hasToFitPageTable() {
		return table.isTableFitsPage();
	}

	/**
	 * Checks if the cells of this <CODE>Table</CODE> have to fit a page.
	 *
	 * @return  true if the cells may not be split
	 */
	
	public final boolean hasToFitPageCells() {
		return table.isCellsFitPage();
	}

	/**
	 * Gets the offset of this table.
	 *
	 * @return  the space between this table and the previous element.
	 */
	public float getOffset() {
		return table.getOffset();
	}
}
