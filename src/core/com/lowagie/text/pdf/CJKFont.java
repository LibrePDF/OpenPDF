/*
 * $Id: CJKFont.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2000, 2001, 2002 by Paulo Soares.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.DocumentException;

/**
 * Creates a CJK font compatible with the fonts in the Adobe Asian font Pack.
 *
 * @author  Paulo Soares (psoares@consiste.pt)
 */

class CJKFont extends BaseFont {
    /** The encoding used in the PDF document for CJK fonts
     */
    static final String CJK_ENCODING = "UnicodeBigUnmarked";
    private static final int FIRST = 0;
    private static final int BRACKET = 1;
    private static final int SERIAL = 2;
    private static final int V1Y = 880;
        
    static Properties cjkFonts = new Properties();
    static Properties cjkEncodings = new Properties();
    static Hashtable allCMaps = new Hashtable();
    static Hashtable allFonts = new Hashtable();
    private static boolean propertiesLoaded = false;
    
    /** The font name */
    private String fontName;
    /** The style modifier */
    private String style = "";
    /** The CMap name associated with this font */
    private String CMap;
    
    private boolean cidDirect = false;
    
    private char[] translationMap;
    private IntHashtable vMetrics;
    private IntHashtable hMetrics;
    private HashMap fontDesc;
    private boolean vertical = false;
    
    private static void loadProperties() {
        if (propertiesLoaded)
            return;
        synchronized (allFonts) {
            if (propertiesLoaded)
                return;
            try {
                InputStream is = getResourceStream(RESOURCE_PATH + "cjkfonts.properties");
                cjkFonts.load(is);
                is.close();
                is = getResourceStream(RESOURCE_PATH + "cjkencodings.properties");
                cjkEncodings.load(is);
                is.close();
            }
            catch (Exception e) {
                cjkFonts = new Properties();
                cjkEncodings = new Properties();
            }
            propertiesLoaded = true;
        }
    }
    
    /** Creates a CJK font.
     * @param fontName the name of the font
     * @param enc the encoding of the font
     * @param emb always <CODE>false</CODE>. CJK font and not embedded
     * @throws DocumentException on error
     */
    CJKFont(String fontName, String enc, boolean emb) throws DocumentException {
        loadProperties();
        fontType = FONT_TYPE_CJK;
        String nameBase = getBaseName(fontName);
        if (!isCJKFont(nameBase, enc))
            throw new DocumentException(MessageLocalization.getComposedMessage("font.1.with.2.encoding.is.not.a.cjk.font", fontName, enc));
        if (nameBase.length() < fontName.length()) {
            style = fontName.substring(nameBase.length());
            fontName = nameBase;
        }
        this.fontName = fontName;
        encoding = CJK_ENCODING;
        vertical = enc.endsWith("V");
        CMap = enc;
        if (enc.startsWith("Identity-")) {
            cidDirect = true;
            String s = cjkFonts.getProperty(fontName);
            s = s.substring(0, s.indexOf('_'));
            char c[] = (char[])allCMaps.get(s);
            if (c == null) {
                c = readCMap(s);
                if (c == null)
                    throw new DocumentException(MessageLocalization.getComposedMessage("the.cmap.1.does.not.exist.as.a.resource", s));
                c[CID_NEWLINE] = '\n';
                allCMaps.put(s, c);
            }
            translationMap = c;
        }
        else {
            char c[] = (char[])allCMaps.get(enc);
            if (c == null) {
                String s = cjkEncodings.getProperty(enc);
                if (s == null)
                    throw new DocumentException(MessageLocalization.getComposedMessage("the.resource.cjkencodings.properties.does.not.contain.the.encoding.1", enc));
                StringTokenizer tk = new StringTokenizer(s);
                String nt = tk.nextToken();
                c = (char[])allCMaps.get(nt);
                if (c == null) {
                    c = readCMap(nt);
                    allCMaps.put(nt, c);
                }
                if (tk.hasMoreTokens()) {
                    String nt2 = tk.nextToken();
                    char m2[] = readCMap(nt2);
                    for (int k = 0; k < 0x10000; ++k) {
                        if (m2[k] == 0)
                            m2[k] = c[k];
                    }
                    allCMaps.put(enc, m2);
                    c = m2;
                }
            }
            translationMap = c;
        }
        fontDesc = (HashMap)allFonts.get(fontName);
        if (fontDesc == null) {
            fontDesc = readFontProperties(fontName);
            allFonts.put(fontName, fontDesc);
        }
        hMetrics = (IntHashtable)fontDesc.get("W");
        vMetrics = (IntHashtable)fontDesc.get("W2");
    }
    
