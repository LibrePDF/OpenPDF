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

package com.sun.pdfview.font.ttf;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.sun.pdfview.PDFDebugger;

/**
 * Model the TrueType Post table
 *
 * @author  jkaplan
 */
public class PostTable extends TrueTypeTable {
    
    /** Holds value of property format. */
    private int format;
    
    /** Holds value of property italicAngle. */
    private int italicAngle;
    
    /** Holds value of property underlinePosition. */
    private short underlinePosition;
    
    /** Holds value of property underlineThickness. */
    private short underlineThickness;
    
    /** Holds value of property isFixedPitch. */
    private short isFixedPitch;
    
    /** Holds value of property minMemType42. */
    private int minMemType42;
    
    /** Holds value of property maxMemType42. */
    private int maxMemType42;
    
    /** Holds value of property minMemType1. */
    private int minMemType1;
    
    /** Holds value of property maxMemType1. */
    private int maxMemType1;
    
    /** A map which character values to names and vice versa */
    private PostMap nameMap;
      
    /** Creates a new instance of PostTable */
    protected PostTable() {
        super (TrueTypeTable.POST_TABLE);
        
        this.nameMap = new PostMap();
    }
      
    /**
     * Map a character name to a glyphNameIndex
     */
    public short getGlyphNameIndex(String name) {
        return this.nameMap.getCharIndex(name);
    }
    
    /** 
     * Map a character code to a glyphIndex name
     */
    public String getGlyphName(char c) {
        return this.nameMap.getCharName(c);
    }
    
    /** get the data in this map as a ByteBuffer */
    @Override
	public ByteBuffer getData() {
        int size = getLength();
        
        ByteBuffer buf = ByteBuffer.allocate(size);
        
        // write the header
        buf.putInt(getFormat());
        buf.putInt(getItalicAngle());
        buf.putShort(getUnderlinePosition());
        buf.putShort(getUnderlineThickness());
        buf.putShort(getIsFixedPitch());
        buf.putShort((short) 0);
        buf.putInt(getMinMemType42());
        buf.putInt(getMaxMemType42());
        buf.putInt(getMinMemType1());
        buf.putInt(getMaxMemType1());
        
        // now write the table
        buf.put(this.nameMap.getData());
        
        // reset the start pointer
        buf.flip();
        
        return buf;
    }
    
    /** Initialize this structure from a ByteBuffer */
    @Override
	public void setData(ByteBuffer data) {
        setFormat(data.getInt());
        setItalicAngle(data.getInt());
        setUnderlinePosition(data.getShort());
        setUnderlineThickness(data.getShort());
        setIsFixedPitch(data.getShort());
        data.getShort();
        setMinMemType42(data.getInt());
        setMaxMemType42(data.getInt());
        setMinMemType1(data.getInt());
        setMaxMemType1(data.getInt());
        
        // create the map, based on the type
        switch (this.format) {
            case 0x10000:
                this.nameMap = new PostMapFormat0();
                break;
            case 0x20000:
                this.nameMap = new PostMapFormat2();
                break;
            case 0x30000:
                // empty post map.
                this.nameMap = new PostMap();
                break;
            default:
                this.nameMap = new PostMap();
                PDFDebugger.debug("Unknown post map type: " + 
                                   Integer.toHexString(this.format));
                break;
        }
        
        // fill in the data in the map
        this.nameMap.setData(data);
    }
    
    /**
     * Get the length of this table
     */
    @Override
	public int getLength() {
        int size = 32;
        if (this.nameMap != null) {
            size += this.nameMap.getLength();
        }
        
        return size;
    }
    
    /** Getter for property format.
     * @return Value of property format.
     *
     */
    public int getFormat() {
        return this.format;
    }
    
