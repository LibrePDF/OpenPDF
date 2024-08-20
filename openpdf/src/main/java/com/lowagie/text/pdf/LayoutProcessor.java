/*
 * LayoutProcessor.java
 *
 * Copyright 2020-2024 Volker Kunert.
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
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.text.AttributedString;
import java.text.Bidi;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides glyph layout e.g. for accented Latin letters.
 */
public class LayoutProcessor {

    public enum Version {
        ONE,
        TWO
    }

    private static Version version = Version.TWO;

    private static final int DEFAULT_FLAGS = -1;
    private static final Map<BaseFont, java.awt.Font> awtFontMap = new ConcurrentHashMap<>();

    private static final Map<TextAttribute, Object> globalTextAttributes = new ConcurrentHashMap<>();

    // Static variables can only be set once
    private static boolean enabled = false;
    private static int flags = DEFAULT_FLAGS;

    private static boolean writeActualText;

    private LayoutProcessor() {
        throw new UnsupportedOperationException("static class");
    }

    /**
     * Enables the processor.
     * <p>
     * Kerning and ligatures are switched off. This method can only be called once.
     */
    public static void enable() {
        enabled = true;
    }

    /**
     * Enables the processor with the provided flags.
     * <p>
     * Kerning and ligatures are switched off. This method can only be called once.
     *
     * @param flags see java.awt.Font.layoutGlyphVector
     */
    public static void enable(int flags) {
        if (enabled) {
            throw new UnsupportedOperationException("LayoutProcessor is already enabled");
        }
        enable();
        LayoutProcessor.flags = flags;
    }

    /**
     * Enables the processor.
     * <p>
     * Kerning and ligatures are switched on. This method can only be called once.
     */
    public static void enableKernLiga() {
        enableKernLiga(DEFAULT_FLAGS);
    }

