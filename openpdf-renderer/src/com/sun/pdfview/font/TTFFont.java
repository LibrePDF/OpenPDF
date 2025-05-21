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

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.font.ttf.AdobeGlyphList;
import com.sun.pdfview.font.ttf.CMap;
import com.sun.pdfview.font.ttf.CmapTable;
import com.sun.pdfview.font.ttf.Glyf;
import com.sun.pdfview.font.ttf.GlyfCompound;
import com.sun.pdfview.font.ttf.GlyfSimple;
import com.sun.pdfview.font.ttf.GlyfTable;
import com.sun.pdfview.font.ttf.HeadTable;
import com.sun.pdfview.font.ttf.HmtxTable;
import com.sun.pdfview.font.ttf.PostTable;
import com.sun.pdfview.font.ttf.TrueTypeFont;

/**
 * A true-type font
 */
public class TTFFont extends OutlineFont {

    /** the truetype font itself */
    private TrueTypeFont font;

    /** the number of units per em in the font */
    private float unitsPerEm;

    public TTFFont (String baseFont, PDFObject fontObj,
                    PDFFontDescriptor descriptor) throws IOException {
        this(baseFont, fontObj, descriptor, null);
    }
    /**
     * create a new TrueTypeFont object based on a description of the
     * font from the PDF file.  If the description happens to contain
     * an in-line true-type font file (under key "FontFile2"), use the
     * true type font.  Otherwise, parse the description for key information
     * and use that to generate an appropriate font.
     */
    public TTFFont (String baseFont, PDFObject fontObj,
                    PDFFontDescriptor descriptor, File fontFile)
            throws IOException {
        super (baseFont, fontObj, descriptor);

        PDFObject ttfObj = descriptor.getFontFile2 ();

        if (ttfObj != null || fontFile != null) {
            if (ttfObj != null) {
                font = TrueTypeFont.parseFont (ttfObj.getStreamBuffer ());
            } else {
                final RandomAccessFile raFile = fontFile != null ? new RandomAccessFile(fontFile, "r") : null;
                final FileChannel fc = raFile.getChannel();
                try {
                    MappedByteBuffer mappedFont = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                    font = TrueTypeFont.parseFont(mappedFont);
                    mappedFont = null;
                } finally {
                    try {
                        fc.close();
                    } catch (IOException ioEx) {
                        // swallow
                    }
                    try {
                        raFile.close();
                    } catch (IOException ioEx) {
                        // swallow
                    }
                }
            }
            // read the units per em from the head table
            HeadTable head = (HeadTable) font.getTable ("head");
            unitsPerEm = head.getUnitsPerEm ();
        } else {
            font = null;
        }
    }

    public Collection<String> getNames()
    {
        return font.getNames();
    }

    /**
     * Get the outline of a character given the character code
     */
    @Override
	protected synchronized GeneralPath getOutline (char src, float width) {
        // find the cmaps
        CmapTable cmap = (CmapTable) this.font.getTable ("cmap");

        // if there are no cmaps, this is (hopefully) a cid-mapped font,
        // so just trust the value we were given for src
        if (cmap == null) {
            return getOutline ((int) src, width);
        }

        CMap[] maps = cmap.getCMaps ();

        // try the maps in order
        for (int i = 0; i < maps.length; i++) {
            int idx = maps[i].map (src);
            if (idx != 0) {
                return getOutline (idx, width);
            }
        }
        
        // windows symbol font CMap may use one of the following code ranges
        if (src >= 0 && src <= 0xFF) {
        	int[] symbolPages = new int[]{0xF000, 0xF100, 0xF200};        	
        	for (int codePage : symbolPages) {
                for (int i = 0; i < maps.length; i++) {
                    int idx = maps[i].map ( (char)(src | codePage));
                    if (idx != 0) {
                        return getOutline (idx, width);
                    }
                }        					
			}
        }

        // not found, return the empty glyph
        return getOutline (0, width);
    }

    /**
     * lookup the outline using the (3, 1) cmap, as specified in 32000-1:2008,
     * 9.6.6.4, when an Encoding is specified.
     * 
     * @param val
     * @param width
     * @return GeneralPath
     */
    protected synchronized GeneralPath getOutlineFrom31CMap (char val,
                                                            float width) {
        // find the cmaps
        CmapTable cmap = (CmapTable) this.font.getTable ("cmap");

        if (cmap == null) {
            return null;
        }

        // find the (3, 1) cmap subtable (Microsoft Unicode)
        CMap map = cmap.getCMap ((short) 3, (short) 1);
        if (map == null) {
        	return null;
        }
        int idx = map.map (val);
        if (idx != 0) {
            return getOutline (idx, width);
        }

        return null;
    }

    /**
     * Get the outline of a character given the character name
     */
    @Override
	protected synchronized GeneralPath getOutline (String name, float width) {
        int idx;
        PostTable post = (PostTable) this.font.getTable ("post");
        if (post != null) {
            idx = post.getGlyphNameIndex (name);
            if (idx != 0) {
                return getOutline (idx, width);
            }
        }

        Integer res = AdobeGlyphList.getGlyphNameIndex (name);
        if (res != null) {
            idx = res;
            return getOutlineFrom31CMap ((char) idx, width);
        }
        return null;
    }

