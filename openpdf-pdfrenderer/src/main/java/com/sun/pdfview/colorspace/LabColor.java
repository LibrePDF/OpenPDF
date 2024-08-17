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
 * A ColorSpace for Lab color
 * @author Mike Wessler
 */
public class LabColor extends ColorSpace {
    float white[]= {1f, 1f, 1f};
    float black[]= {0, 0, 0};
    float range[]= {-100f, 100f, -100f, 100f};
    static ColorSpace cie= ColorSpace.getInstance(ColorSpace.CS_sRGB);

    /**
     * Create a new Lab color space object, given the description in
     * a PDF dictionary.
     * @param obj a dictionary that contains an Array of 3 Numbers for
     * "WhitePoint" and "BlackPoint", and an array of 4 Numbers for
     * "Range".
     */
    public LabColor(PDFObject obj) throws IOException {
	// obj is a dictionary that has the following parts:
	// WhitePoint [a b c]
	// BlackPoint [a b c]
	// Gamma a
	super(TYPE_Lab, 3);
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
	ary= obj.getDictRef("Range");
	if (ary!=null) {
	    for (int i=0; i<4; i++) {
		this.range[i]= ary.getAt(i).getFloatValue();
	    }
	}
    }

    /**
     * get the number of components for this color space (3)
     */
    @Override public int getNumComponents() {
	return 3;
    }

    /**
     * Stage 2 of the conversion algorithm.  Pulled out because
     * it gets invoked for each component
     */
    public final float stage2(float s1) {
	return (s1>=6f/29f)?s1*s1*s1:108f/841f*(s1-4f/29f);
    }

    /**
     * convert from Lab to RGB
     * @param comp the Lab values (0-1)
     * @return the RGB values (0-1)
     */
    @Override
	public float[] toRGB(float comp[]) {
	if (comp.length==3) {
	    float l= (comp[0]+16)/116+comp[1]/500;
	    float m= (comp[0]+16)/116;
	    float n= (comp[0]+16)/116-comp[2]/200;
	    float xyz[]= {
		this.white[0]*stage2(l),
		this.white[0]*stage2(m),
		this.white[0]*stage2(n)};
	    float rgb[]= cie.fromCIEXYZ(xyz);
	    return rgb;
	} else {
	    return this.black;
	}
    }

    /**
     * convert from RGB to Lab.  NOT IMPLEMENTED
     */
    @Override
	public float[] fromRGB(float[] rgbvalue) {
	return new float[3];
    }

    /**
     * convert from CIEXYZ to Lab.  NOT IMPLEMENTED
     */
    @Override
	public float[] fromCIEXYZ(float[] colorvalue) {
	return new float[3];
    }

    /**
     * get the type of this colorspace (TYPE_Lab)
     */
    @Override public int getType() {
	return TYPE_Lab;
    }

    /**
     * convert from Lab to CIEXYZ.   NOT IMPLEMENTED
     */
    @Override
	public float[] toCIEXYZ(float[] colorvalue) {
	return new float[3];
    }
    
}