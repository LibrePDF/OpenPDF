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

import java.awt.color.ColorSpace;
import java.io.IOException;

import com.sun.pdfview.PDFObject;

/**
 * A ColorSpace for calibrated gray
 * @author Mike Wessler
 */
public class CalGrayColor extends ColorSpace {
    float white[]= {1f, 1f, 1f};
    float black[]= {0, 0, 0};
    float gamma= 1;
    static ColorSpace cie= ColorSpace.getInstance(ColorSpace.CS_sRGB);

    /**
     * Create a new Calibrated Gray color space object, given
     * the description in a PDF dictionary.
     * @param obj a dictionary that contains an Array of 3 Numbers
     * for "WhitePoint" and "BlackPoint", and a Number for "Gamma"
     */
    public CalGrayColor(PDFObject obj) throws IOException {
	// obj is a dictionary that has the following parts:
	// WhitePoint [a b c]
	// BlackPoint [a b c]
	// Gamma a
	super(TYPE_GRAY, 1);
	PDFObject ary= obj.getDictRef("WhitePoint");
	if (ary!=null) {
	    for(int i=0; i<3; i++) {
		this.white[i]= ary.getAt(i).getFloatValue();
	    }
	}
	ary= obj.getDictRef("BlackPoint");
	if (ary!=null) {
	    for(int i=0; i<3; i++) {
		this.black[i]= ary.getAt(i).getFloatValue();
	    }
	}
	PDFObject g= obj.getDictRef("Gamma");
	if (g!=null) {
	    this.gamma= g.getFloatValue();
	}
    }

    /**
     * Create a new calibrated gray color space object, with the
     * default values for black point, white point and gamma
     */
    public CalGrayColor() {
        super(TYPE_GRAY, 1);
    }
    
    /**
     * get the number of components (1).
     */
    @Override public int getNumComponents() {
	return 1;
    }

    /**
     * convert from Calibrated Gray to RGB.
     * @param comp the gray value (0-1)
     * @return the RGB values (0-1)
     */
    @Override
	public float[] toRGB(float comp[]) {
	if (comp.length==1) {
	    float mul= (float)Math.pow(comp[0], this.gamma);
	    float xyz[] = {
		this.white[0]*mul,
		0,
		0};
	    float rgb[]= cie.fromCIEXYZ(xyz);
	    return rgb;
	} else {
	    return this.black;
	}
    }

    /**
     * convert from RGB to Calibrated Gray.  NOT IMPLEMENTED
     */
    @Override
	public float[] fromRGB(float[] rgbvalue) {
	return new float[1];
    }

    /**
     * convert from CIEXYZ to Calibrated Gray.  NOT IMPLEMENTED
     */
    @Override
	public float[] fromCIEXYZ(float[] colorvalue) {
	return new float[1];
    }

    /**
     * get the type of this ColorSpace (TYPE_GRAY)
     */
    @Override public int getType() {
	return TYPE_GRAY;
    }

    /**
     * convert from Calibrated Gray to CIEXYZ.  NOT IMPLEMENTED
     */
    @Override
	public float[] toCIEXYZ(float[] colorvalue) {
	return new float[3];
    }
    
}
