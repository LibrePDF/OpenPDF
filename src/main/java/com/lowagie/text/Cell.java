/*
 * $Id: Cell.java 4065 2009-09-16 23:09:11Z psoares33 $
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

package com.lowagie.text;

import java.util.ArrayList;
import java.util.Iterator;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.pdf.PdfPCell;

/**
 * A <CODE>Cell</CODE> is a <CODE>Rectangle</CODE> containing other
 * <CODE>Element</CODE>s.
 * <P>
 * A <CODE>Cell</CODE> must be added to a <CODE>Table</CODE>.
 * The <CODE>Table</CODE> will place the <CODE>Cell</CODE> in
 * a <CODE>Row</CODE>.
 * <P>
 * Example:
 * <BLOCKQUOTE><PRE>
 * Table table = new Table(3);
 * table.setBorderWidth(1);
 * table.setBorderColor(new Color(0, 0, 255));
 * table.setCellpadding(5);
 * table.setCellspacing(5);
 * <STRONG>Cell cell = new Cell("header");</STRONG>
 * <STRONG>cell.setHeader(true);</STRONG>
 * <STRONG>cell.setColspan(3);</STRONG>
 * table.addCell(cell);
 * <STRONG>cell = new Cell("example cell with colspan 1 and rowspan 2");</STRONG>
 * <STRONG>cell.setRowspan(2);</STRONG>
 * <STRONG>cell.setBorderColor(new Color(255, 0, 0));</STRONG>
 * table.addCell(cell);
 * table.addCell("1.1");
 * table.addCell("2.1");
 * table.addCell("1.2");
 * table.addCell("2.2");
 * </PRE></BLOCKQUOTE>
 *
 * @see		Rectangle
 * @see		Element
 * @see		Table
 * @see		Row
 */

public class Cell extends Rectangle implements TextElementArray {

	// membervariables

	/**
	 * The <CODE>ArrayList</CODE> of <CODE>Element</CODE>s
	 * that are part of the content of the Cell.
	 */
	protected ArrayList arrayList = null;

	/** The horizontal alignment of the cell content. */
	protected int horizontalAlignment = Element.ALIGN_UNDEFINED;

	/** The vertical alignment of the cell content. */
	protected int verticalAlignment = Element.ALIGN_UNDEFINED;

	/**
	 * The width of the cell as a String.
	 * It can be an absolute value "100" or a percentage "20%".
	 */
	protected float width;
	protected boolean percentage = false;

	/** The colspan of the cell. */
	protected int colspan = 1;

	/** The rowspan of the cell. */
	protected int rowspan = 1;

	/** The leading of the content inside the cell. */
	float leading = Float.NaN;

	/** Is this <CODE>Cell</CODE> a header? */
	protected boolean header;

	/**
	 * Maximum number of lines allowed in the cell.  
	 * The default value of this property is not to limit the maximum number of lines
	 * (contributed by dperezcar@fcc.es)
	 */
	protected int maxLines = Integer.MAX_VALUE;
	
	/**
	 * If a truncation happens due to the maxLines property, then this text will 
	 * be added to indicate a truncation has happened.
	 * Default value is null, and means avoiding marking the truncation.  
	 * A useful value of this property could be e.g. "..."
	 * (contributed by dperezcar@fcc.es)
	 */
	String showTruncation;

    /**
     * Indicates that the largest ascender height should be used to determine the
     * height of the first line.  Note that this only has an effect when rendered
     * to PDF.  Setting this to true can help with vertical alignment problems.
     */
    protected boolean useAscender = false;

    /**
     * Indicates that the largest descender height should be added to the height of
     * the last line (so characters like y don't dip into the border).   Note that
     * this only has an effect when rendered to PDF.
     */
    protected boolean useDescender = false;

    /**
     * Adjusts the cell contents to compensate for border widths.  Note that
     * this only has an effect when rendered to PDF.
     */
    protected boolean useBorderPadding;
    
	/** Does this <CODE>Cell</CODE> force a group change? */
	protected boolean groupChange = true;

	// constructors

    /** Constructs an empty <CODE>Cell</CODE>. */
	public Cell() {
		// creates a Rectangle with BY DEFAULT a border of 0.5
		super(0, 0, 0, 0);
		setBorder(UNDEFINED);
		setBorderWidth(0.5f);
		// initializes the arraylist
		arrayList = new ArrayList();
	}

