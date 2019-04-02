/*
 * Copyright 2008 by Kevin Day.
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
 * the Initial Developer are Copyright (C) 1999-2008 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2008 by Paulo Soares. All Rights Reserved.
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
package com.lowagie.text.pdf.parser;

import com.lowagie.text.pdf.CMapAwareDocumentFont;
import com.lowagie.text.pdf.DocumentFont;

/**
 * Keeps all the parameters of the graphics state.
 *
 * @since 2.1.4
 */
@SuppressWarnings("WeakerAccess")
public class GraphicsState {
    /**
     * The current transformation matrix.
     */
    private Matrix ctm;
    /**
     * The current character spacing.
     */
    private float characterSpacing;
    /**
     * The current word spacing.
     */
    private float wordSpacing;
    /**
     * The current horizontal scaling
     */
    private float horizontalScaling;
    /**
     * The current leading.
     */
    private float leading;
    /**
     * The active font.
     */
    private CMapAwareDocumentFont font;
    /**
     * The current font size.
     */
    private float fontSize;
    /**
     * The current render mode.
     */
    private int renderMode;
    /**
     * The current text rise
     */
    private float rise;
    /**
     * The current knockout value.
     */
    private boolean knockout;

    /**
     * Constructs a new Graphics State object with the default values.
     */
    public GraphicsState() {
        ctm = new Matrix();
        characterSpacing = 0;
        wordSpacing = 0;
        horizontalScaling = 1.0f;
        leading = 0;
        font = null;
        fontSize = 0;
        renderMode = 0;
        rise = 0;
        knockout = true;
    }

    /**
     * Copy constructor.
     *
     * @param source another GraphicsState object
     */
    public GraphicsState(GraphicsState source) {
        // note: all of the following are immutable, with the possible exception of font
        // so it is safe to copy them as-is
        ctm = source.ctm;
        characterSpacing = source.characterSpacing;
        wordSpacing = source.wordSpacing;
        horizontalScaling = source.horizontalScaling;
        leading = source.leading;
        font = source.font;
        fontSize = source.fontSize;
        renderMode = source.renderMode;
        rise = source.rise;
        knockout = source.knockout;
    }

    /**
     * Get the current transformation matrix.
     *
     * @return current transformation matrix
     */
    public Matrix getCtm() {
        return ctm;
    }

    public float getCharacterSpacing() {
        return characterSpacing;
    }

    public void setCharacterSpacing(float characterSpacing) {
        this.characterSpacing = characterSpacing;
    }

    public float getWordSpacing() {
        return wordSpacing;
    }

    public void setWordSpacing(float wordSpacing) {
        this.wordSpacing = wordSpacing;
    }

    public float getHorizontalScaling() {
        return horizontalScaling;
    }

    public void setHorizontalScaling(float horizontalScaling) {
        this.horizontalScaling = horizontalScaling;
    }

    public float getLeading() {
        return leading;
    }

    public void setLeading(float leading) {
        this.leading = leading;
    }

    /**
     * Get maximum height above the baseline reached by glyphs in this font,
     * excluding the height of glyphs for accented characters.
     *
     * @return ascent descriptor value
     */
    public float getFontAscentDescriptor() {
        return font.getFontDescriptor(DocumentFont.ASCENT, fontSize);
    }

    /**
     * Get maximum depth below the baseline reached by glyphs in this font. The
     * value is a negative number
     *
     * @return descent descriptor value
     */
    public float getFontDescentDescriptor() {
        return font.getFontDescriptor(DocumentFont.DESCENT, fontSize);
    }

    public float calculateCharacterWidthWithSpace(float charFontWidth) {
        return (charFontWidth * fontSize + characterSpacing + wordSpacing) * horizontalScaling;
    }

    public float calculateCharacterWidthWithoutSpace(float charFontWidth) {
        return (charFontWidth * fontSize + characterSpacing) * horizontalScaling;
    }

    public CMapAwareDocumentFont getFont() {
        return font;
    }

    public void setFont(CMapAwareDocumentFont font) {
        this.font = font;
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public int getRenderMode() {
        return renderMode;
    }

    public void setRenderMode(int renderMode) {
        this.renderMode = renderMode;
    }

    public float getRise() {
        return rise;
    }

    public void setRise(float rise) {
        this.rise = rise;
    }

    public boolean isKnockout() {
        return knockout;
    }

    /**
     * Multiply transformation matrix and get result. Result would be also stored in this {@link GraphicsState} instance
     *
     * @param matrix multiply by matrix
     * @return result matrix
     */
    public Matrix multiplyCtm(Matrix matrix) {
        ctm = ctm.multiply(matrix);
        return ctm;
    }
}
