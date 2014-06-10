/*
 *
 * Copyright 2002 by Paulo Soares.
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
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;

/** Writes text vertically. Note that the naming is done according
 * to horizontal text although it refers to vertical text.
 * A line with the alignment Element.LEFT_ALIGN will actually
 * be top aligned.
 */
public class VerticalText {

/** Signals that there are no more text available. */    
    public static final int NO_MORE_TEXT = 1;
	
/** Signals that there is no more column. */    
    public static final int NO_MORE_COLUMN = 2;

/** The chunks that form the text. */    
    protected ArrayList chunks = new ArrayList();

    /** The <CODE>PdfContent</CODE> where the text will be written to. */    
    protected PdfContentByte text;
    
    /** The column alignment. Default is left alignment. */
    protected int alignment = Element.ALIGN_LEFT;

    /** Marks the chunks to be eliminated when the line is written. */
    protected int currentChunkMarker = -1;
    
    /** The chunk created by the splitting. */
    protected PdfChunk currentStandbyChunk;
    
    /** The chunk created by the splitting. */
    protected String splittedChunkText;

    /** The leading
     */    
    protected float leading;
    
    /** The X coordinate.
     */    
    protected float startX;
    
    /** The Y coordinate.
     */    
    protected float startY;
    
    /** The maximum number of vertical lines.
     */    
    protected int maxLines;
    
    /** The height of the text.
     */    
    protected float height;
    
    /** Creates new VerticalText
     * @param text the place where the text will be written to. Can
     * be a template.
     */
    public VerticalText(PdfContentByte text) {
        this.text = text;
    }
    
    /**
     * Adds a <CODE>Phrase</CODE> to the current text array.
     * @param phrase the text
     */
    public void addText(Phrase phrase) {
        for (Iterator j = phrase.getChunks().iterator(); j.hasNext();) {
            chunks.add(new PdfChunk((Chunk)j.next(), null));
        }
    }
    
    /**
     * Adds a <CODE>Chunk</CODE> to the current text array.
     * @param chunk the text
     */
    public void addText(Chunk chunk) {
        chunks.add(new PdfChunk(chunk, null));
    }

    /** Sets the layout.
     * @param startX the top right X line position
     * @param startY the top right Y line position
     * @param height the height of the lines
     * @param maxLines the maximum number of lines
     * @param leading the separation between the lines
     */    
    public void setVerticalLayout(float startX, float startY, float height, int maxLines, float leading) {
        this.startX = startX;
        this.startY = startY;
        this.height = height;
        this.maxLines = maxLines;
        setLeading(leading);
    }
    
    /** Sets the separation between the vertical lines.
     * @param leading the vertical line separation
     */    
    public void setLeading(float leading) {
        this.leading = leading;
    }

    /** Gets the separation between the vertical lines.
     * @return the vertical line separation
     */    
    public float getLeading() {
        return leading;
    }
    
    /**
     * Creates a line from the chunk array.
     * @param width the width of the line
     * @return the line or null if no more chunks
     */
    protected PdfLine createLine(float width) {
        if (chunks.isEmpty())
            return null;
        splittedChunkText = null;
        currentStandbyChunk = null;
        PdfLine line = new PdfLine(0, width, alignment, 0);
        String total;
        for (currentChunkMarker = 0; currentChunkMarker < chunks.size(); ++currentChunkMarker) {
            PdfChunk original = (PdfChunk)(chunks.get(currentChunkMarker));
            total = original.toString();
            currentStandbyChunk = line.add(original);
            if (currentStandbyChunk != null) {
                splittedChunkText = original.toString();
                original.setValue(total);
                return line;
            }
        }
        return line;
    }
    
    /**
     * Normalizes the list of chunks when the line is accepted.
     */
    protected void shortenChunkArray() {
        if (currentChunkMarker < 0)
            return;
        if (currentChunkMarker >= chunks.size()) {
            chunks.clear();
            return;
        }
        PdfChunk split = (PdfChunk)(chunks.get(currentChunkMarker));
        split.setValue(splittedChunkText);
        chunks.set(currentChunkMarker, currentStandbyChunk);
        for (int j = currentChunkMarker - 1; j >= 0; --j)
            chunks.remove(j);
    }

