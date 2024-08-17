/*
 * Copyright 2008 Pirion Systems Pty Ltd, 139 Warry St,
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

import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;

/**
 * <p> Utility methods for dealing with PDF Strings, such as:
 * <ul>
 * <li>{@link #asTextString(String) converting to text strings}
 * <li>{@link #asPDFDocEncoded(String) converting to PDFDocEncoded strings}
 * <li>{@link #asUTF16BEEncoded converting to UTF-16BE strings}
 * <li>converting basic strings between {@link #asBytes(String) byte} and
 * {@link #asBasicString(byte[], int, int) string} representations
 * </ul></p>
 *
 * <p>We refer to basic strings as those corresponding to the PDF 'string' type.
 * PDFRenderer represents these as {@link String}s, though this is somewhat
 * deceiving, as they are, effectively, just sequences of bytes, although byte
 * values &lt;= 127 do correspond to the ASCII character set. Outside of this,
 * the 'string' type, as repesented by basic strings do not possess any
 * character set or encoding, and byte values &gt;= 128 are entirely acceptable.
 * For a basic string as represented by a String, each character has a value
 * less than 256 and is represented in the String as if the bytes represented as
 * it were in ISO-8859-1 encoding. This, however, is merely for convenience. For
 * strings that are user visible, and that don't merely represent some
 * identifying token, the PDF standard employs a 'text string' type that offers
 * the basic string as an encoding of in either UTF-16BE (with a byte order
 * marking) or a specific 8-byte encoding, PDFDocEncoding. Using a basic string
 * without conversion when the actual type is a 'text string' is erroneous
 * (though without consequence if the string consists only of ASCII
 * alphanumeric values). Care must be taken to either convert basic strings to
 * text strings (also expressed as a String) when appropriate, using either the
 * methods in this class, or {@link PDFObject#getTextStringValue()}}. For
 * strings that are 'byte strings', {@link #asBytes(String)} or {@link
 * PDFObject#getStream()} should be used. </p>.
 *
 * @author Luke Kirby
 */
public class PDFStringUtil {

    /**
     * <p>Take a basic PDF string and determine if it is in UTF-16BE encoding
     * by looking at the lead characters for a byte order marking (BOM). If it
     * appears to be UTF-16BE, we return the string representation of the
     * UTF-16BE encoding of those bytes. If the BOM is not present, the bytes
     * from the input string are decoded using the PDFDocEncoding charset.</p>
     *
     * <p>From the PDF Reference 1.7, p158:
     *
     * <blockquote>The text string type is used for character strings that are
     * encoded in either PDFDocEncoding or the UTF-16BE Unicode character
     * encoding scheme. PDFDocEncoding can encode all of the ISO Latin 1
     * character set and is documented in Appendix D. UTF-16BE can encode all
     * Unicode characters. UTF-16BE and Unicode character encoding are
     * described in the Unicode Standard by the Unicode Consortium (see the
     * Bibliography). Note that PDFDocEncoding does not support all Unicode
     * characters whereas UTF-16BE does.</blockquote>
     * </p>
     *
     * @param basicString the basic PDF string, as offered by {@link
     *  PDFObject#getStringValue()}
     * @return either the original input, or the input decoded as UTF-16
     */
    public static String asTextString(String basicString) {
        if (basicString == null) {
            return null;
        }

        if (basicString.length() >= 2) {
            if ((basicString.charAt(0) == (char) 0xFE
                    && basicString.charAt(1) == (char) 0xFF)) {
                // found the BOM!
                return asUTF16BEEncoded(basicString);
            }
        }

        // it's not UTF16-BE encoded, so it must be
        return asPDFDocEncoded(basicString);
    }

    /**
     * Take a basic PDF string and produce a string of its bytes as encoded in
     * PDFDocEncoding. The PDFDocEncoding is described in the PDF Reference.
     *
     * @param basicString the basic PDF string, as offered by {@link
     *  PDFObject#getStringValue()}
     * @return the decoding of the string's bytes in PDFDocEncoding
     */
    public static String asPDFDocEncoded(String basicString) {
        final StringBuilder buf = new StringBuilder(basicString.length());
        for (int i = 0; i < basicString.length(); ++i) {
            final char c = PDF_DOC_ENCODING_MAP[basicString.charAt(i) & 0xFF];
            buf.append(c);
        }
        return buf.toString();
    }

    public byte[] toPDFDocEncoded(String string)
            throws CharacterCodingException {
        // we can just grab array since we know that if charset completes
        // without error then there's the output buffer will be exactly
        // correct in size, since there's only ever 1 byte for one char.
        return new PDFDocCharsetEncoder().encode(CharBuffer.wrap(string)).
                array();
    }

    /**
     * Take a basic PDF string and produce a string from its bytes as an
     * UTF16-BE encoding. The first 2 bytes are presumed to be the big-endian
     * byte markers, 0xFE and 0xFF; that is not checked by this method.
     *
     * @param basicString the basic PDF string, as offered by {@link
     *  PDFObject#getStringValue()}
     * @return the decoding of the string's bytes in UTF16-BE
     */
    public static String asUTF16BEEncoded(String basicString) {
        try {
            return new String(asBytes(basicString),
                    2, basicString.length() - 2, "UTF-16BE");
        } catch (UnsupportedEncodingException e) {
            // UTF-16BE should always be available
            throw new RuntimeException("No UTF-16BE charset!");
        }
    }

