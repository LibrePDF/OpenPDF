/*
 * $Id: Row.java 4065 2009-09-16 23:09:11Z psoares33 $
 * $Name$
 *
 * Copyright 1999, 2000, 2001, 2002 by Bruno Lowagie.
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
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU LIBRARY GENERAL PUBLIC LICENSE for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text;

import java.util.ArrayList;
import com.lowagie.text.error_messages.MessageLocalization;

/**
 * A <CODE>Row</CODE> is part of a <CODE>Table</CODE>
 * and contains some <CODE>Cells</CODE>.
 * <P>
 * All <CODE>Row</CODE>s are constructed by a <CODE>Table</CODE>-object.
 * You don't have to construct any <CODE>Row</CODE> yourself.
 * In fact you can't construct a <CODE>Row</CODE> outside the package.
 * <P>
 * Since a <CODE>Cell</CODE> can span several rows and/or columns
 * a row can contain reserved space without any content.
 *
 * @see   Element
 * @see   Cell
 * @see   Table
 */
public class Row implements Element {
    
    // constants
    
	/** id of a null element in a Row*/
    public static final int NULL = 0;
    
    /** id of the Cell element in a Row*/
    public static final int CELL = 1;
    
    /** id of the Table element in a Row*/
    public static final int TABLE = 2;
    
    // member variables
    
    /** This is the number of columns in the <CODE>Row</CODE>. */
    protected int columns;
    
    /** This is a valid position the <CODE>Row</CODE>. */
    protected int currentColumn;
    
    /** This is the array that keeps track of reserved cells. */
    protected boolean[] reserved;
    
    /** This is the array of Objects (<CODE>Cell</CODE> or <CODE>Table</CODE>). */
    protected Object[] cells;
    
    /** This is the vertical alignment. */
    protected int horizontalAlignment;
    
    // constructors
    
    /**
     * Constructs a <CODE>Row</CODE> with a certain number of <VAR>columns</VAR>.
     *
     * @param columns   a number of columns
     */
    protected Row(int columns) {
        this.columns = columns;
        reserved = new boolean[columns];
        cells = new Object[columns];
        currentColumn = 0;
    }
    
    // implementation of the Element-methods
    
    /**
     * Processes the element by adding it (or the different parts) to a
     * <CODE>ElementListener</CODE>.
     *
     * @param listener  an <CODE>ElementListener</CODE>
     * @return  <CODE>true</CODE> if the element was processed successfully
     */
    public boolean process(ElementListener listener) {
        try {
            return listener.add(this);
        }
        catch(DocumentException de) {
            return false;
        }
    }
    
    /**
     * Gets the type of the text element.
     *
     * @return  a type
     */
    public int type() {
        return Element.ROW;
    }
    
    /**
     * Gets all the chunks in this element.
     *
     * @return  an <CODE>ArrayList</CODE>
     */
    public ArrayList getChunks() {
        return new ArrayList();
    }
    
	/**
	 * @see com.lowagie.text.Element#isContent()
	 * @since	iText 2.0.8
	 */
	public boolean isContent() {
		return true;
	}

	/**
	 * @see com.lowagie.text.Element#isNestable()
	 * @since	iText 2.0.8
	 */
	public boolean isNestable() {
		return false;
	}
    
    // method to delete a column
    
    /**
     * Returns a <CODE>Row</CODE> that is a copy of this <CODE>Row</CODE>
     * in which a certain column has been deleted.
     *
     * @param column  the number of the column to delete
     */    
    void deleteColumn(int column) {
        if ((column >= columns) || (column < 0)) {
            throw new IndexOutOfBoundsException(MessageLocalization.getComposedMessage("getcell.at.illegal.index.1", column));
        }
        columns--;
        boolean newReserved[] = new boolean[columns];
        Object newCells[] = new Cell[columns];
        
        for (int i = 0; i < column; i++) {
            newReserved[i] = reserved[i];
            newCells[i] = cells[i];
            if (newCells[i] != null && (i + ((Cell) newCells[i]).getColspan() > column)) {
                ((Cell) newCells[i]).setColspan(((Cell) cells[i]).getColspan() - 1);
            }
        }
        for (int i = column; i < columns; i++) {
            newReserved[i] = reserved[i + 1];
            newCells[i] = cells[i + 1];
        }
        if (cells[column] != null && ((Cell) cells[column]).getColspan() > 1) {
            newCells[column] = cells[column];
            ((Cell) newCells[column]).setColspan(((Cell) newCells[column]).getColspan() - 1);
        }
        reserved = newReserved;
        cells = newCells;
    }
    
    // methods
    
    /**
     * Adds a <CODE>Cell</CODE> to the <CODE>Row</CODE>.
     *
     * @param       element the element to add (currently only Cells and Tables supported)
     * @return      the column position the <CODE>Cell</CODE> was added,
     *                      or <CODE>-1</CODE> if the <CODE>element</CODE> couldn't be added.
     */
    int addElement(Object element) {
        return addElement(element, currentColumn);
    }
    