	/**
	 * Constructs an empty <CODE>Cell</CODE> (for internal use only).
	 *
	 * @param   dummy   a dummy value
	 */
	public Cell(boolean dummy) {
		this();
		arrayList.add(new Paragraph(0));
	}

	/**
	 * Constructs a <CODE>Cell</CODE> with a certain content.<p>
	 * The <CODE>String</CODE> will be converted into a <CODE>Paragraph</CODE>.
	 * @param	content		a <CODE>String</CODE>
	 */
	public Cell(String content) {
		this();
		try {
			addElement(new Paragraph(content));
		}
		catch(BadElementException bee) {
		}
	}

	/**
	 * Constructs a <CODE>Cell</CODE> with a certain <CODE>Element</CODE>.<p>
	 * if the element is a <CODE>ListItem</CODE>, <CODE>Row</CODE> or
	 * <CODE>Cell</CODE>, an exception will be thrown.
	 *
	 * @param	element		the element
	 * @throws	BadElementException when the creator was called with a <CODE>ListItem</CODE>, <CODE>Row</CODE> or <CODE>Cell</CODE>
	 */
	public Cell(Element element) throws BadElementException {
		this();
 		if(element instanceof Phrase) {
			setLeading(((Phrase)element).getLeading());
		}
		addElement(element);
	}

	// implementation of the Element-methods

	/**
	 * Processes the element by adding it (or the different parts) to an
	 * <CODE>ElementListener</CODE>.
	 *
	 * @param	listener	an <CODE>ElementListener</CODE>
	 * @return	<CODE>true</CODE> if the element was processed successfully
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
	 * @return	a type
	 */
	public int type() {
		return Element.CELL;
	}

	/**
	 * Gets all the chunks in this element.
	 *
	 * @return	an <CODE>ArrayList</CODE>
	 */
	public ArrayList getChunks() {
		ArrayList tmp = new ArrayList();
		for (Iterator i = arrayList.iterator(); i.hasNext(); ) {
			tmp.addAll(((Element) i.next()).getChunks());
		}
		return tmp;
	}

	// Getters and setters

	/**
     * Gets the horizontal alignment.
     *
     * @return	a value
     */
   	public int getHorizontalAlignment() {
   		return horizontalAlignment;
   	}

	/**
	 * Sets the horizontal alignment.
	 * @param	value	the new value
	 */
	public void setHorizontalAlignment(int value) {
		horizontalAlignment = value;
	}

	/**
	 * Sets the alignment of this cell.
	 * This methods allows you to set the alignment as a String.
	 * @param	alignment		the new alignment as a <CODE>String</CODE>
	 */
	public void setHorizontalAlignment(String alignment) {
		setHorizontalAlignment(ElementTags.alignmentValue(alignment));
	}

	/**
	 * Gets the vertical alignment.
	 * @return	a value
	 */
	public int getVerticalAlignment() {
		return verticalAlignment;
	}

	/**
	 * Sets the vertical alignment.
	 * @param	value	the new value
	 */
	public void setVerticalAlignment(int value) {
		verticalAlignment = value;
	}

	/**
	 * Sets the alignment of this paragraph.
	 *
	 * @param	alignment		the new alignment as a <CODE>String</CODE>
	 */
	public void setVerticalAlignment(String alignment) {
		setVerticalAlignment(ElementTags.alignmentValue(alignment));
	}

	/**
	 * Sets the width.
	 *
	 * @param	value	the new value
	 */
	public void setWidth(float value) {
		this.width = value;
	}
	
	/**
	 * Sets the width.
	 * It can be an absolute value "100" or a percentage "20%"
	 *
	 * @param	value	the new value
	 */
	public void setWidth(String value) {
		if (value.endsWith("%")) {
			value = value.substring(0, value.length() - 1);
			percentage = true;
		}
		width = Integer.parseInt(value);
	}
	
	/**
	 * Gets the width.
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Gets the width as a String.
	 *
	 * @return	a value
	 */
	public String getWidthAsString() {
		String w = String.valueOf(width);
		if (w.endsWith(".0")) w = w.substring(0, w.length() - 2);
		if (percentage) w += "%";
		return w;
	}

	/**
	 * Sets the colspan.
	 *
	 * @param	value	the new value
	 */
	public void setColspan(int value) {
		colspan = value;
	}

	/**
	 * Gets the colspan.
	 * @return	a value
	 */
	public int getColspan() {
		return colspan;
	}

