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

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.OpenType;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.sun.pdfview.BaseWatchable;
import com.sun.pdfview.PDFDebugger;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;
import com.sun.pdfview.font.ttf.CMap;
import com.sun.pdfview.font.ttf.CMapFormat0;
import com.sun.pdfview.font.ttf.CMapFormat4;
import com.sun.pdfview.font.ttf.CmapTable;
import com.sun.pdfview.font.ttf.HeadTable;
import com.sun.pdfview.font.ttf.HmtxTable;
import com.sun.pdfview.font.ttf.NameTable;
import com.sun.pdfview.font.ttf.PostTable;
import com.sun.pdfview.font.ttf.TrueTypeFont;
import com.sun.pdfview.font.ttf.TrueTypeTable;

/**
 * a font object derived from a true type font.
 *
 * @author Mike Wessler
 */
public class NativeFont extends OutlineFont {

    /** Control characters to filter out of the underlying font */
    protected static final char[] controlChars = {0x9, 0xa, 0xd};

    /** the ids of our favorite CMaps */
    protected static final short[] mapIDs = {
        3, 1, /* Microsoft Unicode */
        0, 0, /* unicode default */
        0, 3, /* unicode 2.0 map */
        1, 0 /* macintosh */};

    /** the actual font in use */
    private Font f;

    /** the font render context */
    private final FontRenderContext basecontext =
                              new FontRenderContext (new AffineTransform (),
            true, true);

    /** the cmap table from a TrueType font */
    private CmapTable cmapTable;

    /** the post table from a TrueType font */
    private PostTable postTable;

    /** the number of font units in one em */
    private int unitsPerEm;

    /** the hmtx table from the TrueType font */
    private HmtxTable hmtxTable;

    /**
     * create a new NativeFont object based on a description of the
     * font from the PDF file.  If the description happens to contain
     * an in-line true-type font file (under key "FontFile2"), use the
     * true type font.  Otherwise, parse the description for key information
     * and use that to generate an appropriate font.
     */
    public NativeFont (String baseFont, PDFObject fontObj,
                       PDFFontDescriptor descriptor)
            throws IOException {
        super (baseFont, fontObj, descriptor);

        String fontName = descriptor.getFontName ();

        PDFObject ttf = descriptor.getFontFile2 ();
        if (ttf != null) {
            byte[] fontdata = ttf.getStream ();

            try {
                setFont (fontdata);
            } catch (FontFormatException ffe) {
                throw new PDFParseException ("Font format exception: " + ffe);
            }
        } else {
            int flags = descriptor.getFlags ();
            int style = ((flags & PDFFontDescriptor.FORCEBOLD) != 0) ? Font.BOLD : Font.PLAIN;

            if (fontName.indexOf ("Bold") > 0) {
                style |= Font.BOLD;
            }
            if (descriptor.getItalicAngle () != 0) {
                style |= Font.ITALIC;
            }
            if ((flags & PDFFontDescriptor.FIXED_PITCH) != 0) { // fixed width
                setFont (new Font ("Monospaced", style, 1));
            } else if ((flags & PDFFontDescriptor.SERIF) != 0) {  // serif font
                setFont (new Font ("Serif", style, 1));
            } else {
                setFont (new Font ("Sans-serif", style, 1));
            }
        }
    }

    /**
     * Get a glyph outline by name
     *
     * @param name the name of the desired glyph
     * @return the glyph outline, or null if unavailable
     */
    @Override
	protected GeneralPath getOutline (String name, float width) {
        if (this.postTable != null && this.cmapTable != null) {
            // map this character name to a glyph ID
            short glyphID = this.postTable.getGlyphNameIndex (name);

            if (glyphID == 0) {
                // no glyph -- try by index
                return null;
            }

            // the mapped character
            char mappedChar = 0;

            for (int i = 0; i < mapIDs.length; i += 2) {
                CMap map = this.cmapTable.getCMap (mapIDs[i], mapIDs[i + 1]);
                if (map != null) {
                    mappedChar = map.reverseMap (glyphID);

                    // we found a character
                    if (mappedChar != 0) {
                        break;
                    }
                }
            }

            return getOutline (mappedChar, width);
        }

        // no maps found, hope the font can deal
        return null;
    }