    /** Checks if its a valid CJK font.
     * @param fontName the font name
     * @param enc the encoding
     * @return <CODE>true</CODE> if it is CJK font
     */
    public static boolean isCJKFont(String fontName, String enc) {
        loadProperties();
        String encodings = cjkFonts.getProperty(fontName);
        return (encodings != null && (enc.equals("Identity-H") || enc.equals("Identity-V") || encodings.indexOf("_" + enc + "_") >= 0));
    }
        
    /**
     * Gets the width of a <CODE>char</CODE> in normalized 1000 units.
     * @param char1 the unicode <CODE>char</CODE> to get the width of
     * @return the width in normalized 1000 units
     */
    public int getWidth(int char1) {
        int c = char1;
        if (!cidDirect)
            c = translationMap[c];
        int v;
        if (vertical)
            v = vMetrics.get(c);
        else
            v = hMetrics.get(c);
        if (v > 0)
            return v;
        else
            return 1000;
    }
    
    public int getWidth(String text) {
        int total = 0;
        for (int k = 0; k < text.length(); ++k) {
            int c = text.charAt(k);
            if (!cidDirect)
                c = translationMap[c];
            int v;
            if (vertical)
                v = vMetrics.get(c);
            else
                v = hMetrics.get(c);
            if (v > 0)
                total += v;
            else
                total += 1000;
        }
        return total;
    }
    
    int getRawWidth(int c, String name) {
        return 0;
    }
  
    public int getKerning(int char1, int char2) {
        return 0;
    }

    private PdfDictionary getFontDescriptor() {
        PdfDictionary dic = new PdfDictionary(PdfName.FONTDESCRIPTOR);
        dic.put(PdfName.ASCENT, new PdfLiteral((String)fontDesc.get("Ascent")));
        dic.put(PdfName.CAPHEIGHT, new PdfLiteral((String)fontDesc.get("CapHeight")));
        dic.put(PdfName.DESCENT, new PdfLiteral((String)fontDesc.get("Descent")));
        dic.put(PdfName.FLAGS, new PdfLiteral((String)fontDesc.get("Flags")));
        dic.put(PdfName.FONTBBOX, new PdfLiteral((String)fontDesc.get("FontBBox")));
        dic.put(PdfName.FONTNAME, new PdfName(fontName + style));
        dic.put(PdfName.ITALICANGLE, new PdfLiteral((String)fontDesc.get("ItalicAngle")));
        dic.put(PdfName.STEMV, new PdfLiteral((String)fontDesc.get("StemV")));
        PdfDictionary pdic = new PdfDictionary();
        pdic.put(PdfName.PANOSE, new PdfString((String)fontDesc.get("Panose"), null));
        dic.put(PdfName.STYLE, pdic);
        return dic;
    }
    
    private PdfDictionary getCIDFont(PdfIndirectReference fontDescriptor, IntHashtable cjkTag) {
        PdfDictionary dic = new PdfDictionary(PdfName.FONT);
        dic.put(PdfName.SUBTYPE, PdfName.CIDFONTTYPE0);
        dic.put(PdfName.BASEFONT, new PdfName(fontName + style));
        dic.put(PdfName.FONTDESCRIPTOR, fontDescriptor);
        int keys[] = cjkTag.toOrderedKeys();
        String w = convertToHCIDMetrics(keys, hMetrics);
        if (w != null)
            dic.put(PdfName.W, new PdfLiteral(w));
        if (vertical) {
            w = convertToVCIDMetrics(keys, vMetrics, hMetrics);
            if (w != null)
                dic.put(PdfName.W2, new PdfLiteral(w));
        }
        else
            dic.put(PdfName.DW, new PdfNumber(1000));
        PdfDictionary cdic = new PdfDictionary();
        cdic.put(PdfName.REGISTRY, new PdfString((String)fontDesc.get("Registry"), null));
        cdic.put(PdfName.ORDERING, new PdfString((String)fontDesc.get("Ordering"), null));
        cdic.put(PdfName.SUPPLEMENT, new PdfLiteral((String)fontDesc.get("Supplement")));
        dic.put(PdfName.CIDSYSTEMINFO, cdic);
        return dic;
    }
    
