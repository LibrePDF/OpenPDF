/*
 * $Id: PdfChunk.java 4092 2009-11-11 17:58:16Z psoares33 $
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

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.lowagie.text.Chunk;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.SplitCharacter;
import com.lowagie.text.Utilities;

/**
 * A <CODE>PdfChunk</CODE> is the PDF translation of a <CODE>Chunk</CODE>.
 * <P>
 * A <CODE>PdfChunk</CODE> is a <CODE>PdfString</CODE> in a certain
 * <CODE>PdfFont</CODE> and <CODE>Color</CODE>.
 *
 * @see		PdfString
 * @see		com.lowagie.text.Chunk
 * @see		com.lowagie.text.Font
 */

public class PdfChunk {

    private static final char singleSpace[] = {' '};
    private static final PdfChunk thisChunk[] = new PdfChunk[1];
    private static final float ITALIC_ANGLE = 0.21256f;
/** The allowed attributes in variable <CODE>attributes</CODE>. */
    private static final HashMap keysAttributes = new HashMap();
    
/** The allowed attributes in variable <CODE>noStroke</CODE>. */
    private static final HashMap keysNoStroke = new HashMap();
    
    static {
        keysAttributes.put(Chunk.ACTION, null);
        keysAttributes.put(Chunk.UNDERLINE, null);
        keysAttributes.put(Chunk.REMOTEGOTO, null);
        keysAttributes.put(Chunk.LOCALGOTO, null);
        keysAttributes.put(Chunk.LOCALDESTINATION, null);
        keysAttributes.put(Chunk.GENERICTAG, null);
        keysAttributes.put(Chunk.NEWPAGE, null);
        keysAttributes.put(Chunk.IMAGE, null);
        keysAttributes.put(Chunk.BACKGROUND, null);
        keysAttributes.put(Chunk.PDFANNOTATION, null);
        keysAttributes.put(Chunk.SKEW, null);
        keysAttributes.put(Chunk.HSCALE, null);
        keysAttributes.put(Chunk.SEPARATOR, null);
        keysAttributes.put(Chunk.TAB, null);
        keysAttributes.put(Chunk.CHAR_SPACING, null);
        keysNoStroke.put(Chunk.SUBSUPSCRIPT, null);
        keysNoStroke.put(Chunk.SPLITCHARACTER, null);
        keysNoStroke.put(Chunk.HYPHENATION, null);
        keysNoStroke.put(Chunk.TEXTRENDERMODE, null);
    }
    
    // membervariables

    /** The value of this object. */
    protected String value = PdfObject.NOTHING;
    
    /** The encoding. */
    protected String encoding = BaseFont.WINANSI;
    
    
/** The font for this <CODE>PdfChunk</CODE>. */
    protected PdfFont font;
    
    protected BaseFont baseFont;
    
    protected SplitCharacter splitCharacter;
/**
 * Metric attributes.
 * <P>
 * This attributes require the measurement of characters widths when rendering
 * such as underline.
 */
    protected HashMap attributes = new HashMap();
    
/**
 * Non metric attributes.
 * <P>
 * This attributes do not require the measurement of characters widths when rendering
 * such as Color.
 */
    protected HashMap noStroke = new HashMap();
    
/** <CODE>true</CODE> if the chunk split was cause by a newline. */
    protected boolean newlineSplit;
    
/** The image in this <CODE>PdfChunk</CODE>, if it has one */
    protected Image image;
    
/** The offset in the x direction for the image */
    protected float offsetX;
    
/** The offset in the y direction for the image */
    protected float offsetY;

/** Indicates if the height and offset of the Image has to be taken into account */
    protected boolean changeLeading = false;

    // constructors
    
/**
 * Constructs a <CODE>PdfChunk</CODE>-object.
 *
 * @param string the content of the <CODE>PdfChunk</CODE>-object
 * @param other Chunk with the same style you want for the new Chunk
 */
    
