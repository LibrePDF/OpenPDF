/*
 * LayoutProcessor.java
 *
 * Copyright 2020 by Volker Kunert.
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

package com.lowagie.text.pdf;

import com.lowagie.text.FontFactory;
import com.lowagie.text.error_messages.MessageLocalization;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.AttributedString;
import java.text.Bidi;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides glyph layout e.g. for accented Latin letters.
 */
public class LayoutProcessor {
    private static final int DEFAULT_FLAGS = -1;
    private static final Map<BaseFont, java.awt.Font> awtFontMap = new ConcurrentHashMap<BaseFont, java.awt.Font>();

    // Static variables can only be set once
    private static boolean enabled = false;
    private static int flags = DEFAULT_FLAGS;

    private LayoutProcessor() {
        throw new UnsupportedOperationException("static class");
    }

    /**
     * Enables the processor
     */
    public static void enable() {
        enabled = true;
    }

    /**
     * Enables the processor providing flags
     * This method can only be called once.
     *
     * @param flags see java.awt.Font.layoutGlyphVector
     */
    public static void enable(int flags) {
        if (enabled) {
            throw new UnsupportedOperationException("LayoutProcessor is already enabled");
        }
        enabled = true;
        LayoutProcessor.flags = flags;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static int getFlags() {
        return flags;
    }

    public static boolean isSet(int queryFlags) {
        return (flags & queryFlags) == queryFlags;
    }

    public static boolean supportsFont(BaseFont baseFont) {
        boolean supports = enabled && (awtFontMap.get(baseFont) != null);
        return supports;
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param baseFont  OpenPdf base font
     * @param filename of the font file
     *
     * @throws RuntimeException if font can not be loaded
     */
    public static void loadFont(BaseFont baseFont, String filename) {
        if (!enabled) {
            return;
        }

        java.awt.Font awtFont = null;
        InputStream inputStream = null;
        try {
            awtFont = awtFontMap.get(baseFont);
            if (awtFont == null) {
                // getting the inputStream is adapted from com.lowagie.text.pdf.RandomAccessFileOrArray
                File file = new File(filename);
                if (!file.exists() && FontFactory.isRegistered(filename)) {
                    filename = (String) FontFactory.getFontImp().getFontPath(filename);
                    file = new File(filename);
                }
                if (file.canRead()) {
                    inputStream = new FileInputStream(file);
                } else if (filename.startsWith("file:/") || filename.startsWith("http://")
                        || filename.startsWith("https://") || filename.startsWith("jar:")
                        || filename.startsWith("wsjar:")) {
                    inputStream = new URL(filename).openStream();
                } else if ("-".equals(filename)) {
                    inputStream = System.in;
                } else {
                    inputStream = BaseFont.getResourceStream(filename);
                }
                if (inputStream == null) {
                    throw new IOException(
                            MessageLocalization.getComposedMessage("1.not.found.as.file.or.resource", filename));
                }
                awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, inputStream);
                if (awtFont != null) {
                    awtFontMap.put(baseFont, awtFont);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Font creation failed for %s.", filename), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Computes glyph positioning
     *
     * @param baseFont  OpenPdf base font
     * @param text input text
     * @return glyph vector containing reordered text, width and positioning info
     */
    public static GlyphVector computeGlyphVector(BaseFont baseFont, float fontSize, String text) {
        char[] chars = text.toCharArray();

        FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(), false, true);
        // specify fractional metrics to compute accurate positions

        int localFlags = LayoutProcessor.flags;
        if (localFlags == DEFAULT_FLAGS) {
            AttributedString as = new AttributedString(text);
            Bidi bidi = new Bidi(as.getIterator());
            localFlags = bidi.isLeftToRight() ? java.awt.Font.LAYOUT_LEFT_TO_RIGHT : java.awt.Font.LAYOUT_RIGHT_TO_LEFT;
        }
        java.awt.Font awtFont = LayoutProcessor.awtFontMap.get(baseFont).deriveFont(fontSize);
        GlyphVector glyphVector = awtFont.layoutGlyphVector(fontRenderContext, chars, 0, chars.length, localFlags);

        return glyphVector;
    }

   /**
    * Checks if the glyphVector contains adjustments
    * that make advanced layout necessary
    *
    * @param glyphVector glyph vector containing the positions
    * @return true, if the glyphVector contains adjustments
    */
    private static boolean hasAdjustments(GlyphVector glyphVector) {
        boolean retVal = false;
        float lastX = 0f;
        float lastY = 0f;

        for (int i = 0; i < glyphVector.getNumGlyphs(); i++) {
            Point2D p = glyphVector.getGlyphPosition(i);
            float dx = (float) p.getX() - lastX;
            float dy = (float) p.getY() - lastY;

            float ax = (i == 0) ? 0.0f : glyphVector.getGlyphMetrics(i - 1).getAdvanceX();
            float ay = (i == 0) ? 0.0f : glyphVector.getGlyphMetrics(i - 1).getAdvanceY();

            if (dx != ax || dy != ay) {
                retVal = true;
                break;
            }
            lastX = (float) p.getX();
            lastY = (float) p.getY();
        }
        return retVal;
    }

    /**
     * Shows a text using glyph positioning (if needed)
     *
     * @param cb       object containing the content of the page
     * @param baseFont base font to use
     * @param fontSize font size to apply
     * @param text     text to show
     * @return layout position correction to correct the start of the next line
     */
    public static Point2D showText(PdfContentByte cb, BaseFont baseFont, float fontSize, String text) {
        GlyphVector glyphVector = computeGlyphVector(baseFont, fontSize, text);
        if (!hasAdjustments(glyphVector)) {
            cb.showText(glyphVector);
            Point2D p = glyphVector.getGlyphPosition(glyphVector.getNumGlyphs());
            float dx = (float) p.getX();
            float dy = (float) p.getY();
            cb.moveTextBasic(dx, -dy);
            return new Point2D.Double(-dx, dy);
        }
        float lastX = 0f;
        float lastY = 0f;

        for (int i = 0; i < glyphVector.getNumGlyphs(); i++) {
            Point2D p = glyphVector.getGlyphPosition(i);

            float dx = (float) p.getX() - lastX;
            float dy = (float) p.getY() - lastY;

            cb.moveTextBasic(dx, -dy);

            cb.showText(glyphVector, i, i + 1);

            lastX = (float) p.getX();
            lastY = (float) p.getY();
        }
        Point2D p = glyphVector.getGlyphPosition(glyphVector.getNumGlyphs());
        float dx = (float) p.getX() - lastX;
        float dy = (float) p.getY() - lastY;
        cb.moveTextBasic(dx, -dy);
        return new Point2D.Double(-p.getX(), p.getY());
    }
}
