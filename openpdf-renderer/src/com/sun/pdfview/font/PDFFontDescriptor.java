/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.sun.pdfview.font;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParser;

/**
 *
 * @author  jkaplan
 */
public class PDFFontDescriptor {

    /** All glyphs have the same width. */
    public final static int FIXED_PITCH = 1 << (1-1);
    /** Glyphs have serifs. */
    public final static int SERIF = 1 << (2-1);
    /** Font contains glyphs outside the Adobe standard Latin. */
    public final static int SYMBOLIC = 1 << (3-1);
    /** Glyphs resemble cursive handwriting. */
    public final static int SCRIPT = 1 << (4-1);
    /** Font uses the Adobe standard Latic character set. */
    public final static int NONSYMBOLIC = 1 << (6-1);
    /** Glyphs have dominant vertical strokes that are slanted. */
    public final static int ITALIC = 1 << (7-1);
    /** Font contains no lowercase letters. */
    public final static int ALLCAP = 1 << (17-1);
    /** Font contains both uppercase and lowercase letters.. */
    public final static int SMALLCAP = 1 << (18-1);
    /** Determines whether bold glyphs shall be painted with
     * extra pixels even at very small text sizes. */
    public final static int FORCEBOLD = 1 << (19-1);
    /** Holds value of property ascent. */
    private int ascent;
    /** Holds value of property capHeight. */
    private int capHeight;
    /** Holds value of property descent. */
    private int descent;
    /** Holds value of property flags. */
    private int flags;
    /** Holds the optional FontFamily (PDF 1.5) */
    private String fontFamily;
    /** Holds value of property fontName. */
    private String fontName;
    /** Holds the optional FontStretch (PDF 1.5) */
    private String fontStretch;
    /** Holds the optional FontWeight (PDF 1.5) */
    private int fontWeight;
    /** Holds value of property italicAngle. */
    private int italicAngle = 0;
    /** Holds value of property stemV. */
    private int stemV;
    /** Holds value of property avgWidth. */
    private int avgWidth = 0;
    /** Holds value of property fontFile. */
    private PDFObject fontFile;
    /** Holds value of property fontFile2. */
    private PDFObject fontFile2;
    /** Holds value of property fontFile3. */
    private PDFObject fontFile3;
    /** Holds value of property leading. */
    private int leading = 0;
    /** Holds value of property maxWidth. */
    private int maxWidth = 0;
    /** Holds value of property misingWidth. */
    private int missingWidth = 0;
    /** Holds value of property stemH. */
    private int stemH = 0;
    /** Holds value of property xHeight. */
    private int xHeight = 0;
    /** Holds value of property charSet. */
    private PDFObject charSet;
    /** Holds value of property fontBBox. */
    private Rectangle2D.Float fontBBox;

    /** Creates a new instance of PDFFontDescriptor */
    public PDFFontDescriptor(String basefont) {
        setFontName(basefont);
    // [[MW TODO: find basefont info and fill in the rest of the
    // descriptor?]]
    }

