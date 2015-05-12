/*
 * $Id: PdfCell.java 3671 2009-02-01 14:46:09Z blowagie $
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

import com.lowagie.text.Anchor;
import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;

/**
 * A <CODE>PdfCell</CODE> is the PDF translation of a <CODE>Cell</CODE>.
 * <P>
 * A <CODE>PdfCell</CODE> is an <CODE>ArrayList</CODE> of <CODE>PdfLine</CODE>s.
 * <P>
 * When using variable borders ({@link com.lowagie.text.Rectangle#isUseVariableBorders isUseVariableBorders()} == true),
 * the borders are drawn completely inside the cell Rectangle
 * so that adjacent cell borders will not overlap.
 * Otherwise, the borders are drawn on top of the edges of the
 * cell Rectangle and will overlap the borders of adjacent
 * cells.
 *
 * @see		com.lowagie.text.Rectangle
 * @see		com.lowagie.text.Cell
 * @see		PdfLine
 * @see		PdfTable
 */

public class PdfCell extends Rectangle {

    // membervariables
    
    /**
     * These are the PdfLines in the Cell.
     */
    private ArrayList lines;

    /**
     * These are the PdfLines in the Cell.
     */
    private PdfLine line;

    /**
     * These are the Images in the Cell.
     */
    private ArrayList images;

    /**
     * This is the leading of the lines.
     */
    private float leading;

    /**
     * This is the number of the row the cell is in.
     */
    private int rownumber;

    /**
     * This is the rowspan of the cell.
     */
    private int rowspan;

    /**
     * This is the cellspacing of the cell.
     */
    private float cellspacing;

    /**
     * This is the cellpadding of the cell.
     */
    private float cellpadding;

    /**
     * Indicates if this cell belongs to the header of a <CODE>PdfTable</CODE>
     */
    private boolean header = false;

    /**
     * This is the total height of the content of the cell.  Note that the actual cell
     * height may be larger due to another cell on the row *
     */
    private float contentHeight = 0.0f;

    /**
     * Indicates that the largest ascender height should be used to
     * determine the height of the first line. Setting this to true can help
     * with vertical alignment problems. */
    private boolean useAscender;

    /**
     * Indicates that the largest descender height should be added to the height of
     * the last line (so characters like y don't dip into the border). */
    private boolean useDescender;

    /**
     * Adjusts the cell contents to compensate for border widths.
     */
    private boolean useBorderPadding;

    private int verticalAlignment;

    private PdfLine firstLine;
    private PdfLine lastLine;

    // constructors
    
    /**
     * Constructs a <CODE>PdfCell</CODE>-object.
     *
     * @param	cell		the original <CODE>Cell</CODE>
     * @param	rownumber	the number of the <CODE>Row</CODE> the <CODE>Cell</CODE> was in.
     * @param	left		the left border of the <CODE>PdfCell</CODE>
     * @param	right		the right border of the <CODE>PdfCell</CODE>
     * @param	top			the top border of the <CODE>PdfCell</CODE>
     * @param	cellspacing	the cellspacing of the <CODE>Table</CODE>
     * @param	cellpadding	the cellpadding	of the <CODE>Table</CODE>
     */

