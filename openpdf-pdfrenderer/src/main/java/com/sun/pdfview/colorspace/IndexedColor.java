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
package com.sun.pdfview.colorspace;

import java.awt.Color;
import java.io.IOException;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPaint;

/**
 * A PDFColorSpace for an IndexedColor model
 *
 * @author Mike Wessler
 */
public class IndexedColor extends PDFColorSpace {

    /**
     * r,g,and b components of the color table as a single array, for
     * Java's IndexColorModel */
    protected byte[] finalcolors;
    /** the color table */
    Color table[];
    /** size of the color table */
    int count;
    /** number of channels in the base Color Space (unused) */
    int nchannels = 1;

    /**
     * create a new IndexColor PDFColorSpace based on another PDFColorSpace,
     * a count of colors, and a stream of values.  Every consecutive n bytes
     * of the stream is interpreted as a color in the base ColorSpace, where
     * n is the number of components in that color space.
     *
     * @param base the color space in which the data is interpreted
     * @param count the number of colors in the table
     * @param stream a stream of bytes.  The number of bytes must be count*n,
     * where n is the number of components in the base colorspace.
     */
    public IndexedColor(PDFColorSpace base, int count, PDFObject stream) throws IOException {
        super(null);
        count++;
        this.count = count;
        byte[] data = stream.getStream();
        this.nchannels = base.getNumComponents();
        boolean offSized = (data.length / this.nchannels) < count;
        this.finalcolors = new byte[3 * count];
        this.table = new Color[count];
        float comps[] = new float[this.nchannels];
        int loc = 0;
        int finalloc = 0;
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < comps.length; j++) {
                if (loc < data.length) {
                    comps[j] = ((data[loc++]) & 0xff) / 255f;
                } else {
                    comps[j] = 1.0f;
                }
            }
            this.table[i] = (Color) base.getPaint(comps).getPaint();
            this.finalcolors[finalloc++] = (byte) this.table[i].getRed();
            this.finalcolors[finalloc++] = (byte) this.table[i].getGreen();
            this.finalcolors[finalloc++] = (byte) this.table[i].getBlue();
        }
    }

    /**
     * create a new IndexColor PDFColorSpace based on a table of colors.  
     * 
     * @param table an array of colors
     */
    public IndexedColor(Color[] table) throws IOException {
        super(null);

        this.count = table.length;
        this.table = table;

        this.finalcolors = new byte[3 * this.count];
        this.nchannels = 3;

        int loc = 0;

        for (int i = 0; i < this.count; i++) {
            this.finalcolors[loc++] = (byte) table[i].getRed();
            this.finalcolors[loc++] = (byte) table[i].getGreen();
            this.finalcolors[loc++] = (byte) table[i].getBlue();
        }
    }

    /**
     * Get the number of indices
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Get the table of color components
     */
    public byte[] getColorComponents() {
        return this.finalcolors;
    }

    /**
     * get the number of components of this colorspace (1)
     */
    @Override
    public int getNumComponents() {
        return 1;
    }

    /**
     * get the color represented by the index.
     * @param components an array of exactly one integer number whose
     * value is between 0 and the size of the color table - 1.
     */
    @Override
    public PDFPaint getPaint(float components[]) {
        return PDFPaint.getPaint(this.table[(int) components[0]]);
    }
}
