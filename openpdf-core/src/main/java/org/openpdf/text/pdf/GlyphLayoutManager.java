/*
 * Copyright 2020-2026 Volker Kunert.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
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
 * https://github.com/LibrePDF/OpenPDF
 */

package org.openpdf.text.pdf;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.InputStream;
import java.text.Bidi;
import java.util.Map;
import org.openpdf.text.pdf.GlyphLayoutFontManager.FontLoadException;
import org.openpdf.text.pdf.GlyphLayoutFontManager.FontOptions;

/**
 * Provides glyph layout e.g. for accented Latin letters.
 */
public class GlyphLayoutManager {

    private final GlyphLayoutFontManager glyphLayoutFontManager;
    private boolean writeActualText;

    private int defaultBidiDirection = Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT;

    public GlyphLayoutManager() {
        this(new GlyphLayoutFontManager());
    }

    @SuppressWarnings("deprecation")
    public GlyphLayoutManager(GlyphLayoutFontManager glyphLayoutFontManager) {
        this.glyphLayoutFontManager = glyphLayoutFontManager;
        if (LayoutProcessor.isEnabled()) {
            throw new IllegalStateException("LayoutProcessor is enabled. Don't use the deprecated LayoutProcessor!");
        }
    }

    /**
     * Checks if the glyphVector contains adjustments that make advanced layout necessary
     *
     * @param glyphVector glyph vector containing the positions
     * @return true, if the glyphVector contains adjustments
     */
    protected static boolean hasNoAdjustments(GlyphVector glyphVector) {
        boolean hasAdjustments = false;
        float lastX = 0f;
        float lastY = 0f;

        for (int i = 0; i < glyphVector.getNumGlyphs(); i++) {
            Point2D p = glyphVector.getGlyphPosition(i);
            float dx = (float) p.getX() - lastX;
            float dy = (float) p.getY() - lastY;

            float ax = (i == 0) ? 0.0f : glyphVector.getGlyphMetrics(i - 1).getAdvanceX();
            float ay = (i == 0) ? 0.0f : glyphVector.getGlyphMetrics(i - 1).getAdvanceY();

            if (dx != ax || dy != ay) {
                hasAdjustments = true;
                break;
            }
            lastX = (float) p.getX();
            lastY = (float) p.getY();
        }
        return !hasAdjustments;
    }

    protected static void completeCmap(PdfContentByte cb, BaseFont baseFont, String text, GlyphVector glyphVector) {
        cb.state.fontDetails.addMissingCmapEntries(text, glyphVector, baseFont);
    }

    /**
     * Sets the flag writeActualText If the flag is set, ActualText is written to the PDF file for extraction and
     * accessibility
     *
     * @param writeActualText Controls writing of actual text
     * @return this
     */
    public GlyphLayoutManager setWriteActualText(boolean writeActualText) {
        this.writeActualText = writeActualText;
        return this;
    }

    /**
     * Sets the defaultBidiDirection  for Bidi processing
     *
     * @param defaultBidiDirection default direction for bidi processing
     * @return this GlyphLayoutManager
     */
    public GlyphLayoutManager setDefaultBidiDirection(int defaultBidiDirection) {
        this.defaultBidiDirection = defaultBidiDirection;
        return this;
    }

    /**
     * Set default font options
     *
     * @param fontOptions font options
     * @return this GlyphLayoutManager
     */
    public GlyphLayoutManager setDefaultFontOptions(FontOptions fontOptions) {
        glyphLayoutFontManager.setDefaultFontOptions(fontOptions);
        return this;
    }

    /**
     * Loads an OpenPdf-Font and the corresponding AWT-Font only Truetype/OpenTyoe-Fonts are supported
     *
     * @param inputStream input stream of the font*
     * @param fontSize    Size
     * @return the loaded OpenPdf-Font
     */
    public org.openpdf.text.Font loadFont(String name, InputStream inputStream, float fontSize)
            throws FontLoadException {
        return glyphLayoutFontManager.loadFont(name, inputStream, fontSize);
    }

    /**
     * Loads an OpenPdf-Font and the corresponding AWT font only Truetype/OpenTyoe-Fonts are supported
     *
     * @param inputStream input stream of the font
     * @param fontSize    Size
     * @param fontOptions font options
     * @return the loaded OpenPdf font
     */
    public org.openpdf.text.Font loadFont(String name, InputStream inputStream, float fontSize,
            FontOptions fontOptions) throws FontLoadException {
        return glyphLayoutFontManager.loadFont(name, inputStream, fontSize, fontOptions);
    }