    private PdfDictionary getFontBaseType(PdfIndirectReference CIDFont) {
        PdfDictionary dic = new PdfDictionary(PdfName.FONT);
        dic.put(PdfName.SUBTYPE, PdfName.TYPE0);
        String name = fontName;
        if (style.length() > 0)
            name += "-" + style.substring(1);
        name += "-" + CMap;
        dic.put(PdfName.BASEFONT, new PdfName(name));
        dic.put(PdfName.ENCODING, new PdfName(CMap));
        dic.put(PdfName.DESCENDANTFONTS, new PdfArray(CIDFont));
        return dic;
    }
    
    void writeFont(PdfWriter writer, PdfIndirectReference ref, Object params[]) throws DocumentException, IOException {
        IntHashtable cjkTag = (IntHashtable)params[0];
        PdfIndirectReference ind_font = null;
        PdfObject pobj = null;
        PdfIndirectObject obj = null;
        pobj = getFontDescriptor();
        if (pobj != null){
            obj = writer.addToBody(pobj);
            ind_font = obj.getIndirectReference();
        }
        pobj = getCIDFont(ind_font, cjkTag);
        if (pobj != null){
            obj = writer.addToBody(pobj);
            ind_font = obj.getIndirectReference();
        }
        pobj = getFontBaseType(ind_font);
        writer.addToBody(pobj, ref);
    }

    /**
     * You can't get the FontStream of a CJK font (CJK fonts are never embedded),
     * so this method always returns null.
   	 * @return	null
     * @since	2.1.3
     */
    public PdfStream getFullFontStream() {
    	return null;
    }
    
    private float getDescNumber(String name) {
        return Integer.parseInt((String)fontDesc.get(name));
    }
    
    private float getBBox(int idx) {
        String s = (String)fontDesc.get("FontBBox");
        StringTokenizer tk = new StringTokenizer(s, " []\r\n\t\f");
        String ret = tk.nextToken();
        for (int k = 0; k < idx; ++k)
            ret = tk.nextToken();
        return Integer.parseInt(ret);
    }
    
    /** Gets the font parameter identified by <CODE>key</CODE>. Valid values
     * for <CODE>key</CODE> are <CODE>ASCENT</CODE>, <CODE>CAPHEIGHT</CODE>, <CODE>DESCENT</CODE>
     * and <CODE>ITALICANGLE</CODE>.
     * @param key the parameter to be extracted
     * @param fontSize the font size in points
     * @return the parameter in points
     */
    public float getFontDescriptor(int key, float fontSize) {
        switch (key) {
            case AWT_ASCENT:
            case ASCENT:
                return getDescNumber("Ascent") * fontSize / 1000;
            case CAPHEIGHT:
                return getDescNumber("CapHeight") * fontSize / 1000;
            case AWT_DESCENT:
            case DESCENT:
                return getDescNumber("Descent") * fontSize / 1000;
            case ITALICANGLE:
                return getDescNumber("ItalicAngle");
            case BBOXLLX:
                return fontSize * getBBox(0) / 1000;
            case BBOXLLY:
                return fontSize * getBBox(1) / 1000;
            case BBOXURX:
                return fontSize * getBBox(2) / 1000;
            case BBOXURY:
                return fontSize * getBBox(3) / 1000;
            case AWT_LEADING:
                return 0;
            case AWT_MAXADVANCE:
                return fontSize * (getBBox(2) - getBBox(0)) / 1000;
        }
        return 0;
    }
    