    /**
     * Get the outline of a character given the glyph id
     */
    protected synchronized GeneralPath getOutline (int glyphId, float width) {
        // find the glyph itself
        GlyfTable glyf = (GlyfTable) this.font.getTable ("glyf");
        Glyf g = glyf.getGlyph (glyphId);

        GeneralPath gp = null;
        if (g instanceof GlyfSimple) {
            gp = renderSimpleGlyph ((GlyfSimple) g);
        } else if (g instanceof GlyfCompound) {
            gp = renderCompoundGlyph (glyf, (GlyfCompound) g);
        } else {
            gp = new GeneralPath ();
        }

        // calculate the advance
        HmtxTable hmtx = (HmtxTable) this.font.getTable ("hmtx");
        float advance = hmtx.getAdvance (glyphId) / this.unitsPerEm;

        // scale the glyph to match the desired advance
        float widthfactor = width / advance;

		// the base transform scales the glyph to 1x1
		AffineTransform at = AffineTransform.getScaleInstance(1 / this.unitsPerEm, 1 / this.unitsPerEm);
		if (advance != 0) {
			at.concatenate(AffineTransform.getScaleInstance(widthfactor, 1));
		}

        gp.transform (at);

        return gp;
    }

    /**
     * Render a simple glyf
     */
    protected GeneralPath renderSimpleGlyph (GlyfSimple g) {
        // the current contour
        int curContour = 0;

        // the render state
        RenderState rs = new RenderState ();
        rs.gp = new GeneralPath ();

        for (int i = 0; i < g.getNumPoints (); i++) {
            PointRec rec = new PointRec (g, i);

            if (rec.onCurve) {
                addOnCurvePoint (rec, rs);
            } else {
                addOffCurvePoint (rec, rs);
            }

            // see if we just ended a contour
            if (i == g.getContourEndPoint (curContour)) {
                curContour++;

                if (rs.firstOff != null) {
                    addOffCurvePoint (rs.firstOff, rs);
                }

                if (rs.firstOn != null) {
                    addOnCurvePoint (rs.firstOn, rs);
                }

                rs.firstOn = null;
                rs.firstOff = null;
                rs.prevOff = null;
            }
        }

        return rs.gp;
    }

    /**
     * Render a compound glyf
     */
    protected GeneralPath renderCompoundGlyph (GlyfTable glyf, GlyfCompound g) {
        GeneralPath gp = new GeneralPath ();

        for (int i = 0; i < g.getNumComponents (); i++) {
            // find and render the component glyf
            Glyf gl = glyf.getGlyph (g.getGlyphIndex (i));
            GeneralPath path = null;
            if (gl instanceof GlyfSimple) {
                path = renderSimpleGlyph ((GlyfSimple) gl);
            } else if (gl instanceof GlyfCompound) {
                path = renderCompoundGlyph (glyf, (GlyfCompound) gl);
            } else {
                throw new RuntimeException (
                        "Unsupported glyph type " + gl.getClass ().getCanonicalName ());
            }

            // multiply the translations by units per em
            double[] matrix = g.getTransform (i);

            // transform the path
            path.transform (new AffineTransform (matrix));

            // add it to the global path
            gp.append (path, false);
        }

        return gp;
    }

    /** add a point on the curve */
    private void addOnCurvePoint (PointRec rec, RenderState rs) {
        // if the point is on the curve, either move to it,
        // or draw a line from the previous point
        if (rs.firstOn == null) {
            rs.firstOn = rec;
            rs.gp.moveTo (rec.x, rec.y);
        } else if (rs.prevOff != null) {
            rs.gp.quadTo (rs.prevOff.x, rs.prevOff.y, rec.x, rec.y);
            rs.prevOff = null;
        } else {
            rs.gp.lineTo (rec.x, rec.y);
        }
    }

    /** add a point off the curve */
    private void addOffCurvePoint (PointRec rec, RenderState rs) {
        if (rs.prevOff != null) {
            PointRec oc = new PointRec ((rec.x + rs.prevOff.x) / 2,
                    (rec.y + rs.prevOff.y) / 2,
                    true);
            addOnCurvePoint (oc, rs);
        } else if (rs.firstOn == null) {
            rs.firstOff = rec;
        }
        rs.prevOff = rec;
    }

    class RenderState {
        // the shape itself

        GeneralPath gp;
        // the first off and on-curve points in the current segment

        PointRec firstOn;

        PointRec firstOff;
        // the previous off and on-curve points in the current segment

        PointRec prevOff;

    }

    /** a point on the stack of points */
    static class PointRec {

        int x;

        int y;

        boolean onCurve;

        public PointRec (int x, int y, boolean onCurve) {
            this.x = x;
            this.y = y;
            this.onCurve = onCurve;
        }

        public PointRec (GlyfSimple g, int idx) {
            this.x = g.getXCoord (idx);
            this.y = g.getYCoord (idx);
            this.onCurve = g.onCurve (idx);
        }
    }
}