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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.sun.pdfview.BaseWatchable;
import com.sun.pdfview.PDFDebugger;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;
import com.sun.pdfview.PDFRenderer;
import com.sun.pdfview.font.cid.PDFCMap;
import com.sun.pdfview.font.cid.ToUnicodeMap;
import com.sun.pdfview.font.ttf.TrueTypeFont;

/**
 * a Font definition for PDF files
 * @author Mike Wessler
 */
public abstract class PDFFont {

    private static final FilenameFilter TTF_FILTER = new FilenameFilter() {
        @Override
		public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".ttf");
        }
    };

    private static Map<String,File> namedFontsToLocalTtfFiles = null;

    /** the font SubType of this font */
    private String subtype;
    /** the postscript name of this font */
    private String baseFont;
    /** the font encoding (maps character ids to glyphs) */
    private PDFFontEncoding encoding;
    /** the font descriptor */
    private PDFFontDescriptor descriptor;
    /** the CMap that maps this font to unicode values */
    private PDFCMap unicodeMap;
    /** a cache of glyphs indexed by character */
    private Map<Character,PDFGlyph> charCache;



    /**
     * get the PDFFont corresponding to the font described in a PDFObject.
     * The object is actually a dictionary containing the following keys:<br>
     * Type = "Font"<br>
     * Subtype = (Type1 | TrueType | Type3 | Type0 | MMType1 | CIDFontType0 |
     * CIDFontType2)<br>
     * FirstChar = #<br>
     * LastChar = #<br>
     * Widths = array of #<br>
     * Encoding = (some name representing a dictionary in the resources | an
     * inline dictionary)
     * <p>
     * For Type1 and TrueType fonts, the dictionary also contains:<br>
     * BaseFont = (some name, or XXXXXX+Name as a subset of font Name)
     * <p>
     * For Type3 font, the dictionary contains:<br>
     * FontBBox = (rectangle)<br>
     * FontMatrix = (array, typically [0.001, 0, 0, 0.001, 0, 0])<br>
     * CharProcs = (dictionary)
     * Resources = (dictionary)
     */
    public synchronized static PDFFont getFont(PDFObject obj,
            HashMap<String,PDFObject> resources)
            throws IOException {
        // the obj is actually a dictionary containing:
        //    Type (=Font)
        //    Subtype (Type1, TrueType, Type3, Type0, MMType1, CIDFontType0,2)
        //    FirstChar (int)
        //    LastChar (int)
        //    Widths (array)
        //    Encoding (name or dict) : assumes StandardEncoding
        // and........
        // Type1 and TrueType fonts:
        //    BaseFont (name)  // may be XXXXXX+Fontname as a subset.
        //    FontDescriptor (dict)
        // Type3 fonts:
        //    FontBBox (rectangle)
        //    FontMatrix (array) // e.g. [0.001 0 0 0.001 0 0]
        //    CharProcs (dict)
        //    Resources (dict)
        //
        // Font descriptor (Type1 and TrueType fonts):
        //    FontName (name)
        //    Flags (1=monospace, 2=serif, 4=script, 7=italic, 19=bold)
        //    FontBBox (rectangle)
        //    ItalicAngle (float)
        //    Ascent (float)
        //    Descent (float)
        //    CapHeight (float)
        //    StemV (float)
        //    FontFile (stream for Type1 fonts)
        //    FontFile2 (stream for TrueType fonts)
        //    FontFile3 (stream for CFF/Type1C fonts)
        //
        // Font data can be Type1, TrueType(native), or Type1C
        PDFFont font = (PDFFont) obj.getCache();
        if (font != null) {
            return font;
        }

        String baseFont = null;
        PDFFontEncoding encoding = null;
        PDFFontDescriptor descriptor = null;

        String subType = obj.getDictRef("Subtype").getStringValue();
        if (subType == null) {
            subType = obj.getDictRef("S").getStringValue();
        }
        PDFObject baseFontObj = obj.getDictRef("BaseFont");
        PDFObject encodingObj = obj.getDictRef("Encoding");
        PDFObject descObj = obj.getDictRef("FontDescriptor");

        if (baseFontObj != null) {
            baseFont = baseFontObj.getStringValue();
        } else {
            baseFontObj = obj.getDictRef("Name");
            if (baseFontObj != null) {
                baseFont = baseFontObj.getStringValue();
            }
        }

        if (encodingObj != null) {
            encoding = new PDFFontEncoding(subType, encodingObj);
        }

        if (descObj != null) {
            descriptor = new PDFFontDescriptor(descObj, subType);
        } else {
            descriptor = new PDFFontDescriptor(baseFont);
        }

        if (subType.equals("Type0")) {
            font = new Type0Font(baseFont, obj, descriptor);
        } else if (subType.equals("Type1")) {
            // load a type1 font
			if (descriptor.getFontFile() != null) {
                // it's a Type1 font, included.
                font = new Type1Font(baseFont, obj, descriptor);
                if(!((Type1Font)font).isName2OutlineFilled()){
                	PDFDebugger.debug("Type1Font can't be parsed completelly, character mapping missing. Use a basefont instead.");
                	font = new BuiltinFont(baseFont, obj, descriptor);
                }
            } else if (descriptor.getFontFile3() != null) {
                // it's a CFF (Type1C) font
                font = new Type1CFont(baseFont, obj, descriptor);
            } else {
                // no font info. Fake it based on the FontDescriptor
                font = new BuiltinFont(baseFont, obj, descriptor);
            }
        } else if (subType.equals("TrueType")) {
            if (descriptor.getFontFile2() != null) {
                // load a TrueType font
                try {
                    font = new TTFFont(baseFont, obj, descriptor);
                }catch (Exception e) {
//            		PDFRenderer.getErrorHandler().publishException(e);
                	PDFDebugger.debug("Error parsing font : " + baseFont);
                    // fake it with a built-in font
                    font = new BuiltinFont(baseFont, obj, descriptor);
                }
            } else {
                final File extFontFile = findExternalTtf(baseFont);
                if (extFontFile != null) {
                	try {
                        font = new TTFFont(baseFont, obj, descriptor, extFontFile);
                	}catch (Exception e) {
//                		PDFRenderer.getErrorHandler().publishException(e);
                		PDFDebugger.debug("Error parsing font : " + baseFont);
                		// fake it with a built-in font
                		font = new BuiltinFont(baseFont, obj, descriptor);
					}
                } else {
                    // fake it with a built-in font
                    font = new BuiltinFont(baseFont, obj, descriptor);
                }
            }
        } else if (subType.equals("Type3")) {
            // load a type 3 font
            font = new Type3Font(baseFont, obj, resources, descriptor);
        } else if (subType.equals("CIDFontType2")) {
        	if(descriptor.getFontFile2() != null) {
                font = new CIDFontType2(baseFont, obj, descriptor);
        	}else {
                // fake it with a built-in font
       		//but it prefer to use the CIDFontType0 that have the extra handling of ToUnicode, if found in the fontObj
	                font = new CIDFontType0(baseFont, obj, descriptor);
        	}
        } else if (subType.equals("CIDFontType0")) {
        	if(descriptor.getFontFile2() !=null){
                font = new CIDFontType2(baseFont, obj, descriptor);
        	}else {
                font = new CIDFontType0(baseFont, obj, descriptor);
        	}
		} else if (subType.equals("MMType1")) {
			// not yet implemented, fake it with a built-in font
			font = new BuiltinFont(baseFont, obj, descriptor);
        } else {
            throw new PDFParseException("Don't know how to handle a '" +
                    subType + "' font");
        }

        font.setSubtype(subType);
        font.setEncoding(encoding);

        obj.setCache(font);
        return font;
    }

    private static File findExternalTtf(String fontName) {
        ensureNamedTtfFontFiles();
        return namedFontsToLocalTtfFiles.get(fontName);
    }

    private synchronized static void ensureNamedTtfFontFiles() {
        if (namedFontsToLocalTtfFiles == null) {
            namedFontsToLocalTtfFiles = new HashMap<String, File>();

            if (Boolean.getBoolean("PDFRenderer.avoidExternalTtf")) {
                return;
            }

            for (final String fontDirName : getFontSearchPath()) {

                final File fontDir = new File(fontDirName);
                if (fontDir.exists()) {
                    for (final File ttfFile : fontDir.listFiles(TTF_FILTER)) {
                        if (ttfFile.canRead()) {
                            try {
                                byte[] fontBytes;
                                RandomAccessFile fontRa = null;
                                try {
                                    fontRa = new RandomAccessFile (ttfFile, "r");
                                    int size = (int) fontRa.length ();
                                    fontBytes = new byte[size];
                                    fontRa.readFully(fontBytes);
                                } finally {
                                    if (fontRa != null) {
                                        fontRa.close();
                                    }
                                }

                                TrueTypeFont ttf = TrueTypeFont.parseFont(fontBytes);
                                for (final String fontName : ttf.getNames()) {
                                    if (!namedFontsToLocalTtfFiles.containsKey(fontName)) {
                                        namedFontsToLocalTtfFiles.put(fontName, ttfFile);
                                    }
                                }
                            } catch (Throwable t) {
                                // I'm not sure how much confidence we should have
                                // in the font parsing, so we'll avoid relying on
                                // this not to fail
                                System.err.println("Problem parsing " + ttfFile);
                                BaseWatchable.getErrorHandler().publishException(t);
                            }
                        }
                    }
                }
            }
        }

    }

    private static String[] getFontSearchPath() {
        String pathProperty = System.getProperty("PDFRenderer.fontSearchPath");
        if (pathProperty != null) {
            return pathProperty.split(Pattern.quote(File.pathSeparator));
        } else {
            return getDefaultFontSearchPath();
        }
    }


    private static String[] getDefaultFontSearchPath()
    {
        String osName = null;
        try {
            osName = System.getProperty("os.name");
        } catch (SecurityException e) {
            // preserve null osName
        }

        if (osName == null) {
            // Makes it a bit tricky to figure out a nice default
            return new String[0];
        }

        osName = osName != null ? osName.toLowerCase() : "";
        if (osName.startsWith("windows")) {
            // start with some reasonable default
            String path = "C:/WINDOWS/Fonts";
            try {
                String windir = System.getenv("WINDIR");
                if (windir != null) {
                    path = windir + "/Fonts/";
                }
            } catch (SecurityException secEx) {
                // drop through and accept default path
            }
            return new String[] { path };
        } else if (osName != null && osName.startsWith("mac")) {
            List<String> paths = new ArrayList<String>(Arrays.asList(
                    "/Library/Fonts",
                    "/Network/Library/Fonts",
                    "/System/Library/Fonts",
                    "/System Folder/Fonts"));
            // try and add the user font dir at the front
            try {
                paths.add(0, System.getProperty("user.home") + "/Library/Fonts");
            } catch (SecurityException e) {
                // I suppose we just won't use the user fonts
            }
            return paths.toArray(new String[paths.size()]);
        } else {
            // Feel free to insert some reasonable defaults for other
            // (UNIX, most likely) platforms here
            return new String[0];
        }
    }

    /**
     * Get the subtype of this font.
     * @return the subtype, one of: Type0, Type1, TrueType or Type3
     */
    public String getSubtype() {
        return this.subtype;
    }

    /**
     * Set the font subtype
     */
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    /**
     * Get the postscript name of this font
     * @return the postscript name of this font
     */
    public String getBaseFont() {
        return this.baseFont;
    }

    /**
     * Set the postscript name of this font
     * @param baseFont the postscript name of the font
     */
    public void setBaseFont(String baseFont) {
        this.baseFont = baseFont;
    }

    /**
     * Get the encoding for this font
     * @return the encoding which maps from this font to actual characters
     */
    public PDFFontEncoding getEncoding() {
        return this.encoding;
    }

    /**
     * Set the encoding for this font
     */
    public void setEncoding(PDFFontEncoding encoding) {
        this.encoding = encoding;
    }

    /**
     * Get the descriptor for this font
     * @return the font descriptor
     */
    public PDFFontDescriptor getDescriptor() {
        return this.descriptor;
    }

    /**
     * Set the descriptor font descriptor
     */
    public void setDescriptor(PDFFontDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Get the CMap which maps the characters in this font to unicode names
     */
    public PDFCMap getUnicodeMap() {
        return this.unicodeMap;
    }

    /**
     * Set the CMap which maps the characters in this font to unicode names
     */
    public void setUnicodeMap(PDFCMap unicodeMap) {
        this.unicodeMap = unicodeMap;
    }

    /**
     * Get the glyphs associated with a given String in this font
     *
     * @param text the text to translate into glyphs
     */
    public List<PDFGlyph> getGlyphs(String text) {
        List<PDFGlyph> outList = null;

        // if we have an encoding, use it to get the commands
        // don't use the encoding if it is "OneByteIdentityH" (hack for case #205739)
        if (this.encoding != null && !this.encoding.isOneByteIdentity()) {
            outList = this.encoding.getGlyphs(this, text);
        } else {
            // use the default mapping
            char[] arry = text.toCharArray();
            outList = new ArrayList<PDFGlyph>(arry.length);

            for (int i = 0; i < arry.length; i++) {
                // only look at 2 bytes when there is no encoding
                char src = (char) (arry[i] & 0xff);
                outList.add(getCachedGlyph(src, null));
            }
        }

        return outList;
    }

    /**
     * Get a glyph for a given character code.  The glyph is returned
     * from the cache if available, or added to the cache if not
     *
     * @param src the character code of this glyph
     * @param name the name of the glyph, or null if the name is unknown
     * @return a glyph for this character
     */
    public PDFGlyph getCachedGlyph(char src, String name) {
        if (this.charCache == null) {
            this.charCache = new HashMap<Character,PDFGlyph>();
        }

        // try the cache
        PDFGlyph glyph = this.charCache.get(Character.valueOf(src));

        // if it's not there, add it to the cache
        if (glyph == null) {
            glyph = getGlyph(src, name);
            this.charCache.put(Character.valueOf(src), glyph);
        }

        return glyph;
    }

    /**
     * Create a PDFFont given the base font name and the font descriptor
     * @param baseFont the postscript name of this font
     * @param descriptor the descriptor for the font
     */
    protected PDFFont(String baseFont, PDFFontDescriptor descriptor) {
        setBaseFont(baseFont);
        setDescriptor(descriptor);
    }

    /**
     * Get the glyph for a given character code and name
     *
     * The preferred method of getting the glyph should be by name.  If the
     * name is null or not valid, then the character code should be used.
     * If the both the code and the name are invalid, the undefined glyph 
     * should be returned.
     *
     * Note this method must *always* return a glyph.  
     *
     * @param src the character code of this glyph
     * @param name the name of this glyph or null if unknown
     * @return a glyph for this character
     */
    protected abstract PDFGlyph getGlyph(char src, String name);

    /**
     * Turn this font into a pretty String
     */
    @Override
    public String toString() {
        return getBaseFont();
    }

    /** 
     * Compare two fonts base on the baseFont
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PDFFont)) {
            return false;
        }

        return ((PDFFont) o).getBaseFont().equals(getBaseFont());
    }

    /**
     * Hash a font based on its base font
     */
    @Override
    public int hashCode() {
        return getBaseFont().hashCode();
    }
}
