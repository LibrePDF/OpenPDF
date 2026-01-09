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
import java.util.Arrays;
import java.util.HashMap;

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
                ttu = (TrueTypeFontUnicode) baseFont;
                symbolic = baseFont.isFontSpecific();
                break;
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
     * @param options rendering options
     * @return the conversion
     */
    byte[] convertToBytes(String text, TextRenderingOptions options) {
        switch (fontType) {
            case BaseFont.FONT_TYPE_T3:
                return convertType3Font(text);

            case BaseFont.FONT_TYPE_T1:
            case BaseFont.FONT_TYPE_TT:
                return convertType1OrTrueTypeFont(text);

            case BaseFont.FONT_TYPE_CJK:
                return convertCjkFont(text);

            case BaseFont.FONT_TYPE_DOCUMENT:
                return convertDocumentFont(text);

            case BaseFont.FONT_TYPE_TTUNI:
                return convertTrueTypeUnicodeFont(text, options);

            default:
                return null;
        }
    }

    // Converts Type 3 font text to bytes
    private byte[] convertType3Font(String text) {
        return baseFont.convertToBytes(text);
    }

    // Converts Type 1 or TrueType font text to bytes
    private byte[] convertType1OrTrueTypeFont(String text) {
        byte[] bytes = baseFont.convertToBytes(text);
        recordUsedCharacters(bytes);
        return bytes;
    }

    // Records characters that have been used
    private void recordUsedCharacters(byte[] bytes) {
        for (byte b : bytes) {
            shortTag[b & 0xff] = 1;
        }
    }

    // Converts CJK font text to bytes
    private byte[] convertCjkFont(String text) {
        recordCjkCharacters(text);
        return baseFont.convertToBytes(text);
    }

    // Records CJK characters that have been used
    private void recordCjkCharacters(String text) {
        for (int i = 0; i < text.length(); i++) {
            int cidCode = cjkFont.getCidCode(text.charAt(i));
            cjkTag.put(cidCode, 0);
        }
    }

    // Converts document font text to bytes
    private byte[] convertDocumentFont(String text) {
        return baseFont.convertToBytes(text);
    }

    // Converts TrueType Unicode font text to bytes
    private byte[] convertTrueTypeUnicodeFont(String text, TextRenderingOptions options) {
        try {
            if (symbolic) {
                return convertSymbolicFont(text);
            }

            // Handle IVS (Ideographic Variation Sequence) fonts
            if (mayContainIVS(text)) {
                return handleIvsText(text, text.length(), 0);
            }

            // Use Fop glyph processor if applicable
            if (shouldUseFopGlyphProcessor(options)) {
                String fileName = ((TrueTypeFontUnicode) getBaseFont()).fileName;
                return FopGlyphProcessor.convertToBytesWithGlyphs(
                        ttu, text, fileName, longTag, options.getDocumentLanguage()
                );
            }

            // Default glyph conversion
            return convertToBytesWithGlyphs(text);
        } catch (UnsupportedEncodingException e) {
            throw new ExceptionConverter(e);
        }
    }

    // Converts symbolic font text to bytes
    private byte[] convertSymbolicFont(String text) throws UnsupportedEncodingException {
        byte[] symbolBytes = PdfEncodings.convertToBytes(text, "symboltt");
        char[] glyphCodes = extractGlyphCodes(symbolBytes);
        String glyphString = new String(glyphCodes);
        return glyphString.getBytes(CJKFont.CJK_ENCODING);
    }

    // Extracts glyph codes from symbol bytes and records metrics
    private char[] extractGlyphCodes(byte[] symbolBytes) {
        char[] glyphCodes = new char[symbolBytes.length];
        int glyphCount = 0;

        for (byte b : symbolBytes) {
            int[] metrics = ttu.getMetricsTT(b & 0xff);
            if (metrics == null) {
                continue;
            }

            int glyphCode = metrics[0];
            int width = metrics[1];
            int unicodeDiff = ttu.getUnicodeDifferences(b & 0xff);

            longTag.put(glyphCode, new int[]{glyphCode, width, unicodeDiff});
            glyphCodes[glyphCount++] = (char) glyphCode;
        }

        return java.util.Arrays.copyOf(glyphCodes, glyphCount);
    }

    // Determines whether to use Fop glyph processor
    private boolean shouldUseFopGlyphProcessor(TextRenderingOptions options) {
        if (!options.isGlyphSubstitutionEnabled() || !FopGlyphProcessor.isFopSupported()) {
            return false;
        }

        String fileName = ((TrueTypeFontUnicode) getBaseFont()).fileName;
        return fileName != null
                && !fileName.isEmpty()
                && isTrueTypeFile(fileName);
    }

    // Checks if the file is a TrueType font file
    private boolean isTrueTypeFile(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".ttf");
    }

    private static boolean isVariationSelector(int codePoint) {
        return (codePoint >= 0xFE00 && codePoint <= 0xFE0F)
                || (codePoint >= 0xE0100 && codePoint <= 0xE01EF);
    }

    /**
     * Quickly determine whether the text may contain IVS (to decide whether to use the IVS dedicated path)
     * Note: This means "may contain," not "must contain"—err on the side of caution to avoid omissions
     */
    private static boolean mayContainIVS(String text) {
        if (text == null) return false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c >= '\uFE00' && c <= '\uFE0F') {
                return true;
            }

            if (c >= '\udb40' && c <= '\udb43') {
                return true;
            }
        }
        return false;
    }

    private byte[] convertCharsToBytes(char[] chars) {
        byte[] result = new byte[chars.length * 2];

        for (int i = 0; i < chars.length; ++i) {
            result[2 * i] = (byte) (chars[i] / 256);
            result[2 * i + 1] = (byte) (chars[i] % 256);
        }

        return result;
    }

    private byte[] convertToBytesWithGlyphs(String text) throws UnsupportedEncodingException {
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
            Integer gl = m0;
            if (!longTag.containsKey(gl)) {
                longTag.put(gl, new int[]{m0, metrics[1], val});
            }
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

    byte[] convertToBytes(GlyphVector glyphVector) {
        return convertToBytes(glyphVector, 0, glyphVector.getNumGlyphs());
    }

    byte[] convertToBytes(GlyphVector glyphVector, int beginIndex, int endIndex) {
        if (fontType != BaseFont.FONT_TYPE_TTUNI || symbolic) {
            throw new UnsupportedOperationException("Only supported for True Type Unicode fonts");
        }

        char[] glyphs = new char[endIndex - beginIndex];
        int glyphCount = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            int code = glyphVector.getGlyphCode(i);
            if (code == 0xFFFE || code == 0xFFFF) {
                // considered non-glyphs by AWT
                continue;
            }

            glyphs[glyphCount++] = (char) code; // FIXME supplementary plane?

            Integer codeKey = code;
            if (!longTag.containsKey(codeKey)) {
                int glyphWidth = ttu.getGlyphWidth(code);
                Integer charCode = ttu.getCharacterCode(code);
                int[] metrics = charCode != null ? new int[]{code, glyphWidth, charCode} : new int[]{
                        code, glyphWidth};
                longTag.put(codeKey, metrics);
            }
        }

        String s = new String(glyphs, 0, glyphCount);
        try {
            byte[] b = s.getBytes(CJKFont.CJK_ENCODING);
            return b;
        } catch (UnsupportedEncodingException e) {
            throw new ExceptionConverter(e);
        }
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
                    baseFont.writeFont(writer, indirectReference, new Object[]{longTag, subset});
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

    /**
     * handle ivs text
     */
    private byte[] handleIvsText(String text, int len, int startIndex) {
        char[] glyph = new char[len * 2];
        int glyphIndex = startIndex;
        int k = 0;

        while (k < len) {
            CodePointInfo baseChar = parseCodePoint(text, k, len);
            CodePointInfo vsChar = parseVariationSelector(text, k + baseChar.charCount, len);
            int skipCount = baseChar.charCount;
            if (vsChar != null) {
                glyphIndex = addIvsGlyph(baseChar.codePoint, vsChar.codePoint, glyph, glyphIndex);
                skipCount += vsChar.charCount;
            } else {
                glyphIndex = addDefaultGlyph(baseChar.codePoint, glyph, glyphIndex);
            }
            k += skipCount;
        }

        glyph = Arrays.copyOfRange(glyph, 0, glyphIndex);
        return convertCharsToBytes(glyph);
    }

    private CodePointInfo parseCodePoint(String text, int index, int len) {
        if (index < len - 1
                && Character.isHighSurrogate(text.charAt(index))
                && Character.isLowSurrogate(text.charAt(index + 1))) {
            // Surrogate pair
            int codePoint = Character.toCodePoint(text.charAt(index), text.charAt(index + 1));
            return new CodePointInfo(codePoint, 2);
        } else {
            // BMP
            return new CodePointInfo(text.charAt(index), 1);
        }
    }

    private CodePointInfo parseVariationSelector(String text, int index, int len) {
        if (index >= len) {
            return null;
        }

        char currentChar = text.charAt(index);

        // single char IVS
        if (isVariationSelector(currentChar)) {
            return new CodePointInfo(currentChar, 1);
        }

        // surrogate pair IVS
        if (index < len - 1
                && Character.isHighSurrogate(currentChar)
                && Character.isLowSurrogate(text.charAt(index + 1))) {
            int codePoint = Character.toCodePoint(currentChar, text.charAt(index + 1));
            if (isVariationSelector(codePoint)) {
                return new CodePointInfo(codePoint, 2);
            }
        }

        return null;
    }

    private int addIvsGlyph(int baseCp, int vsCp, char[] glyph, int glyphIndex) {
        int[] format14Metrics = this.ttu.getFormat14MetricsTT(baseCp, vsCp);

        if (format14Metrics != null) {
            int glyphId = format14Metrics[0];
            cacheGlyphMetrics(glyphId, format14Metrics[1], baseCp, vsCp);
            glyph[glyphIndex] = (char) glyphId;
            return glyphIndex + 1;
        }

        // fallback
        return addDefaultGlyph(baseCp, glyph, glyphIndex);
    }

    private int addDefaultGlyph(int codePoint, char[] glyph, int glyphIndex) {
        int[] metrics = this.ttu.getMetricsTT(codePoint);

        if (metrics != null) {
            int glyphId = metrics[0];
            cacheGlyphMetrics(glyphId, metrics[1], codePoint);
            glyph[glyphIndex] = (char) glyphId;
            return glyphIndex + 1;
        }

        return glyphIndex;
    }

    /**
     * cache IVS glyph metrics info
     */
    private void cacheGlyphMetrics(int glyphId, int width, int baseCp) {
        if (!this.longTag.containsKey(glyphId)) {
            this.longTag.put(glyphId, new int[]{glyphId, width, baseCp});
        }
    }

    /**
     * cache IVS glyph metrics info
     */
    private void cacheGlyphMetrics(int glyphId, int width, int baseCp, int vsCp) {
        if (!this.longTag.containsKey(glyphId)) {
            this.longTag.put(glyphId, new int[]{glyphId, width, baseCp, vsCp});
        }
    }

    private static class CodePointInfo {
        final int codePoint;
        final int charCount;

        CodePointInfo(int codePoint, int charCount) {
            this.codePoint = codePoint;
            this.charCount = charCount;
        }
    }
}
