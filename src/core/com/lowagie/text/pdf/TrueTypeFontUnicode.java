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
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.pdf;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Utilities;

/** Represents a True Type font with Unicode encoding. All the character
 * in the font can be used directly by using the encoding Identity-H or
 * Identity-V. This is the only way to represent some character sets such
 * as Thai.
 * @author  Paulo Soares (psoares@consiste.pt)
 */
class TrueTypeFontUnicode extends TrueTypeFont implements Comparator{
    
    /**
     * <CODE>true</CODE> if the encoding is vertical.
     */    
    boolean vertical = false;
    
    /**
     * Creates a new TrueType font addressed by Unicode characters. The font
     * will always be embedded.
     * @param ttFile the location of the font on file. The file must end in '.ttf'.
     * The modifiers after the name are ignored.
     * @param enc the encoding to be applied to this font
     * @param emb true if the font is to be embedded in the PDF
     * @param ttfAfm the font as a <CODE>byte</CODE> array
     * @throws DocumentException the font is invalid
     * @throws IOException the font file could not be read
     */
    TrueTypeFontUnicode(String ttFile, String enc, boolean emb, byte ttfAfm[], boolean forceRead) throws DocumentException, IOException {
        String nameBase = getBaseName(ttFile);
        String ttcName = getTTCName(nameBase);
        if (nameBase.length() < ttFile.length()) {
            style = ttFile.substring(nameBase.length());
        }
        encoding = enc;
        embedded = emb;
        fileName = ttcName;
        ttcIndex = "";
        if (ttcName.length() < nameBase.length())
            ttcIndex = nameBase.substring(ttcName.length() + 1);
        fontType = FONT_TYPE_TTUNI;
        if ((fileName.toLowerCase().endsWith(".ttf") || fileName.toLowerCase().endsWith(".otf") || fileName.toLowerCase().endsWith(".ttc")) && ((enc.equals(IDENTITY_H) || enc.equals(IDENTITY_V)) && emb)) {
            process(ttfAfm, forceRead);
            if (os_2.fsType == 2)
                throw new DocumentException(MessageLocalization.getComposedMessage("1.cannot.be.embedded.due.to.licensing.restrictions", fileName + style));
            // Sivan
            if ((cmap31 == null && !fontSpecific) || (cmap10 == null && fontSpecific))
                directTextToByte=true;
                //throw new DocumentException(MessageLocalization.getComposedMessage("1.2.does.not.contain.an.usable.cmap", fileName, style));
            if (fontSpecific) {
                fontSpecific = false;
                String tempEncoding = encoding;
                encoding = "";
                createEncoding();
                encoding = tempEncoding;
                fontSpecific = true;
            }
        }
        else
            throw new DocumentException(MessageLocalization.getComposedMessage("1.2.is.not.a.ttf.font.file", fileName, style));
        vertical = enc.endsWith("V");
    }
    
    /**
     * Gets the width of a <CODE>char</CODE> in normalized 1000 units.
     * @param char1 the unicode <CODE>char</CODE> to get the width of
     * @return the width in normalized 1000 units
     */
    public int getWidth(int char1) {
        if (vertical)
            return 1000;
        if (fontSpecific) {
            if ((char1 & 0xff00) == 0 || (char1 & 0xff00) == 0xf000)
                return getRawWidth(char1 & 0xff, null);
            else
                return 0;
        }
        else {
            return getRawWidth(char1, encoding);
        }
    }
    
    /**
     * Gets the width of a <CODE>String</CODE> in normalized 1000 units.
     * @param text the <CODE>String</CODE> to get the width of
     * @return the width in normalized 1000 units
     */
    public int getWidth(String text) {
        if (vertical)
            return text.length() * 1000;
        int total = 0;
        if (fontSpecific) {
            char cc[] = text.toCharArray();
            int len = cc.length;
            for (int k = 0; k < len; ++k) {
                char c = cc[k];
                if ((c & 0xff00) == 0 || (c & 0xff00) == 0xf000)
                    total += getRawWidth(c & 0xff, null);
            }
        }
        else {
            int len = text.length();
            for (int k = 0; k < len; ++k) {
                if (Utilities.isSurrogatePair(text, k)) {
                    total += getRawWidth(Utilities.convertToUtf32(text, k), encoding);
                    ++k;
                }
                else
                    total += getRawWidth(text.charAt(k), encoding);
            }
        }
        return total;
    }