    PdfChunk(String string, PdfChunk other) {
        thisChunk[0] = this;
        value = string;
        this.font = other.font;
        this.attributes = other.attributes;
        this.noStroke = other.noStroke;
        this.baseFont = other.baseFont;
        Object obj[] = (Object[])attributes.get(Chunk.IMAGE);
        if (obj == null)
            image = null;
        else {
            image = (Image)obj[0];
            offsetX = ((Float)obj[1]).floatValue();
            offsetY = ((Float)obj[2]).floatValue();
            changeLeading = ((Boolean)obj[3]).booleanValue();
        }
        encoding = font.getFont().getEncoding();
        splitCharacter = (SplitCharacter)noStroke.get(Chunk.SPLITCHARACTER);
        if (splitCharacter == null)
            splitCharacter = DefaultSplitCharacter.DEFAULT;
    }
    
/**
 * Constructs a <CODE>PdfChunk</CODE>-object.
 *
 * @param chunk the original <CODE>Chunk</CODE>-object
 * @param action the <CODE>PdfAction</CODE> if the <CODE>Chunk</CODE> comes from an <CODE>Anchor</CODE>
 */
    
    PdfChunk(Chunk chunk, PdfAction action) {
        thisChunk[0] = this;
        value = chunk.getContent();
        
        Font f = chunk.getFont();
        float size = f.getSize();
        if (size == Font.UNDEFINED)
            size = 12;
        baseFont = f.getBaseFont();
        int style = f.getStyle();
        if (style == Font.UNDEFINED) {
            style = Font.NORMAL;
        }
        if (baseFont == null) {
            // translation of the font-family to a PDF font-family
            baseFont = f.getCalculatedBaseFont(false);
        }
        else {
            // bold simulation
            if ((style & Font.BOLD) != 0)
                attributes.put(Chunk.TEXTRENDERMODE, new Object[]{new Integer(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE), new Float(size / 30f), null});
            // italic simulation
            if ((style & Font.ITALIC) != 0)
                attributes.put(Chunk.SKEW, new float[]{0, ITALIC_ANGLE});
        }
        font = new PdfFont(baseFont, size);
        // other style possibilities
        HashMap attr = chunk.getAttributes();
        if (attr != null) {
            for (Iterator i = attr.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                Object name = entry.getKey();
                if (keysAttributes.containsKey(name)) {
                    attributes.put(name, entry.getValue());
                }
                else if (keysNoStroke.containsKey(name)) {
                    noStroke.put(name, entry.getValue());
                }
            }
            if ("".equals(attr.get(Chunk.GENERICTAG))) {
                attributes.put(Chunk.GENERICTAG, chunk.getContent());
            }
        }
        if (f.isUnderlined()) {
            Object obj[] = {null, new float[]{0, 1f / 15, 0, -1f / 3, 0}};
            Object unders[][] = Utilities.addToArray((Object[][])attributes.get(Chunk.UNDERLINE), obj);
            attributes.put(Chunk.UNDERLINE, unders);
        }
        if (f.isStrikethru()) {
            Object obj[] = {null, new float[]{0, 1f / 15, 0, 1f / 3, 0}};
            Object unders[][] = Utilities.addToArray((Object[][])attributes.get(Chunk.UNDERLINE), obj);
            attributes.put(Chunk.UNDERLINE, unders);
        }
        if (action != null)
            attributes.put(Chunk.ACTION, action);
        // the color can't be stored in a PdfFont
        noStroke.put(Chunk.COLOR, f.getColor());
        noStroke.put(Chunk.ENCODING, font.getFont().getEncoding());
        Object obj[] = (Object[])attributes.get(Chunk.IMAGE);
        if (obj == null) {
            image = null;
        }
        else {
            attributes.remove(Chunk.HSCALE); // images are scaled in other ways
            image = (Image)obj[0];
            offsetX = ((Float)obj[1]).floatValue();
            offsetY = ((Float)obj[2]).floatValue();
            changeLeading = ((Boolean)obj[3]).booleanValue();
        }
        font.setImage(image);
        Float hs = (Float)attributes.get(Chunk.HSCALE);
        if (hs != null)
            font.setHorizontalScaling(hs.floatValue());
        encoding = font.getFont().getEncoding();
        splitCharacter = (SplitCharacter)noStroke.get(Chunk.SPLITCHARACTER);
        if (splitCharacter == null)
            splitCharacter = DefaultSplitCharacter.DEFAULT;
    }
    
    // methods
    