    /**
     * Enables the processor with the provided flags.
     * <p>
     * Kerning and ligatures are switched on. This method can only be called once.
     *
     * @param flags see java.awt.Font.layoutGlyphVector
     */
    public static void enableKernLiga(int flags) {
        if (enabled) {
            throw new UnsupportedOperationException("LayoutProcessor is already enabled");
        }
        setKerning();
        setLigatures();
        enable();
        LayoutProcessor.flags = flags;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Set version
     *
     * @param version to set
     * @deprecated To be used *only*, if version two produces incorrect PDF - please file an issue if this occurs
     */
    @Deprecated
    public static void setVersion(Version version) {
        LayoutProcessor.version = version;
    }

    /**
     * Set kerning
     *
     * @see <a href="https://docs.oracle.com/javase/tutorial/2d/text/textattributes.html">
     * Oracle: The Java™ Tutorials, Using Text Attributes to Style Text</a>
     */
    public static void setKerning() {
        LayoutProcessor.globalTextAttributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
    }

    /**
     * Set kerning for one font
     *
     * @param font The font for which kerning is to be turned on
     * @see <a href="https://docs.oracle.com/javase/tutorial/2d/text/textattributes.html">
     * Oracle: The Java™ Tutorials, Using Text Attributes to Style Text</a>
     */
    public static void setKerning(com.lowagie.text.Font font) {
        Map<TextAttribute, Object> textAttributes = new HashMap<>();
        textAttributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        setTextAttributes(font, textAttributes);
    }

    /**
     * Add ligatures
     */
    public static void setLigatures() {
        LayoutProcessor.globalTextAttributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
    }

    /**
     * Set ligatures for one font
     *
     * @param font The font for which ligatures are to be turned on
     */
    public static void setLigatures(com.lowagie.text.Font font) {
        Map<TextAttribute, Object> textAttributes = new HashMap<>();
        textAttributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
        setTextAttributes(font, textAttributes);
    }

    /**
     * Set run direction for one font to RTL
     *
     * @param font The font for which the run direction is set
     */
    public static void setRunDirectionRtl(com.lowagie.text.Font font) {
        setRunDirection(font, TextAttribute.RUN_DIRECTION_RTL);
    }

    /**
     * Set run direction for one font to LTR
     *
     * @param font The font for which the run direction is set
     */
    public static void setRunDirectionLtr(com.lowagie.text.Font font) {
        setRunDirection(font, TextAttribute.RUN_DIRECTION_LTR);
    }

    /**
     * Set run direction for one font
     *
     * @param font The font for which the run direction is set
     */
    private static void setRunDirection(com.lowagie.text.Font font, Boolean runDirection) {
        Map<TextAttribute, Object> textAttributes = new HashMap<>();
        textAttributes.put(TextAttribute.RUN_DIRECTION, runDirection);
        setTextAttributes(font, textAttributes);
    }

    /**
     * Set text attributes to font The attributes are used only for glyph layout, and don't change the visual appearance
     * of the font
     *
     * @param font           The font for which kerning is to be turned on
     * @param textAttributes Map of text attributes to be set
     * @see <a href="https://docs.oracle.com/javase/tutorial/2d/text/textattributes.html">
     * Oracle: The Java™ Tutorials, Using Text Attributes to Style Text</a>
     */
    private static void setTextAttributes(com.lowagie.text.Font font, Map<TextAttribute, Object> textAttributes) {
        BaseFont baseFont = font.getBaseFont();
        java.awt.Font awtFont = awtFontMap.get(baseFont);
        if (awtFont != null) {
            awtFont = awtFont.deriveFont(textAttributes);
            awtFontMap.put(baseFont, awtFont);
        }
    }

    /**
     * Include ACTUALTEXT in PDF
     */
    public static void setWriteActualText() {
        writeActualText = true;
    }

    public static int getFlags() {
        return flags;
    }

    /**
     * Returns the currennt version
     *
     * @return current version
     */
    public static Version getVersion() {
        return  LayoutProcessor.version;
    }

    public static boolean isSet(int queryFlags) {
        return flags != DEFAULT_FLAGS && (flags & queryFlags) == queryFlags;
    }

    public static boolean supportsFont(BaseFont baseFont) {
        return enabled && (awtFontMap.get(baseFont) != null);
    }

    /**
     * Loads the AWT font needed for layout
     * <p>
     * If baseFont is not instanceof TrueTypeFontUnicode *no* font is loaded.
     *
     * @param baseFont OpenPdf base font
     * @param filename of the font file
     * @throws RuntimeException if font can not be loaded
     */
    public static void loadFont(BaseFont baseFont, String filename) {
        if (!enabled || awtFontMap.get(baseFont) != null) {
            return;
        }
        if (!(baseFont instanceof TrueTypeFontUnicode)) {
            return;
        }
        java.awt.Font awtFont;
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
                    inputStream = Files.newInputStream(file.toPath());
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
                    if (!globalTextAttributes.isEmpty()) {
                        awtFont = awtFont.deriveFont(LayoutProcessor.globalTextAttributes);
                    }
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
     * @param baseFont OpenPdf base font
     * @param text     input text
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
        Map<TextAttribute, ?> textAttributes = awtFont.getAttributes();
        if (textAttributes != null) {
            Object runDirection = textAttributes.get(TextAttribute.RUN_DIRECTION);
            if (runDirection != null) {
                localFlags = runDirection == TextAttribute.RUN_DIRECTION_LTR ? java.awt.Font.LAYOUT_LEFT_TO_RIGHT :
                        java.awt.Font.LAYOUT_RIGHT_TO_LEFT;
            }
        }
        return awtFont.layoutGlyphVector(fontRenderContext, chars, 0, chars.length, localFlags);
    }

    /**
     * Checks if the glyphVector contains adjustments that make advanced layout necessary
     *
     * @param glyphVector glyph vector containing the positions
     * @return true, if the glyphVector contains adjustments
     */
    private static boolean noAdjustments(GlyphVector glyphVector) {
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
        return !retVal;
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

        if (LayoutProcessor.version == Version.ONE) {
            return showText1(cb, baseFont, fontSize, text);
        } else {
            return showText2(cb, baseFont, fontSize, text);
        }
    }


    private static void completeCmap(PdfContentByte cb, BaseFont baseFont, String text, GlyphVector glyphVector) {
        cb.state.fontDetails.addMissingCmapEntries(text, glyphVector, baseFont);
    }


    @Deprecated
    private static Point2D showText1(PdfContentByte cb, BaseFont baseFont, float fontSize, String text) {
        GlyphVector glyphVector = computeGlyphVector(baseFont, fontSize, text);
        completeCmap(cb, baseFont, text, glyphVector);

        if (noAdjustments(glyphVector)) {
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


    private static Point2D showText2(PdfContentByte cb, BaseFont baseFont, float fontSize, String text) {
        GlyphVector glyphVector = computeGlyphVector(baseFont, fontSize, text);
        completeCmap(cb, baseFont, text, glyphVector);

        if (writeActualText) {
            PdfDictionary d = new PdfDictionary();
            d.put(PdfName.ACTUALTEXT, new PdfString(text, PdfObject.TEXT_UNICODE));
            cb.beginMarkedContentSequence(PdfName.SPAN, d, true);
        }
        if (noAdjustments(glyphVector)) {
            cb.showText(glyphVector);
        } else {
            adjustAndShowText(cb, fontSize, glyphVector);
        }
        if (writeActualText) {
            cb.endMarkedContentSequence();
        }
        return new Point2D.Double(0.0, 0.0);
    }


    private static void adjustAndShowText(PdfContentByte cb, final float fontSize, final GlyphVector glyphVector) {

        final float deltaY = 1e-5f;
        final float deltaX = deltaY;
        final float factorX = 1000f / fontSize;

        float lastX = 0f;

        PdfGlyphArray ga = new PdfGlyphArray();

        for (int i = 0; i < glyphVector.getNumGlyphs(); i++) {
            Point2D p = glyphVector.getGlyphPosition(i);
            float ax = (i == 0) ? 0.0f : glyphVector.getGlyphMetrics(i - 1).getAdvanceX();
            float dx = (float) p.getX() - lastX - ax;
            float py = (float) p.getY();

            if (Math.abs(py) >= deltaY) {
                if (!ga.isEmpty()) {
                    cb.showText(ga);
                    ga.clear();
                }
                cb.setTextRise(-py);
            }
            if (Math.abs(dx) >= deltaX) {
                ga.add(-dx * factorX);
            }
            ga.add(glyphVector.getGlyphCode(i));
            if (Math.abs(py) >= deltaY) {
                cb.showText(ga);
                ga.clear();
                cb.setTextRise(0.0f);
            }
            lastX = (float) p.getX();
        }
        Point2D p = glyphVector.getGlyphPosition(glyphVector.getNumGlyphs());
        float ax = (glyphVector.getNumGlyphs() == 0) ? 0.0f : glyphVector.getGlyphMetrics(glyphVector.getNumGlyphs() - 1).getAdvanceX();
        float dx = (float) p.getX() - lastX - ax;
        if (Math.abs(dx) >= deltaX) {
            ga.add(-dx * factorX);
        }
        cb.showText(ga);
        ga.clear();
    }

    public static void disable() {
        enabled = false;
        flags = DEFAULT_FLAGS;
        awtFontMap.clear();
        globalTextAttributes.clear();
        writeActualText = false;
        setVersion(Version.TWO);
    }
}