    /**
     * Get the corresponding byte array for a basic string. This is effectively
     * the char[] array cast to bytes[], as chars in basic strings only use the
     * least significant byte.
     *
     * @param basicString the basic PDF string, as offered by {@link
     *  PDFObject#getStringValue()}
     * @return the bytes corresponding to its characters
     */
    public static byte[] asBytes(String basicString) {
        final byte[] b = new byte[basicString.length()];
        for (int i = 0; i < b.length; ++i) {
            b[i] = (byte) basicString.charAt(i);
        }
        return b;
    }

    /**
     * Create a basic string from bytes. This is effectively the byte array
     * cast to a char array and turned into a String.
     * @param bytes the source of the bytes for the basic string
     * @param offset the offset into butes where the string starts
     * @param length the number of bytes to turn into a string
     * @return the corresponding string
     */
    public static String asBasicString(
            byte[] bytes, int offset, int length) {
        final char[] c = new char[length];
        for (int i = 0; i < c.length; ++i) {
            c[i] = (char) bytes[i + offset];
        }
        return new String(c);
    }

    /**
     * Create a basic string from bytes. This is effectively the byte array
     * cast to a char array and turned into a String.
     * @param bytes the bytes, all of which are used
     * @return the corresponding string
     */
    public static String asBasicString(byte[] bytes) {
        return asBasicString(bytes, 0, bytes.length);
    }

    /**
     * Maps from PDFDocEncoding bytes to unicode characters. Table generated
     * by PDFDocEncodingMapGenerator.
     */
    final static char[] PDF_DOC_ENCODING_MAP = new char[] {
        0x0000, 0x0001, 0x0002, 0x0003, 0x0004, 0x0005, 0x0006, 0x0007,  //00-07
        0x0008, 0x0009, 0x000A, 0x000B, 0x000C, 0x000D, 0x000E, 0x000F,  //08-0F
        0x0010, 0x0011, 0x0012, 0x0013, 0x0014, 0x0015, 0x0016, 0x0017,  //10-17
        0x02D8, 0x02C7, 0x02C6, 0x02D9, 0x02DD, 0x02DB, 0x02DA, 0x02DC,  //18-1F
        0x0020, 0x0021, 0x0022, 0x0023, 0x0024, 0x0025, 0x0026, 0x0027,  //20-27
        0x0028, 0x0029, 0x002A, 0x002B, 0x002C, 0x002D, 0x002E, 0x002F,  //28-2F
        0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037,  //30-37
        0x0038, 0x0039, 0x003A, 0x003B, 0x003C, 0x003D, 0x003E, 0x003F,  //38-3F
        0x0040, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047,  //40-47
        0x0048, 0x0049, 0x004A, 0x004B, 0x004C, 0x004D, 0x004E, 0x004F,  //48-4F
        0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057,  //50-57
        0x0058, 0x0059, 0x005A, 0x005B, 0x005C, 0x005D, 0x005E, 0x005F,  //58-5F
        0x0060, 0x0061, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067,  //60-67
        0x0068, 0x0069, 0x006A, 0x006B, 0x006C, 0x006D, 0x006E, 0x006F,  //68-6F
        0x0070, 0x0071, 0x0072, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077,  //70-77
        0x0078, 0x0079, 0x007A, 0x007B, 0x007C, 0x007D, 0x007E, 0xFFFD,  //78-7F
        0x2022, 0x2020, 0x2021, 0x2026, 0x2014, 0x2013, 0x0192, 0x2044,  //80-87
        0x2039, 0x203A, 0x2212, 0x2030, 0x201E, 0x201C, 0x201D, 0x2018,  //88-8F
        0x2019, 0x201A, 0x2122, 0xFB01, 0xFB02, 0x0141, 0x0152, 0x0160,  //90-97
        0x0178, 0x017D, 0x0131, 0x0142, 0x0153, 0x0161, 0x017E, 0xFFFD,  //98-9F
        0x20AC, 0x00A1, 0x00A2, 0x00A3, 0x00A4, 0x00A5, 0x00A6, 0x00A7,  //A0-A7
        0x00A8, 0x00A9, 0x00AA, 0x00AB, 0x00AC, 0xFFFD, 0x00AE, 0x00AF,  //A8-AF
        0x00B0, 0x00B1, 0x00B2, 0x00B3, 0x00B4, 0x00B5, 0x00B6, 0x00B7,  //B0-B7
        0x00B8, 0x00B9, 0x00BA, 0x00BB, 0x00BC, 0x00BD, 0x00BE, 0x00BF,  //B8-BF
        0x00C0, 0x00C1, 0x00C2, 0x00C3, 0x00C4, 0x00C5, 0x00C6, 0x00C7,  //C0-C7
        0x00C8, 0x00C9, 0x00CA, 0x00CB, 0x00CC, 0x00CD, 0x00CE, 0x00CF,  //C8-CF
        0x00D0, 0x00D1, 0x00D2, 0x00D3, 0x00D4, 0x00D5, 0x00D6, 0x00D7,  //D0-D7
        0x00D8, 0x00D9, 0x00DA, 0x00DB, 0x00DC, 0x00DD, 0x00DE, 0x00DF,  //D8-DF
        0x00E0, 0x00E1, 0x00E2, 0x00E3, 0x00E4, 0x00E5, 0x00E6, 0x00E7,  //E0-E7
        0x00E8, 0x00E9, 0x00EA, 0x00EB, 0x00EC, 0x00ED, 0x00EE, 0x00EF,  //E8-EF
        0x00F0, 0x00F1, 0x00F2, 0x00F3, 0x00F4, 0x00F5, 0x00F6, 0x00F7,  //F0-F7
        0x00F8, 0x00F9, 0x00FA, 0x00FB, 0x00FC, 0x00FD, 0x00FE, 0x00FF,  //F8-FF
    };



}