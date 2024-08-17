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

import com.sun.pdfview.PDFPaint;
import com.sun.pdfview.function.PDFFunction;

/**
 * A color space that uses another color space to return values, and a
 * function to map between values in the input and input values to the
 * alternate color space
 */ 
public class AlternateColorSpace extends PDFColorSpace {
    /** The alternate color space */
    private PDFColorSpace alternate;
    
    /** The function */
    private PDFFunction function;
    
    private AltColorSpace altcolorspace;
    
    /** Creates a new instance of AlternateColorSpace */
    public AlternateColorSpace(PDFColorSpace alternate, PDFFunction function) {
        super(null);
        
        this.alternate = alternate;
        this.function = function;
    }
    
    /**
     * get the number of components expected in the getPaint command
     */
    @Override public int getNumComponents() {
	if (this.function != null) {
            return this.function.getNumInputs();
        } else {
            return this.alternate.getNumComponents();
        }
    }

    /**
     * get the PDFPaint representing the color described by the
     * given color components
     * @param components the color components corresponding to the given
     * colorspace
     * @return a PDFPaint object representing the closest Color to the
     * given components.
     */
    @Override public PDFPaint getPaint(float[] components) {
        if (this.function != null) {
            // translate values using function
            components = this.function.calculate(components);
        }
        
        return this.alternate.getPaint(components);
    }
    
    /**
     * get the original Java ColorSpace.
     */
    @Override public ColorSpace getColorSpace() {
    	if (altcolorspace == null) altcolorspace = new AltColorSpace(function, alternate.getColorSpace());
    	return altcolorspace;
    	//return this.alternate.getColorSpace();
    }
    
	/*************************************************************************
	 * Get the PDF function
	 * @return PDFFunction
	 ************************************************************************/
	public PDFFunction getFunktion() {
		return this.function;		
	}
	
	
}
