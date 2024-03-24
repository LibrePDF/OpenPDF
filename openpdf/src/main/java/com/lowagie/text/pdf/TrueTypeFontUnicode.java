/*
 * $Id: TrueTypeFontUnicode.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2001, 2002 Paulo Soares
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

import com.lowagie.text.DocumentException;
import com.lowagie.text.Utilities;
import com.lowagie.text.error_messages.MessageLocalization;
import java.awt.font.GlyphVector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a True Type font with Unicode encoding. All the character in the font can be used directly by using the
 * encoding Identity-H or Identity-V. This is the only way to represent some character sets such as Thai.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
class TrueTypeFontUnicode extends TrueTypeFont implements Comparator<int[]> {

    private static final byte[] rotbits = {(byte) 0x80, (byte) 0x40, (byte) 0x20, (byte) 0x10, (byte) 0x08, (byte) 0x04,
            (byte) 0x02, (byte) 0x01};

    /**
     * <CODE>true</CODE> if the encoding is vertical.
     */
    boolean vertical;

    Map<Integer, Integer> inverseCmap;

    /**
     * Creates a new TrueType font addressed by Unicode characters. The font will always be embedded.
     *
     * @param ttFile the location of the font on file. The file must end in '.ttf'. The modifiers after the name are
     *               ignored.
     * @param enc    the encoding to be applied to this font
     * @param emb    true if the font is to be embedded in the PDF
     * @param ttfAfm the font as a <CODE>byte</CODE> array
     * @throws DocumentException the font is invalid
     * @throws IOException       the font file could not be read
     */
    TrueTypeFontUnicode(String ttFile, String enc, boolean emb, byte[] ttfAfm, boolean forceRead)
            throws DocumentException, IOException {
        String nameBase = getBaseName(ttFile);
        String ttcName = getTTCName(nameBase);
        if (nameBase.length() < ttFile.length()) {
            style = ttFile.substring(nameBase.length());
        }
        encoding = enc;
        embedded = emb;
        fileName = ttcName;
        ttcIndex = "";
        if (ttcName.length() < nameBase.length()) {
            ttcIndex = nameBase.substring(ttcName.length() + 1);
        }
        fontType = FONT_TYPE_TTUNI;
        if ((fileName.toLowerCase().endsWith(".ttf") || fileName.toLowerCase().endsWith(".otf")
                || fileName.toLowerCase().endsWith(".ttc")) && ((enc.equals(IDENTITY_H) || enc.equals(IDENTITY_V))
                && emb)) {
            process(ttfAfm, forceRead);
            if (os_2.fsType == 2) {
                throw new DocumentException(
                        MessageLocalization.getComposedMessage("1.cannot.be.embedded.due.to.licensing.restrictions",
                                fileName + style));
            }
            // Sivan
            if ((cmap31 == null && !fontSpecific) || (cmap10 == null && fontSpecific)) {
                directTextToByte = true;
            }
            if (fontSpecific) {
                fontSpecific = false;
                String tempEncoding = encoding;
                encoding = "";
                createEncoding();
                encoding = tempEncoding;
                fontSpecific = true;
            }
        } else {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("1.2.is.not.a.ttf.font.file", fileName, style));
        }
        vertical = enc.endsWith("V");
    }

    private static String toHex4(int n) {
        String s = "0000" + Integer.toHexString(n);
        return s.substring(s.length() - 4);
    }

    /**
     * Gets an hex string in the format "&lt;HHHH&gt;".
     *
     * @param n the number
     * @return the hex string
     */
    static String toHex(int n) {
        if (n < 0x10000) {
            return "<" + toHex4(n) + ">";
        }
        n -= 0x10000;
        int high = (n / 0x400) + 0xd800;
        int low = (n % 0x400) + 0xdc00;
        return "[<" + toHex4(high) + toHex4(low) + ">]";
    }

    @Override
    void readCMaps() throws DocumentException, IOException {
        super.readCMaps();

        Map<Integer, int[]> cmap = null;
        if (cmapExt != null) {
            cmap = cmapExt;
        } else if (cmap31 != null) {
            cmap = cmap31;
        }

        if (cmap != null) {
            inverseCmap = new HashMap<>();
            for (Map.Entry<Integer, int[]> entry : cmap.entrySet()) {
                Integer code = entry.getKey();
                int[] metrics = entry.getValue();
                inverseCmap.put(metrics[0], code);
            }
        }
    }

    protected Integer getCharacterCode(int code) {
        return inverseCmap == null ? null : inverseCmap.get(code);
    }


    /**
     * Gets the width of a <CODE>char</CODE> in normalized 1000 units.
     *
     * @param char1 the unicode <CODE>char</CODE> to get the width of
     * @return the width in normalized 1000 units
     */
    @Override
    public int getWidth(int char1) {
        if (vertical) {
            return 1000;
        }
        if (fontSpecific) {
            if ((char1 & 0xff00) == 0 || (char1 & 0xff00) == 0xf000) {
                return getRawWidth(char1 & 0xff, null);
            } else {
                return 0;
            }
        } else {
            return getRawWidth(char1, encoding);
        }
    }

    /**
     * Gets the width of a <CODE>String</CODE> in normalized 1000 units.
     *
     * @param text the <CODE>String</CODE> to get the width of
     * @return the width in normalized 1000 units
     */
    @Override
    public int getWidth(String text) {
        if (vertical) {
            return text.length() * 1000;
        }
        int total = 0;
        if (fontSpecific) {
            char[] cc = text.toCharArray();
            for (char c : cc) {
                if ((c & 0xff00) == 0 || (c & 0xff00) == 0xf000) {
                    total += getRawWidth(c & 0xff, null);
                }
            }
        } else {
            int len = text.length();
            for (int k = 0; k < len; ++k) {
                if (Utilities.isSurrogatePair(text, k)) {
                    total += getRawWidth(Utilities.convertToUtf32(text, k), encoding);
                    ++k;
                } else {
                    total += getRawWidth(text.charAt(k), encoding);
                }
            }
        }
        return total;
    }

    int[][] getSentenceMissingCmap(String text, GlyphVector glyphVector) {
        char[] chars = text.toCharArray();
        int[] glyphCodes = glyphVector.getGlyphCodes(0, glyphVector.getNumGlyphs(),
                new int[glyphVector.getNumGlyphs()]);

        List<int[]> missingCmapList = new ArrayList<>();
        for (int i = 0; i < glyphCodes.length; i++) {
            int charIndex = glyphVector.getGlyphCharIndex(i);
            int glyphCode = glyphCodes[i];
            Integer cmapCharactherCode = getCharacterCode(glyphCode);
            if (cmapCharactherCode == null) {
                missingCmapList.add(new int[]{glyphCode, chars[charIndex]});
            }
        }

        return missingCmapList.toArray(new int[missingCmapList.size()][]);
    }

    /**
     * Creates a ToUnicode CMap to allow copy and paste from Acrobat.
     *
     * @param metrics metrics[0] contains the glyph index and metrics[2] contains the Unicode code
     * @return the stream representing this CMap or <CODE>null</CODE>
     */
    private PdfStream getToUnicode(int[][] metrics) {
        metrics = filterCmapMetrics(metrics);
        if (metrics.length == 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder(
                "/CIDInit /ProcSet findresource begin\n" +
                        "12 dict begin\n" +
                        "begincmap\n" +
                        "/CIDSystemInfo\n" +
                        "<< /Registry (TTX+0)\n" +
                        "/Ordering (T42UV)\n" +
                        "/Supplement 0\n" +
                        ">> def\n" +
                        "/CMapName /TTX+0 def\n" +
                        "/CMapType 2 def\n" +
                        "1 begincodespacerange\n" +
                        "<0000><FFFF>\n" +
                        "endcodespacerange\n");
        int size = 0;
        for (int k = 0; k < metrics.length; ++k) {
            if (size == 0) {
                if (k != 0) {
                    buf.append("endbfrange\n");
                }
                size = Math.min(100, metrics.length - k);
                buf.append(size).append(" beginbfrange\n");
            }
            --size;
            int[] metric = metrics[k];
            String fromTo = toHex(metric[0]);
            buf.append(fromTo).append(fromTo).append(toHex(metric[2])).append('\n');
        }
        buf.append(
                "endbfrange\n" +
                        "endcmap\n" +
                        "CMapName currentdict /CMap defineresource pop\n" +
                        "end end\n");
        String s = buf.toString();
        PdfStream stream = new PdfStream(PdfEncodings.convertToBytes(s, null));
        stream.flateCompress(compressionLevel);
        return stream;
    }

    private int[][] filterCmapMetrics(int[][] metrics) {
        if (metrics.length == 0) {
            return metrics;
        }

        List<int[]> cmapMetrics = new ArrayList<>(metrics.length);
        for (int[] metric : metrics) {
            // PdfContentByte.showText(GlyphVector) uses glyphs that might not
            // map to a character.
            // the glyphs are included in the metrics array, but we need to
            // exclude them from the cmap.
            if (metric.length >= 3) {
                cmapMetrics.add(metric);
            }
        }

        if (cmapMetrics.size() == metrics.length) {
            return metrics;
        }

        return cmapMetrics.toArray(new int[0][]);
    }

    /**
     * Generates the CIDFontTyte2 dictionary.
     *
     * @param fontDescriptor the indirect reference to the font descriptor
     * @param subsetPrefix   the subset prefix
     * @param metrics        the horizontal width metrics
     * @return a stream
     */
    private PdfDictionary getCIDFontType2(PdfIndirectReference fontDescriptor, String subsetPrefix, int[][] metrics) {
        PdfDictionary dic = new PdfDictionary(PdfName.FONT);
        // sivan; cff
        if (cff) {
            dic.put(PdfName.SUBTYPE, PdfName.CIDFONTTYPE0);
            dic.put(PdfName.BASEFONT, new PdfName(subsetPrefix + fontName + "-" + encoding));
        } else {
            dic.put(PdfName.SUBTYPE, PdfName.CIDFONTTYPE2);
            dic.put(PdfName.BASEFONT, new PdfName(subsetPrefix + fontName));
        }
        dic.put(PdfName.FONTDESCRIPTOR, fontDescriptor);
        if (!cff) {
            dic.put(PdfName.CIDTOGIDMAP, PdfName.IDENTITY);
        }
        PdfDictionary cdic = new PdfDictionary();
        cdic.put(PdfName.REGISTRY, new PdfString("Adobe"));
        cdic.put(PdfName.ORDERING, new PdfString("Identity"));
        cdic.put(PdfName.SUPPLEMENT, new PdfNumber(0));
        dic.put(PdfName.CIDSYSTEMINFO, cdic);
        if (!vertical) {
            dic.put(PdfName.DW, new PdfNumber(1000));
            StringBuilder buf = new StringBuilder("[");
            int lastNumber = -10;
            boolean firstTime = true;
            for (int[] metric : metrics) {
                if (metric[1] == 1000) {
                    continue;
                }
                int m = metric[0];
                if (m == lastNumber + 1) {
                    buf.append(' ').append(metric[1]);
                } else {
                    if (!firstTime) {
                        buf.append(']');
                    }
                    firstTime = false;
                    buf.append(m).append('[').append(metric[1]);
                }
                lastNumber = m;
            }
            if (buf.length() > 1) {
                buf.append("]]");
                dic.put(PdfName.W, new PdfLiteral(buf.toString()));
            }
        }
        return dic;
    }

    /**
     * Generates the font dictionary.
     *
     * @param descendant   the descendant dictionary
     * @param subsetPrefix the subset prefix
     * @param toUnicode    the ToUnicode stream
     * @return the stream
     */
    private PdfDictionary getFontBaseType(PdfIndirectReference descendant, String subsetPrefix,
            PdfIndirectReference toUnicode) {
        PdfDictionary dic = new PdfDictionary(PdfName.FONT);

        dic.put(PdfName.SUBTYPE, PdfName.TYPE0);
        // The PDF Reference manual advises to add -encoding to CID font names
        if (cff) {
            dic.put(PdfName.BASEFONT, new PdfName(subsetPrefix + fontName + "-" + encoding));
        } else {
            dic.put(PdfName.BASEFONT, new PdfName(subsetPrefix + fontName));
        }
        dic.put(PdfName.ENCODING, new PdfName(encoding));
        dic.put(PdfName.DESCENDANTFONTS, new PdfArray(descendant));
        if (toUnicode != null) {
            dic.put(PdfName.TOUNICODE, toUnicode);
        }
        return dic;
    }

    /**
     * The method used to sort the metrics array.
     *
     * @param o1 the first element
     * @param o2 the second element
     * @return the comparison
     */
    public int compare(int[] o1, int[] o2) {
        int m1 = (o1)[0];
        int m2 = (o2)[0];
        return Integer.compare(m1, m2);
    }

    /**
     * Outputs to the writer the font dictionaries and streams.
     *
     * @param writer the writer for this document
     * @param ref    the font indirect reference
     * @param params several parameters that depend on the font type
     * @throws IOException       on error
     * @throws DocumentException error in generating the object
     */
    @Override
    @SuppressWarnings("unchecked")
    void writeFont(PdfWriter writer, PdfIndirectReference ref, Object[] params) throws DocumentException, IOException {
        HashMap<Integer, int[]> longTag = (HashMap<Integer, int[]>) params[0];
        Map<Integer, int[]> fillerCmap = (Map<Integer, int[]>) params[2];
        addRangeUni(longTag, true, subset);
        int[][] metrics = longTag.values().toArray(new int[0][]);
        Arrays.sort(metrics, this);
        PdfIndirectReference indFont;
        PdfObject pobj;
        PdfIndirectObject obj;
        PdfIndirectReference cidset = null;
        if (includeCidSet || writer.getPDFXConformance() == PdfWriter.PDFA1A
                || writer.getPDFXConformance() == PdfWriter.PDFA1B) {
            PdfStream stream;
            if (metrics.length == 0) {
                stream = new PdfStream(new byte[]{(byte) 0x80});
            } else {
                int top = metrics[metrics.length - 1][0];
                byte[] bt = new byte[top / 8 + 1];
                for (int[] metric : metrics) {
                    int v = metric[0];
                    bt[v / 8] |= rotbits[v % 8];
                }
                stream = new PdfStream(bt);
                stream.flateCompress(compressionLevel);
            }
            cidset = writer.addToBody(stream).getIndirectReference();
        }
        // sivan: cff
        if (cff) {
            byte[] b = readCffFont();
            if (subset || subsetRanges != null) {
                CFFFontSubset cff = new CFFFontSubset(new RandomAccessFileOrArray(b), longTag);
                b = cff.Process(cff.getNames()[0]);
            }
            pobj = new StreamFont(b, "CIDFontType0C", compressionLevel);
            obj = writer.addToBody(pobj);
            indFont = obj.getIndirectReference();
        } else {
            byte[] b;
            if (subset || directoryOffset != 0) {
                TrueTypeFontSubSet sb = new TrueTypeFontSubSet(fileName, new RandomAccessFileOrArray(rf), longTag,
                        directoryOffset, false, false);
                b = sb.process();
            } else {
                b = getFullFont();
            }
            int[] lengths = new int[]{b.length};
            pobj = new StreamFont(b, lengths, compressionLevel);
            obj = writer.addToBody(pobj);
            indFont = obj.getIndirectReference();
        }
        String subsetPrefix = "";
        if (subset) {
            subsetPrefix = createSubsetPrefix();
        }
        PdfDictionary dic = getFontDescriptor(indFont, subsetPrefix, cidset);
        obj = writer.addToBody(dic);
        indFont = obj.getIndirectReference();

        pobj = getCIDFontType2(indFont, subsetPrefix, metrics);
        obj = writer.addToBody(pobj);
        indFont = obj.getIndirectReference();

        pobj = getToUnicode(mergeMetricsAndFillerCmap(metrics, fillerCmap));
        PdfIndirectReference toUnicodeRef = null;

        if (pobj != null) {
            obj = writer.addToBody(pobj);
            toUnicodeRef = obj.getIndirectReference();
        }

        pobj = getFontBaseType(indFont, subsetPrefix, toUnicodeRef);
        writer.addToBody(pobj, ref);
    }

    public int[][] mergeMetricsAndFillerCmap(int[][] metric, Map<Integer, int[]> fillerCmap) {
        Map<Integer, int[]> result = new HashMap<>();
        for (int[] ints : metric) {
            result.put(ints[0], ints);
        }

        for (Map.Entry<Integer, int[]> entry : fillerCmap.entrySet()) {
            int[] row = entry.getValue();
            result.put(entry.getKey(), new int[]{row[0], 0, row[1]});
        }

        return result.values().toArray(new int[0][]);
    }

    /**
     * Returns a PdfStream object with the full font program.
     *
     * @return a PdfStream with the font program
     * @since 2.1.3
     */
    @Override
    public PdfStream getFullFontStream() throws IOException, DocumentException {
        if (cff) {
            return new StreamFont(readCffFont(), "CIDFontType0C", compressionLevel);
        }
        return super.getFullFontStream();
    }

    /**
     * A forbidden operation. Will throw a null pointer exception.
     *
     * @param text the text
     * @return always <CODE>null</CODE>
     */
    @Override
    byte[] convertToBytes(String text) {
        return null;
    }

    @Override
    byte[] convertToBytes(int char1) {
        return null;
    }

    /**
     * Gets the glyph index and metrics for a character.
     *
     * @param c the character
     * @return an <CODE>int</CODE> array with {glyph index, width}
     */
    @Override
    public int[] getMetricsTT(int c) {
        if (cmapExt != null) {
            return cmapExt.get(c);
        }
        Map<Integer, int[]> map;
        if (fontSpecific) {
            map = cmap10;
        } else {
            map = cmap31;
        }
        if (map == null) {
            return null;
        }
        if (fontSpecific) {
            if ((c & 0xffffff00) == 0 || (c & 0xffffff00) == 0xf000) {
                return map.get(c & 0xff);
            } else {
                return null;
            }
        } else {
            return map.get(c);
        }
    }

    /**
     * Checks if a character exists in this font.
     *
     * @param c the character to check
     * @return <CODE>true</CODE> if the character has a glyph,
     * <CODE>false</CODE> otherwise
     */
    @Override
    public boolean charExists(int c) {
        return getMetricsTT(c) != null;
    }

    /**
     * Sets the character advance.
     *
     * @param c       the character
     * @param advance the character advance normalized to 1000 units
     * @return <CODE>true</CODE> if the advance was set,
     * <CODE>false</CODE> otherwise
     */
    @Override
    public boolean setCharAdvance(int c, int advance) {
        int[] m = getMetricsTT(c);
        if (m == null) {
            return false;
        }
        m[1] = advance;
        return true;
    }

    @Override
    public int[] getCharBBox(int c) {
        if (bboxes == null) {
            return null;
        }
        int[] m = getMetricsTT(c);
        if (m == null) {
            return null;
        }
        return bboxes[m[0]];
    }

}
