/*
 * $Id: LineSeparator.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2008 by Paulo Soares.
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

package com.lowagie.text.pdf.draw;

import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfContentByte;

import java.awt.Color;

/**
 * Element that draws a solid line from left to right.
 * Can be added directly to a document or column.
 * Can also be used to create a separator chunk.
 * @author	Paulo Soares
 * @since	2.1.2
 */
public class LineSeparator extends VerticalPositionMark {
	
    /** The thickness of the line. */
    protected float lineWidth = 1;
    /** The width of the line as a percentage of the available page width. */
    protected float percentage = 100;
    /** The color of the line. */
    protected Color lineColor;
    /** The alignment of the line. */
    protected int alignment = Element.ALIGN_CENTER;
    
    /**
     * Creates a new instance of the LineSeparator class.
     * @param lineWidth		the thickness of the line
     * @param percentage	the width of the line as a percentage of the available page width
     * @param lineColor			the color of the line
     * @param align			the alignment
     * @param offset		the offset of the line relative to the current baseline (negative = under the baseline)
     */
    public LineSeparator(float lineWidth, float percentage, Color lineColor, int align, float offset) {
        this.lineWidth = lineWidth;
        this.percentage = percentage;
        this.lineColor = lineColor;
        this.alignment = align;
        this.offset = offset;
    }

    /**
     * Creates a new instance of the LineSeparator class with
     * default values: lineWidth 1 user unit, width 100%, centered with offset 0.
     */
    public LineSeparator() {
    }

    /**
     * @see com.lowagie.text.pdf.draw.DrawInterface#draw(com.lowagie.text.pdf.PdfContentByte, float, float, float, float, float)
     */
    public void draw(PdfContentByte canvas, float llx, float lly, float urx, float ury, float y) {
        canvas.saveState();
        drawLine(canvas, llx, urx, y);
        canvas.restoreState();
    }

    /**
     * Draws a horizontal line.
     * @param canvas	the canvas to draw on
     * @param leftX		the left x coordinate
     * @param rightX	the right x coordindate
     * @param y			the y coordinate
     */
    public void drawLine(PdfContentByte canvas, float leftX, float rightX, float y) {
    	float w;
        if (getPercentage() < 0)
            w = -getPercentage();
        else
            w = (rightX - leftX) * getPercentage() / 100.0f;
        float s;
        switch (getAlignment()) {
            case Element.ALIGN_LEFT:
                s = 0;
                break;
            case Element.ALIGN_RIGHT:
                s = rightX - leftX - w;
                break;
            default:
                s = (rightX - leftX - w) / 2;
                break;
        }
        canvas.setLineWidth(getLineWidth());
        if (getLineColor() != null)
            canvas.setColorStroke(getLineColor());
        canvas.moveTo(s + leftX, y + offset);
        canvas.lineTo(s + w + leftX, y + offset);
        canvas.stroke();
    }
    
    /**
     * Getter for the line width.
     * @return	the thickness of the line that will be drawn.
     */
    public float getLineWidth() {
        return lineWidth;
    }

    /**
     * Setter for the line width.
     * @param lineWidth	the thickness of the line that will be drawn.
     */
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * Setter for the width as a percentage of the available width.
     * @return	a width percentage
     */
    public float getPercentage() {
        return percentage;
    }

    /**
     * Setter for the width as a percentage of the available width.
     * @param percentage	a width percentage
     */
    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    /**
     * Getter for the color of the line that will be drawn.
     * @return	a color
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Setter for the color of the line that will be drawn.
     * @param color	a color
     */
    public void setLineColor(Color color) {
        this.lineColor = color;
    }

    /**
     * Getter for the alignment of the line.
     * @return	an alignment value
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * Setter for the alignment of the line.
     * @param align	an alignment value
     */
    public void setAlignment(int align) {
        this.alignment = align;
    }
}