	/**
	 * Sets the rowspan.
	 *
	 * @param	value	the new value
	 */
	public void setRowspan(int value) {
		rowspan = value;
	}

	/**
	 * Gets the rowspan.
	 * @return	a value
	 */
	public int getRowspan() {
		return rowspan;
	}

	/**
	 * Sets the leading.
	 *
	 * @param	value	the new value
	 */
	public void setLeading(float value) {
		leading = value;
	}

	/**
	 * Gets the leading.
	 *
	 * @return	a value
	 */
	public float getLeading() {
		if (Float.isNaN(leading)) {
			return 16;
		}
		return leading;
	}

	/**
	 * Sets header.
	 *
	 * @param	value	the new value
	 */
	public void setHeader(boolean value) {
		header = value;
	}

	/**
	 * Is this <CODE>Cell</CODE> a header?
	 *
	 * @return	a value
	 */
	public boolean isHeader() {
		return header;
	}
	
	/**
	 * Setter for maxLines
	 * @param value the maximum number of lines
	 */
	public void setMaxLines(int value) {
		maxLines = value;
	}
	
	/**
	 * Getter for maxLines
	 * @return the maxLines value
	 */
	public int getMaxLines() {
		return maxLines;
	}
		
	/**
	 * Setter for showTruncation
	 * @param value	Can be null for avoiding marking the truncation.
	 */
	public void setShowTruncation(String value) {
		showTruncation = value;
	}
	
	/**
	 * Getter for showTruncation
	 * @return the showTruncation value
	 */
	public String getShowTruncation() {
		return showTruncation;
	}

	/**
	 * Sets the value of useAscender.
	 * @param use use ascender height if true
	 */
	public void setUseAscender(boolean use) {
	    useAscender = use;
	}

	/**
	 * Gets the value of useAscender
	 * @return useAscender
	 */
	public boolean isUseAscender() {
	    return useAscender;
	}

	/**
	 * Sets the value of useDescender.
	 * @param use use descender height if true
	 */
	public void setUseDescender(boolean use) {
	    useDescender = use;
	}

	/**
	 * gets the value of useDescender
	 * @return useDescender
	 */
	public boolean isUseDescender() {
	    return useDescender;
	}

	/**
	 * Sets the value of useBorderPadding.
	 * @param use adjust layout for borders if true
	 */
	public void setUseBorderPadding(boolean use) {
	    useBorderPadding = use;
	}

	/**
	 * Gets the value of useBorderPadding.
	 * @return useBorderPadding
	 */
	public boolean isUseBorderPadding() {
	    return useBorderPadding;
	}

	/**
	 * Does this <CODE>Cell</CODE> force a group change?
	 *
	 * @return	a value
	 */
	public boolean getGroupChange() {
		return groupChange;
	}

	/**
	 * Sets group change.
	 *
	 * @param	value	the new value
	 */
	public void setGroupChange(boolean value) {
		groupChange = value;
	}
	
// arraylist stuff

	/**
	 * Gets the number of <CODE>Element</CODE>s in the Cell.
	 *
	 * @return	a <CODE>size</CODE>.
	 */
	public int size() {
		return arrayList.size();
	}

	/**
	 * Gets an iterator of <CODE>Element</CODE>s.
	 *
	 * @return	an <CODE>Iterator</CODE>.
	 */
	public Iterator getElements() {
		return arrayList.iterator();
	}
	
	/**
	 * Clears all the <CODE>Element</CODE>s of this <CODE>Cell</CODE>.
	 */
	public void clear() {
		arrayList.clear();
	}

	/**
	 * Checks if the <CODE>Cell</CODE> is empty.
	 *
	 * @return	<CODE>false</CODE> if there are non-empty <CODE>Element</CODE>s in the <CODE>Cell</CODE>.
	 */
	public boolean isEmpty() {
		switch(size()) {
			case 0:
				return true;
			case 1:
				Element element = (Element) arrayList.get(0);
				switch (element.type()) {
					case Element.CHUNK:
						return ((Chunk) element).isEmpty();
					case Element.ANCHOR:
					case Element.PHRASE:
					case Element.PARAGRAPH:
						return ((Phrase) element).isEmpty();
					case Element.LIST:
						return ((List) element).isEmpty();
				}
			return false;
			default:
				return false;
		}
	}
	
