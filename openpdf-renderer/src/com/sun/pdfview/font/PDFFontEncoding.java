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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.pdfview.PDFDebugger;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFRenderer;
import com.sun.pdfview.font.cid.PDFCMap;

/**
 * The PDFFont encoding encapsulates the mapping from character codes
 * in the PDF document to glyphs of the font.
 *
 * Encodings take two basic forms.  For Type1, TrueType, and Type3 fonts,
 * the encoding maps from character codes to Strings, which represent the
 * glyphs of the font.  For Type0 fonts, the mapping is a CMap which maps
 * character codes to characters in one of many descendant fonts.
 *
 * Note that the data in the PDF might be ASCII characters (bytes) or it might
 * be a multi-byte format such as unicode.  For now we will assume all
 * glyph ids fit into at most the two bytes of a character.
 */
public class PDFFontEncoding {

    /** Encoding types */
    private static final int TYPE_ENCODING = 0;
    private static final int TYPE_CMAP = 1;
    /** 
     * the base encoding (an array of integers which can be mapped to names
     * using the methods on FontSupport
     */
    private int[] baseEncoding;
    /** any differences from the base encoding */
    private Map<Character,String> differences;
    /**
     * a CMap for fonts encoded by CMap
     */
    private PDFCMap cmap;
    /**
     * the type of this encoding (encoding or CMap)
     */
    private int type;
	private PDFObject mapName;
    
    public PDFFontEncoding(PDFCMap cmap) {
    	super();
    	this.type = TYPE_CMAP;
    	this.cmap = cmap;
    }

    /** Creates a new instance of PDFFontEncoding */
    public PDFFontEncoding(String fontType, PDFObject encoding)
            throws IOException {
        if (encoding.getType() == PDFObject.NAME) {
            // if the encoding is a String, it is the name of an encoding
            // or the name of a CMap, depending on the type of the font
            if (fontType.equals("Type0")) {
                this.type = TYPE_CMAP;
                this.cmap = PDFCMap.getCMap(encoding.getStringValue());
            } else {
                this.type = TYPE_ENCODING;

                this.differences = new HashMap<Character,String>();
                this.baseEncoding = this.getBaseEncoding(encoding.getStringValue());
            }
        } else {
            // loook at the "Type" entry of the encoding to determine the type
            String typeStr = encoding.getDictRef("Type").getStringValue();

            if (typeStr.equals("Encoding")) {
                // it is an encoding
                this.type = TYPE_ENCODING;
                parseEncoding(encoding);
            } else if (typeStr.equals("CMap")) {
                // it is a CMap
                this.type = TYPE_CMAP;
                this.cmap = PDFCMap.getCMap(encoding);
                this.mapName = encoding.getDictRef("CMapName");
            } else {
                throw new IllegalArgumentException("Uknown encoding type: " + this.type);
            }
        }
    }

    /** Get the glyphs associated with a given String */
    public List<PDFGlyph> getGlyphs(PDFFont font, String text) {
        List<PDFGlyph> outList = new ArrayList<PDFGlyph>(text.length());

        // go character by character through the text
        char[] arry = text.toCharArray();
        for (int i = 0; i < arry.length; i++) {
            switch (this.type) {
                case TYPE_ENCODING:
                    outList.add(getGlyphFromEncoding(font, arry[i]));
                    break;
                case TYPE_CMAP:
                    // 2 bytes -> 1 character in a CMap
                    char c = (char) ((arry[i] & 0xff) << 8);
                    if (i < arry.length - 1) {
                        c |= (char) (arry[++i] & 0xff);
                    }
                    outList.add(getGlyphFromCMap(font, c));
                    break;
            }
        }

        return outList;
    }

    /**
     * Get a glyph from an encoding, given a font and character
     */
    private PDFGlyph getGlyphFromEncoding(PDFFont font, char src) {
        String charName = null;

        // only deal with one byte of source
        src &= 0xff;

        // see if this character is in the differences list
        if (this.differences.containsKey(Character.valueOf(src))) {
            charName = this.differences.get(Character.valueOf(src));
        } else if (this.baseEncoding != null) {
            // get the character name from the base encoding
            int charID = this.baseEncoding[src];
            charName = FontSupport.getName(charID);
        }

        return font.getCachedGlyph(src, charName);
    }

    /**
     * Get a glyph from a CMap, given a Type0 font and a character
     */
    private PDFGlyph getGlyphFromCMap(PDFFont font, char src) {
        int fontID = this.cmap.getFontID(src);
        char charID = this.cmap.map(src);

        if (font instanceof Type0Font) {
            font = ((Type0Font) font).getDescendantFont(fontID);
        }

        return font.getCachedGlyph(charID, null);
    }

    /**
     * Parse a PDF encoding object for the actual encoding
     */
    public void parseEncoding(PDFObject encoding) throws IOException {
        this.differences = new HashMap<Character,String>();

        // figure out the base encoding, if one exists
        PDFObject baseEncObj = encoding.getDictRef("BaseEncoding");
        if (baseEncObj != null) {
            this.baseEncoding = getBaseEncoding(baseEncObj.getStringValue());
        }

        // parse the differences array
        PDFObject diffArrayObj = encoding.getDictRef("Differences");
        if (diffArrayObj != null) {
            PDFObject[] diffArray = diffArrayObj.getArray();
            int curPosition = -1;

            for (int i = 0; i < diffArray.length; i++) {
                if (diffArray[i].getType() == PDFObject.NUMBER) {
                    curPosition = diffArray[i].getIntValue();
                } else if (diffArray[i].getType() == PDFObject.NAME) {
                    Character key = Character.valueOf((char) curPosition);
                    this.differences.put(key, diffArray[i].getStringValue());
                    curPosition++;
                } else {
                    throw new IllegalArgumentException("Unexpected type in diff array: " + diffArray[i]);
                }
            }
        }
    }

    /** Get the base encoding for a given name */
    private int[] getBaseEncoding(String encodingName) {
        if (encodingName.equals("MacRomanEncoding")) {
            return FontSupport.macRomanEncoding;
        } else if (encodingName.equals("MacExpertEncoding")) {
            return FontSupport.type1CExpertCharset;
        } else if (encodingName.equals("WinAnsiEncoding")) {
            return FontSupport.winAnsiEncoding;
        } else if (encodingName.equals("StandardEncoding")) {
        	return FontSupport.standardEncoding; 
        } else if(encodingName.equals("SymbolSetEncoding")) {
            return FontSupport.symbolSetEncoding;
        } else {
            throw new IllegalArgumentException("Unknown encoding: " + encodingName);
        }
    }
    
    public boolean isOneByteIdentity() {
    	if(this.mapName != null) {
    		try {
				return "OneByteIdentityH".equals(this.mapName.getStringValue());
			} catch (IOException e) {
				PDFRenderer.getErrorHandler().publishException(e);
			}
    	}
    	
    	return false;
    }
}