    public PdfCell(Cell cell, int rownumber, float left, float right, float top, float cellspacing, float cellpadding) {
        // constructs a Rectangle (the bottomvalue will be changed afterwards)
        super(left, top, right, top);
        // copying the other Rectangle attributes from class Cell
        cloneNonPositionParameters(cell);
        this.cellpadding = cellpadding;
        this.cellspacing = cellspacing;
        this.verticalAlignment = cell.getVerticalAlignment();
        this.useAscender = cell.isUseAscender();
        this.useDescender = cell.isUseDescender();
        this.useBorderPadding = cell.isUseBorderPadding();

        // initialization of some parameters
        PdfChunk chunk;
        Element element;
        PdfChunk overflow;
        lines = new ArrayList();
        images = new ArrayList();
        leading = cell.getLeading();
        int alignment = cell.getHorizontalAlignment();
        left += cellspacing + cellpadding;
        right -= cellspacing + cellpadding;

        left += getBorderWidthInside(LEFT);
        right -= getBorderWidthInside(RIGHT);


        contentHeight = 0;

        rowspan = cell.getRowspan();

        ArrayList allActions;
        int aCounter;
        // we loop over all the elements of the cell
        for (Iterator i = cell.getElements(); i.hasNext();) {
            element = (Element) i.next();
            switch (element.type()) {
                case Element.JPEG:
                case Element.JPEG2000:
                case Element.JBIG2:
                case Element.IMGRAW:
                case Element.IMGTEMPLATE:
                    addImage((Image) element, left, right, 0.4f * leading, alignment); //
                    break;
                    // if the element is a list
                case Element.LIST:
                    if (line != null && line.size() > 0) {
                        line.resetAlignment();
                        addLine(line);
                    }
                    // we loop over all the listitems
                    addList((List)element, left, right, alignment);
                    line = new PdfLine(left, right, alignment, leading);
                    break;
                    // if the element is something else
                default:
                    allActions = new ArrayList();
                    processActions(element, null, allActions);
                    aCounter = 0;

                    float currentLineLeading = leading;
                    float currentLeft = left;
                    float currentRight = right;
                    if (element instanceof Phrase) {
                        currentLineLeading = ((Phrase) element).getLeading();
                    }
                    if (element instanceof Paragraph) {
                        Paragraph p = (Paragraph) element;
                        currentLeft += p.getIndentationLeft();
                        currentRight -= p.getIndentationRight();
                    }
                    if (line == null) {
                        line = new PdfLine(currentLeft, currentRight, alignment, currentLineLeading);
                    }
                    // we loop over the chunks
                    ArrayList chunks = element.getChunks();
                    if (chunks.isEmpty()) {
                       addLine(line); // add empty line - all cells need some lines even if they are empty
                       line = new PdfLine(currentLeft, currentRight, alignment, currentLineLeading);
                    }
                    else {
                        for (Iterator j = chunks.iterator(); j.hasNext();) {
                            Chunk c = (Chunk) j.next();
                            chunk = new PdfChunk(c, (PdfAction) (allActions.get(aCounter++)));
                            while ((overflow = line.add(chunk)) != null) {
                                addLine(line);
                                line = new PdfLine(currentLeft, currentRight, alignment, currentLineLeading);
                                chunk = overflow;
                            }
                        }
                    }
                    // if the element is a paragraph, section or chapter, we reset the alignment and add the line
                    switch (element.type()) {
                        case Element.PARAGRAPH:
                        case Element.SECTION:
                        case Element.CHAPTER:
                            line.resetAlignment();
                            flushCurrentLine();
                    }
            }
        }
        flushCurrentLine();
        if (lines.size() > cell.getMaxLines()) {
            while (lines.size() > cell.getMaxLines()) {
                removeLine(lines.size() - 1);
            }
            if (cell.getMaxLines() > 0) {
                String more = cell.getShowTruncation();
                if (more != null && more.length() > 0) {
                    // Denote that the content has been truncated
                    lastLine = (PdfLine) lines.get(lines.size() - 1);
                    if (lastLine.size() >= 0) {
                        PdfChunk lastChunk = lastLine.getChunk(lastLine.size() - 1);
                        float moreWidth = new PdfChunk(more, lastChunk).width();
                        while (lastChunk.toString().length() > 0 && lastChunk.width() + moreWidth > right - left) {
                            // Remove characters to leave room for the 'more' indicator
                            lastChunk.setValue(lastChunk.toString().substring(0, lastChunk.length() - 1));
                        }
                        lastChunk.setValue(lastChunk.toString() + more);
                    } else {
                        lastLine.add(new PdfChunk(new Chunk(more), null));
                    }
                }
            }
        }
        // we set some additional parameters
        if (useDescender && lastLine != null) {
            contentHeight -= lastLine.getDescender();
        }

        // adjust first line height so that it touches the top
        if (!lines.isEmpty()) {
            firstLine = (PdfLine) lines.get(0);
            float firstLineRealHeight = firstLineRealHeight();
            contentHeight -= firstLine.height();
            firstLine.height = firstLineRealHeight;
            contentHeight += firstLineRealHeight;
        }

        float newBottom = top - contentHeight - (2f * cellpadding()) - (2f * cellspacing());
        newBottom -= getBorderWidthInside(TOP) + getBorderWidthInside(BOTTOM);
        setBottom(newBottom);

        this.rownumber = rownumber;
    }