    /** Gets the Unicode equivalent to a CID.
     * The (inexistent) CID <FF00> is translated as '\n'. 
     * It has only meaning with CJK fonts with Identity encoding.
     * @param c the CID code
     * @return the Unicode equivalent
     */    
    public int getUnicodeEquivalent(int c) {
        return baseFont.getUnicodeEquivalent(c);
    }

    protected int getWord(String text, int start) {
        int len = text.length();
        while (start < len) {
            if (!Character.isLetter(text.charAt(start)))
                break;
            ++start;
        }
        return start;
    }
    
/**
 * Splits this <CODE>PdfChunk</CODE> if it's too long for the given width.
 * <P>
 * Returns <VAR>null</VAR> if the <CODE>PdfChunk</CODE> wasn't truncated.
 *
 * @param		width		a given width
 * @return		the <CODE>PdfChunk</CODE> that doesn't fit into the width.
 */
    
    PdfChunk split(float width) {
        newlineSplit = false;
        if (image != null) {
            if (image.getScaledWidth() > width) {
                PdfChunk pc = new PdfChunk(Chunk.OBJECT_REPLACEMENT_CHARACTER, this);
                value = "";
                attributes = new HashMap();
                image = null;
                font = PdfFont.getDefaultFont();
                return pc;
            }
            else
                return null;
        }
        HyphenationEvent hyphenationEvent = (HyphenationEvent)noStroke.get(Chunk.HYPHENATION);
        int currentPosition = 0;
        int splitPosition = -1;
        float currentWidth = 0;
        
        // loop over all the characters of a string
        // or until the totalWidth is reached
        int lastSpace = -1;
        float lastSpaceWidth = 0;
        int length = value.length();
        char valueArray[] = value.toCharArray();
        char character = 0;
        BaseFont ft = font.getFont();
        boolean surrogate = false;
        if (ft.getFontType() == BaseFont.FONT_TYPE_CJK && ft.getUnicodeEquivalent(' ') != ' ') {
            while (currentPosition < length) {
                // the width of every character is added to the currentWidth
                char cidChar = valueArray[currentPosition];
                character = (char)ft.getUnicodeEquivalent(cidChar);
                // if a newLine or carriageReturn is encountered
                if (character == '\n') {
                    newlineSplit = true;
                    String returnValue = value.substring(currentPosition + 1);
                    value = value.substring(0, currentPosition);
                    if (value.length() < 1) {
                        value = "\u0001";
                    }
                    PdfChunk pc = new PdfChunk(returnValue, this);
                    return pc;
                }
                currentWidth += getCharWidth(cidChar);
                if (character == ' ') {
                    lastSpace = currentPosition + 1;
                    lastSpaceWidth = currentWidth;
                }
                if (currentWidth > width)
                    break;
                // if a split-character is encountered, the splitPosition is altered
                if (splitCharacter.isSplitCharacter(0, currentPosition, length, valueArray, thisChunk))
                    splitPosition = currentPosition + 1;
                currentPosition++;
            }
        }
        else {
            while (currentPosition < length) {
                // the width of every character is added to the currentWidth
                character = valueArray[currentPosition];
                // if a newLine or carriageReturn is encountered
                if (character == '\r' || character == '\n') {
                    newlineSplit = true;
                    int inc = 1;
                    if (character == '\r' && currentPosition + 1 < length && valueArray[currentPosition + 1] == '\n')
                        inc = 2;
                    String returnValue = value.substring(currentPosition + inc);
                    value = value.substring(0, currentPosition);
                    if (value.length() < 1) {
                        value = " ";
                    }
                    PdfChunk pc = new PdfChunk(returnValue, this);
                    return pc;
                }
                surrogate = Utilities.isSurrogatePair(valueArray, currentPosition);
                if (surrogate)
                    currentWidth += getCharWidth(Utilities.convertToUtf32(valueArray[currentPosition], valueArray[currentPosition + 1]));
                else
                    currentWidth += getCharWidth(character);
                if (character == ' ') {
                    lastSpace = currentPosition + 1;
                    lastSpaceWidth = currentWidth;
                }
                if (surrogate)
                    currentPosition++;
                if (currentWidth > width)
                    break;
                // if a split-character is encountered, the splitPosition is altered
                if (splitCharacter.isSplitCharacter(0, currentPosition, length, valueArray, null))
                    splitPosition = currentPosition + 1;
                currentPosition++;
            }
        }
        
        // if all the characters fit in the total width, null is returned (there is no overflow)
        if (currentPosition == length) {
            return null;
        }
        // otherwise, the string has to be truncated
        if (splitPosition < 0) {
            String returnValue = value;
            value = "";
            PdfChunk pc = new PdfChunk(returnValue, this);
            return pc;
        }
        if (lastSpace > splitPosition && splitCharacter.isSplitCharacter(0, 0, 1, singleSpace, null))
            splitPosition = lastSpace;
        if (hyphenationEvent != null && lastSpace >= 0 && lastSpace < currentPosition) {
            int wordIdx = getWord(value, lastSpace);
            if (wordIdx > lastSpace) {
                String pre = hyphenationEvent.getHyphenatedWordPre(value.substring(lastSpace, wordIdx), font.getFont(), font.size(), width - lastSpaceWidth);
                String post = hyphenationEvent.getHyphenatedWordPost();
                if (pre.length() > 0) {
                    String returnValue = post + value.substring(wordIdx);
                    value = trim(value.substring(0, lastSpace) + pre);
                    PdfChunk pc = new PdfChunk(returnValue, this);
                    return pc;
                }
            }
        }
        String returnValue = value.substring(splitPosition);
        value = trim(value.substring(0, splitPosition));
        PdfChunk pc = new PdfChunk(returnValue, this);
        return pc;
    }
    
/**
 * Truncates this <CODE>PdfChunk</CODE> if it's too long for the given width.
 * <P>
 * Returns <VAR>null</VAR> if the <CODE>PdfChunk</CODE> wasn't truncated.
 *
 * @param		width		a given width
 * @return		the <CODE>PdfChunk</CODE> that doesn't fit into the width.
 */
    