    /** Setter for property format.
     * @param format New value of property format.
     *
     */
    public void setFormat(int format) {
        this.format = format;
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
    
    /** Getter for property underlinePosition.
     * @return Value of property underlinePosition.
     *
     */
    public short getUnderlinePosition() {
        return this.underlinePosition;
    }
    
    /** Setter for property underlinePosition.
     * @param underlinePosition New value of property underlinePosition.
     *
     */
    public void setUnderlinePosition(short underlinePosition) {
        this.underlinePosition = underlinePosition;
    }
    
    /** Getter for property underlineThickness.
     * @return Value of property underlineThickness.
     *
     */
    public short getUnderlineThickness() {
        return this.underlineThickness;
    }
    
    /** Setter for property underlineThickness.
     * @param underlineThickness New value of property underlineThickness.
     *
     */
    public void setUnderlineThickness(short underlineThickness) {
        this.underlineThickness = underlineThickness;
    }
    
    /** Getter for property isFixedPitch.
     * @return Value of property isFixedPitch.
     *
     */
    public short getIsFixedPitch() {
        return this.isFixedPitch;
    }
    
    /** Setter for property isFixedPitch.
     * @param isFixedPitch New value of property isFixedPitch.
     *
     */
    public void setIsFixedPitch(short isFixedPitch) {
        this.isFixedPitch = isFixedPitch;
    }
    
    /** Getter for property minMemType42.
     * @return Value of property minMemType42.
     *
     */
    public int getMinMemType42() {
        return this.minMemType42;
    }
    
    /** Setter for property minMemType42.
     * @param minMemType42 New value of property minMemType42.
     *
     */
    public void setMinMemType42(int minMemType42) {
        this.minMemType42 = minMemType42;
    }
    
    /** Getter for property maxMemType42.
     * @return Value of property maxMemType42.
     *
     */
    public int getMaxMemType42() {
        return this.maxMemType42;
    }
    
    /** Setter for property maxMemType42.
     * @param maxMemType42 New value of property maxMemType42.
     *
     */
    public void setMaxMemType42(int maxMemType42) {
        this.maxMemType42 = maxMemType42;
    }
    
    /** Getter for property minMemType1.
     * @return Value of property minMemType1.
     *
     */
    public int getMinMemType1() {
        return this.minMemType1;
    }
    
    /** Setter for property minMemType1.
     * @param minMemType1 New value of property minMemType1.
     *
     */
    public void setMinMemType1(int minMemType1) {
        this.minMemType1 = minMemType1;
    }
    
    /** Getter for property maxMemType1.
     * @return Value of property maxMemType1.
     *
     */
    public int getMaxMemType1() {
        return this.maxMemType1;
    }
    
    /** Setter for property maxMemType1.
     * @param maxMemType1 New value of property maxMemType1.
     *
     */
    public void setMaxMemType1(int maxMemType1) {
        this.maxMemType1 = maxMemType1;
    }
    
    /** An empty post map */
    class PostMap {
        /** map a name to a character index */
        short getCharIndex(String charName) {
            return (short) 0;
        }
        
        /** name a character index to a name */
        String getCharName(char charIndex) {
            return null;
        }
        
        /** get the length of the data in this map */
        int getLength() {
            return 0;
        }
        
        /** get the data in this map as a ByteBuffer */
        ByteBuffer getData() {
            return ByteBuffer.allocate(0);
        }
        
        /** set the data in this map from a ByteBuffer */
        void setData(ByteBuffer data) {
            // do nothing
            return;
        }
    }
    
    /** A Format 0 post map */
    class PostMapFormat0 extends PostMap {
        /** the glyph names in standard Macintosh ordering */
        protected final String stdNames[] = {
/* 0 */     ".notdef", ".null", "nonmarkingreturn", "space", "exclam", "quotedbl", "numbersign", "dollar",
/* 8 */     "percent", "ampersand", "quotesingle", "parenleft", "parenright", "asterisk", "plus", "comma",
/* 16 */    "hyphen", "period", "slash", "zero", "one", "two", "three", "four",
/* 24 */    "five", "six", "seven", "eight", "nine", "colon", "semicolon", "less", 
/* 32 */    "equal", "greater", "question", "at", "A", "B", "C", "D",
/* 40 */    "E", "F", "G", "H", "I", "J", "K", "L",
/* 48 */    "M", "N", "O", "P", "Q", "R", "S", "T", 
/* 56 */    "U", "V", "W", "X", "Y", "Z", "bracketleft", "ackslash",
/* 64 */    "bracketright", "asciicircum", "underscore", "grave", "a", "b", "c", "d",
/* 72 */    "e", "f", "g", "h", "i", "j", "k", "l", 
/* 80 */    "m", "n", "o", "p", "q", "r", "s", "t",
/* 88 */    "u", "v", "w", "x", "y", "z", "braceleft", "bar",
/* 96 */    "braceright", "asciitilde", "Adieresis", "Aring", "Ccedilla", "Eacute", "Ntilde", "Odieresis",
/* 104 */   "Udieresis", "aacute", "agrave", "acircumflex", "adieresis", "atilde", "aring", "ccedilla",
/* 112 */   "eacute", "egrave", "ecircumflex", "edieresis", "iacute", "igrave", "icircumflex", "idieresis",
/* 120 */   "ntilde", "oacute", "ograve", "ocircumflex", "odieresis", "otilde", "uacute", "ugrave", 
/* 128 */   "ucircumflex", "udieresis", "dagger", "degree", "cent", "sterling", "section", "bullet",
/* 136 */   "paragraph", "germandbls", "registered", "copyright", "trademark", "acute", "dieresis", "notequal",
/* 144 */   "AE", "Oslash", "infinity", "plusminus", "lessequal", "greaterequal", "yen", "mu",
/* 152 */   "partialdiff", "summation", "product", "pi", "integral", "ordfeminine", "ordmasculine", "Omega",
/* 160 */   "ae", "oslash", "questiondown", "exclamdown", "logicalnot", "radical", "florin", "approxequal",
/* 168 */   "Delta", "guillemotleft", "guillemotright", "ellipsis", "nonbreakingspace", "Agrave", "Atilde", "Otilde",
/* 176 */   "OE", "oe", "endash", "emdash", "quotedblleft", "quotedblright", "quoteleft", "quoteright",
/* 184 */   "divide", "lozenge", "ydieresis", "Ydieresis", "fraction", "currency", "guilsinglleft", "guilsinglright",
/* 192 */   "fi", "fl", "daggerdbl", "periodcentered", "quotesinglbase", "quotedblbase", "perthousand", "Acircumflex",
/* 200 */   "Ecircumflex", "Aacute", "Edieresis", "Egrave", "Iacute", "Icircumflex", "Idieresis", "Igrave",
/* 208 */   "Oacute", "Ocircumflex", "apple", "Ograve", "Uacute", "Ucircumflex", "Ugrave", "dotlessi",
/* 216 */   "circumflex", "tilde", "macron", "breve", "dotaccent", "ring", "cedilla", "hungarumlaut",
/* 224 */   "ogonek", "caron", "Lslash", "lslash", "Scaron", "scaron", "Zcaron", "zcaron",
/* 232 */   "brokenbar", "Eth", "eth", "Yacute", "yacute", "Thorn", "thorn", "minus",
/* 240 */   "multiply", "onesuperior", "twosuperior", "threesuperior", "onehalf", "onequarter", "threequarters", "franc",
/* 248 */   "Gbreve", "gbreve", "Idotaccent", "Scedilla", "scedilla", "Cacute", "cacute", "Ccaron",
/* 256 */   "ccaron", "dcroat"
        };
        
        @Override
		/** map a name to a character index */
        short getCharIndex(String charName) {
            for (int i = 0; i < this.stdNames.length; i++) {
                if (charName.equals(this.stdNames[i])) {
                    return (short) i;
                }
            }
            
            return (short) 0;
        }
        
        @Override
		/** name a character index to a name */
        String getCharName(char charIndex) {
            return this.stdNames[charIndex];
        }
        
        @Override
		/** get the length of the data in this map */
        int getLength() {
            return 0;
        }
        
        @Override
		/** get the data in this map as a ByteBuffer */
        ByteBuffer getData() {
            return ByteBuffer.allocate(0);
        }
        
        @Override
		/** set the data in this map from a ByteBuffer */
        void setData(ByteBuffer data) {
            // do nothing
            return;
        }
    }
 
    /** an extension to handle format 2 post maps */
    class PostMapFormat2 extends PostMapFormat0 {
        /** the glyph name index */
        short[] glyphNameIndex;
        
        /** the glyph names */
        String[] glyphNames;
    
        @Override
		/** Map a character name to an index */
        short getCharIndex(String charName) {
            // find the index of this character name
            short idx = -1;
            
            // first try the local names map
            for (int i = 0; i < this.glyphNames.length; i++) {
                if (charName.equals(this.glyphNames[i])) {
                    // this is the value from the glyph name index
                    idx = (short) (this.stdNames.length + i);
                    break;
                }
            }
                    
            // if that doesn't work, try the standard names
            if (idx == -1) {
                idx = super.getCharIndex(charName);
            }
            
            // now get the entry in the index
            for (int c = 0; c < this.glyphNameIndex.length; c++) {
                if (this.glyphNameIndex[c] == idx) {
                    return (short) c;
                }
            }
            
            // not found
            return (short) 0;
        }
        
        @Override
		/** Map an index to a character name */
        String getCharName(char charIndex) {
            if (charIndex >= this.stdNames.length) {
                return this.glyphNames[charIndex - this.stdNames.length];
            }
            
            return super.getCharName(charIndex);
        }
        
        @Override
		/** get the length of this class's data */
        int getLength() {
            // the size of the header plus the table of mappings
            int size = 2 + (2 * this.glyphNameIndex.length);
            
            // the size of each string -- note the extra byte for a pascal
            // string
            for (int i = 0; i < this.glyphNames.length; i++) {
                size += this.glyphNames[i].length() + 1;
            }
            
            return size;
        }
        
        @Override
		/** get the data in this map as a byte array */
        ByteBuffer getData() {
            ByteBuffer buf = ByteBuffer.allocate(getLength());
            
            // write the number of glyphs
            buf.putShort((short) this.glyphNameIndex.length);
            
            // write the name indices
            for (int i = 0; i < this.glyphNameIndex.length; i++) {
                buf.putShort(this.glyphNameIndex[i]);
            }
            
            // write the names as pascal strings
            for (int i = 0; i < this.glyphNames.length; i++) {
                buf.put((byte) this.glyphNames[i].length());
                buf.put(this.glyphNames[i].getBytes());
            }
            
            // reset the start pointer
            buf.flip();
            
            return buf;
        }
        
        @Override
		/** set the contents of this map from a ByteBuffer */
        void setData(ByteBuffer data) {
            short numGlyphs = data.getShort();
            this.glyphNameIndex = new short[numGlyphs];
            
            // the highest glyph index seen so far
            int maxGlyph = 257;
            for (int i = 0; i < numGlyphs; i++) {
                this.glyphNameIndex[i] = data.getShort();
                    
                // see if this is the highest glyph
                if (this.glyphNameIndex[i] > maxGlyph) {
                    maxGlyph = this.glyphNameIndex[i];
                }
            }
                
            // subtract off the default glyphs
            maxGlyph -= 257;
            
            // read in any additional names
            this.glyphNames = new String[maxGlyph];
            // fill with empty strings for avoiding nullpointer exception: glyph names
            // are not mandatory for true type fonts according to the PDF spec.
            Arrays.fill(this.glyphNames, "");
                
            // read each name from a pascal string
            // the length is stored in the first byte, followed by
            // the data
            for (int i = 0; i < maxGlyph; i++) {
            	if(data.hasRemaining()) {
                    // size in the first byte
                    byte size = data.get();
                            
                    // then the data
                    byte[] stringData = new byte[size];
                    data.get(stringData);
                            
                    this.glyphNames[i] = new String(stringData);
            	}
            }
        }
    }
}