    private void addList(List list, float left, float right, int alignment) {
        PdfChunk chunk;
        PdfChunk overflow;
        ArrayList allActions = new ArrayList();
        processActions(list, null, allActions);
        int aCounter = 0;
        for (Iterator it = list.getItems().iterator(); it.hasNext();) {
            Element ele = (Element)it.next();
            switch (ele.type()) {
                case Element.LISTITEM:
                    ListItem item = (ListItem)ele;
                    line = new PdfLine(left + item.getIndentationLeft(), right, alignment, item.getLeading());
                    line.setListItem(item);
                    for (Iterator j = item.getChunks().iterator(); j.hasNext();) {
                        chunk = new PdfChunk((Chunk) j.next(), (PdfAction) (allActions.get(aCounter++)));
                        while ((overflow = line.add(chunk)) != null) {
                            addLine(line);
                            line = new PdfLine(left + item.getIndentationLeft(), right, alignment, item.getLeading());
                            chunk = overflow;
                        }
                        line.resetAlignment();
                        addLine(line);
                        line = new PdfLine(left + item.getIndentationLeft(), right, alignment, leading);
                    }
                    break;
                case Element.LIST:
                    List sublist = (List)ele;
                    addList(sublist, left + sublist.getIndentationLeft(), right, alignment);
                    break;
            }
        }
    }

    // overriding of the Rectangle methods


    /**
     * Sets the bottom of the Rectangle and determines the proper {link #verticalOffset}
     * to appropriately align the contents vertically.
     * @param value
     */
    public void setBottom(float value) {
        super.setBottom(value);
        float firstLineRealHeight = firstLineRealHeight();

        float totalHeight = ury - value; // can't use top (already compensates for cellspacing)
        float nonContentHeight = (cellpadding() * 2f) + (cellspacing() * 2f);
        nonContentHeight += getBorderWidthInside(TOP) + getBorderWidthInside(BOTTOM);

        float interiorHeight = totalHeight - nonContentHeight;
        float extraHeight = 0.0f;

        switch (verticalAlignment) {
            case Element.ALIGN_BOTTOM:
                extraHeight = interiorHeight - contentHeight;
                break;
            case Element.ALIGN_MIDDLE:
                extraHeight = (interiorHeight - contentHeight) / 2.0f;
                break;
            default:    // ALIGN_TOP
                extraHeight = 0f;
        }

        extraHeight += cellpadding() + cellspacing();
        extraHeight += getBorderWidthInside(TOP);
        if (firstLine != null) {
            firstLine.height = firstLineRealHeight + extraHeight;
        }
    }

	/**
     * Returns the lower left x-coordinate.
     *
     * @return		the lower left x-coordinate
     */

    public float getLeft() {
        return super.getLeft(cellspacing);
    }

	/**
     * Returns the upper right x-coordinate.
     *
     * @return		the upper right x-coordinate
     */

    public float getRight() {
        return super.getRight(cellspacing);
    }

	/**
     * Returns the upper right y-coordinate.
     *
     * @return		the upper right y-coordinate
     */