    PdfChunk truncate(float width) {
        if (image != null) {
            if (image.getScaledWidth() > width) {
                PdfChunk pc = new PdfChunk("", this);
                value = "";
                attributes.remove(Chunk.IMAGE);
                image = null;
                font = PdfFont.getDefaultFont();
                return pc;
            }
            else
                return null;
        }
        
        int currentPosition = 0;
        float currentWidth = 0;
        
        // it's no use trying to split if there isn't even enough place for a space
        if (width < font.width()) {
            String returnValue = value.substring(1);
            value = value.substring(0, 1);
            PdfChunk pc = new PdfChunk(returnValue, this);
            return pc;
        }
        
        // loop over all the characters of a string
        // or until the totalWidth is reached
        int length = value.length();
        boolean surrogate = false;
        char character;
        while (currentPosition < length) {
            // the width of every character is added to the currentWidth
            surrogate = Utilities.isSurrogatePair(value, currentPosition);
            if (surrogate)
                currentWidth += getCharWidth(Utilities.convertToUtf32(value, currentPosition));
            else
                currentWidth += getCharWidth(value.charAt(currentPosition));
            if (currentWidth > width)
                break;
            if (surrogate)
                currentPosition++;
            currentPosition++;
        }
        
        // if all the characters fit in the total width, null is returned (there is no overflow)
        if (currentPosition == length) {
            return null;
        }
        
        // otherwise, the string has to be truncated
        //currentPosition -= 2;
        // we have to chop off minimum 1 character from the chunk
        if (currentPosition == 0) {
            currentPosition = 1;
            if (surrogate)
                ++currentPosition;
        }
        String returnValue = value.substring(currentPosition);
        value = value.substring(0, currentPosition);
        PdfChunk pc = new PdfChunk(returnValue, this);
        return pc;
    }
    
    // methods to retrieve the membervariables
    
/**
 * Returns the font of this <CODE>Chunk</CODE>.
 *
 * @return	a <CODE>PdfFont</CODE>
 */
    
    PdfFont font() {
        return font;
    }
    
/**
 * Returns the color of this <CODE>Chunk</CODE>.
 *
 * @return	a <CODE>Color</CODE>
 */
    
    Color color() {
        return (Color)noStroke.get(Chunk.COLOR);
    }
    
/**
 * Returns the width of this <CODE>PdfChunk</CODE>.
 *
 * @return	a width
 */
    