	/**
	 * Makes sure there is at least 1 object in the Cell.
	 *
	 * Otherwise it might not be shown in the table.
	 */
	void fill() {
		if (size() == 0) arrayList.add(new Paragraph(0));
	}

	/**
	 * Checks if this <CODE>Cell</CODE> is a placeholder for a (nested) table.
	 *
	 * @return	true if the only element in this cell is a table
	 */
	public boolean isTable() {
		return (size() == 1)
			&& (((Element)arrayList.get(0)).type() == Element.TABLE);
	}
	
	/**
	 * Adds an element to this <CODE>Cell</CODE>.
	 * <P>
	 * Remark: you can't add <CODE>ListItem</CODE>s, <CODE>Row</CODE>s, <CODE>Cell</CODE>s,
	 * <CODE>JPEG</CODE>s, <CODE>GIF</CODE>s or <CODE>PNG</CODE>s to a <CODE>Cell</CODE>.
	 *
	 * @param element The <CODE>Element</CODE> to add
	 * @throws BadElementException if the method was called with a <CODE>ListItem</CODE>, <CODE>Row</CODE> or <CODE>Cell</CODE>
	 */
	public void addElement(Element element) throws BadElementException {
		if (isTable()) {
			Table table = (Table) arrayList.get(0);
			Cell tmp = new Cell(element);
			tmp.setBorder(NO_BORDER);
			tmp.setColspan(table.getColumns());
			table.addCell(tmp);
			return;
		}
		switch(element.type()) {
			case Element.LISTITEM:
			case Element.ROW:
			case Element.CELL:
				throw new BadElementException(MessageLocalization.getComposedMessage("you.can.t.add.listitems.rows.or.cells.to.a.cell"));
			case Element.LIST:
				List list = (List)element;
				if (Float.isNaN(leading)) {
					setLeading(list.getTotalLeading());
				}
				if (list.isEmpty()) return;
				arrayList.add(element);
				return;
			case Element.ANCHOR:
			case Element.PARAGRAPH:
			case Element.PHRASE:
				Phrase p = (Phrase)element;
				if (Float.isNaN(leading)) {
					setLeading(p.getLeading());
				}
				if (p.isEmpty()) return;
				arrayList.add(element);
				return;
			case Element.CHUNK:
				if (((Chunk) element).isEmpty()) return;
				arrayList.add(element);
				return;
			case Element.TABLE:
				Table table = new Table(3);
				float[] widths = new float[3];
				widths[1] = ((Table)element).getWidth();
				switch(((Table)element).getAlignment()) {
					case Element.ALIGN_LEFT:
						widths[0] = 0f;
						widths[2] = 100f - widths[1];
						break;
					case Element.ALIGN_CENTER:
						widths[0] = (100f - widths[1]) / 2f;
						widths[2] = widths[0];
						break;
					case Element.ALIGN_RIGHT:
						widths[0] = 100f - widths[1];
						widths[2] = 0f;
				}
				table.setWidths(widths);
				Cell tmp;
				if (arrayList.isEmpty()) {
					table.addCell(getDummyCell());
				}
				else {
					tmp = new Cell();
					tmp.setBorder(NO_BORDER);
					tmp.setColspan(3);
					for (Iterator i = arrayList.iterator(); i.hasNext(); ) {
						tmp.add(i.next());
					}
					table.addCell(tmp);
				}
				tmp = new Cell();
				tmp.setBorder(NO_BORDER);
				table.addCell(tmp);
				table.insertTable((Table)element);
				tmp = new Cell();
				tmp.setBorder(NO_BORDER);
				table.addCell(tmp);
				table.addCell(getDummyCell());
				clear();
				arrayList.add(table);
				return;
			default:
				arrayList.add(element);
		}
	}

	/**
	 * Add an <CODE>Object</CODE> to this cell.
	 *
	 * @param o the object to add
	 * @return always <CODE>true</CODE>
	 */
	public boolean add(Object o) {
		try {
			this.addElement((Element) o);
			return true;
		}
		catch(ClassCastException cce) {
			throw new ClassCastException(MessageLocalization.getComposedMessage("you.can.only.add.objects.that.implement.the.element.interface"));
		}
		catch(BadElementException bee) {
			throw new ClassCastException(bee.getMessage());
		}
	}

	// helper methods
	
	/**
     * Get dummy cell used when merging inner tables. 
     * @return a cell with colspan 3 and no border
     */
    private static Cell getDummyCell() {
        Cell cell = new Cell(true);
        cell.setColspan(3);
        cell.setBorder(NO_BORDER);
        return cell;
	}