    /** Creates a new instance of PDFFontDescriptor */
    public PDFFontDescriptor(PDFObject obj, String fontSubType) throws IOException {
        // required parameters
        setFlags(obj.getDictRef("Flags").getIntValue());
        PDFObject fontNameObj = obj.getDictRef("FontName");
        if (fontNameObj == null){
        	// fallback to avoid NPE try to use the BaseFont
        	fontNameObj = obj.getDictRef("BaseFont");
        }
        setFontName(fontNameObj.getStringValue());
        setItalicAngle(obj.getDictRef("ItalicAngle").getIntValue());
        
        // conditionally required parameters
        boolean areConditionalParametersRequired = !"Type3".equals(fontSubType)
                && !Boolean.getBoolean("PDFRenderer.lenientFontDescriptorParsing");
        
    	// these values are declared as Required except for Type 3 fonts
    	// however a value might not be available for some fonts and 
    	// therefore some predefined value is set, so that we have a fallback
        if ( obj.getDictionary().containsKey("Ascent")) {
            setAscent(obj.getDictRef("Ascent").getIntValue());
        }
        else if (areConditionalParametersRequired) {
        	setAscent(728); // value of ArialMT as used with Report Label
        }
        if ( obj.getDictionary().containsKey("CapHeight")) {
            setCapHeight(obj.getDictRef("CapHeight").getIntValue());
        }
        else if (areConditionalParametersRequired) {
        	setCapHeight(716); // value of ArialMT as used with Report Label
        }
        if ( obj.getDictionary().containsKey("Descent")) {
            setDescent(obj.getDictRef("Descent").getIntValue());
        }
        else if (areConditionalParametersRequired) {
        	setDescent(-210); // value of ArialMT as used with Report Label
        }
        if ( obj.getDictionary().containsKey("StemV")) {
            setStemV(obj.getDictRef("StemV").getIntValue());
        }
        else if (areConditionalParametersRequired) {
        	setStemV(109); // "normal" value for vertical stem width (PDFlib)
        }

        // font bounding box (non-optional but a NPE won't help)
        if (obj.getDictionary().containsKey("FontBBox")) {
            PDFObject[] bboxdef = obj.getDictRef("FontBBox").getArray();
            float[] bboxfdef = new float[4];
            for (int i = 0; i < 4; i++) {
                bboxfdef[i] = bboxdef[i].getFloatValue();
            }
            setFontBBox(new Rectangle2D.Float(bboxfdef[0], bboxfdef[1],
                    bboxfdef[2] - bboxfdef[0],
                    bboxfdef[3] - bboxfdef[1]));
        }

        // optional parameters
        if (obj.getDictionary().containsKey("AvgWidth")) {
            setAvgWidth(obj.getDictRef("AvgWidth").getIntValue());
        }
        if (obj.getDictionary().containsKey("FontFile")) {
            setFontFile(obj.getDictRef("FontFile"));
        }
        if (obj.getDictionary().containsKey("FontFile2")) {
            setFontFile2(obj.getDictRef("FontFile2"));
        }
        if (obj.getDictionary().containsKey("FontFile3")) {
            setFontFile3(obj.getDictRef("FontFile3"));
        }
        if (obj.getDictionary().containsKey("Leading")) {
            setLeading(obj.getDictRef("Leading").getIntValue());
        }
        if (obj.getDictionary().containsKey("MaxWidth")) {
            setMaxWidth(obj.getDictRef("MaxWidth").getIntValue());
        }
        if (obj.getDictionary().containsKey("MissingWidth")) {
            setMissingWidth(obj.getDictRef("MissingWidth").getIntValue());
        }
        if (obj.getDictionary().containsKey("StemH")) {
            setStemH(obj.getDictRef("StemH").getIntValue());
        }
        if (obj.getDictionary().containsKey("XHeight")) {
            setXHeight(obj.getDictRef("XHeight").getIntValue());
        }
        if (obj.getDictionary().containsKey("CharSet")) {
            setCharSet(obj.getDictRef("CharSet"));
        }
        if (obj.getDictionary().containsKey("FontFamily")) {
            setFontFamily(obj.getDictRef("FontFamily").getStringValue());
        }
        if (obj.getDictionary().containsKey("FontWeight")) {
            setFontWeight(obj.getDictRef("FontWeight").getIntValue());
        }
        if (obj.getDictionary().containsKey("FontStretch")) {
            setFontStretch(obj.getDictRef("FontStretch").getStringValue());
        }
    }

    /** Getter for property ascent.
     * @return Value of property ascent.
     *
     */
    public int getAscent() {
        return this.ascent;
    }

    /** Setter for property ascent.
     * @param ascent New value of property ascent.
     *
     */
    public void setAscent(int ascent) {
        this.ascent = ascent;
    }

    /** Getter for property capHeight.
     * @return Value of property capHeight.
     *
     */
    public int getCapHeight() {
        return this.capHeight;
    }

    /** Setter for property capHeight.
     * @param capHeight New value of property capHeight.
     *
     */
    public void setCapHeight(int capHeight) {
        this.capHeight = capHeight;
    }

    /** Getter for property descent.
     * @return Value of property descent.
     *
     */
    public int getDescent() {
        return this.descent;
    }

    /** Setter for property descent.
     * @param descent New value of property descent.
     *
     */
    public void setDescent(int descent) {
        this.descent = descent;
    }

    /** Getter for property flags.
     * @return Value of property flags.
     *
     */
    public int getFlags() {
        return this.flags;
    }

    /** Setter for property flags.
     * @param flags New value of property flags.
     *
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /** Getter for property fontFamily. Option (PDF 1.5)
     * @return Value of the property fontFamily
     */
    public String getFontFamily() {
        return this.fontFamily;
    }

    /** Setter for property fontFamily.
     * @param fontFamily New value of property fontFamily.
     *
     */
    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    /** Getter for property fontName.
     * @return Value of property fontName.
     *
     */
    public String getFontName() {
        return this.fontName;
    }

    /** Setter for property fontName.
     * @param fontName New value of property fontName.
     *
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    /** Getter for property fontStretch. Option (PDF 1.5)
     * 
     * @return Value of the property fontStretch
     */
    public String getFontStretch() {
        return this.fontStretch;
    }

    /** Setter for property fontStretch. Possible values are:
     * UltraCondensed, ExtraCondensed, Condensed, SemiCondensed,
     * Normal, SemiExpanded, Expanded, ExtraExpanded or UltraExpanded
     * We do not check at this time.
     *
     * @param fontStretch New value of property fontStretch.
     *
     */
    public void setFontStretch(String fontStretch) {
        this.fontStretch = fontStretch;
    }