    float width() {
        if (isAttribute(Chunk.CHAR_SPACING)) {
        	Float cs = (Float) getAttribute(Chunk.CHAR_SPACING);
            return font.width(value) + value.length() * cs.floatValue();
		}
        return font.width(value);
    }
    
/**
 * Checks if the <CODE>PdfChunk</CODE> split was caused by a newline.
 * @return <CODE>true</CODE> if the <CODE>PdfChunk</CODE> split was caused by a newline.
 */
    
    public boolean isNewlineSplit()
    {
        return newlineSplit;
    }
    
/**
 * Gets the width of the <CODE>PdfChunk</CODE> taking into account the
 * extra character and word spacing.
 * @param charSpacing the extra character spacing
 * @param wordSpacing the extra word spacing
 * @return the calculated width
 */
    
    public float getWidthCorrected(float charSpacing, float wordSpacing)
    {
        if (image != null) {
            return image.getScaledWidth() + charSpacing;
        }
        int numberOfSpaces = 0;
        int idx = -1;
        while ((idx = value.indexOf(' ', idx + 1)) >= 0)
            ++numberOfSpaces;
        return width() + (value.length() * charSpacing + numberOfSpaces * wordSpacing);
    }
    
    /**
     * Gets the text displacement relative to the baseline.
     * @return a displacement in points
     */
    public float getTextRise() {
    	Float f = (Float) getAttribute(Chunk.SUBSUPSCRIPT);
    	if (f != null) {
    		return f.floatValue();
    	}
    	return 0.0f;
    }
    
/**
 * Trims the last space.
 * @return the width of the space trimmed, otherwise 0
 */
    
    public float trimLastSpace()
    {
        BaseFont ft = font.getFont();
        if (ft.getFontType() == BaseFont.FONT_TYPE_CJK && ft.getUnicodeEquivalent(' ') != ' ') {
            if (value.length() > 1 && value.endsWith("\u0001")) {
                value = value.substring(0, value.length() - 1);
                return font.width('\u0001');
            }
        }
        else {
            if (value.length() > 1 && value.endsWith(" ")) {
                value = value.substring(0, value.length() - 1);
                return font.width(' ');
            }
        }
        return 0;
    }    
    public float trimFirstSpace()
    {
        BaseFont ft = font.getFont();
        if (ft.getFontType() == BaseFont.FONT_TYPE_CJK && ft.getUnicodeEquivalent(' ') != ' ') {
            if (value.length() > 1 && value.startsWith("\u0001")) {
                value = value.substring(1);
                return font.width('\u0001');
            }
        }
        else {
            if (value.length() > 1 && value.startsWith(" ")) {
                value = value.substring(1);
                return font.width(' ');
            }
        }
        return 0;
    }
    
/**
 * Gets an attribute. The search is made in <CODE>attributes</CODE>
 * and <CODE>noStroke</CODE>.
 * @param name the attribute key
 * @return the attribute value or null if not found
 */
    
    Object getAttribute(String name)
    {
        if (attributes.containsKey(name))
            return attributes.get(name);
        return noStroke.get(name);
    }
    
/**
 *Checks if the attribute exists.
 * @param name the attribute key
 * @return <CODE>true</CODE> if the attribute exists
 */
    
    boolean isAttribute(String name)
    {
        if (attributes.containsKey(name))
            return true;
        return noStroke.containsKey(name);
    }
    
/**
 * Checks if this <CODE>PdfChunk</CODE> needs some special metrics handling.
 * @return <CODE>true</CODE> if this <CODE>PdfChunk</CODE> needs some special metrics handling.
 */
    
    boolean isStroked()
    {
        return (!attributes.isEmpty());
    }
    
    /**
     * Checks if this <CODE>PdfChunk</CODE> is a Separator Chunk.
     * @return	true if this chunk is a separator.
     * @since	2.1.2
     */
    boolean isSeparator() {
    	return isAttribute(Chunk.SEPARATOR);
    }
    
    /**
     * Checks if this <CODE>PdfChunk</CODE> is a horizontal Separator Chunk.
     * @return	true if this chunk is a horizontal separator.
     * @since	2.1.2
     */
    boolean isHorizontalSeparator() {
    	if (isAttribute(Chunk.SEPARATOR)) {
    		Object[] o = (Object[])getAttribute(Chunk.SEPARATOR);
    		return !((Boolean)o[1]).booleanValue();
    	}
    	return false;
    }
    
