/*
 * $Id: PdfLine.java 3994 2009-06-24 13:08:04Z blowagie $
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

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.ListItem;

/**
 * <CODE>PdfLine</CODE> defines an array with <CODE>PdfChunk</CODE>-objects
 * that fit into 1 line.
 */

public class PdfLine {
    
    // membervariables
    
    /** The arraylist containing the chunks. */
    protected ArrayList line;
    
    /** The left indentation of the line. */
    protected float left;
    
    /** The width of the line. */
    protected float width;
    
    /** The alignment of the line. */
    protected int alignment;
    
    /** The height of the line. */
    protected float height;
    
    /** The listsymbol (if necessary). */
    protected Chunk listSymbol = null;
    
    /** The listsymbol (if necessary). */
    protected float symbolIndent;
    
    /** <CODE>true</CODE> if the chunk splitting was caused by a newline. */
    protected boolean newlineSplit = false;
    
    /** The original width. */
    protected float originalWidth;
    
    protected boolean isRTL = false;
    
    // constructors
    
    /**
     * Constructs a new <CODE>PdfLine</CODE>-object.
     *
     * @param	left		the limit of the line at the left
     * @param	right		the limit of the line at the right
     * @param	alignment	the alignment of the line
     * @param	height		the height of the line
     */
    
    PdfLine(float left, float right, int alignment, float height) {
        this.left = left;
        this.width = right - left;
        this.originalWidth = this.width;
        this.alignment = alignment;
        this.height = height;
        this.line = new ArrayList();
    }
    
    /**
     * Creates a PdfLine object.
     * @param left				the left offset
     * @param originalWidth		the original width of the line
     * @param remainingWidth	bigger than 0 if the line isn't completely filled
     * @param alignment			the alignment of the line
     * @param newlineSplit		was the line splitted (or does the paragraph end with this line)
     * @param line				an array of PdfChunk objects
     * @param isRTL				do you have to read the line from Right to Left?
     */
    PdfLine(float left, float originalWidth, float remainingWidth, int alignment, boolean newlineSplit, ArrayList line, boolean isRTL) {
        this.left = left;
        this.originalWidth = originalWidth;
        this.width = remainingWidth;
        this.alignment = alignment;
        this.line = line;
        this.newlineSplit = newlineSplit;
        this.isRTL = isRTL;
    }
    
    // methods
    
    /**
     * Adds a <CODE>PdfChunk</CODE> to the <CODE>PdfLine</CODE>.
     *
     * @param		chunk		the <CODE>PdfChunk</CODE> to add
     * @return		<CODE>null</CODE> if the chunk could be added completely; if not
     *				a <CODE>PdfChunk</CODE> containing the part of the chunk that could
     *				not be added is returned
     */
    
    PdfChunk add(PdfChunk chunk) {
        // nothing happens if the chunk is null.
        if (chunk == null || chunk.toString().equals("")) {
        	return null;
        }
        
        // we split the chunk to be added
        PdfChunk overflow = chunk.split(width);
        newlineSplit = (chunk.isNewlineSplit() || overflow == null);
        //        if (chunk.isNewlineSplit() && alignment == Element.ALIGN_JUSTIFIED)
        //            alignment = Element.ALIGN_LEFT;
        if (chunk.isTab()) {
        	Object[] tab = (Object[])chunk.getAttribute(Chunk.TAB);
    		float tabPosition = ((Float)tab[1]).floatValue();
    		boolean newline = ((Boolean)tab[2]).booleanValue();
    		if (newline && tabPosition < originalWidth - width) {
    		    return chunk;
    		}
    		width = originalWidth - tabPosition;
    		chunk.adjustLeft(left);
            addToLine(chunk);
        }
        // if the length of the chunk > 0 we add it to the line
        else if (chunk.length() > 0 || chunk.isImage()) {
            if (overflow != null)
                chunk.trimLastSpace();
            width -= chunk.width();
            addToLine(chunk);
        }
        // if the length == 0 and there were no other chunks added to the line yet,
        // we risk to end up in an endless loop trying endlessly to add the same chunk
        else if (line.size() < 1) {
            chunk = overflow;
            overflow = chunk.truncate(width);
            width -= chunk.width();
            if (chunk.length() > 0) {
                addToLine(chunk);
                return overflow;
            }
            // if the chunk couldn't even be truncated, we add everything, so be it
            else {
                if (overflow != null)
                    addToLine(overflow);
                return null;
            }
        }
        else {
            width += ((PdfChunk)(line.get(line.size() - 1))).trimLastSpace();
        }
        return overflow;
    }
    