    /** Creates a ToUnicode CMap to allow copy and paste from Acrobat.
     * @param metrics metrics[0] contains the glyph index and metrics[2]
     * contains the Unicode code
     * @return the stream representing this CMap or <CODE>null</CODE>
     */    
    private PdfStream getToUnicode(Object metrics[]) {
        if (metrics.length == 0)
            return null;
        StringBuffer buf = new StringBuffer(
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
            int metric[] = (int[])metrics[k];
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
    
    private static String toHex4(int n) {
        String s = "0000" + Integer.toHexString(n);
        return s.substring(s.length() - 4);
    }
    
    /** Gets an hex string in the format "&lt;HHHH&gt;".
     * @param n the number
     * @return the hex string
     */    
    static String toHex(int n) {
        if (n < 0x10000)
            return "<" + toHex4(n) + ">";
        n -= 0x10000;
        int high = (n / 0x400) + 0xd800;
        int low = (n % 0x400) + 0xdc00;
        return "[<" + toHex4(high) + toHex4(low) + ">]";
    }
    
    /** Generates the CIDFontTyte2 dictionary.
     * @param fontDescriptor the indirect reference to the font descriptor
     * @param subsetPrefix the subset prefix
     * @param metrics the horizontal width metrics
     * @return a stream
     */    
    private PdfDictionary getCIDFontType2(PdfIndirectReference fontDescriptor, String subsetPrefix, Object metrics[]) {
        PdfDictionary dic = new PdfDictionary(PdfName.FONT);
        // sivan; cff
        if (cff) {
			dic.put(PdfName.SUBTYPE, PdfName.CIDFONTTYPE0);
            dic.put(PdfName.BASEFONT, new PdfName(subsetPrefix+fontName+"-"+encoding));
        }
		else {
			dic.put(PdfName.SUBTYPE, PdfName.CIDFONTTYPE2);
            dic.put(PdfName.BASEFONT, new PdfName(subsetPrefix + fontName));
        }
        dic.put(PdfName.FONTDESCRIPTOR, fontDescriptor);
        if (!cff)
          dic.put(PdfName.CIDTOGIDMAP,PdfName.IDENTITY);
        PdfDictionary cdic = new PdfDictionary();
        cdic.put(PdfName.REGISTRY, new PdfString("Adobe"));
        cdic.put(PdfName.ORDERING, new PdfString("Identity"));
        cdic.put(PdfName.SUPPLEMENT, new PdfNumber(0));
        dic.put(PdfName.CIDSYSTEMINFO, cdic);
        if (!vertical) {
            dic.put(PdfName.DW, new PdfNumber(1000));
            StringBuffer buf = new StringBuffer("[");
            int lastNumber = -10;
            boolean firstTime = true;
            for (int k = 0; k < metrics.length; ++k) {
                int metric[] = (int[])metrics[k];
                if (metric[1] == 1000)
                    continue;
                int m = metric[0];
                if (m == lastNumber + 1) {
                    buf.append(' ').append(metric[1]);
                }
                else {
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
    
    /** Generates the font dictionary.
     * @param descendant the descendant dictionary
     * @param subsetPrefix the subset prefix
     * @param toUnicode the ToUnicode stream
     * @return the stream
     */    
    private PdfDictionary getFontBaseType(PdfIndirectReference descendant, String subsetPrefix, PdfIndirectReference toUnicode) {
        PdfDictionary dic = new PdfDictionary(PdfName.FONT);

        dic.put(PdfName.SUBTYPE, PdfName.TYPE0);
        // The PDF Reference manual advises to add -encoding to CID font names
		if (cff)
		  dic.put(PdfName.BASEFONT, new PdfName(subsetPrefix+fontName+"-"+encoding));
		  //dic.put(PdfName.BASEFONT, new PdfName(subsetPrefix+fontName));
		else
		  dic.put(PdfName.BASEFONT, new PdfName(subsetPrefix + fontName));
		  //dic.put(PdfName.BASEFONT, new PdfName(fontName));
        dic.put(PdfName.ENCODING, new PdfName(encoding));
        dic.put(PdfName.DESCENDANTFONTS, new PdfArray(descendant));
        if (toUnicode != null)
            dic.put(PdfName.TOUNICODE, toUnicode);  
        return dic;
    }

    /** The method used to sort the metrics array.
     * @param o1 the first element
     * @param o2 the second element
     * @return the comparison
     */    
    public int compare(Object o1, Object o2) {
        int m1 = ((int[])o1)[0];
        int m2 = ((int[])o2)[0];
        if (m1 < m2)
            return -1;
        if (m1 == m2)
            return 0;
        return 1;
    }
    
    private static final byte[] rotbits = {(byte)0x80,(byte)0x40,(byte)0x20,(byte)0x10,(byte)0x08,(byte)0x04,(byte)0x02,(byte)0x01};
    
    /** Outputs to the writer the font dictionaries and streams.
     * @param writer the writer for this document
     * @param ref the font indirect reference
     * @param params several parameters that depend on the font type
     * @throws IOException on error
     * @throws DocumentException error in generating the object
     */
    void writeFont(PdfWriter writer, PdfIndirectReference ref, Object params[]) throws DocumentException, IOException {
        HashMap longTag = (HashMap)params[0];
        addRangeUni(longTag, true, subset);
        Object metrics[] = longTag.values().toArray();
        Arrays.sort(metrics, this);
        PdfIndirectReference ind_font = null;
        PdfObject pobj = null;
        PdfIndirectObject obj = null;
        PdfIndirectReference cidset = null;
        if (writer.getPDFXConformance() == PdfWriter.PDFA1A || writer.getPDFXConformance() == PdfWriter.PDFA1B) {
            PdfStream stream;
            if (metrics.length == 0) {
                stream = new PdfStream(new byte[]{(byte)0x80});
            }
            else {
                int top = ((int[])metrics[metrics.length - 1])[0];
                byte[] bt = new byte[top / 8 + 1];
                for (int k = 0; k < metrics.length; ++k) {
                    int v = ((int[])metrics[k])[0];
                    bt[v / 8] |= rotbits[v % 8];
                }
                stream = new PdfStream(bt);
                stream.flateCompress(compressionLevel);
            }
            cidset = writer.addToBody(stream).getIndirectReference();
        }
        // sivan: cff
        if (cff) {
			byte b[] = readCffFont();
            if (subset || subsetRanges != null) {
                CFFFontSubset cff = new CFFFontSubset(new RandomAccessFileOrArray(b),longTag);
                b = cff.Process(cff.getNames()[0]);
            }
			pobj = new StreamFont(b, "CIDFontType0C", compressionLevel);
			obj = writer.addToBody(pobj);
			ind_font = obj.getIndirectReference();
        } else {
            byte[] b;
            if (subset || directoryOffset != 0) {
                TrueTypeFontSubSet sb = new TrueTypeFontSubSet(fileName, new RandomAccessFileOrArray(rf), longTag, directoryOffset, false, false);
                b = sb.process();
            }
            else {
                b = getFullFont();
            }
            int lengths[] = new int[]{b.length};
            pobj = new StreamFont(b, lengths, compressionLevel);
            obj = writer.addToBody(pobj);
            ind_font = obj.getIndirectReference();
        }
        String subsetPrefix = "";
        if (subset)
            subsetPrefix = createSubsetPrefix();
        PdfDictionary dic = getFontDescriptor(ind_font, subsetPrefix, cidset);
        obj = writer.addToBody(dic);
        ind_font = obj.getIndirectReference();

        pobj = getCIDFontType2(ind_font, subsetPrefix, metrics);
        obj = writer.addToBody(pobj);
        ind_font = obj.getIndirectReference();

        pobj = getToUnicode(metrics);
        PdfIndirectReference toUnicodeRef = null;
        
        if (pobj != null) {
            obj = writer.addToBody(pobj);
            toUnicodeRef = obj.getIndirectReference();
        }

        pobj = getFontBaseType(ind_font, subsetPrefix, toUnicodeRef);
        writer.addToBody(pobj, ref);
    }
    
    /**
     * Returns a PdfStream object with the full font program.
     * @return	a PdfStream with the font program
     * @since	2.1.3
     */
    public PdfStream getFullFontStream() throws IOException, DocumentException {
    	if (cff) {
			return new StreamFont(readCffFont(), "CIDFontType0C", compressionLevel);
        }
    	return super.getFullFontStream();
    }
    
    /** A forbidden operation. Will throw a null pointer exception.
     * @param text the text
     * @return always <CODE>null</CODE>
     */    
    byte[] convertToBytes(String text) {
        return null;
    }

    byte[] convertToBytes(int char1) {
        return null;
    }

    /** Gets the glyph index and metrics for a character.
     * @param c the character
     * @return an <CODE>int</CODE> array with {glyph index, width}
     */    
    public int[] getMetricsTT(int c) {
        if (cmapExt != null)
            return (int[])cmapExt.get(new Integer(c));
        HashMap map = null;
        if (fontSpecific)
            map = cmap10;
        else
            map = cmap31;
        if (map == null)
            return null;
        if (fontSpecific) {
            if ((c & 0xffffff00) == 0 || (c & 0xffffff00) == 0xf000)
                return (int[])map.get(new Integer(c & 0xff));
            else
                return null;
        }
        else
            return (int[])map.get(new Integer(c));
    }
    
    /**
     * Checks if a character exists in this font.
     * @param c the character to check
     * @return <CODE>true</CODE> if the character has a glyph,
     * <CODE>false</CODE> otherwise
     */
    public boolean charExists(int c) {
        return getMetricsTT(c) != null;
    }
    
    /**
     * Sets the character advance.
     * @param c the character
     * @param advance the character advance normalized to 1000 units
     * @return <CODE>true</CODE> if the advance was set,
     * <CODE>false</CODE> otherwise
     */
    public boolean setCharAdvance(int c, int advance) {
        int[] m = getMetricsTT(c);
        if (m == null)
            return false;
        m[1] = advance;
        return true;
    }
    
    public int[] getCharBBox(int c) {
        if (bboxes == null)
            return null;
        int[] m = getMetricsTT(c);
        if (m == null)
            return null;
        return bboxes[m[0]];
    }
}