    /**
     * Get a glyph outline by character code
     *
     * Note this method must always return an outline 
     *
     * @param src the character code of the desired glyph
     * @return the glyph outline
     */
    @Override
	protected GeneralPath getOutline (char src, float width) {
        // some true type fonts put characters in the undefined
        // region of Unicode instead of as normal characters.
        if (!this.f.canDisplay (src) && this.f.canDisplay ((char) (src + 0xf000))) {
            src += 0xf000;
        }

        // filter out control characters
        for (int i = 0; i < controlChars.length; i++) {
            if (controlChars[i] == src) {
                src = (char) (0xf000 | src);
                break;
            }
        }

        char[] glyph = new char[1];
        glyph[0] = src;

        GlyphVector gv = this.f.createGlyphVector (this.basecontext, glyph);
        GeneralPath gp = new GeneralPath (gv.getGlyphOutline (0));

        // this should be gv.getGlyphMetrics(0).getAdvance(), but that is
        // broken on the Mac, so we need to read the advance from the
        // hmtx table in the font
        CMap map = this.cmapTable.getCMap (mapIDs[0], mapIDs[1]);
        int glyphID = map.map (src);
        float advance = (float) this.hmtxTable.getAdvance (glyphID) / (float) this.unitsPerEm;

        float widthfactor = width / advance;
        gp.transform (AffineTransform.getScaleInstance (widthfactor, -1));

        return gp;
    }

    /**
     * Set the font
     *
     * @param f the font to use
     */
    protected void setFont (Font f) {
        this.f = f;

        // if it's an OpenType font, parse the relevant tables to get
        // glyph name to code mappings
        if (f instanceof OpenType) {
            OpenType ot = (OpenType) f;

            byte[] cmapData = ot.getFontTable (OpenType.TAG_CMAP);
            byte[] postData = ot.getFontTable (OpenType.TAG_POST);

            TrueTypeFont ttf = new TrueTypeFont (0x10000);

            this.cmapTable =
            (CmapTable) TrueTypeTable.createTable (ttf, "cmap",
                    ByteBuffer.wrap (cmapData));
            ttf.addTable ("cmap", this.cmapTable);

            this.postTable =
            (PostTable) TrueTypeTable.createTable (ttf, "post",
                    ByteBuffer.wrap (postData));
            ttf.addTable ("post", this.postTable);
        }
    }