    private void addToLine(PdfChunk chunk) {
        if (chunk.changeLeading && chunk.isImage()) {
        	float f = chunk.getImage().getScaledHeight() + chunk.getImageOffsetY() + chunk.getImage().getBorderWidthTop();
        	if (f > height) height = f;
        }
    	line.add(chunk);
    }
    
    // methods to retrieve information
    
    /**
     * Returns the number of chunks in the line.
     *
     * @return	a value
     */
    
    public int size() {
        return line.size();
    }
    
    /**
     * Returns an iterator of <CODE>PdfChunk</CODE>s.
     *
     * @return	an <CODE>Iterator</CODE>
     */
    
    public Iterator iterator() {
        return line.iterator();
    }
    
    /**
     * Returns the height of the line.
     *
     * @return	a value
     */
    
    float height() {
        return height;
    }
    
    /**
     * Returns the left indentation of the line taking the alignment of the line into account.
     *
     * @return	a value
     */
    
    float indentLeft() {
        if (isRTL) {
            switch (alignment) {
                case Element.ALIGN_LEFT:
                    return left + width;
                case Element.ALIGN_CENTER:
                    return left + (width / 2f);
                default:
                    return left;
            }
        }
        else if (this.getSeparatorCount() == 0) {
            switch (alignment) {
                case Element.ALIGN_RIGHT:
                    return left + width;
                case Element.ALIGN_CENTER:
                    return left + (width / 2f);
            }
        }
        return left;
    }
    
    /**
     * Checks if this line has to be justified.
     *
     * @return	<CODE>true</CODE> if the alignment equals <VAR>ALIGN_JUSTIFIED</VAR> and there is some width left.
     */
    
    public boolean hasToBeJustified() {
        return ((alignment == Element.ALIGN_JUSTIFIED || alignment == Element.ALIGN_JUSTIFIED_ALL) && width != 0);
    }
    
    /**
     * Resets the alignment of this line.
     * <P>
     * The alignment of the last line of for instance a <CODE>Paragraph</CODE>
     * that has to be justified, has to be reset to <VAR>ALIGN_LEFT</VAR>.
     */
    
    public void resetAlignment() {
        if (alignment == Element.ALIGN_JUSTIFIED) {
            alignment = Element.ALIGN_LEFT;
        }
    }
    
    /** Adds extra indentation to the left (for Paragraph.setFirstLineIndent). */
    void setExtraIndent(float extra) {
    	left += extra;
    	width -= extra;
    }
    
    /**
     * Returns the width that is left, after a maximum of characters is added to the line.
     *
     * @return	a value
     */
    
    float widthLeft() {
        return width;
    }
    
    /**
     * Returns the number of space-characters in this line.
     *
     * @return	a value
     */
    
    int numberOfSpaces() {
        String string = toString();
        int length = string.length();
        int numberOfSpaces = 0;
        for (int i = 0; i < length; i++) {
            if (string.charAt(i) == ' ') {
                numberOfSpaces++;
            }
        }
        return numberOfSpaces;
    }
    
    /**
     * Sets the listsymbol of this line.
     * <P>
     * This is only necessary for the first line of a <CODE>ListItem</CODE>.
     *
     * @param listItem the list symbol
     */
    
    public void setListItem(ListItem listItem) {
        this.listSymbol = listItem.getListSymbol();
        this.symbolIndent = listItem.getIndentationLeft();
    }
    
    /**
     * Returns the listsymbol of this line.
     *
     * @return	a <CODE>PdfChunk</CODE> if the line has a listsymbol; <CODE>null</CODE> otherwise
     */
    
    public Chunk listSymbol() {
        return listSymbol;
    }
    
    /**
     * Return the indentation needed to show the listsymbol.
     *
     * @return	a value
     */
    
    public float listIndent() {
        return symbolIndent;
    }
    
    /**
     * Get the string representation of what is in this line.
     *
     * @return	a <CODE>String</CODE>
     */
    
    public String toString() {
        StringBuffer tmp = new StringBuffer();
        for (Iterator i = line.iterator(); i.hasNext(); ) {
            tmp.append(((PdfChunk) i.next()).toString());
        }
        return tmp.toString();
    }
    
    /**
     * Returns the length of a line in UTF32 characters
     * @return	the length in UTF32 characters
     * @since	2.1.2
     */
    public int GetLineLengthUtf32() {
        int total = 0;
        for (Iterator i = line.iterator(); i.hasNext();) {
            total += ((PdfChunk)i.next()).lengthUtf32();
        }
        return total;
    }
    
    /**
     * Checks if a newline caused the line split.
     * @return <CODE>true</CODE> if a newline caused the line split
     */
    public boolean isNewlineSplit() {
        return newlineSplit && (alignment != Element.ALIGN_JUSTIFIED_ALL);
    }
    
