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

package com.sun.pdfview.decode;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * decode an array of hex nybbles into a byte array
 *
 * @author Mike Wessler
 */
public class ASCIIHexDecode {
    private ByteBuffer buf;
    
    /**
     * initialize the decoder with an array of bytes in ASCIIHex format
     */
    private ASCIIHexDecode(ByteBuffer buf) {
	this.buf = buf;
    }

    /**
     * get the next character from the input
     * @return a number from 0-15, or -1 for the end character
     */
    private int readHexDigit() throws PDFParseException {    
        // read until we hit a non-whitespace character or the
        // end of the stream
        while (this.buf.remaining() > 0) {
            int c = this.buf.get();
        
            // see if we found a useful character
            if (!PDFFile.isWhiteSpace((char) c)) {
                if (c >= '0' && c <= '9') {
                    c -= '0';
                } else if (c >= 'a' && c <= 'f') {
                    c -= 'a' - 10;
                } else if (c >= 'A' && c <= 'F') {
                    c -= 'A' - 10;
                } else if (c == '>') {
                    c = -1;
                } else {
                    // unknown character
                    throw new PDFParseException("Bad character " + c + 
                                                "in ASCIIHex decode");
                }
                
                // return the useful character
                return c;
            }
        }
        
        // end of stream reached
	throw new PDFParseException("Short stream in ASCIIHex decode");
    }

    /**
     * decode the array
     * @return the decoded bytes
     */
    private ByteBuffer decode() throws PDFParseException {
        // start at the beginning of the buffer
        buf.rewind();

        // allocate the output buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while (true) {
            int first = readHexDigit();
            if (first == -1) {
                break;
            }
            int second = readHexDigit();

            if (second == -1) {
                baos.write((byte) (first << 4));
                break;
            } else {
                baos.write((byte) ((first << 4) + second));
            }
        }

        return ByteBuffer.wrap(baos.toByteArray());
    }

    /**
     * decode an array of bytes in ASCIIHex format.
     * <p>
     * ASCIIHex format consists of a sequence of Hexidecimal
     * digits, with possible whitespace, ending with the
     * '&gt;' character.
     * 
     * @param buf the encoded ASCII85 characters in a byte
     *        buffer
     * @param params parameters to the decoder (ignored)
     * @return the decoded bytes
     */
    public static ByteBuffer decode(ByteBuffer buf, PDFObject params)
	throws PDFParseException 
    {
	ASCIIHexDecode me = new ASCIIHexDecode(buf);
	return me.decode();
    }
}