    public float getTop() {
        return super.getTop(cellspacing);
    }

	/**
     * Returns the lower left y-coordinate.
     *
     * @return		the lower left y-coordinate
     */

    public float getBottom() {
        return super.getBottom(cellspacing);
    }
    
    // methods

    private void addLine(PdfLine line) {
        lines.add(line);
        contentHeight += line.height();
        lastLine = line;
        this.line = null;
    }

    private PdfLine removeLine(int index) {
        PdfLine oldLine = (PdfLine) lines.remove(index);
        contentHeight -= oldLine.height();
        if (index == 0) {
            if (!lines.isEmpty()) {
                firstLine = (PdfLine) lines.get(0);
                float firstLineRealHeight = firstLineRealHeight();
                contentHeight -= firstLine.height();
                firstLine.height = firstLineRealHeight;
                contentHeight += firstLineRealHeight;
            }
        }
        return oldLine;
    }

    private void flushCurrentLine() {
        if (line != null && line.size() > 0) {
            addLine(line);
        }
    }

    /**
     * Calculates what the height of the first line should be so that the content will be
     * flush with the top.  For text, this is the height of the ascender.  For an image,
     * it is the actual height of the image.
     * @return the real height of the first line
     */
    private float firstLineRealHeight() {
        float firstLineRealHeight = 0f;
        if (firstLine != null) {
            PdfChunk chunk = firstLine.getChunk(0);
            if (chunk != null) {
                Image image = chunk.getImage();
                if (image != null) {
                    firstLineRealHeight = firstLine.getChunk(0).getImage().getScaledHeight();
                } else {
                    firstLineRealHeight = useAscender ? firstLine.getAscender() : leading;
                }
            }
        }
        return firstLineRealHeight;
    }

    /**
     * Gets the amount of the border for the specified side that is inside the Rectangle.
     * For non-variable width borders this is only 1/2 the border width on that side.  This
     * always returns 0 if {@link #useBorderPadding} is false;
     * @param side the side to check.  One of the side constants in {@link com.lowagie.text.Rectangle}
     * @return the borderwidth inside the cell
     */
    private float getBorderWidthInside(int side) {
        float width = 0f;
        if (useBorderPadding) {
            switch (side) {
                case Rectangle.LEFT:
                    width = getBorderWidthLeft();
                    break;

                case Rectangle.RIGHT:
                    width = getBorderWidthRight();
                    break;

                case Rectangle.TOP:
                    width = getBorderWidthTop();
                    break;

                default:    // default and BOTTOM
                    width = getBorderWidthBottom();
                    break;
            }
            // non-variable (original style) borders overlap the rectangle (only 1/2 counts)
            if (!isUseVariableBorders()) {
                width = width / 2f;
            }
        }
        return width;
    }


    /**
     * Adds an image to this Cell.
     *
     * @param i           the image to add
     * @param left        the left border
     * @param right       the right border
     * @param extraHeight extra height to add above image
     * @param alignment   horizontal alignment (constant from Element class)
     * @return the height of the image
     */

    private float addImage(Image i, float left, float right, float extraHeight, int alignment) {
        Image image = Image.getInstance(i);
        if (image.getScaledWidth() > right - left) {
            image.scaleToFit(right - left, Float.MAX_VALUE);
        }
        flushCurrentLine();
        if (line == null) {
            line = new PdfLine(left, right, alignment, leading);
        }
        PdfLine imageLine = line;

        // left and right in chunk is relative to the start of the line
        right = right - left;
        left = 0f;

        if ((image.getAlignment() & Image.RIGHT) == Image.RIGHT) {
            left = right - image.getScaledWidth();
        } else if ((image.getAlignment() & Image.MIDDLE) == Image.MIDDLE) {
            left = left + ((right - left - image.getScaledWidth()) / 2f);
        }
        Chunk imageChunk = new Chunk(image, left, 0);
        imageLine.add(new PdfChunk(imageChunk, null));
        addLine(imageLine);
        return imageLine.height();
    }

