/*
 * Copyright 2008 by Kevin Day.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999-2008 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2008 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */
package com.lowagie.text.pdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.pdf.fonts.cmaps.CMap;
import com.lowagie.text.pdf.fonts.cmaps.CMapParser;


/**
 * Implementation of DocumentFont used while parsing PDF streams.
 * @since 2.1.4
 */
public class CMapAwareDocumentFont extends DocumentFont {

	/** The font dictionary. */
    private PdfDictionary fontDic;
    /** the width of a space for this font, in normalized 1000 point units */
    private int spaceWidth;
    /** The CMap constructed from the ToUnicode map from the font's dictionary, if present.
	 *  This CMap transforms CID values into unicode equivalent
	 */
    private CMap toUnicodeCmap;
	/**
	 *	Mapping between CID code (single byte only for now) and unicode equivalent
	 *  as derived by the font's encoding.  Only needed if the ToUnicode CMap is not provided.
	 */
    private char[] cidbyte2uni;
    
    /**
     * Creates an instance of a CMapAwareFont based on an indirect reference to a font.
     * @param refFont	the indirect reference to a font
     */
    public CMapAwareDocumentFont(PRIndirectReference refFont) {
        super(refFont);
        fontDic = (PdfDictionary)PdfReader.getPdfObjectRelease(refFont);

        processToUnicode();
        if (toUnicodeCmap == null)
            processUni2Byte();
        
        spaceWidth = super.getWidth(' ');
        if (spaceWidth == 0){
            spaceWidth = computeAverageWidth();
        }
        
    }

    /**
     * Parses the ToUnicode entry, if present, and constructs a CMap for it
     * @since 2.1.7
     */
    private void processToUnicode(){
        
        PdfObject toUni = fontDic.get(PdfName.TOUNICODE);
        if (toUni != null){
            
            try {
                byte[] touni = PdfReader.getStreamBytes((PRStream)PdfReader.getPdfObjectRelease(toUni));
    
                CMapParser cmapParser = new CMapParser();
                toUnicodeCmap = cmapParser.parse(new ByteArrayInputStream(touni));
            } catch (IOException e) {
                throw new Error("Unable to process ToUnicode map - " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Inverts DocumentFont's uni2byte mapping to obtain a cid-to-unicode mapping based
     * on the font's encoding
     * @since 2.1.7
     */
    private void processUni2Byte(){
        IntHashtable uni2byte = getUni2Byte();
        int e[] = uni2byte.toOrderedKeys();
        
        cidbyte2uni = new char[256];
        for (int k = 0; k < e.length; ++k) {
            int n = uni2byte.get(e[k]);
            
            // this is messy, messy - an encoding can have multiple unicode values mapping to the same cid - we are going to arbitrarily choose the first one
            // what we really need to do is to parse the encoding, and handle the differences info ourselves.  This is a huge duplication of code of what is already
            // being done in DocumentFont, so I really hate to go down that path without seriously thinking about a change in the organization of the Font class hierarchy
            if (cidbyte2uni[n] == 0)
                cidbyte2uni[n] = (char)e[k];
        }
    }
    

    
    /**
     * For all widths of all glyphs, compute the average width in normalized 1000 point units.
     * This is used to give some meaningful width in cases where we need an average font width 
     * (such as if the width of a space isn't specified by a given font)
     * @return the average width of all non-zero width glyphs in the font
     */
    private int computeAverageWidth(){
        int count = 0;
        int total = 0;
        for(int i = 0; i < super.widths.length; i++){
            if(super.widths[i] != 0){
                total += super.widths[i];
                count++;
            }
        }
        return count != 0 ? total/count : 0;
    }
    
    /**
     * @since 2.1.5
     * Override to allow special handling for fonts that don't specify width of space character
     * @see com.lowagie.text.pdf.DocumentFont#getWidth(int)
     */
    public int getWidth(int char1) {
        if (char1 == ' ')
            return spaceWidth;
        
        return super.getWidth(char1);
    }
    
    /**
     * Decodes a single CID (represented by one or two bytes) to a unicode String.
     * @param bytes		the bytes making up the character code to convert
     * @param offset	an offset
     * @param len		a length
     * @return	a String containing the encoded form of the input bytes using the font's encoding.
     */
    private String decodeSingleCID(byte[] bytes, int offset, int len){
        if (toUnicodeCmap != null){
            if (offset + len > bytes.length)
                throw new ArrayIndexOutOfBoundsException(MessageLocalization.getComposedMessage("invalid.index.1", offset + len));
            return toUnicodeCmap.lookup(bytes, offset, len);
        }

        if (len == 1){
            return new String(cidbyte2uni, 0xff & bytes[offset], 1);
        }
        
        throw new Error("Multi-byte glyphs not implemented yet");
    }

    /**
     * Decodes a string of bytes (encoded in the font's encoding) into a unicode string
     * This will use the ToUnicode map of the font, if available, otherwise it uses
     * the font's encoding
     * @param cidbytes    the bytes that need to be decoded
     * @return  the unicode String that results from decoding
     * @since 2.1.7
     */
    public String decode(byte[] cidbytes, final int offset, final int len){
        StringBuffer sb = new StringBuffer(); // it's a shame we can't make this StringBuilder
        for(int i = offset; i < offset + len; i++){
            String rslt = decodeSingleCID(cidbytes, i, 1);
            if (rslt == null){
                rslt = decodeSingleCID(cidbytes, i, 2);
                i++;
            }
            sb.append(rslt);
        }

        return sb.toString();
    }

    /**
     * Encodes bytes to a String.
     * @param bytes		the bytes from a stream
     * @param offset	an offset
     * @param len		a length
     * @return	a String encoded taking into account if the bytes are in unicode or not.
     * @deprecated method name is not indicative of what it does.  Use <code>decode</code> instead.
     */
    public String encode(byte[] bytes, int offset, int len){
        return decode(bytes, offset, len);    
    }
}