    public String getPostscriptFontName() {
        return fontName;
    }
    
    /** Gets the full name of the font. If it is a True Type font
     * each array element will have {Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.<br>
     * For the other fonts the array has a single element with {"", "", "",
     * font name}.
     * @return the full name of the font
     */
    public String[][] getFullFontName() {
        return new String[][]{{"", "", "", fontName}};
    }
    
    /** Gets all the entries of the names-table. If it is a True Type font
     * each array element will have {Name ID, Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.<br>
     * For the other fonts the array has a single element with {"4", "", "", "",
     * font name}.
     * @return the full name of the font
     */
    public String[][] getAllNameEntries() {
        return new String[][]{{"4", "", "", "", fontName}};
    }
    
    /** Gets the family name of the font. If it is a True Type font
     * each array element will have {Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.<br>
     * For the other fonts the array has a single element with {"", "", "",
     * font name}.
     * @return the family name of the font
     */
    public String[][] getFamilyFontName() {
        return getFullFontName();
    }
    
    static char[] readCMap(String name) {
        try {
            name = name + ".cmap";
            InputStream is = getResourceStream(RESOURCE_PATH + name);
            char c[] = new char[0x10000];
            for (int k = 0; k < 0x10000; ++k)
                c[k] = (char)((is.read() << 8) + is.read());
            is.close();
            return c;
        }
        catch (Exception e) {
            // empty on purpose
        }
        return null;
    }
    
    static IntHashtable createMetric(String s) {
        IntHashtable h = new IntHashtable();
        StringTokenizer tk = new StringTokenizer(s);
        while (tk.hasMoreTokens()) {
            int n1 = Integer.parseInt(tk.nextToken());
            h.put(n1, Integer.parseInt(tk.nextToken()));
        }
        return h;
    }
    
    static String convertToHCIDMetrics(int keys[], IntHashtable h) {
        if (keys.length == 0)
            return null;
        int lastCid = 0;
        int lastValue = 0;
        int start;
        for (start = 0; start < keys.length; ++start) {
            lastCid = keys[start];
            lastValue = h.get(lastCid);
            if (lastValue != 0) {
                ++start;
                break;
            }
        }
        if (lastValue == 0)
            return null;
        StringBuffer buf = new StringBuffer();
        buf.append('[');
        buf.append(lastCid);
        int state = FIRST;
        for (int k = start; k < keys.length; ++k) {
            int cid = keys[k];
            int value = h.get(cid);
            if (value == 0)
                continue;
            switch (state) {
                case FIRST: {
                    if (cid == lastCid + 1 && value == lastValue) {
                        state = SERIAL;
                    }
                    else if (cid == lastCid + 1) {
                        state = BRACKET;
                        buf.append('[').append(lastValue);
                    }
                    else {
                        buf.append('[').append(lastValue).append(']').append(cid);
                    }
                    break;
                }
                case BRACKET: {
                    if (cid == lastCid + 1 && value == lastValue) {
                        state = SERIAL;
                        buf.append(']').append(lastCid);
                    }
                    else if (cid == lastCid + 1) {
                        buf.append(' ').append(lastValue);
                    }
                    else {
                        state = FIRST;
                        buf.append(' ').append(lastValue).append(']').append(cid);
                    }
                    break;
                }
                case SERIAL: {
                    if (cid != lastCid + 1 || value != lastValue) {
                        buf.append(' ').append(lastCid).append(' ').append(lastValue).append(' ').append(cid);
                        state = FIRST;
                    }
                    break;
                }
            }
            lastValue = value;
            lastCid = cid;
        }
        switch (state) {
            case FIRST: {
                buf.append('[').append(lastValue).append("]]");
                break;
            }
            case BRACKET: {
                buf.append(' ').append(lastValue).append("]]");
                break;
            }
            case SERIAL: {
                buf.append(' ').append(lastCid).append(' ').append(lastValue).append(']');
                break;
            }
        }
        return buf.toString();
    }
    
