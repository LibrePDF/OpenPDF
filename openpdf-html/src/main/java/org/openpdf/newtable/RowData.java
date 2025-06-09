/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.newtable;

import java.util.ArrayList;
import java.util.List;

/**
 * A row in the table grid.  The list of cells it maintains is always large
 * enough to set a table cell at every position in the row.  If there are no
 * colspans, rowspans, or missing cells, the grid row will exactly correspond
 * to the row in the original markup.  On the other hand, colspans may force
 * spanning cells to be inserted, rowspans will mean cells appear in more than
 * one grid row, and positions may be {@code null} if no cell occupies that
 * position in the grid.
 */
public class RowData {
    private final List<TableCellBox> _row = new ArrayList<>();

    public List<TableCellBox> getRow() {
        return _row;
    }

    public void extendToColumnCount(int columnCount) {
        while (_row.size() < columnCount) {
            _row.add(null);
        }
    }

    public void splitColumn(int pos) {
        TableCellBox current = _row.get(pos);
        _row.add(pos+1, current == null ? null : TableCellBox.SPANNING_CELL);
    }
}