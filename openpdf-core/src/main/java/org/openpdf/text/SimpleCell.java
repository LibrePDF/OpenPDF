/*
 * $Id: SimpleCell.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2005 by Bruno Lowagie.
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
 * https://github.com/LibrePDF/OpenPDF
 */
package org.openpdf.text;

import org.openpdf.text.alignment.HorizontalAlignment;
import org.openpdf.text.alignment.VerticalAlignment;
import org.openpdf.text.error_messages.MessageLocalization;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPCellEvent;
import org.openpdf.text.pdf.PdfPTable;
import java.util.ArrayList;
import java.util.Optional;


/**
 * Rectangle that can be used for Cells. This Rectangle is padded and knows how to draw itself in a PdfPTable or
 * PdfPcellEvent.
 */
public class SimpleCell extends Rectangle implements PdfPCellEvent, TextElementArray {

    // constants
    /**
     * the CellAttributes object represents a row.
     */
    public static final boolean ROW = true;
    /**
     * the CellAttributes object represents a cell.
     */
    public static final boolean CELL = false;

    // member variables
    /**
     * Indicates that the largest ascender height should be used to determine the height of the first line.  Note that
     * this only has an effect when rendered to PDF.  Setting this to true can help with vertical alignment problems.
     */
    protected boolean useAscender = false;
    /**
     * Indicates that the largest descender height should be added to the height of the last line (so characters like y
     * don't dip into the border).   Note that this only has an effect when rendered to PDF.
     */
    protected boolean useDescender = false;
    /**
     * Adjusts the cell contents to compensate for border widths.  Note that this only has an effect when rendered to
     * PDF.
     */
    protected boolean useBorderPadding;
    /**
     * the content of the Cell.
     */
    private java.util.List<Element> content = new ArrayList<>();
    /**
     * the width of the Cell.
     */
    private float width = 0f;
    /**
     * the widthpercentage of the Cell.
     */
    private float widthpercentage = 0f;
    /**
     * Extracted spacing and padding dimensions for this cell.
     */
    private final CellSpacingPadding spacingPadding = new CellSpacingPadding();
    /**
     * the colspan of a Cell
     */
    private int colspan = 1;
    /**
     * horizontal alignment inside the Cell.
     */
    private int horizontalAlignment = Element.ALIGN_UNDEFINED;
    /**
     * vertical alignment inside the Cell.
     */
    private int verticalAlignment = Element.ALIGN_UNDEFINED;
    /**
     * indicates if these are the attributes of a single Cell (false) or a group of Cells (true).
     */
    private boolean cellgroup = false;

    /**
     * A CellAttributes object is always constructed without any dimensions. Dimensions are defined after creation.
     *
     * @param row only true if the CellAttributes object represents a row.
     */
    public SimpleCell(boolean row) {
        super(0f, 0f, 0f, 0f);
        cellgroup = row;
        setBorder(BOX);
    }

    /**
     * Adds content to this object.
     *
     * @param element an object of type {@link Element} that you want to add to the cell
     * @throws BadElementException on error
     */
    public void addElement(Element element) throws BadElementException {
        if (cellgroup) {
            if (element instanceof SimpleCell) {
                if (((SimpleCell) element).isCellgroup()) {
                    throw new BadElementException(
                            MessageLocalization.getComposedMessage("you.can.t.add.one.row.to.another.row"));
                }
                content.add(element);
                return;
            } else {
                throw new BadElementException(
                        MessageLocalization.getComposedMessage("you.can.only.add.cells.to.rows.no.objects.of.type.1",
                                element.getClass().getName()));
            }
        }
        if (isAllowedCellElement(element)) {
            content.add(element);
        } else {
            throw new BadElementException(
                    MessageLocalization.getComposedMessage("you.can.t.add.an.element.of.type.1.to.a.simplecell",
                            element.getClass().getName()));
        }
    }

    private boolean isAllowedCellElement(Element element) {
        int elementType = element.type();
        return elementType == Element.PARAGRAPH
                || elementType == Element.PHRASE
                || elementType == Element.ANCHOR
                || elementType == Element.CHUNK
                || elementType == Element.LIST
                || elementType == Element.MARKED
                || elementType == Element.JPEG
                || elementType == Element.JPEG2000
                || elementType == Element.JBIG2
                || elementType == Element.IMGRAW
                || elementType == Element.IMGTEMPLATE;
    }

