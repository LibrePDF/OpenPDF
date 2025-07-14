/*
 * $Id: FontDetails.java 4024 2009-07-12 00:33:57Z xlv $
 *
 * Copyright 2001, 2002 by Paulo Soares.
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
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.pdf;

import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.TextRenderingOptions;
import com.lowagie.text.Utilities;
import java.awt.font.GlyphVector;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Each font in the document will have an instance of this class where the characters used will be represented.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
class FontDetails {

    /**
     * Indicates if only a subset of the glyphs and widths for that particular encoding should be included in the
     * document.
     */
    protected boolean subset = true;
    /**
     * The indirect reference to this font
     */
    PdfIndirectReference indirectReference;
    /**
     * The font name that appears in the document body stream
     */
    PdfName fontName;
    /**
     * The font
     */
    BaseFont baseFont;
    /**
     * The font if it's an instance of <CODE>TrueTypeFontUnicode</CODE>
     */
    TrueTypeFontUnicode ttu;
    /**
     * The font if it's an instance of <CODE>CJKFont</CODE>
     */
    CJKFont cjkFont;
    /**
     * The array used with single byte encodings
     */
    byte[] shortTag;
    /**
     * The map used with double byte encodings. The key is Integer(glyph) and the value is int[]{glyph, width, Unicode
     * code}
     */
    HashMap<Integer, int[]> longTag;
    /**
     * IntHashtable with CIDs of CJK glyphs that are used in the text.
     */
    IntHashtable cjkTag;
    /**
     * The font type
     */
    int fontType;
    /**
     * <CODE>true</CODE> if the font is symbolic
     */
    boolean symbolic;
    /**
     * Contain glyphs that used but missing in Cmap. the value is int[]{glyph, Unicode code}
     */
    private Map<Integer, int[]> fillerCmap;


    /**
     * Each font used in a document has an instance of this class. This class stores the characters used in the document
     * and other specifics unique to the current working document.
     *
     * @param fontName          the font name
     * @param indirectReference the indirect reference to the font
     * @param baseFont          the <CODE>BaseFont</CODE>
     */
    FontDetails(PdfName fontName, PdfIndirectReference indirectReference, BaseFont baseFont) {
        this.fontName = fontName;
        this.indirectReference = indirectReference;
        this.baseFont = baseFont;
        fontType = baseFont.getFontType();
        switch (fontType) {
            case BaseFont.FONT_TYPE_T1:
            case BaseFont.FONT_TYPE_TT:
                shortTag = new byte[256];
                break;
            case BaseFont.FONT_TYPE_CJK:
                cjkTag = new IntHashtable();
                cjkFont = (CJKFont) baseFont;
                break;
            case BaseFont.FONT_TYPE_TTUNI:
                longTag = new HashMap<>();
                fillerCmap = new HashMap<>();
                ttu = (TrueTypeFontUnicode) baseFont;
                symbolic = baseFont.isFontSpecific();
                break;
        }
    }

    Map<Integer, int[]> getFillerCmap() {
        return fillerCmap;
    }

    void putFillerCmap(Integer key, int[] value) {
        fillerCmap.put(key, value);
    }

    void addMissingCmapEntries(String text, GlyphVector glyphVector, BaseFont baseFont) {

        if (baseFont instanceof TrueTypeFontUnicode trueTypeFont && getFillerCmap() != null) {
            int[][] localCmap = trueTypeFont.getSentenceMissingCmap(text, glyphVector);

            for (int[] ints : localCmap) {
                putFillerCmap(ints[0], new int[]{ints[0], ints[1]});
            }
        }
    }


    /**
     * Gets the indirect reference to this font.
     *
     * @return the indirect reference to this font
     */
    PdfIndirectReference getIndirectReference() {
        return indirectReference;
    }

    /**
     * Gets the font name as it appears in the document body.
     *
     * @return the font name
     */
    PdfName getFontName() {
        return fontName;
    }

    /**
     * Gets the <CODE>BaseFont</CODE> of this font.
     *
     * @return the <CODE>BaseFont</CODE> of this font
     */
    BaseFont getBaseFont() {
        return baseFont;
    }

    /**
     * Converts the text into bytes to be placed in the document. The conversion is done according to the font and the
     * encoding and the characters used are stored.
     *
     * @param text the text to convert
     * @return the conversion
     */
    byte[] convertToBytes(String text, TextRenderingOptions options) {
        byte[] b = null;
        switch (fontType) {
            case BaseFont.FONT_TYPE_T3:
                return baseFont.convertToBytes(text);
            case BaseFont.FONT_TYPE_T1:
            case BaseFont.FONT_TYPE_TT: {
                b = baseFont.convertToBytes(text);

                for (byte b1 : b) {
                    shortTag[b1 & 0xff] = 1;
                }
                break;
            }
            case BaseFont.FONT_TYPE_CJK: {
                int len = text.length();
                for (int k = 0; k < len; ++k) {
                    cjkTag.put(cjkFont.getCidCode(text.charAt(k)), 0);
                }
                b = baseFont.convertToBytes(text);
                break;
            }
            case BaseFont.FONT_TYPE_DOCUMENT: {
                b = baseFont.convertToBytes(text);
                break;
            }
            case BaseFont.FONT_TYPE_TTUNI: {
                try {
                    int len = text.length();
                    int[] metrics = null;
                    char[] glyph = new char[len];
                    int i = 0;
                    if (symbolic) {
                        b = PdfEncodings.convertToBytes(text, "symboltt");
                        len = b.length;
                        for (int k = 0; k < len; ++k) {
                            metrics = ttu.getMetricsTT(b[k] & 0xff);
                            if (metrics == null) {
                                continue;
                            }
                            longTag.put(metrics[0],
                                    new int[]{metrics[0], metrics[1], ttu.getUnicodeDifferences(b[k] & 0xff)});
                            glyph[i++] = (char) metrics[0];
                        }
                        String s = new String(glyph, 0, i);
                        b = s.getBytes(CJKFont.CJK_ENCODING);

                    } else {
                        String fileName = ((TrueTypeFontUnicode) getBaseFont()).fileName;
                        if (options.isGlyphSubstitutionEnabled() && FopGlyphProcessor.isFopSupported()
                                && (fileName != null && fileName.length() > 0
                                && (fileName.contains(".ttf") || fileName.contains(".TTF")))) {
                            return FopGlyphProcessor.convertToBytesWithGlyphs(ttu, text, fileName, longTag,
                                    options.getDocumentLanguage());
                        } else {
                            return convertToBytesWithGlyphs(text);
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new ExceptionConverter(e);
                }
                break;
            }
        }
        return b;
    }

    private byte[] convertToBytesWithGlyphs(String text) {
        int len = text.length();
        int[] metrics = null;
        int[] glyph = new int[len];
        int i = 0;
        for (int k = 0; k < len; ++k) {
            int val;
            if (Utilities.isSurrogatePair(text, k)) {
                val = Utilities.convertToUtf32(text, k);
                k++;
            } else {
                val = text.charAt(k);
            }
            metrics = ttu.getMetricsTT(val);
            if (metrics == null) {
                continue;
            }
            int m0 = metrics[0];
            int m1 = metrics[1];
            longTag.computeIfAbsent(m0, key -> new int[]{m0, m1, val});
            glyph[i++] = m0;
        }
        return getCJKEncodingBytes(glyph, i);
    }

    private byte[] getCJKEncodingBytes(int[] glyph, int size) {
        byte[] result = new byte[size * 2];
        for (int i = 0; i < size; i++) {
            int g = glyph[i];
            result[i * 2] = (byte) (g >> 8);
            result[i * 2 + 1] = (byte) (g & 0xFF);
        }
        return result;
    }

    /**
     * Convert a glyph code to bytes
     *
     * @param glyphCodes List of glyph codes
     * @return byte array with one or two bytes as UTF-16BE representation of the glyph code
     * @see <CODE>convertToBytes(GlyphVector glyphVector,...)</CODE>
     */
    byte[] convertToBytes(final List<Integer> glyphCodes) {
        if (fontType != BaseFont.FONT_TYPE_TTUNI) {
            throw new UnsupportedOperationException("Only supported for True Type Unicode fonts");
        }
        List<Integer> codePoints = new ArrayList<>();
        for (int glyphCode : glyphCodes) {
            if (glyphCode == 0xFFFE || glyphCode == 0xFFFF) {
                // considered non-glyphs by AWT
                return new byte[]{};
            }
            codePoints.add(glyphCode);
            if (!longTag.containsKey(glyphCode)) {
                int glyphWidth = ttu.getGlyphWidth(glyphCode);
                Integer charCode = ttu.getCharacterCode(glyphCode);
                int[] metrics = charCode != null ? new int[]{glyphCode, glyphWidth, charCode} : new int[]{
                        glyphCode, glyphWidth};
                longTag.put(glyphCode, metrics);
            }
        }
        return getBytesFromCodePoints(codePoints);
    }

    private static byte[] getBytesFromCodePoints(List<Integer> codePoints) {
        int[] codePointsArray = new int[codePoints.size()];
        for (int i = 0; i < codePoints.size(); i++) {
            codePointsArray[i] = codePoints.get(i);
        }
        String s = new String(codePointsArray, 0, codePointsArray.length);
        return s.getBytes(StandardCharsets.UTF_16BE);
    }

    byte[] convertToBytes(GlyphVector glyphVector, int beginIndex, int endIndex) {
        if (fontType != BaseFont.FONT_TYPE_TTUNI || symbolic) {
            throw new UnsupportedOperationException("Only supported for True Type Unicode fonts");
        }
        List<Integer> codePoints = new ArrayList<>();

        for (int i = beginIndex; i < endIndex; i++) {
            int code = glyphVector.getGlyphCode(i);
            if (code == 0xFFFE || code == 0xFFFF) {
                // considered non-glyphs by AWT
                continue;
            }
            codePoints.add(code);

            Integer codeKey = code;
            if (!longTag.containsKey(codeKey)) {
                int glyphWidth = ttu.getGlyphWidth(code);
                Integer charCode = ttu.getCharacterCode(code);
                int[] metrics = charCode != null ? new int[]{code, glyphWidth, charCode} : new int[]{
                        code, glyphWidth};
                longTag.put(codeKey, metrics);
            }
        }
        return getBytesFromCodePoints(codePoints);
    }


    /**
     * Writes the font definition to the document.
     *
     * @param writer the <CODE>PdfWriter</CODE> of this document
     */
    void writeFont(PdfWriter writer) {
        try {
            switch (fontType) {
                case BaseFont.FONT_TYPE_T3:
                    baseFont.writeFont(writer, indirectReference, null);
                    break;
                case BaseFont.FONT_TYPE_T1:
                case BaseFont.FONT_TYPE_TT: {
                    int firstChar;
                    int lastChar;
                    for (firstChar = 0; firstChar < 256; ++firstChar) {
                        if (shortTag[firstChar] != 0) {
                            break;
                        }
                    }
                    for (lastChar = 255; lastChar >= firstChar; --lastChar) {
                        if (shortTag[lastChar] != 0) {
                            break;
                        }
                    }
                    if (firstChar > 255) {
                        firstChar = 255;
                        lastChar = 255;
                    }
                    baseFont.writeFont(writer, indirectReference, new Object[]{firstChar, lastChar, shortTag, subset});
                    break;
                }
                case BaseFont.FONT_TYPE_CJK:
                    baseFont.writeFont(writer, indirectReference, new Object[]{cjkTag});
                    break;
                case BaseFont.FONT_TYPE_TTUNI:
                    baseFont.writeFont(writer, indirectReference, new Object[]{longTag, subset, getFillerCmap()});
                    break;
            }
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Indicates if all the glyphs and widths for that particular encoding should be included in the document.
     *
     * @return <CODE>false</CODE> to include all the glyphs and widths.
     */
    public boolean isSubset() {
        return subset;
    }

    /**
     * Indicates if all the glyphs and widths for that particular encoding should be included in the document. Set to
     * <CODE>false</CODE> to include all.
     *
     * @param subset new value of property subset
     */
    public void setSubset(boolean subset) {
        this.subset = subset;
    }
}
