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

import java.io.IOException;

import com.sun.pdfview.PDFObject;

/**
 * Type 0 fonts are composite fonts with a CMAP to map between
 * source character codes and destination fonts/codes
 *
 * @author  Jonathan Kaplan
 */
public class Type0Font extends PDFFont {
   
    /**
     * The decendant fonts, indexed by font number from the CMAP
     */
    PDFFont[] fonts;
        
    /** Creates a new instance of Type0Font */
    public Type0Font(String baseFont, PDFObject fontObj,
                     PDFFontDescriptor descriptor) throws IOException {
        super (baseFont, descriptor);
                         
        PDFObject[] descendantFonts = fontObj.getDictRef("DescendantFonts").getArray();
        
        this.fonts = new PDFFont[descendantFonts.length];
        
        for (int i = 0; i < descendantFonts.length; i++) {
            PDFFont descFont = PDFFont.getFont(descendantFonts[i], null);
            if (descFont instanceof CIDFontType0) {
            	((CIDFontType0)descFont).parseToUnicodeMap(fontObj);
            }
			this.fonts[i] = descFont;
        }
    }
    
    /** 
     * Get a descendant font of this font by fontId
     */
    public PDFFont getDescendantFont(int fontID) {
        return this.fonts[fontID];
    }
    
    /**
     * Get a character from the first font in the descendant fonts array
     */
    @Override
	protected PDFGlyph getGlyph(char src, String name) {
        return (getDescendantFont(0).getGlyph(src, name));
    }
}