    /**
     * Set the font
     *
     * @param fontdata the font data as a byte array
     */
    protected void setFont (byte[] fontdata)
            throws FontFormatException, IOException {
        try {
            // read the true type information
            TrueTypeFont ttf = TrueTypeFont.parseFont (fontdata);

            // get the cmap, post, and hmtx tables for later use
            this.cmapTable = (CmapTable) ttf.getTable ("cmap");
            this.postTable = (PostTable) ttf.getTable ("post");
            this.hmtxTable = (HmtxTable) ttf.getTable ("hmtx");

            // read the units per em from the head table
            HeadTable headTable = (HeadTable) ttf.getTable ("head");
            this.unitsPerEm = headTable.getUnitsPerEm ();

            /* Find out if we have the right info in our name table.
             * This is a hack because Java can only deal with fonts that
             * have a Microsoft encoded name in their name table (PlatformID 3).
             * We'll 'adjust' the font to add it if not, and take our chances
             * with our parsing, since it wasn't going to work anyway.
             */
            NameTable nameTable = null;

            try {
                nameTable = (NameTable) ttf.getTable ("name");
            } catch (Exception ex) {
                PDFDebugger.debug("Error reading name table for font " + getBaseFont () + ".  Repairing!");
            }

            boolean nameFixed = fixNameTable (ttf, nameTable);

            /* Figure out if we need to hack the CMap table.  This might
             * be the case if we use characters that Java considers control
             * characters (0x9, 0xa and 0xd), that have to be re-mapped
             */
            boolean cmapFixed = fixCMapTable (ttf, this.cmapTable);

            // use the parsed font instead of the original
            if (nameFixed || cmapFixed) {
                fontdata = ttf.writeFont ();
            }
        } catch (Exception ex) {
            PDFDebugger.debug("Error parsing font : " + getBaseFont ());
            BaseWatchable.getErrorHandler().publishException(ex);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream (fontdata);
        this.f = Font.createFont (Font.TRUETYPE_FONT, bais);
        bais.close ();
    }

    /**
     * Fix a broken font name table for a TrueType font.  Some fonts do not
     * have Microsoft-specific name information, but Java won't work without
     * it (grrr.).  This method takes a font and adds the Microsoft data into
     * it.
     *
     * @param ttf the font
     * @param name the font's name table
     * @return true if the table was fixed, or false if it was left as is
     */
    private boolean fixNameTable (TrueTypeFont ttf, NameTable name) {
        // if we didn't find the table, or there was an exception,
        // just create a new one
        if (name == null) {
            name = (NameTable) TrueTypeTable.createTable (ttf, "name");
            ttf.addTable ("name", name);
        }

        // first, figure out some info about the font
        String fName = this.getBaseFont ();
        String style = "Regular";

        if (fName.indexOf ("Italic") > -1 || fName.indexOf ("italic") > -1) {
            style = "Italic";
        } else if (fName.indexOf ("Bold") > -1 || fName.indexOf ("bold") > -1) {
            style = "Bold";
        }

        if (fName.indexOf ('-') > -1) {
            fName = fName.substring (0, fName.indexOf ('-'));
        }

        short platID = NameTable.PLATFORMID_MICROSOFT;
        short encID = 1;
        short langID = 1033;

        short[] nameIDs = {
            NameTable.NAMEID_COPYRIGHT,
            NameTable.NAMEID_FAMILY,
            NameTable.NAMEID_SUBFAMILY,
            NameTable.NAMEID_SUBFAMILY_UNIQUE,
            NameTable.NAMEID_FULL_NAME,
            NameTable.NAMEID_VERSION,
            NameTable.NAMEID_POSTSCRIPT_NAME,
            NameTable.NAMEID_TRADEMARK
        };

        String[] defaultValues = {
            "No copyright",
            fName,
            style,
            fName + " " + style,
            fName + " " + style,
            "1.0 (Fake)",
            fName,
            "No Trademark"
        };

        boolean changed = false;

        for (int i = 0; i < nameIDs.length; i++) {
            if (name.getRecord (platID, encID, langID, nameIDs[i]) == null) {
                name.addRecord (platID, encID, langID, nameIDs[i],
                        defaultValues[i]);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Fix the CMap table.  This can be necessary if characters are mapped to
     * control characters (0x9, 0xa, 0xd) Java will not render them, even 
     * though they are valid.
     *
     * Also, Java tends to not like it when there is only a Format 0 CMap,
     * which happens frequently when included Format 4 CMaps are broken.
     * Since PDF prefers the Format 0 map, while Java prefers the Format 4 map,
     * it is generally necessary to re-write the Format 0 map as a Format 4 map
     * to make most PDFs work.
     *
     * @param ttf the font
     * @param cmap the CMap table
     * @return true if the font was changed, or false if it was left as-is
     */
    private boolean fixCMapTable (TrueTypeFont ttf, CmapTable cmap) {
        CMapFormat4 fourMap = null;
        CMapFormat0 zeroMap = null;

        for (int i = 0; i < mapIDs.length; i += 2) {
            CMap map = this.cmapTable.getCMap (mapIDs[i], mapIDs[i + 1]);
            if (map != null) {
                if (fourMap == null && map instanceof CMapFormat4) {
                    fourMap = (CMapFormat4) map;
                } else if (zeroMap == null && map instanceof CMapFormat0) {
                    zeroMap = (CMapFormat0) map;
                }
            }
        }

        // if there were no maps, we could have problems.  Just try creating
        // an identity map
        if (zeroMap == null && fourMap == null) {
            fourMap = (CMapFormat4) CMap.createMap ((short) 4, (short) 0);
            fourMap.addSegment ((short) getFirstChar (),
                    (short) getLastChar (),
                    (short) 0);
        }

        // create our map based on the type 0 map, since PDF seems
        // to prefer a type 0 map (Java prefers a unicode map)
        if (zeroMap != null) {
            fourMap = (CMapFormat4) CMap.createMap ((short) 4, (short) 0);

            // add the mappings from 0 to null and 1 to notdef
            fourMap.addSegment ((short) 0, (short) 1, (short) 0);

            for (int i = getFirstChar (); i <= getLastChar (); i++) {
                short value = (short) (zeroMap.map ((byte) i) & 0xff);
                if (value != 0) {
                    fourMap.addSegment ((short) i, (short) i,
                            (short) (value - i));
                }
            }
        }

        // now that we have a type four map, remap control characters
        for (int i = 0; i < controlChars.length; i++) {
            short idx = (short) (0xf000 | controlChars[i]);
            short value = (short) fourMap.map (controlChars[i]);

            fourMap.addSegment (idx, idx, (short) (value - idx));
        }

        // create a whole new table with just our map
        cmap = (CmapTable) TrueTypeTable.createTable (ttf, "cmap");
        cmap.addCMap ((short) 3, (short) 1, fourMap);

        // replace the table in the font
        ttf.addTable ("cmap", cmap);

        // change the stored table
        this.cmapTable = cmap;

        return true;
    }
}