    /**
     * Adds an element to the <CODE>Row</CODE> at the position given.
     *
     * @param       element the element to add. (currently only Cells and Tables supported
     * @param       column  the position where to add the cell.
     * @return      the column position the <CODE>Cell</CODE> was added,
     *                      or <CODE>-1</CODE> if the <CODE>Cell</CODE> couldn't be added.
     */
    int addElement(Object element, int column) {
        if (element == null) throw new NullPointerException(MessageLocalization.getComposedMessage("addcell.null.argument"));
        if ((column < 0) || (column > columns)) throw new IndexOutOfBoundsException(MessageLocalization.getComposedMessage("addcell.illegal.column.argument"));
        if ( !((getObjectID(element) == CELL) || (getObjectID(element) == TABLE)) ) throw new IllegalArgumentException(MessageLocalization.getComposedMessage("addcell.only.cells.or.tables.allowed"));
        
        int lColspan = ( (Cell.class.isInstance(element)) ? ((Cell) element).getColspan() : 1);
        
        if (!reserve(column, lColspan)) {
            return -1;
        }
        
        cells[column] = element;
        currentColumn += lColspan - 1;
        
        return column;
    }
    
    /**
     * Puts <CODE>Cell</CODE> to the <CODE>Row</CODE> at the position given, doesn't reserve colspan.
     *
     * @param   aElement    the cell to add.
     * @param   column  the position where to add the cell.
     */
    void setElement(Object aElement, int column) {
        if (reserved[column]) throw new IllegalArgumentException(MessageLocalization.getComposedMessage("setelement.position.already.taken"));
        
        cells[column] = aElement;
        if (aElement != null) {
            reserved[column] = true;
        }
    }
    
    /**
     * Reserves a <CODE>Cell</CODE> in the <CODE>Row</CODE>.
     *
     * @param   column  the column that has to be reserved.
     * @return  <CODE>true</CODE> if the column was reserved, <CODE>false</CODE> if not.
     */
    boolean reserve(int column) {
        return reserve(column, 1);
    }
    
    
    /**
     * Reserves a <CODE>Cell</CODE> in the <CODE>Row</CODE>.
     *
     * @param   column  the column that has to be reserved.
     * @param   size    the number of columns
     * @return  <CODE>true</CODE> if the column was reserved, <CODE>false</CODE> if not.
     */
    boolean reserve(int column, int size) {
        if ((column < 0) || ((column + size) > columns)) throw new IndexOutOfBoundsException(MessageLocalization.getComposedMessage("reserve.incorrect.column.size"));
        
        for(int i=column; i < column + size; i++)
        {
            if (reserved[i]) {
                // undo reserve
                for(int j=i; j >= column; j--) {
                    reserved[j] = false;
                }
                return false;
            }
            reserved[i] = true;
        }
        return true;
    }
    
    // methods to retrieve information
    
    /**
     * Returns true/false when this position in the <CODE>Row</CODE> has been reserved, either filled or through a colspan of an Element.
     *
     * @param       column  the column.
     * @return      <CODE>true</CODE> if the column was reserved, <CODE>false</CODE> if not.
     */
    boolean isReserved(int column) {
        return reserved[column];
    }
    
    /**
     * Returns the type-id of the element in a Row.
     *
     * @param       column  the column of which you'd like to know the type
     * @return the type-id of the element in the row
     */
    int getElementID(int column) {
        if (cells[column] == null) return NULL;
        else if (Cell.class.isInstance(cells[column])) return CELL;
        else if (Table.class.isInstance(cells[column])) return TABLE;
        
        return -1;
    }
    
    /**
     * Returns the type-id of an Object.
     *
     * @param       element the object of which you'd like to know the type-id, -1 if invalid
     * @return the type-id of an object
     */
    int getObjectID(Object element) {
        if (element == null) return NULL;
        else if (Cell.class.isInstance(element)) return CELL;
        else if (Table.class.isInstance(element)) return TABLE; 
        return -1;
    }
    
    /**
     * Gets a <CODE>Cell</CODE> or <CODE>Table</CODE> from a certain column.
     *
     * @param   column  the column the <CODE>Cell/Table</CODE> is in.
     * @return  the <CODE>Cell</CODE>,<CODE>Table</CODE> or <VAR>Object</VAR> if the column was
     *                  reserved or null if empty.
     */
    public Object getCell(int column) {
        if ((column < 0) || (column > columns)) {
            throw new IndexOutOfBoundsException(MessageLocalization.getComposedMessage("getcell.at.illegal.index.1.max.is.2", String.valueOf(column), String.valueOf(columns)));
        }
        return cells[column];
    }
    
    /**
     * Checks if the row is empty.
     *
     * @return  <CODE>true</CODE> if none of the columns is reserved.
     */
    public boolean isEmpty() {
        for (int i = 0; i < columns; i++) {
            if (cells[i] != null) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets the number of columns.
     *
     * @return  a value
     */
    public int getColumns() {
        return columns;
    }
    
    /**
     * Sets the horizontal alignment.
     *
     * @param value the new value
     */
    public void setHorizontalAlignment(int value) {
        horizontalAlignment = value;
    }
    
    /**
     * Gets the horizontal alignment.
     *
     * @return  a value
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }
}