    /**
     * Gets the lines of a cell that can be drawn between certain limits.
     * <P>
     * Remark: all the lines that can be drawn are removed from the object!
     *
     * @param	top		the top of the part of the table that can be drawn
     * @param	bottom	the bottom of the part of the table that can be drawn
     * @return	an <CODE>ArrayList</CODE> of <CODE>PdfLine</CODE>s
     */

    public ArrayList getLines(float top, float bottom) {
        float lineHeight;
        float currentPosition = Math.min(getTop(), top);
        setTop(currentPosition + cellspacing);
        ArrayList result = new ArrayList();

        // if the bottom of the page is higher than the top of the cell: do nothing
        if (getTop() < bottom) {
            return result;
        }
        
        // we loop over the lines
        int size = lines.size();
        boolean aboveBottom = true;
        for (int i = 0; i < size && aboveBottom; i++) {
            line = (PdfLine) lines.get(i);
            lineHeight = line.height();
            currentPosition -= lineHeight;
            // if the currentPosition is higher than the bottom, we add the line to the result
            if (currentPosition > (bottom + cellpadding + getBorderWidthInside(BOTTOM))) {
                result.add(line);
            } else {
                aboveBottom = false;
            }
        }
        // if the bottom of the cell is higher than the bottom of the page, the cell is written, so we can remove all lines
        float difference = 0f;
        if (!header) {
            if (aboveBottom) {
                lines = new ArrayList();
                contentHeight = 0f;
            } else {
                size = result.size();
                for (int i = 0; i < size; i++) {
                    line = removeLine(0);
                    difference += line.height();
                }
            }
        }
        if (difference > 0) {
            Image image;
            for (Iterator i = images.iterator(); i.hasNext();) {
                image = (Image) i.next();
                image.setAbsolutePosition(image.getAbsoluteX(), image.getAbsoluteY() - difference - leading);
            }
        }
        return result;
    }

    /**
     * Gets the images of a cell that can be drawn between certain limits.
     * <P>
     * Remark: all the lines that can be drawn are removed from the object!
     *
     * @param	top		the top of the part of the table that can be drawn
     * @param	bottom	the bottom of the part of the table that can be drawn
     * @return	an <CODE>ArrayList</CODE> of <CODE>Image</CODE>s
     */

    public ArrayList getImages(float top, float bottom) {

        // if the bottom of the page is higher than the top of the cell: do nothing
        if (getTop() < bottom) {
            return new ArrayList();
        }
        top = Math.min(getTop(), top);
        // initializations
        Image image;
        float height;
        ArrayList result = new ArrayList();
        // we loop over the images
        for (Iterator i = images.iterator(); i.hasNext() && !header;) {
            image = (Image) i.next();
            height = image.getAbsoluteY();
            // if the currentPosition is higher than the bottom, we add the line to the result
            if (top - height > (bottom + cellpadding)) {
                image.setAbsolutePosition(image.getAbsoluteX(), top - height);
                result.add(image);
                i.remove();
            }
        }
        return result;
    }

    /**
     * Checks if this cell belongs to the header of a <CODE>PdfTable</CODE>.
     *
     * @return	<CODE>void</CODE>
     */

    boolean isHeader() {
        return header;
    }

    /**
     * Indicates that this cell belongs to the header of a <CODE>PdfTable</CODE>.
     */

    void setHeader() {
        header = true;
    }

    /**
     * Checks if the cell may be removed.
     * <P>
     * Headers may always be removed, even if they are drawn only partially:
     * they will be repeated on each following page anyway!
     *
     * @return	<CODE>true</CODE> if all the lines are already drawn; <CODE>false</CODE> otherwise.
     */

    boolean mayBeRemoved() {
        return (header || (lines.isEmpty() && images.isEmpty()));
    }