    /**
     * Checks if this <CODE>PdfChunk</CODE> is a tab Chunk.
     * @return	true if this chunk is a separator.
     * @since	2.1.2
     */
    boolean isTab() {
    	return isAttribute(Chunk.TAB);
    }
    
    /**
     * Correction for the tab position based on the left starting position.
     * @param	newValue	the new value for the left X.
     * @since	2.1.2
     */
    void adjustLeft(float newValue) {
    	Object[] o = (Object[])attributes.get(Chunk.TAB);
    	if (o != null) {
    		attributes.put(Chunk.TAB, new Object[]{o[0], o[1], o[2], new Float(newValue)});
    	}
    }
    
/**
 * Checks if there is an image in the <CODE>PdfChunk</CODE>.
 * @return <CODE>true</CODE> if an image is present
 */
    
    boolean isImage()
    {
        return image != null;
    }
    
/**
 * Gets the image in the <CODE>PdfChunk</CODE>.
 * @return the image or <CODE>null</CODE>
 */
    
    Image getImage()
    {
        return image;
    }
    
/**
 * Sets the image offset in the x direction
 * @param  offsetX the image offset in the x direction
 */
    
    void setImageOffsetX(float offsetX)
    {
        this.offsetX = offsetX;
    }
    
/**
 * Gets the image offset in the x direction
 * @return the image offset in the x direction
 */
    
    float getImageOffsetX()
    {
        return offsetX;
    }
    
/**
 * Sets the image offset in the y direction
 * @param  offsetY the image offset in the y direction
 */
    
    void setImageOffsetY(float offsetY)
    {
        this.offsetY = offsetY;
    }
    
/**
 * Gets the image offset in the y direction
 * @return Gets the image offset in the y direction
 */
    
    float getImageOffsetY()
    {
        return offsetY;
    }
    
/**
 * sets the value.
 * @param value content of the Chunk
 */
    
    void setValue(String value)
    {
        this.value = value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return value;
    }

    /**
     * Tells you if this string is in Chinese, Japanese, Korean or Identity-H.
     * @return true if the Chunk has a special encoding
     */
    
    boolean isSpecialEncoding() {
        return encoding.equals(CJKFont.CJK_ENCODING) || encoding.equals(BaseFont.IDENTITY_H);
    }
    
    /**
     * Gets the encoding of this string.
     *
     * @return		a <CODE>String</CODE>
     */
    
    String getEncoding() {
        return encoding;
    }

    int length() {
        return value.length();
    }
    
    int lengthUtf32() {
        if (!BaseFont.IDENTITY_H.equals(encoding))
            return value.length();
        int total = 0;
        int len = value.length();
        for (int k = 0; k < len; ++k) {
            if (Utilities.isSurrogateHigh(value.charAt(k)))
                ++k;
            ++total;
        }
        return total;
    }
    
    boolean isExtSplitCharacter(int start, int current, int end, char[] cc, PdfChunk[] ck) {
        return splitCharacter.isSplitCharacter(start, current, end, cc, ck);
    }
    
/**
 * Removes all the <VAR>' '</VAR> and <VAR>'-'</VAR>-characters on the right of a <CODE>String</CODE>.
 * <P>
 * @param	string		the <CODE>String<CODE> that has to be trimmed.
 * @return	the trimmed <CODE>String</CODE>
 */    
    String trim(String string) {
        BaseFont ft = font.getFont();
        if (ft.getFontType() == BaseFont.FONT_TYPE_CJK && ft.getUnicodeEquivalent(' ') != ' ') {
            while (string.endsWith("\u0001")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        else {
            while (string.endsWith(" ") || string.endsWith("\t")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }

    public boolean changeLeading() {
        return changeLeading;
    }
    
    float getCharWidth(int c) {
        if (noPrint(c))
            return 0;
        if (isAttribute(Chunk.CHAR_SPACING)) {
        	Float cs = (Float) getAttribute(Chunk.CHAR_SPACING);
			return font.width(c) + cs.floatValue();
		}
        return font.width(c);
    }
    
    public static boolean noPrint(int c) {
        return ((c >= 0x200b && c <= 0x200f) || (c >= 0x202a && c <= 0x202e));
    }
    
}