    static String convertToVCIDMetrics(int keys[], IntHashtable v, IntHashtable h) {
        if (keys.length == 0)
            return null;
        int lastCid = 0;
        int lastValue = 0;
        int lastHValue = 0;
        int start;
        for (start = 0; start < keys.length; ++start) {
            lastCid = keys[start];
            lastValue = v.get(lastCid);
            if (lastValue != 0) {
                ++start;
                break;
            }
            else
                lastHValue = h.get(lastCid);
        }
        if (lastValue == 0)
            return null;
        if (lastHValue == 0)
            lastHValue = 1000;
        StringBuffer buf = new StringBuffer();
        buf.append('[');
        buf.append(lastCid);
        int state = FIRST;
        for (int k = start; k < keys.length; ++k) {
            int cid = keys[k];
            int value = v.get(cid);
            if (value == 0)
                continue;
            int hValue = h.get(lastCid);
            if (hValue == 0)
                hValue = 1000;
            switch (state) {
                case FIRST: {
                    if (cid == lastCid + 1 && value == lastValue && hValue == lastHValue) {
                        state = SERIAL;
                    }
                    else {
                        buf.append(' ').append(lastCid).append(' ').append(-lastValue).append(' ').append(lastHValue / 2).append(' ').append(V1Y).append(' ').append(cid);
                    }
                    break;
                }
                case SERIAL: {
                    if (cid != lastCid + 1 || value != lastValue || hValue != lastHValue) {
                        buf.append(' ').append(lastCid).append(' ').append(-lastValue).append(' ').append(lastHValue / 2).append(' ').append(V1Y).append(' ').append(cid);
                        state = FIRST;
                    }
                    break;
                }
            }
            lastValue = value;
            lastCid = cid;
            lastHValue = hValue;
        }
        buf.append(' ').append(lastCid).append(' ').append(-lastValue).append(' ').append(lastHValue / 2).append(' ').append(V1Y).append(" ]");
        return buf.toString();
    }
    
    static HashMap readFontProperties(String name) {
        try {
            name += ".properties";
            InputStream is = getResourceStream(RESOURCE_PATH + name);
            Properties p = new Properties();
            p.load(is);
            is.close();
            IntHashtable W = createMetric(p.getProperty("W"));
            p.remove("W");
            IntHashtable W2 = createMetric(p.getProperty("W2"));
            p.remove("W2");
            HashMap map = new HashMap();
            for (Enumeration e = p.keys(); e.hasMoreElements();) {
                Object obj = e.nextElement();
                map.put(obj, p.getProperty((String)obj));
            }
            map.put("W", W);
            map.put("W2", W2);
            return map;
        }
        catch (Exception e) {
            // empty on purpose
        }
        return null;
    }

    public int getUnicodeEquivalent(int c) {
        if (cidDirect)
            return translationMap[c];
        return c;
    }
    
    public int getCidCode(int c) {
        if (cidDirect)
            return c;
        return translationMap[c];
    }
    
    /** Checks if the font has any kerning pairs.
     * @return always <CODE>false</CODE>
     */    
    public boolean hasKernPairs() {
        return false;
    }
    
    /**
     * Checks if a character exists in this font.
     * @param c the character to check
     * @return <CODE>true</CODE> if the character has a glyph,
     * <CODE>false</CODE> otherwise
     */
    public boolean charExists(int c) {
        return translationMap[c] != 0;
    }
    
    /**
     * Sets the character advance.
     * @param c the character
     * @param advance the character advance normalized to 1000 units
     * @return <CODE>true</CODE> if the advance was set,
     * <CODE>false</CODE> otherwise. Will always return <CODE>false</CODE>
     */
    public boolean setCharAdvance(int c, int advance) {
        return false;
    }
    
    /**
     * Sets the font name that will appear in the pdf font dictionary.
     * Use with care as it can easily make a font unreadable if not embedded.
     * @param name the new font name
     */    
    public void setPostscriptFontName(String name) {
        fontName = name;
    }   
    
    public boolean setKerning(int char1, int char2, int kern) {
        return false;
    }
    
    public int[] getCharBBox(int c) {
        return null;
    }
    
    protected int[] getRawCharBBox(int c, String name) {
        return null;
    }
}
