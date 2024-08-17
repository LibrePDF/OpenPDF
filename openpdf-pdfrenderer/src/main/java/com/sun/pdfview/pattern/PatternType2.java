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

package com.sun.pdfview.pattern;

import java.io.IOException;
import java.util.Map;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPaint;

/**
 * A type 1 (tiling) pattern
 */
public class PatternType2 extends PDFPattern {
        
    /** the shader */
    private PDFShader shader;
        
    /** Creates a new instance of PatternType1 */
    public PatternType2() {
        super(2);
    }
    
    /**
     * Parse the pattern from the PDFObject
     *
     * Note the resources passed in are ignored...
     */
    @Override
	protected void parse(PDFObject patternObj, Map rsrc) throws IOException
    {
        this.shader = PDFShader.getShader(patternObj.getDictRef("Shading"), rsrc);        
    }
    
    /** 
     * Create a PDFPaint from this pattern and set of components.  
     * This creates a buffered image of this pattern using
     * the given paint, then uses that image to create the correct 
     * TexturePaint to use in the PDFPaint.
     *
     * @param basePaint the base paint to use, or null if not needed
     */
    @Override
	public PDFPaint getPaint(PDFPaint basePaint) {
    	return shader.getPaint();
    }    
}