    /**
     * Loads an OpenPdf-Font and the corresponding AWT font only Truetype/OpenType-Fonts are supported
     *
     * @param path     of the font file
     * @param fontSize size
     * @return the loaded OpenPdf font
     */
    public org.openpdf.text.Font loadFont(String path, float fontSize) throws FontLoadException {
        return glyphLayoutFontManager.loadFont(path, fontSize);
    }

    /**
     * Loads an OpenPdf-Font and the corresponding AWT font only Truetype/OpenType-Fonts are supported
     *
     * @param path        of the font file
     * @param fontSize    size
     * @param fontOptions options
     * @return the loaded OpenPdf font
     */
    public org.openpdf.text.Font loadFont(String path, float fontSize,
            FontOptions fontOptions) throws FontLoadException {
        return glyphLayoutFontManager.loadFont(path, fontSize, fontOptions);
    }

    /**
     * Checks if the baseFont is loaded and supported
     *
     * @param baseFont BaseFont
     * @return true if the baseFont is supported
     * @throws UnsupportedOperationException if the font has not been loaded with GlyphLayoutManager.loadFont
     */
    public boolean supportsFont(BaseFont baseFont) {
        return glyphLayoutFontManager.supportsFont(baseFont);
    }

    /**
     * Computes glyph positioning
     *
     * @param baseFont OpenPdf base font
     * @param text     input text
     * @return glyph vector containing reordered text, width and positioning info
     */
    public GlyphVector computeGlyphVector(BaseFont baseFont, float fontSize, String text) {

        FontRenderContext fontRenderContext = createFontRenderContext();
        // specify fractional metrics to compute accurate positions
        char[] chars = text.toCharArray();
        java.awt.Font awtFont = glyphLayoutFontManager.getFont(baseFont, fontSize);
        int bidiFlags = getFontRunDirection(baseFont, fontSize);
        if (bidiFlags < 0) {
            bidiFlags = computeBidiFlags(text);
        }
        return awtFont.layoutGlyphVector(fontRenderContext, chars, 0, chars.length, bidiFlags);
    }

    /**
     * Computes the Bidi flags
     *
     * @param text text
     * @return Font.LAYOUT_LEFT_TO_RIGHT or Font.LAYOUT_RIGHT_TO_LEFT
     */
    protected int computeBidiFlags(String text) {
        int bidiFlags = (defaultBidiDirection == Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT ?
                Font.LAYOUT_LEFT_TO_RIGHT : Font.LAYOUT_RIGHT_TO_LEFT);
        if (Bidi.requiresBidi(text.toCharArray(), 0, text.length())) {
            Bidi bidi = new Bidi(text, defaultBidiDirection);

            if (bidi.isRightToLeft()) {
                bidiFlags = Font.LAYOUT_RIGHT_TO_LEFT;
            } else if (bidi.isLeftToRight()) {
                bidiFlags = java.awt.Font.LAYOUT_LEFT_TO_RIGHT;
            } else {
                throw new RuntimeException("not reached");
            }
        }
        return bidiFlags;
    }

    /**
     * Gets the run direction from the font attributes
     *
     * @param baseFont base font
     * @return Font.LAYOUT_LEFT_TO_RIGHT or Font.LAYOUT_RIGHT_TO_LEFT if direction is set for this font or -1 otherwise
     */
    protected int getFontRunDirection(BaseFont baseFont, float fontSize) {
        int bidiFlags = -1;
        java.awt.Font awtFont = glyphLayoutFontManager.getFont(baseFont, fontSize);

        Map<TextAttribute, ?> textAttributes = awtFont.getAttributes();
        if (textAttributes != null) {
            Object runDirection = textAttributes.get(TextAttribute.RUN_DIRECTION);
            if (runDirection != null) {
                bidiFlags = runDirection.equals(TextAttribute.RUN_DIRECTION_LTR) ? java.awt.Font.LAYOUT_LEFT_TO_RIGHT :
                        java.awt.Font.LAYOUT_RIGHT_TO_LEFT;
            }
        }
        return bidiFlags;
    }

    /**
     * Creates the fontRenderContext
     *
     * @return the FontRenderContext
     */
    protected FontRenderContext createFontRenderContext() {
        // specify fractional metrics to compute accurate positions
        return new FontRenderContext(new AffineTransform(), false, true);
    }

