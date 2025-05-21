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
import java.awt.color.ColorSpace;

import com.sun.pdfview.PDFPaint;

/**
 * A color space used to implement masks.  For now, the only type of mask
 * supported is one where the image pixels specify where to paint, and the
 * painting itself is done in a pre-specified PDF Paint.
 */
public class MaskColorSpace extends ColorSpace {
    /** The paint to paint in.  Note this cannot be a pattern or gradient. */
    private final PDFPaint paint;
    
    /** Creates a new instance of PaintColorSpace */
    public MaskColorSpace(PDFPaint paint) {
        super (TYPE_RGB, 1);
        
        this.paint = paint;
    }
    
    @Override
	public float[] fromCIEXYZ(float[] colorvalue) {
        float x = colorvalue[0];
        float y = colorvalue[1];
        float z = colorvalue[2];
        
        float[] mask = new float[1];
        
        if (Math.round(x) > 0 || Math.round(y) > 0 || Math.round(z) > 0) {
            mask[0] = 1;
        } else {
            mask[0] = 0; 
        }
        
        return mask;
    }
    
    @Override
	public float[] fromRGB(float[] rgbvalue) {
        float r = rgbvalue[0];
        float g = rgbvalue[1];
        float b = rgbvalue[2];
        
        float[] mask = new float[1];
        
        if (Math.round(r) > 0 || Math.round(g) > 0 || Math.round(b) > 0) {
            mask[0] = 1;
        } else {
            mask[0] = 0; 
        }
        
        return mask;
    }
    
    ColorSpace cie = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);
    float[] prev1= this.cie.fromRGB(toRGB(new float[] {1.0f}));
    float[] prev0= this.cie.fromRGB(toRGB(new float[] {0.0f}));

    @Override
	public float[] toCIEXYZ(float[] colorvalue) {
	if (colorvalue[0]==1) {
	    return this.prev1;
	} else if (colorvalue[0]==0) {
	    return this.prev0;
	} else {
	    return this.cie.fromRGB(toRGB(colorvalue));
	}
    }
    
    @Override
	public float[] toRGB(float[] colorvalue) {
        return ((Color) this.paint.getPaint()).getRGBColorComponents(null);
    }

    @Override public int getNumComponents() {
	return 1;
    }
    
}