    /**
     * Creates a Cell with these attributes.
     *
     * @param rowAttributes an object of type {@link SimpleCell} whose attributes are to be used to create new cell
     * @return a cell based on these attributes.
     * @throws BadElementException on error
     */
    public Cell createCell(SimpleCell rowAttributes) throws BadElementException {
        Cell cell = new Cell();
        cell.cloneNonPositionParameters(rowAttributes);
        cell.softCloneNonPositionParameters(this);
        cell.setColspan(colspan);
        Optional<HorizontalAlignment> hAlignment = HorizontalAlignment.of(horizontalAlignment);
        cell.setHorizontalAlignment(hAlignment.orElse(HorizontalAlignment.UNDEFINED));
        Optional<VerticalAlignment> vAlignment = VerticalAlignment.of(verticalAlignment);
        cell.setVerticalAlignment(vAlignment.orElse(VerticalAlignment.UNDEFINED));
        cell.setUseAscender(useAscender);
        cell.setUseBorderPadding(useBorderPadding);
        cell.setUseDescender(useDescender);
        Element element;
        for (Object o : content) {
            element = (Element) o;
            cell.addElement(element);
        }
        return cell;
    }

    /**
     * Creates a PdfPCell with these attributes.
     *
     * @param rowAttributes an object of type {@link SimpleCell} whose attributes are to be used to create new cell
     * @return a PdfPCell based on these attributes.
     */
    public PdfPCell createPdfPCell(SimpleCell rowAttributes) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(NO_BORDER);
        SimpleCell tmp = new SimpleCell(CELL);
        spacingPadding.copySpacingTo(tmp);
        tmp.cloneNonPositionParameters(rowAttributes);
        tmp.softCloneNonPositionParameters(this);
        cell.setCellEvent(tmp);
        cell.setHorizontalAlignment(rowAttributes.horizontalAlignment);
        cell.setVerticalAlignment(rowAttributes.verticalAlignment);
        cell.setUseAscender(rowAttributes.useAscender);
        cell.setUseBorderPadding(rowAttributes.useBorderPadding);
        cell.setUseDescender(rowAttributes.useDescender);
        cell.setColspan(colspan);
        if (horizontalAlignment != Element.ALIGN_UNDEFINED) {
            cell.setHorizontalAlignment(horizontalAlignment);
        }
        if (verticalAlignment != Element.ALIGN_UNDEFINED) {
            cell.setVerticalAlignment(verticalAlignment);
        }
        if (useAscender) {
            cell.setUseAscender(useAscender);
        }
        if (useBorderPadding) {
            cell.setUseBorderPadding(useBorderPadding);
        }
        if (useDescender) {
            cell.setUseDescender(useDescender);
        }
        spacingPadding.applyEffectivePaddingTo(cell);
        Element element;
        for (Object o : content) {
            element = (Element) o;
            cell.addElement(element);
        }
        return cell;
    }

    /**
     * @see org.openpdf.text.pdf.PdfPCellEvent#cellLayout(org.openpdf.text.pdf.PdfPCell, org.openpdf.text.Rectangle,
     * org.openpdf.text.pdf.PdfContentByte[])
     */
    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
        Rectangle rect = spacingPadding.createSpacedRectangle(position);
        rect.cloneNonPositionParameters(this);
        canvases[PdfPTable.BACKGROUNDCANVAS].rectangle(rect);
        rect.setBackgroundColor(null);
        canvases[PdfPTable.LINECANVAS].rectangle(rect);
    }

    /**
     * Sets the padding parameters if they are undefined.
     *
     * @param padding padding that will be set
     */
    public void setPadding(float padding) {
        spacingPadding.setDefaultPadding(padding);
    }

    /**
     * @return Returns the colspan.
     */
    public int getColspan() {
        return colspan;
    }

    /**
     * @param colspan The colspan to set.
     */
    public void setColspan(int colspan) {
        if (colspan > 0) {
            this.colspan = colspan;
        }
    }

    /**
     * @return Returns the padding_bottom.
     */
    public float getPadding_bottom() {
        return spacingPadding.getPaddingBottom();
    }

    /**
     * @param padding_bottom The padding_bottom to set.
     */
    public void setPadding_bottom(float padding_bottom) {
        spacingPadding.setPaddingBottom(padding_bottom);
    }

    /**
     * @return Returns the padding_left.
     */
    public float getPadding_left() {
        return spacingPadding.getPaddingLeft();
    }

    /**
     * @param padding_left The padding_left to set.
     */
    public void setPadding_left(float padding_left) {
        spacingPadding.setPaddingLeft(padding_left);
    }

    /**
     * @return Returns the padding_right.
     */
    public float getPadding_right() {
        return spacingPadding.getPaddingRight();
    }

    /**
     * @param padding_right The padding_right to set.
     */
    public void setPadding_right(float padding_right) {
        spacingPadding.setPaddingRight(padding_right);
    }

    /**
     * @return Returns the padding_top.
     */
    public float getPadding_top() {
        return spacingPadding.getPaddingTop();
    }

    /**
     * @param padding_top The padding_top to set.
     */
    public void setPadding_top(float padding_top) {
        spacingPadding.setPaddingTop(padding_top);
    }

    /**
     * @return Returns the spacing.
     */
    public float getSpacing_left() {
        return spacingPadding.getSpacingLeft();
    }

    /**
     * @param spacing The spacing to set.
     */
    public void setSpacing_left(float spacing) {
        spacingPadding.setSpacingLeft(spacing);
    }

    /**
     * @return Returns the spacing.
     */
    public float getSpacing_right() {
        return spacingPadding.getSpacingRight();
    }

    /**
     * @param spacing The spacing to set.
     */
    public void setSpacing_right(float spacing) {
        spacingPadding.setSpacingRight(spacing);
    }

    /**
     * @return Returns the spacing.
     */
    public float getSpacing_top() {
        return spacingPadding.getSpacingTop();
    }

    /**
     * @param spacing The spacing to set.
     */
    public void setSpacing_top(float spacing) {
        spacingPadding.setSpacingTop(spacing);
    }

    /**
     * @return Returns the spacing.
     */
    public float getSpacing_bottom() {
        return spacingPadding.getSpacingBottom();
    }

    /**
     * @param spacing The spacing to set.
     */
    public void setSpacing_bottom(float spacing) {
        spacingPadding.setSpacingBottom(spacing);
    }

    /**
     * @param spacing The spacing to set.
     */
    public void setSpacing(float spacing) {
        spacingPadding.setSpacing(spacing);
    }

    /**
     * @return Returns the cellgroup.
     */
    public boolean isCellgroup() {
        return cellgroup;
    }

    /**
     * @param cellgroup The cellgroup to set.
     */
    public void setCellgroup(boolean cellgroup) {
        this.cellgroup = cellgroup;
    }

    /**
     * @return Returns the horizontal alignment.
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * @param horizontalAlignment The horizontalAlignment to set.
     */
    public void setHorizontalAlignment(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    /**
     * @return Returns the vertical alignment.
     */
    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * @param verticalAlignment The verticalAligment to set.
     */
    public void setVerticalAlignment(int verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    /**
     * @return Returns the width.
     */
    public float getWidth() {
        return width;
    }

    /**
     * @param width The width to set.
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * @return Returns the widthpercentage.
     */
    public float getWidthpercentage() {
        return widthpercentage;
    }

    /**
     * @param widthpercentage The widthpercentage to set.
     */
    public void setWidthpercentage(float widthpercentage) {
        this.widthpercentage = widthpercentage;
    }

    /**
     * @return Returns the useAscender.
     */
    public boolean isUseAscender() {
        return useAscender;
    }

    /**
     * @param useAscender The useAscender to set.
     */
    public void setUseAscender(boolean useAscender) {
        this.useAscender = useAscender;
    }

    /**
     * @return Returns the useBorderPadding.
     */
    public boolean isUseBorderPadding() {
        return useBorderPadding;
    }

    /**
     * @param useBorderPadding The useBorderPadding to set.
     */
    public void setUseBorderPadding(boolean useBorderPadding) {
        this.useBorderPadding = useBorderPadding;
    }

    /**
     * @return Returns the useDescender.
     */
    public boolean isUseDescender() {
        return useDescender;
    }

    /**
     * @param useDescender The useDescender to set.
     */
    public void setUseDescender(boolean useDescender) {
        this.useDescender = useDescender;
    }

    /**
     * @return Returns the content.
     */
    java.util.List<Element> getContent() {
        return content;
    }

    /**
     * @see org.openpdf.text.TextElementArray#add(Element)
     */
    public boolean add(Element o) {
        try {
            addElement(o);
            return true;
        } catch (ClassCastException e) {
            return false;
        } catch (BadElementException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * @see org.openpdf.text.Element#type()
     */
    public int type() {
        return Element.CELL;
    }
}
