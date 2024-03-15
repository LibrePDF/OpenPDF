/*
 * $Id: JTableAutoModelInterface.java 3117 2008-01-31 05:53:22Z xlv $
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

/**
 * Tables that contain the information necessary for creating their model should implement this interface, so that they
 * can use the JTableAutoModel class as TableModel.
 */
public interface JTableAutoModelInterface {

    /**
     * @return int the number of columns
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    int getColumnCount();

    /**
     * @return int the number of rows
     * @see javax.swing.table.TableModel#getRowCount()
     */
    int getRowCount();

    /**
     * @param columnIndex a column number.
     * @return the name of the column
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    String getColumnName(int columnIndex);

    /**
     * @param rowIndex    int        a row number
     * @param columnIndex int    a column number
     * @return Object    an object corresponding with a cell in the table
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    Object getValueAt(int rowIndex, int columnIndex);
}