    /**
     * Gets the index of the last <CODE>PdfChunk</CODE> with metric attributes
     * @return the last <CODE>PdfChunk</CODE> with metric attributes
     */
    public int getLastStrokeChunk() {
        int lastIdx = line.size() - 1;
        for (; lastIdx >= 0; --lastIdx) {
            PdfChunk chunk = (PdfChunk)line.get(lastIdx);
            if (chunk.isStroked())
                break;
        }
        return lastIdx;
    }
    
    /**
     * Gets a <CODE>PdfChunk</CODE> by index.
     * @param idx the index
     * @return the <CODE>PdfChunk</CODE> or null if beyond the array
     */
    public PdfChunk getChunk(int idx) {
        if (idx < 0 || idx >= line.size())
            return null;
        return (PdfChunk)line.get(idx);
    }
    
    /**
     * Gets the original width of the line.
     * @return the original width of the line
     */
    public float getOriginalWidth() {
        return originalWidth;
    }
    
    /*
     * Gets the maximum size of all the fonts used in this line
     * including images.
     * @return maximum size of all the fonts used in this line
     float getMaxSizeSimple() {
        float maxSize = 0;
        PdfChunk chunk;
        for (int k = 0; k < line.size(); ++k) {
            chunk = (PdfChunk)line.get(k);
            if (!chunk.isImage()) {
                maxSize = Math.max(chunk.font().size(), maxSize);
            }
            else {
                maxSize = Math.max(chunk.getImage().getScaledHeight() + chunk.getImageOffsetY() , maxSize);
            }
        }
        return maxSize;
    }*/
    
    /**
     * Gets the difference between the "normal" leading and the maximum
     * size (for instance when there are images in the chunk).
     * @return	an extra leading for images
     * @since	2.1.5
     */
    float[] getMaxSize() {
    	float normal_leading = 0;
    	float image_leading = -10000;
        PdfChunk chunk;
        for (int k = 0; k < line.size(); ++k) {
            chunk = (PdfChunk)line.get(k);
            if (!chunk.isImage()) {
                normal_leading = Math.max(chunk.font().size(), normal_leading);
            }
            else {
                image_leading = Math.max(chunk.getImage().getScaledHeight() + chunk.getImageOffsetY(), image_leading);
            }
        }
        return new float[]{normal_leading, image_leading};
    }
    
    boolean isRTL() {
        return isRTL;
    }
    
    /**
     * Gets the number of separators in the line.
     * @return	the number of separators in the line
     * @since	2.1.2
     */
    int getSeparatorCount() {
    	int s = 0;
    	PdfChunk ck;
        for (Iterator i = line.iterator(); i.hasNext(); ) {
        	ck = (PdfChunk)i.next();
        	if (ck.isTab()) {
        		return 0;
        	}
        	if (ck.isHorizontalSeparator()) {
        		s++;
        	}
        }
        return s;
    }
    
    /**
     * Gets a width corrected with a charSpacing and wordSpacing.
     * @param charSpacing
     * @param wordSpacing
     * @return a corrected width
     */
    public float getWidthCorrected(float charSpacing, float wordSpacing) {
        float total = 0;
        for (int k = 0; k < line.size(); ++k) {
            PdfChunk ck = (PdfChunk)line.get(k);
            total += ck.getWidthCorrected(charSpacing, wordSpacing);
        }
        return total;
    }
    
/**
 * Gets the maximum size of the ascender for all the fonts used
 * in this line.
 * @return maximum size of all the ascenders used in this line
 */
   public float getAscender() {
       float ascender = 0;
       for (int k = 0; k < line.size(); ++k) {
           PdfChunk ck = (PdfChunk)line.get(k);
           if (ck.isImage())
               ascender = Math.max(ascender, ck.getImage().getScaledHeight() + ck.getImageOffsetY());
           else {
               PdfFont font = ck.font();
               ascender = Math.max(ascender, font.getFont().getFontDescriptor(BaseFont.ASCENT, font.size()));
           }
       }
       return ascender;
   }

/**
 * Gets the biggest descender for all the fonts used 
 * in this line.  Note that this is a negative number.
 * @return maximum size of all the ascenders used in this line
 */
    public float getDescender() {
        float descender = 0;
        for (int k = 0; k < line.size(); ++k) {
            PdfChunk ck = (PdfChunk)line.get(k);
            if (ck.isImage())
                descender = Math.min(descender, ck.getImageOffsetY());
            else {
                PdfFont font = ck.font();
                descender = Math.min(descender, font.getFont().getFontDescriptor(BaseFont.DESCENT, font.size()));
            }
        }
        return descender;
    }
}