	/**
	 * Creates a PdfPCell based on this Cell object.
	 * @return a PdfPCell
	 * @throws BadElementException
	 */
	public PdfPCell createPdfPCell() throws BadElementException {
		if (rowspan > 1) throw new BadElementException(MessageLocalization.getComposedMessage("pdfpcells.can.t.have.a.rowspan.gt.1"));
		if (isTable()) return new PdfPCell(((Table)arrayList.get(0)).createPdfPTable());
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(verticalAlignment);
		cell.setHorizontalAlignment(horizontalAlignment);
		cell.setColspan(colspan);
		cell.setUseBorderPadding(useBorderPadding);
		cell.setUseDescender(useDescender);
		cell.setLeading(getLeading(), 0);
		cell.cloneNonPositionParameters(this);
		cell.setNoWrap(getMaxLines() == 1);
		for (Iterator i = getElements(); i.hasNext(); ) {
            Element e = (Element)i.next();
            if (e.type() == Element.PHRASE || e.type() == Element.PARAGRAPH) {
                Paragraph p = new Paragraph((Phrase)e);
                p.setAlignment(horizontalAlignment);
                e = p;
            }
			cell.addElement(e);
		}
		return cell;
	}

	// unsupported Rectangle methods

	/**
	 * This method throws an <CODE>UnsupportedOperationException</CODE>.
	 * @return NA
	 */
	public float getTop() {
		throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("dimensions.of.a.cell.can.t.be.calculated.see.the.faq"));
	}

	/**
	 * This method throws an <CODE>UnsupportedOperationException</CODE>.
	 * @return NA
	 */
	public float getBottom() {
		throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("dimensions.of.a.cell.can.t.be.calculated.see.the.faq"));
	}

	/**
	 * This method throws an <CODE>UnsupportedOperationException</CODE>.
	 * @return NA
	 */
	public float getLeft() {
		throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("dimensions.of.a.cell.can.t.be.calculated.see.the.faq"));
	}

	/**
	 * This method throws an <CODE>UnsupportedOperationException</CODE>.
	 * @return NA
	 */
	public float getRight() {
		throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("dimensions.of.a.cell.can.t.be.calculated.see.the.faq"));
	}

	/**
	 * This method throws an <CODE>UnsupportedOperationException</CODE>.
	 * @param margin
	 * @return NA
	 */
	public float top(int margin) {
		throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("dimensions.of.a.cell.can.t.be.calculated.see.the.faq"));
	}

	/**
	 * This method throws an <CODE>UnsupportedOperationException</CODE>.
	 * @param margin
	 * @return NA
	 */
	public float bottom(int margin) {
		throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("dimensions.of.a.cell.can.t.be.calculated.see.the.faq"));
	}

	/**
	 * This method throws an <CODE>UnsupportedOperationException</CODE>.
	 * @param margin
	 * @return NA
	 */
	public float left(int margin) {
		throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("dimensions.of.a.cell.can.t.be.calculated.see.the.faq"));
	}

	/**
	 * This method throws an <CODE>UnsupportedOperationException</CODE>.
	 * @param margin NA
	 * @return NA
	 */
	public float right(int margin) {
		throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("dimensions.of.a.cell.can.t.be.calculated.see.the.faq"));
	}

	/**
	 * This method throws an <CODE>UnsupportedOperationException</CODE>.
	 * @param value NA
	 */
	public void setTop(int value) {
		throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("dimensions.of.a.cell.are.attributed.automagically.see.the.faq"));
	}

	/**
	 * This method throws an <CODE>UnsupportedOperationException</CODE>.
	 * @param value NA
	 */
	public void setBottom(int value) {
		throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("dimensions.of.a.cell.are.attributed.automagically.see.the.faq"));
	}

	/**
	 * This method throws an <CODE>UnsupportedOperationException</CODE>.
	 * @param value NA
	 */
	public void setLeft(int value) {
		throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("dimensions.of.a.cell.are.attributed.automagically.see.the.faq"));
	}

	/**
	 * This method throws an <CODE>UnsupportedOperationException</CODE>.
	 * @param value NA
	 */
	public void setRight(int value) {
		throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("dimensions.of.a.cell.are.attributed.automagically.see.the.faq"));
	}

}
