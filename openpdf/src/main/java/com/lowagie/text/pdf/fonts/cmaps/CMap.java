/**
 * Copyright (c) 2005, www.fontbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of fontbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.fontbox.org
 *
 */
package com.lowagie.text.pdf.fonts.cmaps;

import java.io.IOException;
import com.lowagie.text.error_messages.MessageLocalization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a CMap file.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @since	2.1.4
 */
public class CMap
{
    private List codeSpaceRanges = new ArrayList();
    private Map singleByteMappings = new HashMap();
    private Map doubleByteMappings = new HashMap();

    /**
     * Creates a new instance of CMap.
     */
    public CMap()
    {
        //default constructor
    }
    
    /**
     * This will tell if this cmap has any one byte mappings.
     * 
     * @return true If there are any one byte mappings, false otherwise.
     */
    public boolean hasOneByteMappings()
    {
        return !singleByteMappings.isEmpty();
    }
    
    /**
     * This will tell if this cmap has any two byte mappings.
     * 
     * @return true If there are any two byte mappings, false otherwise.
     */
    public boolean hasTwoByteMappings()
    {
        return !doubleByteMappings.isEmpty();
    }

    /**
     * This will perform a lookup into the map.
     *
     * @param code The code used to lookup.
     * @param offset The offset into the byte array.
     * @param length The length of the data we are getting.
     *
     * @return The string that matches the lookup.
     */
    public String lookup( byte[] code, int offset, int length )
    {

        String result = null;
        Integer key = null;
        if( length == 1 )
        {
            
            key = new Integer( code[offset] & 0xff );
            result = (String)singleByteMappings.get( key );
        }
        else if( length == 2 )
        {
            int intKey = code[offset] & 0xff;
            intKey <<= 8;
            intKey += code[offset+1] & 0xff;
            key = new Integer( intKey );

            result = (String)doubleByteMappings.get( key );
        }

        return result;
    }

    /**
     * This will add a mapping.
     *
     * @param src The src to the mapping.
     * @param dest The dest to the mapping.
     *
     * @throws IOException if the src is invalid.
     */
    public void addMapping( byte[] src, String dest ) throws IOException
    {
        if( src.length == 1 )
        {
            singleByteMappings.put( new Integer( src[0] & 0xff ), dest );
        }
        else if( src.length == 2 )
        {
            int intSrc = src[0]&0xFF;
            intSrc <<= 8;
            intSrc |= (src[1]&0xFF);
            doubleByteMappings.put( new Integer( intSrc), dest );
        }
        else
        {
            throw new IOException(MessageLocalization.getComposedMessage("mapping.code.should.be.1.or.two.bytes.and.not.1", src.length));
        }
    }


    /**
     * This will add a codespace range.
     *
     * @param range A single codespace range.
     */
    public void addCodespaceRange( CodespaceRange range )
    {
        codeSpaceRanges.add( range );
    }

    /**
     * Getter for property codeSpaceRanges.
     *
     * @return Value of property codeSpaceRanges.
     */
    public List getCodeSpaceRanges()
    {
        return codeSpaceRanges;
    }

}