    /**
     * Returns the number of lines in the cell.
     *
     * @return	a value
     */

    public int size() {
        return lines.size();
    }

    /**
     * Returns the total height of all the lines in the cell.
     *
     * @return	a value
     */
    private float remainingLinesHeight() {
        if (lines.isEmpty()) return 0;
        float result = 0;
        int size = lines.size();
        PdfLine line;
        for (int i = 0; i < size; i++) {
            line = (PdfLine) lines.get(i);
            result += line.height();
        }
        return result;
    }

    /**
     * Returns the height needed to draw the remaining text.
     *
     * @return a height
     */

    public float remainingHeight() {
        float result = 0f;
        for (Iterator i = images.iterator(); i.hasNext();) {
            Image image = (Image) i.next();
            result += image.getScaledHeight();
        }
        return remainingLinesHeight() + cellspacing + 2 * cellpadding + result;
    }
    
    // methods to retrieve membervariables
    
    /**
     * Gets the leading of a cell.
     *
     * @return	the leading of the lines is the cell.
     */

    public float leading() {
        return leading;
    }

    /**
     * Gets the number of the row this cell is in..
     *
     * @return	a number
     */

    public int rownumber() {
        return rownumber;
    }

    /**
     * Gets the rowspan of a cell.
     *
     * @return	the rowspan of the cell
     */

    public int rowspan() {
        return rowspan;
    }

    /**
     * Gets the cellspacing of a cell.
     *
     * @return	a value
     */

    public float cellspacing() {
        return cellspacing;
    }

    /**
     * Gets the cellpadding of a cell..
     *
     * @return	a value
     */

    public float cellpadding() {
        return cellpadding;
    }

    /**
     * Processes all actions contained in the cell.
     * @param element	an element in the cell
     * @param action	an action that should be coupled to the cell
     * @param allActions
     */

    protected void processActions(Element element, PdfAction action, ArrayList allActions) {
        if (element.type() == Element.ANCHOR) {
            String url = ((Anchor) element).getReference();
            if (url != null) {
                action = new PdfAction(url);
            }
        }
        Iterator i;
        switch (element.type()) {
            case Element.PHRASE:
            case Element.SECTION:
            case Element.ANCHOR:
            case Element.CHAPTER:
            case Element.LISTITEM:
            case Element.PARAGRAPH:
                for (i = ((ArrayList) element).iterator(); i.hasNext();) {
                    processActions((Element) i.next(), action, allActions);
                }
                break;
            case Element.CHUNK:
                allActions.add(action);
                break;
            case Element.LIST:
                for (i = ((List) element).getItems().iterator(); i.hasNext();) {
                    processActions((Element) i.next(), action, allActions);
                }
                break;
            default:
                int n = element.getChunks().size();
                while (n-- > 0)
                    allActions.add(action);
                break;
        }
    }

    /**
     * This is the number of the group the cell is in.
     */
    private int groupNumber;

    /**
     * Gets the number of the group this cell is in..
     *
     * @return	a number
     */

    public int getGroupNumber() {
        return groupNumber;
    }

    /**
     * Sets the group number.
     * @param number
     */

    void setGroupNumber(int number) {
        groupNumber = number;
    }

    /**
     * Gets a Rectangle that is altered to fit on the page.
     *
     * @param	top		the top position
     * @param	bottom	the bottom position
     * @return	a <CODE>Rectangle</CODE>
     */

    public Rectangle rectangle(float top, float bottom) {
        Rectangle tmp = new Rectangle(getLeft(), getBottom(), getRight(), getTop());
        tmp.cloneNonPositionParameters(this);
        if (getTop() > top) {
            tmp.setTop(top);
            tmp.setBorder(border - (border & TOP));
        }
        if (getBottom() < bottom) {
            tmp.setBottom(bottom);
            tmp.setBorder(border - (border & BOTTOM));
        }
        return tmp;
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

}