    /**
     * Outputs the lines to the document. It is equivalent to <CODE>go(false)</CODE>.
     * @return returns the result of the operation. It can be <CODE>NO_MORE_TEXT</CODE>
     * and/or <CODE>NO_MORE_COLUMN</CODE>
     */
    public int go() {
        return go(false);
    }
    
    /**
     * Outputs the lines to the document. The output can be simulated.
     * @param simulate <CODE>true</CODE> to simulate the writing to the document
     * @return returns the result of the operation. It can be <CODE>NO_MORE_TEXT</CODE>
     * and/or <CODE>NO_MORE_COLUMN</CODE>
     */
    public int go(boolean simulate) {
        boolean dirty = false;
        PdfContentByte graphics = null;
        if (text != null) {
            graphics = text.getDuplicate();
        }
        else if (!simulate)
            throw new NullPointerException(MessageLocalization.getComposedMessage("verticaltext.go.with.simulate.eq.eq.false.and.text.eq.eq.null"));
        int status = 0;
        for (;;) {
            if (maxLines <= 0) {
                status = NO_MORE_COLUMN;
                if (chunks.isEmpty())
                    status |= NO_MORE_TEXT;
                break;
            }
            if (chunks.isEmpty()) {
                status = NO_MORE_TEXT;
                break;
            }
            PdfLine line = createLine(height);
            if (!simulate && !dirty) {
                text.beginText();
                dirty = true;
            }
            shortenChunkArray();
            if (!simulate) {
                text.setTextMatrix(startX, startY - line.indentLeft());
                writeLine(line, text, graphics);
            }
            --maxLines;
            startX -= leading;
        }
        if (dirty) {
            text.endText();
            text.add(graphics);
        }
        return status;
    }
    
    void writeLine(PdfLine line, PdfContentByte text, PdfContentByte graphics) {
        PdfFont currentFont = null;
        PdfChunk chunk;
        for (Iterator j = line.iterator(); j.hasNext(); ) {
            chunk = (PdfChunk) j.next();
            
            if (chunk.font().compareTo(currentFont) != 0) {
                currentFont = chunk.font();
                text.setFontAndSize(currentFont.getFont(), currentFont.size());
            }
            Color color = chunk.color();
            if (color != null)
                text.setColorFill(color);
            text.showText(chunk.toString());
            if (color != null)
                text.resetRGBColorFill();
        }
    }
    
    /** Sets the new text origin.
     * @param startX the X coordinate
     * @param startY the Y coordinate
     */    
    public void setOrigin(float startX, float startY) {
        this.startX = startX;
        this.startY = startY;
    }
    
    /** Gets the X coordinate where the next line will be written. This value will change
     * after each call to <code>go()</code>.
     * @return  the X coordinate
     */    
    public float getOriginX() {
        return startX;
    }

    /** Gets the Y coordinate where the next line will be written.
     * @return  the Y coordinate
     */    
    public float getOriginY() {
        return startY;
    }
    
    /** Gets the maximum number of available lines. This value will change
     * after each call to <code>go()</code>.
     * @return Value of property maxLines.
     */
    public int getMaxLines() {
        return maxLines;
    }
    
    /** Sets the maximum number of lines.
     * @param maxLines the maximum number of lines
     */
    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }
    
    /** Gets the height of the line
     * @return the height
     */
    public float getHeight() {
        return height;
    }
    
    /** Sets the height of the line
     * @param height the new height
     */
    public void setHeight(float height) {
        this.height = height;
    }
    
    /**
     * Sets the alignment.
     * @param alignment the alignment
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }
    
    /**
     * Gets the alignment.
     * @return the alignment
     */
    public int getAlignment() {
        return alignment;
    }
}