    /** Getter for property fontWeight. Option (PDF 1.5)
     *
     * @return Value of the property fontWeight
     */
    public int getFontWeight() {
        return this.fontWeight;
    }

    /** Setter for property fontWeight. Possible values are:
     * 100, 200, 300, 400, 500, 600, 700, 800, 900
     * We do not check at this time.
     *
     * @param fontWeight New value of property fontWeight.
     *
     */
    public void setFontWeight(int fontWeight) {
        this.fontWeight = fontWeight;
    }

    /** Getter for property italicAngle.
     * @return Value of property italicAngle.
     *
     */
    public int getItalicAngle() {
        return this.italicAngle;
    }

    /** Setter for property italicAngle.
     * @param italicAngle New value of property italicAngle.
     *
     */
    public void setItalicAngle(int italicAngle) {
        this.italicAngle = italicAngle;
    }

    /** Getter for property stemV.
     * @return Value of property stemV.
     *
     */
    public int getStemV() {
        return this.stemV;
    }

    /** Setter for property stemV.
     * @param stemV New value of property stemV.
     *
     */
    public void setStemV(int stemV) {
        this.stemV = stemV;
    }

    /** Getter for property avgWidth.
     * @return Value of property avgWidth.
     *
     */
    public int getAvgWidth() {
        return this.avgWidth;
    }

    /** Setter for property avgWidth.
     * @param avgWidth New value of property avgWidth.
     *
     */
    public void setAvgWidth(int avgWidth) {
        this.avgWidth = avgWidth;
    }

    /** Getter for property fontFile.
     * @return Value of property fontFile.
     *
     */
    public PDFObject getFontFile() {
        return this.fontFile;
    }

    /** Setter for property fontFile.
     * @param fontFile New value of property fontFile.
     *
     */
    public void setFontFile(PDFObject fontFile) {
        this.fontFile = fontFile;
    }

    /** Getter for property fontFile2.
     * @return Value of property fontFile2.
     *
     */
    public PDFObject getFontFile2() {
        return this.fontFile2;
    }

    /** Setter for property fontFile2.
     * @param fontFile2 New value of property fontFile2.
     *
     */
    public void setFontFile2(PDFObject fontFile2) {
        this.fontFile2 = fontFile2;
    }

    /** Getter for property fontFile3.
     * @return Value of property fontFile3.
     *
     */
    public PDFObject getFontFile3() {
        return this.fontFile3;
    }

    /** Setter for property fontFile3.
     * @param fontFile3 New value of property fontFile3.
     *
     */
    public void setFontFile3(PDFObject fontFile3) {
        this.fontFile3 = fontFile3;
    }

    /** Getter for property leading.
     * @return Value of property leading.
     *
     */
    public int getLeading() {
        return this.leading;
    }

    /** Setter for property leading.
     * @param leading New value of property leading.
     *
     */
    public void setLeading(int leading) {
        this.leading = leading;
    }

    /** Getter for property maxWidth.
     * @return Value of property maxWidth.
     *
     */
    public int getMaxWidth() {
        return this.maxWidth;
    }

    /** Setter for property maxWidth.
     * @param maxWidth New value of property maxWidth.
     *
     */
    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    /** Getter for property misingWidth.
     * @return Value of property misingWidth.
     *
     */
    public int getMissingWidth() {
        return this.missingWidth;
    }

    /** Setter for property misingWidth.
     * @param missingWidth New value of property misingWidth.
     */
    public void setMissingWidth(int missingWidth) {
        this.missingWidth = missingWidth;
    }

    /** Getter for property stemH.
     * @return Value of property stemH.
     *
     */
    public int getStemH() {
        return this.stemH;
    }

    /** Setter for property stemH.
     * @param stemH New value of property stemH.
     *
     */
    public void setStemH(int stemH) {
        this.stemH = stemH;
    }

    /** Getter for property xHeight.
     * @return Value of property xHeight.
     *
     */
    public int getXHeight() {
        return this.xHeight;
    }

    /** Setter for property xHeight.
     * @param xHeight New value of property xHeight.
     *
     */
    public void setXHeight(int xHeight) {
        this.xHeight = xHeight;
    }

    /** Getter for property charSet.
     * @return Value of property charSet.
     *
     */
    public PDFObject getCharSet() {
        return this.charSet;
    }

    /** Setter for property charSet.
     * @param charSet New value of property charSet.
     *
     */
    public void setCharSet(PDFObject charSet) {
        this.charSet = charSet;
    }

    /** Getter for property fontBBox.
     * @return Value of property fontBBox.
     *
     */
    public Rectangle2D.Float getFontBBox() {
        return this.fontBBox;
    }

    /** Setter for property fontBBox.
     * @param fontBBox New value of property fontBBox.
     *
     */
    public void setFontBBox(Rectangle2D.Float fontBBox) {
        this.fontBBox = fontBBox;
    }
}
