/* Copyright 2008 Pirion Systems Pty Ltd, 139 Warry St,
 * Fortitude Valley, Queensland, Australia
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

package com.sun.pdfview;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.HashMap;
import java.util.Map;

/**
 * Encodes into a PDFDocEncoding representation. Note that only 256 characters
 * (if that) are represented in the PDFDocEncoding, so users should be
 * prepared to deal with unmappable character exceptions.
 *
 * @see "PDF Reference version 1.7, Appendix D"
 *
 * @author Luke Kirby
 */
public class PDFDocCharsetEncoder extends CharsetEncoder {

    /**
     * Identify whether a particular character preserves the same byte value
     * upon encoding in PDFDocEncoding
     * @param ch the character
     * @return whether the character is identity encoded
     */
    public static boolean isIdentityEncoding(char ch) {
        return ch >= 0 && ch <= 255 && IDENT_PDF_DOC_ENCODING_MAP[ch];

    }

    /**
     * For each character that exists in PDFDocEncoding, identifies whether
     * the byte value in UTF-16BE is the same as it is in PDFDocEncoding
     */
    final static boolean[] IDENT_PDF_DOC_ENCODING_MAP = new boolean[256];

    /**
     * For non-identity encoded characters, maps from the character to
     * the byte value in PDFDocEncoding. If an entry for a non-identity
     * coded character is absent from this map, that character is unmappable
     * in the PDFDocEncoding.
     */
    final static Map<Character,Byte> EXTENDED_TO_PDF_DOC_ENCODING_MAP =
            new HashMap<Character,Byte>();
    static
    {
        for (byte i = 0; i < PDFStringUtil.PDF_DOC_ENCODING_MAP.length; ++i) {
            final char c = PDFStringUtil.PDF_DOC_ENCODING_MAP[i];
            final boolean identical = (c == i);
            IDENT_PDF_DOC_ENCODING_MAP[i] = identical;
            if (!identical) {
                EXTENDED_TO_PDF_DOC_ENCODING_MAP.put(c, i);
            }
        }
    }

    public PDFDocCharsetEncoder() {
        super(null, 1, 1);
    }

    @Override
	protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
        while (in.remaining() > 0) {
            if (out.remaining() < 1) {
                return CoderResult.OVERFLOW;
            }
            final char c = in.get();
            if (c >= 0 && c < 256 && IDENT_PDF_DOC_ENCODING_MAP[c]) {
                out.put((byte) c);
            } else {
                final Byte mapped = EXTENDED_TO_PDF_DOC_ENCODING_MAP.get(c);
                if (mapped != null) {
                    out.put(mapped);
                } else {
                    return CoderResult.unmappableForLength(1);
                }
            }
        }
        return CoderResult.UNDERFLOW;
    }

    @Override
    public boolean isLegalReplacement(byte[] repl) {
        // avoid referencing the non-existent character set
        return true;
    }    
}