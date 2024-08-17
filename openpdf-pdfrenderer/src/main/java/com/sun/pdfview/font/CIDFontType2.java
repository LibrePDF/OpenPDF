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

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.sun.pdfview.PDFObject;

/**
 * a font object derived from a CID font.
 *
 * @author Jonathan Kaplan
 */
public class CIDFontType2 extends TTFFont {

    /**
     * The width of each glyph from the DW and W arrays
     */
    private Map<Character, Float> widths = null;
    /**
     * The vertical width of each glyph from the DW2 and W2 arrays
     */
    private Map<Character, Float> widthsVertical = null;

    /*
     * the default width
     */
    private int defaultWidth = 1000;
    /*
     * the default vertical width
     */
    private int defaultWidthVertical = 1000;
    /** the CIDtoGID map, if any */
    private ByteBuffer cidToGidMap;

    /**
     * create a new CIDFontType2 object based on the name of a built-in font
     * and the font descriptor
     * @param baseName the name of the font, from the PDF file
     * @param fontObj a dictionary that contains the DW (defaultWidth) and
     * W (width) parameters
     * @param descriptor a descriptor for the font
     */
    public CIDFontType2(String baseName, PDFObject fontObj,
            PDFFontDescriptor descriptor) throws IOException {
        super(baseName, fontObj, descriptor);

        parseWidths(fontObj);

        // read the CIDSystemInfo dictionary (required)
        PDFObject systemInfoObj = fontObj.getDictRef("CIDSystemInfo");
        // read the cid to gid map (optional)
        PDFObject mapObj = fontObj.getDictRef("CIDToGIDMap");


        // only read the map if it is a stream (if it is a name, it
        // is "Identity" and can be ignored
        if (mapObj != null && (mapObj.getType() == PDFObject.STREAM)) {
            this.cidToGidMap = mapObj.getStreamBuffer();
        }
    }

    /** Parse the Widths array and DW object */
    private void parseWidths(PDFObject fontObj)
            throws IOException {
        // read the default width (otpional)
        PDFObject defaultWidthObj = fontObj.getDictRef("DW");
        if (defaultWidthObj != null && defaultWidthObj.getIntValue() != 0) {
        	// XOND: commented out the setting of new default width, as several 
        	//		PDFs are displayed in a wrong format due to this:
//            this.defaultWidth = defaultWidthObj.getIntValue();
        }
        
        int entryIdx = 0;
        int first = 0;
        int last = 0;
        PDFObject[] widthArray;

        // read the widths table 
        PDFObject widthObj = fontObj.getDictRef("W");
        if (widthObj != null) {

            // initialize the widths array
            this.widths = new HashMap<Character, Float>();

            // parse the width array
            widthArray = widthObj.getArray();

            /* an entry can be in one of two forms:
             *   <startIndex> <endIndex> <value> or
             *   <startIndex> [ array of values ]
             * we use the entryIdx to differentitate between them
             */
            for (int i = 0; i < widthArray.length; i++) {
                if (entryIdx == 0) {
                    // first value in an entry.  Just store it
                    first = widthArray[i].getIntValue();
                } else if (entryIdx == 1) {
                    // second value -- is it an int or array?
                    if (widthArray[i].getType() == PDFObject.ARRAY) {
                        // add all the entries in the array to the width array
                        PDFObject[] entries = widthArray[i].getArray();
                        for (int c = 0; c < entries.length; c++) {
                            Character key = Character.valueOf((char) (c + first));

                            // value is width / default width
                            float value = entries[c].getIntValue();
                            this.widths.put(key, new Float(value));
                        }
                        // all done
                        entryIdx = -1;
                    } else {
                        last = widthArray[i].getIntValue();
                    }
                } else {
                    // third value.  Set a range
                    int value = widthArray[i].getIntValue();

                    // set the range
                    for (int c = first; c <= last; c++) {
                        this.widths.put(Character.valueOf((char) c), new Float(value));
                    }

                    // all done
                    entryIdx = -1;
                }

                entryIdx++;
            }
        }

        // read the optional vertical default width
        defaultWidthObj = fontObj.getDictRef("DW2");
        if (defaultWidthObj != null) {
            this.defaultWidthVertical = defaultWidthObj.getIntValue();
        }

        // read the vertical widths table
        widthObj = fontObj.getDictRef("W2");
        if (widthObj != null) {

            // initialize the widths array
            this.widthsVertical = new HashMap<Character, Float>();

            // parse the width2 array
            widthArray = widthObj.getArray();

            /* an entry can be in one of two forms:
             *   <startIndex> <endIndex> <value> or
             *   <startIndex> [ array of values ]
             * we use the entryIdx to differentitate between them
             */
            entryIdx = 0;
            first = 0;
            last = 0;

            for (int i = 0; i < widthArray.length; i++) {
                if (entryIdx == 0) {
                    // first value in an entry.  Just store it
                    first = widthArray[i].getIntValue();
                } else if (entryIdx == 1) {
                    // second value -- is it an int or array?
                    if (widthArray[i].getType() == PDFObject.ARRAY) {
                        // add all the entries in the array to the width array
                        PDFObject[] entries = widthArray[i].getArray();
                        for (int c = 0; c < entries.length; c++) {
                            Character key = Character.valueOf((char) (c + first));

                            // value is width / default width
                            float value = entries[c].getIntValue();
                            this.widthsVertical.put(key, new Float(value));
                        }
                        // all done
                        entryIdx = -1;
                    } else {
                        last = widthArray[i].getIntValue();
                    }
                } else {
                    // third value.  Set a range
                    int value = widthArray[i].getIntValue();

                    // set the range
                    for (int c = first; c <= last; c++) {
                        this.widthsVertical.put(Character.valueOf((char) c), new Float(value));
                    }

                    // all done
                    entryIdx = -1;
                }

                entryIdx++;
            }
        }
    }

    /** Get the default width in text space */
    @Override
    public int getDefaultWidth() {
        return this.defaultWidth;
    }

    /** Get the width of a given character */
    @Override
    public float getWidth(char code, String name) {
        if (this.widths == null) {
            return 1f;
        }
        Float w = this.widths.get(Character.valueOf(code));
        if (w == null) {
            return 1f;
        }

        return w.floatValue() / getDefaultWidth();
    }

    /** Get the default vertical width in text space */
    public int getDefaultWidthVertical() {
        return this.defaultWidthVertical;
    }

    /** Get the vertical width of a given character */
    public float getWidthVertical(char code, String name) {
        if (this.widthsVertical == null) {
            return 1f;
        }
        Float w = this.widthsVertical.get(Character.valueOf(code));
        if (w == null) {
            return 1f;
        }

        return w.floatValue() / getDefaultWidthVertical();
    }

    /**
     * Get the outline of a character given the character code.  We
     * interpose here in order to avoid using the CMap of the font in
     * a CID mapped font.
     */
    @Override
    protected synchronized GeneralPath getOutline(char src, float width) {
        int glyphId = (src & 0xffff);

        // check if there is a cidToGidMap
        if (this.cidToGidMap != null) {
            // read the map
            glyphId = this.cidToGidMap.getChar(glyphId * 2);
        }

        // call getOutline on the glyphId
        return getOutline(glyphId, width);
    }
}