    /**
     * Shows a text using glyph positioning (if needed)
     *
     * @param cb       object containing the content of the page
     * @param baseFont base font to use
     * @param fontSize font size to apply
     * @param text     text to show
     */
    public void showText(PdfContentByte cb, BaseFont baseFont, float fontSize, String text) {

        if (ifMixedThenDivideTextAndShow(cb, baseFont, fontSize, text)) {
            return;
        }
        GlyphVector glyphVector = computeGlyphVector(baseFont, fontSize, text);
        completeCmap(cb, baseFont, text, glyphVector);

        if (writeActualText) {
            beginMarkedContentSequence(cb, text);
        }

        if (hasNoAdjustments(glyphVector)) {
            cb.showText(glyphVector);
        } else {
            adjustAndShowText(cb, fontSize, glyphVector);
        }

        if (writeActualText) {
            endMarkedContentSequence(cb);
        }
    }

    /**
     * Check if bidi is mixed, then call showText for each part
     *
     * @param cb       PdfContentByte
     * @param baseFont base font
     * @param fontSize size
     * @param text     text
     * @return true, if bidi is mixed, else false
     */
    protected boolean ifMixedThenDivideTextAndShow(PdfContentByte cb, BaseFont baseFont, float fontSize, String text) {
        if (getFontRunDirection(baseFont, fontSize) < 0 && Bidi.requiresBidi(text.toCharArray(), 0, text.length())) {
            Bidi bidi = new Bidi(text, defaultBidiDirection);
            if (bidi.isMixed()) {
                //See Unicode Bidirectional Algorithm.
                // Split the text if isMixed
                for (int i = 0; i < bidi.getRunCount(); i++) {
                    int run = bidi.baseIsLeftToRight() ? i : bidi.getRunCount() - 1 - i;
                    int start = bidi.getRunStart(run);
                    int limit = bidi.getRunLimit(run);
                    String part = text.substring(start, limit);
                    showText(cb, baseFont, fontSize, part);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Inserts begin marked content sequence into the PDF for ActualText for the given text
     *
     * @param cb   PdfContentByte
     * @param text the given text
     */
    protected void beginMarkedContentSequence(PdfContentByte cb, String text) {
        PdfDictionary d = new PdfDictionary();
        d.put(PdfName.ACTUALTEXT, new PdfString(text, PdfObject.TEXT_UNICODE));
        cb.beginMarkedContentSequence(PdfName.SPAN, d, true);
    }

    /**
     * Inserts end marked content sequence into the PDF
     *
     * @param cb PdfContentByte
     */
    protected void endMarkedContentSequence(PdfContentByte cb) {
        cb.endMarkedContentSequence();
    }

    /**
     * Adjusts and shows the given text according to the glyph vector
     *
     * @param cb          PdfContentByte
     * @param fontSize    font size
     * @param glyphVector the given glyph vector
     */
    protected void adjustAndShowText(PdfContentByte cb, final float fontSize, final GlyphVector glyphVector) {

        final float delta = 1e-5f;
        final float factorX = 1000f / fontSize;
        float lastX = 0f;

        PdfGlyphArray ga = new PdfGlyphArray();

        for (int i = 0; i < glyphVector.getNumGlyphs(); i++) {
            Point2D p = glyphVector.getGlyphPosition(i);
            float ax = (i == 0) ? 0.0f : glyphVector.getGlyphMetrics(i - 1).getAdvanceX();
            float dx = (float) p.getX() - lastX - ax;
            float py = (float) p.getY();

            if (Math.abs(py) >= delta) {
                if (!ga.isEmpty()) {
                    cb.showText(ga);
                    ga.clear();
                }
                cb.setTextRise(-py);
            }
            if (Math.abs(dx) >= delta) {
                ga.add(-dx * factorX);
            }
            ga.add(glyphVector.getGlyphCode(i));
            if (Math.abs(py) >= delta) {
                cb.showText(ga);
                ga.clear();
                cb.setTextRise(0.0f);
            }
            lastX = (float) p.getX();
        }
        Point2D p = glyphVector.getGlyphPosition(glyphVector.getNumGlyphs());
        float ax = (glyphVector.getNumGlyphs() == 0) ? 0.0f
                : glyphVector.getGlyphMetrics(glyphVector.getNumGlyphs() - 1).getAdvanceX();
        float dx = (float) p.getX() - lastX - ax;
        if (Math.abs(dx) >= delta) {
            ga.add(-dx * factorX);
        }
        cb.showText(ga);
        ga.clear();
    }
}