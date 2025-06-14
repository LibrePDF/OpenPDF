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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.pdfview.BaseWatchable;
import com.sun.pdfview.PDFDebugger;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFParser;

/**
 * A Type 3 Font, in which each glyph consists of a sequence of PDF
 * commands.
 * 
 * @author Mike Wessler
 */
public class Type3Font extends PDFFont {

    /** resources for the character definitions */
    HashMap<String,PDFObject> rsrc;
    /** the character processes, mapped by name */
    Map charProcs;
    /** bounding box for the font characters */
    Rectangle2D bbox;
    /** affine transform for the font characters */
    AffineTransform at;
    /** the widths */
    float[] widths;
    /** the start code */
    int firstChar;
    /** the end code */
    int lastChar;

    /**
     * Generate a Type 3 font.
     * @param baseFont the postscript name of this font
     * @param fontObj a dictionary containing references to the character
     * definitions and font information
     * @param resources a set of resources used by the character definitions
     * @param descriptor the descriptor for this font
     */
    public Type3Font(String baseFont, PDFObject fontObj,
            HashMap<String,PDFObject> resources, PDFFontDescriptor descriptor) throws IOException {
        super(baseFont, descriptor);

        this.rsrc = new HashMap<String,PDFObject>();

        if (resources != null) {
            this.rsrc.putAll(resources);
        }

        // get the transform matrix
        PDFObject matrix = fontObj.getDictRef("FontMatrix");
        float matrixAry[] = new float[6];
        for (int i = 0; i < 6; i++) {
            matrixAry[i] = matrix.getAt(i).getFloatValue();
        }
        this.at = new AffineTransform(matrixAry);

        // get the scale from the matrix
        float scale = matrixAry[0] + matrixAry[2];

        // put all the resources in a Hash
        PDFObject rsrcObj = fontObj.getDictRef("Resources");
        if (rsrcObj != null) {
            this.rsrc.putAll(rsrcObj.getDictionary());
        }

        // get the character processes, indexed by name
        this.charProcs = fontObj.getDictRef("CharProcs").getDictionary();

        // get the font bounding box
        bbox = PDFFile.parseNormalisedRectangle(fontObj.getDictRef("FontBBox"));
        if (bbox.isEmpty()) {
            bbox = null;
        }

        // get the widths
        PDFObject[] widthArray = fontObj.getDictRef("Widths").getArray();
        this.widths = new float[widthArray.length];
        for (int i = 0; i < widthArray.length; i++) {
            this.widths[i] = widthArray[i].getFloatValue();
        }

        // get first and last chars
        this.firstChar = fontObj.getDictRef("FirstChar").getIntValue();
        this.lastChar = fontObj.getDictRef("LastChar").getIntValue();
    }

    /**
     * Get the first character code
     */
    public int getFirstChar() {
        return this.firstChar;
    }

    /**
     * Get the last character code
     */
    public int getLastChar() {
        return this.lastChar;
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
    @Override
	protected PDFGlyph getGlyph(char src, String name) {
        if (name == null) {
            throw new IllegalArgumentException("Glyph name required for Type3 font!" +
                    "Source character: " + (int) src);
        }

        PDFObject pageObj = (PDFObject) this.charProcs.get(name);
        if (pageObj == null) {
            // glyph not found.  Return an empty glyph...
            return new PDFGlyph(src, name, new GeneralPath(), new Point2D.Float(0, 0));
        }

        try {
            PDFPage page = new PDFPage(this.bbox, 0);
            page.addXform(this.at);

            PDFParser prc = new PDFParser(page, pageObj.getStream(), this.rsrc);
            prc.go(true);

            float width = this.widths[src - this.firstChar];

            Point2D advance = new Point2D.Float(width, 0);
            advance = this.at.transform(advance, null);

            return new PDFGlyph(src, name, page, advance);
        } catch (IOException ioe) {
            // help!
            PDFDebugger.debug("IOException in Type3 font: " + ioe);
            BaseWatchable.getErrorHandler().publishException(ioe);
            return null;
        }
    }
}