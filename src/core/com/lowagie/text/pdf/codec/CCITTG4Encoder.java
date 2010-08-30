/*
 * Copyright 2005 by Paulo Soares.
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
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
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
 *
 * This code is base in the libtiff encoder
 */
package com.lowagie.text.pdf.codec;

import com.lowagie.text.pdf.ByteBuffer;

/**
 * Encodes data in the CCITT G4 FAX format.
 */
public class CCITTG4Encoder {
    private int rowbytes;
    private int rowpixels;
    private int bit = 8;
    private int data;
    private byte[] refline;
    private ByteBuffer outBuf = new ByteBuffer(1024);
    private byte[] dataBp;
    private int offsetData;
    private int sizeData;
    
    /**
     * Creates a new encoder.
     * @param width the line width
     */    
    public CCITTG4Encoder(int width) {
        rowpixels = width;
        rowbytes = (rowpixels + 7) / 8;
        refline = new byte[rowbytes];
    }
    
    /**
     * Encodes a number of lines.
     * @param data the data to be encoded
     * @param offset the offset into the data
     * @param size the size of the data to be encoded
     */    
    public void fax4Encode(byte[] data, int offset, int size) {
        dataBp = data;
        offsetData = offset;
        sizeData = size;
        while (sizeData > 0) {
            Fax3Encode2DRow();
            System.arraycopy(dataBp, offsetData, refline, 0, rowbytes);
            offsetData += rowbytes;
            sizeData -= rowbytes;
        }
    }
    

    /**
     * Encodes a full image.
     * @param data the data to encode
     * @param width the image width
     * @param height the image height
     * @return the encoded image
     */    
    public static byte[] compress(byte[] data, int width, int height) {
        CCITTG4Encoder g4 = new CCITTG4Encoder(width);
        g4.fax4Encode(data, 0, g4.rowbytes * height);
        return g4.close();
    }
    
    /**
     * Encodes a number of lines.
     * @param data the data to be encoded
     * @param height the number of lines to encode
     */    
    public void fax4Encode(byte[] data, int height) {
        fax4Encode(data, 0, rowbytes * height);
    }

    private void putcode(int[] table) {
        putBits(table[CODE], table[LENGTH]);
    }
    
    private void putspan(int span, int[][] tab) {
        int code, length;
        
        while (span >= 2624) {
            int[] te = tab[63 + (2560>>6)];
            code = te[CODE];
            length = te[LENGTH];
            putBits(code, length);
            span -= te[RUNLEN];
        }
        if (span >= 64) {
            int[] te = tab[63 + (span>>6)];
            code = te[CODE];
            length = te[LENGTH];
            putBits(code, length);
            span -= te[RUNLEN];
        }
        code = tab[span][CODE];
        length = tab[span][LENGTH];
        putBits(code, length);
    }
    
    private void putBits(int bits, int length) {
        while (length > bit) {
            data |= bits >> (length - bit);
            length -= bit;
            outBuf.append((byte)data);
            data = 0;
            bit = 8;
        }
        data |= (bits & msbmask[length]) << (bit - length);
        bit -= length;
        if (bit == 0) {
            outBuf.append((byte)data);
            data = 0;
            bit = 8;
        }
    }
    
    private void Fax3Encode2DRow() {
        int a0 = 0;
        int a1 = (pixel(dataBp, offsetData, 0) != 0 ? 0 : finddiff(dataBp, offsetData, 0, rowpixels, 0));
        int b1 = (pixel(refline, 0, 0) != 0 ? 0 : finddiff(refline, 0, 0, rowpixels, 0));
        int a2, b2;
        
        for (;;) {
            b2 = finddiff2(refline, 0, b1, rowpixels, pixel(refline, 0,b1));
            if (b2 >= a1) {
                int d = b1 - a1;
                if (!(-3 <= d && d <= 3)) {	/* horizontal mode */
                    a2 = finddiff2(dataBp, offsetData, a1, rowpixels, pixel(dataBp, offsetData,a1));
                    putcode(horizcode);
                    if (a0+a1 == 0 || pixel(dataBp, offsetData, a0) == 0) {
                        putspan(a1-a0, TIFFFaxWhiteCodes);
                        putspan(a2-a1, TIFFFaxBlackCodes);
                    } else {
                        putspan(a1-a0, TIFFFaxBlackCodes);
                        putspan(a2-a1, TIFFFaxWhiteCodes);
                    }
                    a0 = a2;
                } else {			/* vertical mode */
                    putcode(vcodes[d+3]);
                    a0 = a1;
                }
            } else {				/* pass mode */
                putcode(passcode);
                a0 = b2;
            }
            if (a0 >= rowpixels)
                break;
            a1 = finddiff(dataBp, offsetData, a0, rowpixels, pixel(dataBp, offsetData,a0));
            b1 = finddiff(refline, 0, a0, rowpixels, pixel(dataBp, offsetData,a0) ^ 1);
            b1 = finddiff(refline, 0, b1, rowpixels, pixel(dataBp, offsetData,a0));
        }
    }
    
    private void Fax4PostEncode() {
        putBits(EOL, 12);
        putBits(EOL, 12);
        if (bit != 8) {
            outBuf.append((byte)data);
            data = 0;
            bit = 8;
        }
    }
    
    /**
     * Closes the encoder and returns the encoded data.
     * @return the encoded data
     */    
    public byte[] close() {
        Fax4PostEncode();
        return outBuf.toByteArray();
    }
    
    private int pixel(byte[] data, int offset, int bit) {
        if (bit >= rowpixels)
            return 0;
        return ((data[offset + (bit >> 3)] & 0xff) >> (7-((bit)&7))) & 1;
    }
    
    private static int find1span(byte[] bp, int offset, int bs, int be) {
        int bits = be - bs;
        int n, span;
        
        int pos = offset + (bs >> 3);
        /*
         * Check partial byte on lhs.
         */
        if (bits > 0 && (n = (bs & 7)) != 0) {
            span = oneruns[(bp[pos] << n) & 0xff];
            if (span > 8-n)		/* table value too generous */
                span = 8-n;
            if (span > bits)	/* constrain span to bit range */
                span = bits;
            if (n+span < 8)		/* doesn't extend to edge of byte */
                return span;
            bits -= span;
            pos++;
        } else
            span = 0;
        /*
         * Scan full bytes for all 1's.
         */
        while (bits >= 8) {
            if (bp[pos] != -1)	/* end of run */
                return (span + oneruns[bp[pos] & 0xff]);
            span += 8;
            bits -= 8;
            pos++;
        }
        /*
         * Check partial byte on rhs.
         */
        if (bits > 0) {
            n = oneruns[bp[pos] & 0xff];
            span += (n > bits ? bits : n);
        }
        return span;
    }
    
    private static int find0span(byte[] bp, int offset, int bs, int be) {
        int bits = be - bs;
        int n, span;
        
        int pos = offset + (bs >> 3);
        /*
         * Check partial byte on lhs.
         */
        if (bits > 0 && (n = (bs & 7)) != 0) {
            span = zeroruns[(bp[pos] << n) & 0xff];
            if (span > 8-n)		/* table value too generous */
                span = 8-n;
            if (span > bits)	/* constrain span to bit range */
                span = bits;
            if (n+span < 8)		/* doesn't extend to edge of byte */
                return span;
            bits -= span;
            pos++;
        } else
            span = 0;
        /*
         * Scan full bytes for all 1's.
         */
        while (bits >= 8) {
            if (bp[pos] != 0)	/* end of run */
                return (span + zeroruns[bp[pos] & 0xff]);
            span += 8;
            bits -= 8;
            pos++;
        }
        /*
         * Check partial byte on rhs.
         */
        if (bits > 0) {
            n = zeroruns[bp[pos] & 0xff];
            span += (n > bits ? bits : n);
        }
        return span;
    }
    
    private static int finddiff(byte[] bp, int offset, int bs, int be, int color) {
        return bs + (color != 0 ? find1span(bp, offset, bs, be) : find0span(bp, offset, bs, be));
    }
    
    private static int finddiff2(byte[] bp, int offset, int bs, int be, int color) {
        return bs < be ? finddiff(bp, offset, bs, be, color) : be;
    }
    
    private static byte zeroruns[] = {
        8, 7, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4,	/* 0x00 - 0x0f */
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,	/* 0x10 - 0x1f */
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,	/* 0x20 - 0x2f */
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,	/* 0x30 - 0x3f */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,	/* 0x40 - 0x4f */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,	/* 0x50 - 0x5f */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,	/* 0x60 - 0x6f */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,	/* 0x70 - 0x7f */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0x80 - 0x8f */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0x90 - 0x9f */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0xa0 - 0xaf */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0xb0 - 0xbf */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0xc0 - 0xcf */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0xd0 - 0xdf */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0xe0 - 0xef */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0	/* 0xf0 - 0xff */
    };
    
    private static byte oneruns[] = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0x00 - 0x0f */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0x10 - 0x1f */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0x20 - 0x2f */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0x30 - 0x3f */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0x40 - 0x4f */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0x50 - 0x5f */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0x60 - 0x6f */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	/* 0x70 - 0x7f */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,	/* 0x80 - 0x8f */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,	/* 0x90 - 0x9f */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,	/* 0xa0 - 0xaf */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,	/* 0xb0 - 0xbf */
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,	/* 0xc0 - 0xcf */
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,	/* 0xd0 - 0xdf */
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,	/* 0xe0 - 0xef */
        4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 7, 8	/* 0xf0 - 0xff */
    };

    private static final int LENGTH = 0; /* bit length of g3 code */
    private static final int CODE = 1;   /* g3 code */
    private static final int RUNLEN = 2; /* run length in bits */

    private static final int EOL = 0x001; /* EOL code value - 0000 0000 0000 1 */

    /* status values returned instead of a run length */
    private static final int G3CODE_EOL	= -1;     /* NB: ACT_EOL - ACT_WRUNT */
    private static final int G3CODE_INVALID = -2; /* NB: ACT_INVALID - ACT_WRUNT */
    private static final int G3CODE_EOF = -3;     /* end of input data */
    private static final int G3CODE_INCOMP = -4;  /* incomplete run code */

    private int[][] TIFFFaxWhiteCodes = {
        { 8, 0x35, 0 },	/* 0011 0101 */
        { 6, 0x7, 1 },	/* 0001 11 */
        { 4, 0x7, 2 },	/* 0111 */
        { 4, 0x8, 3 },	/* 1000 */
        { 4, 0xB, 4 },	/* 1011 */
        { 4, 0xC, 5 },	/* 1100 */
        { 4, 0xE, 6 },	/* 1110 */
        { 4, 0xF, 7 },	/* 1111 */
        { 5, 0x13, 8 },	/* 1001 1 */
        { 5, 0x14, 9 },	/* 1010 0 */
        { 5, 0x7, 10 },	/* 0011 1 */
        { 5, 0x8, 11 },	/* 0100 0 */
        { 6, 0x8, 12 },	/* 0010 00 */
        { 6, 0x3, 13 },	/* 0000 11 */
        { 6, 0x34, 14 },	/* 1101 00 */
        { 6, 0x35, 15 },	/* 1101 01 */
        { 6, 0x2A, 16 },	/* 1010 10 */
        { 6, 0x2B, 17 },	/* 1010 11 */
        { 7, 0x27, 18 },	/* 0100 111 */
        { 7, 0xC, 19 },	/* 0001 100 */
        { 7, 0x8, 20 },	/* 0001 000 */
        { 7, 0x17, 21 },	/* 0010 111 */
        { 7, 0x3, 22 },	/* 0000 011 */
        { 7, 0x4, 23 },	/* 0000 100 */
        { 7, 0x28, 24 },	/* 0101 000 */
        { 7, 0x2B, 25 },	/* 0101 011 */
        { 7, 0x13, 26 },	/* 0010 011 */
        { 7, 0x24, 27 },	/* 0100 100 */
        { 7, 0x18, 28 },	/* 0011 000 */
        { 8, 0x2, 29 },	/* 0000 0010 */
        { 8, 0x3, 30 },	/* 0000 0011 */
        { 8, 0x1A, 31 },	/* 0001 1010 */
        { 8, 0x1B, 32 },	/* 0001 1011 */
        { 8, 0x12, 33 },	/* 0001 0010 */
        { 8, 0x13, 34 },	/* 0001 0011 */
        { 8, 0x14, 35 },	/* 0001 0100 */
        { 8, 0x15, 36 },	/* 0001 0101 */
        { 8, 0x16, 37 },	/* 0001 0110 */
        { 8, 0x17, 38 },	/* 0001 0111 */
        { 8, 0x28, 39 },	/* 0010 1000 */
        { 8, 0x29, 40 },	/* 0010 1001 */
        { 8, 0x2A, 41 },	/* 0010 1010 */
        { 8, 0x2B, 42 },	/* 0010 1011 */
        { 8, 0x2C, 43 },	/* 0010 1100 */
        { 8, 0x2D, 44 },	/* 0010 1101 */
        { 8, 0x4, 45 },	/* 0000 0100 */
        { 8, 0x5, 46 },	/* 0000 0101 */
        { 8, 0xA, 47 },	/* 0000 1010 */
        { 8, 0xB, 48 },	/* 0000 1011 */
        { 8, 0x52, 49 },	/* 0101 0010 */
        { 8, 0x53, 50 },	/* 0101 0011 */
        { 8, 0x54, 51 },	/* 0101 0100 */
        { 8, 0x55, 52 },	/* 0101 0101 */
        { 8, 0x24, 53 },	/* 0010 0100 */
        { 8, 0x25, 54 },	/* 0010 0101 */
        { 8, 0x58, 55 },	/* 0101 1000 */
        { 8, 0x59, 56 },	/* 0101 1001 */
        { 8, 0x5A, 57 },	/* 0101 1010 */
        { 8, 0x5B, 58 },	/* 0101 1011 */
        { 8, 0x4A, 59 },	/* 0100 1010 */
        { 8, 0x4B, 60 },	/* 0100 1011 */
        { 8, 0x32, 61 },	/* 0011 0010 */
        { 8, 0x33, 62 },	/* 0011 0011 */
        { 8, 0x34, 63 },	/* 0011 0100 */
        { 5, 0x1B, 64 },	/* 1101 1 */
        { 5, 0x12, 128 },	/* 1001 0 */
        { 6, 0x17, 192 },	/* 0101 11 */
        { 7, 0x37, 256 },	/* 0110 111 */
        { 8, 0x36, 320 },	/* 0011 0110 */
        { 8, 0x37, 384 },	/* 0011 0111 */
        { 8, 0x64, 448 },	/* 0110 0100 */
        { 8, 0x65, 512 },	/* 0110 0101 */
        { 8, 0x68, 576 },	/* 0110 1000 */
        { 8, 0x67, 640 },	/* 0110 0111 */
        { 9, 0xCC, 704 },	/* 0110 0110 0 */
        { 9, 0xCD, 768 },	/* 0110 0110 1 */
        { 9, 0xD2, 832 },	/* 0110 1001 0 */
        { 9, 0xD3, 896 },	/* 0110 1001 1 */
        { 9, 0xD4, 960 },	/* 0110 1010 0 */
        { 9, 0xD5, 1024 },	/* 0110 1010 1 */
        { 9, 0xD6, 1088 },	/* 0110 1011 0 */
        { 9, 0xD7, 1152 },	/* 0110 1011 1 */
        { 9, 0xD8, 1216 },	/* 0110 1100 0 */
        { 9, 0xD9, 1280 },	/* 0110 1100 1 */
        { 9, 0xDA, 1344 },	/* 0110 1101 0 */
        { 9, 0xDB, 1408 },	/* 0110 1101 1 */
        { 9, 0x98, 1472 },	/* 0100 1100 0 */
        { 9, 0x99, 1536 },	/* 0100 1100 1 */
        { 9, 0x9A, 1600 },	/* 0100 1101 0 */
        { 6, 0x18, 1664 },	/* 0110 00 */
        { 9, 0x9B, 1728 },	/* 0100 1101 1 */
        { 11, 0x8, 1792 },	/* 0000 0001 000 */
        { 11, 0xC, 1856 },	/* 0000 0001 100 */
        { 11, 0xD, 1920 },	/* 0000 0001 101 */
        { 12, 0x12, 1984 },	/* 0000 0001 0010 */
        { 12, 0x13, 2048 },	/* 0000 0001 0011 */
        { 12, 0x14, 2112 },	/* 0000 0001 0100 */
        { 12, 0x15, 2176 },	/* 0000 0001 0101 */
        { 12, 0x16, 2240 },	/* 0000 0001 0110 */
        { 12, 0x17, 2304 },	/* 0000 0001 0111 */
        { 12, 0x1C, 2368 },	/* 0000 0001 1100 */
        { 12, 0x1D, 2432 },	/* 0000 0001 1101 */
        { 12, 0x1E, 2496 },	/* 0000 0001 1110 */
        { 12, 0x1F, 2560 },	/* 0000 0001 1111 */
        { 12, 0x1, G3CODE_EOL },	/* 0000 0000 0001 */
        { 9, 0x1, G3CODE_INVALID },	/* 0000 0000 1 */
        { 10, 0x1, G3CODE_INVALID },	/* 0000 0000 01 */
        { 11, 0x1, G3CODE_INVALID },	/* 0000 0000 001 */
        { 12, 0x0, G3CODE_INVALID }	/* 0000 0000 0000 */
    };

    private int[][] TIFFFaxBlackCodes = {
        { 10, 0x37, 0 },	/* 0000 1101 11 */
        { 3, 0x2, 1 },	/* 010 */
        { 2, 0x3, 2 },	/* 11 */
        { 2, 0x2, 3 },	/* 10 */
        { 3, 0x3, 4 },	/* 011 */
        { 4, 0x3, 5 },	/* 0011 */
        { 4, 0x2, 6 },	/* 0010 */
        { 5, 0x3, 7 },	/* 0001 1 */
        { 6, 0x5, 8 },	/* 0001 01 */
        { 6, 0x4, 9 },	/* 0001 00 */
        { 7, 0x4, 10 },	/* 0000 100 */
        { 7, 0x5, 11 },	/* 0000 101 */
        { 7, 0x7, 12 },	/* 0000 111 */
        { 8, 0x4, 13 },	/* 0000 0100 */
        { 8, 0x7, 14 },	/* 0000 0111 */
        { 9, 0x18, 15 },	/* 0000 1100 0 */
        { 10, 0x17, 16 },	/* 0000 0101 11 */
        { 10, 0x18, 17 },	/* 0000 0110 00 */
        { 10, 0x8, 18 },	/* 0000 0010 00 */
        { 11, 0x67, 19 },	/* 0000 1100 111 */
        { 11, 0x68, 20 },	/* 0000 1101 000 */
        { 11, 0x6C, 21 },	/* 0000 1101 100 */
        { 11, 0x37, 22 },	/* 0000 0110 111 */
        { 11, 0x28, 23 },	/* 0000 0101 000 */
        { 11, 0x17, 24 },	/* 0000 0010 111 */
        { 11, 0x18, 25 },	/* 0000 0011 000 */
        { 12, 0xCA, 26 },	/* 0000 1100 1010 */
        { 12, 0xCB, 27 },	/* 0000 1100 1011 */
        { 12, 0xCC, 28 },	/* 0000 1100 1100 */
        { 12, 0xCD, 29 },	/* 0000 1100 1101 */
        { 12, 0x68, 30 },	/* 0000 0110 1000 */
        { 12, 0x69, 31 },	/* 0000 0110 1001 */
        { 12, 0x6A, 32 },	/* 0000 0110 1010 */
        { 12, 0x6B, 33 },	/* 0000 0110 1011 */
        { 12, 0xD2, 34 },	/* 0000 1101 0010 */
        { 12, 0xD3, 35 },	/* 0000 1101 0011 */
        { 12, 0xD4, 36 },	/* 0000 1101 0100 */
        { 12, 0xD5, 37 },	/* 0000 1101 0101 */
        { 12, 0xD6, 38 },	/* 0000 1101 0110 */
        { 12, 0xD7, 39 },	/* 0000 1101 0111 */
        { 12, 0x6C, 40 },	/* 0000 0110 1100 */
        { 12, 0x6D, 41 },	/* 0000 0110 1101 */
        { 12, 0xDA, 42 },	/* 0000 1101 1010 */
        { 12, 0xDB, 43 },	/* 0000 1101 1011 */
        { 12, 0x54, 44 },	/* 0000 0101 0100 */
        { 12, 0x55, 45 },	/* 0000 0101 0101 */
        { 12, 0x56, 46 },	/* 0000 0101 0110 */
        { 12, 0x57, 47 },	/* 0000 0101 0111 */
        { 12, 0x64, 48 },	/* 0000 0110 0100 */
        { 12, 0x65, 49 },	/* 0000 0110 0101 */
        { 12, 0x52, 50 },	/* 0000 0101 0010 */
        { 12, 0x53, 51 },	/* 0000 0101 0011 */
        { 12, 0x24, 52 },	/* 0000 0010 0100 */
        { 12, 0x37, 53 },	/* 0000 0011 0111 */
        { 12, 0x38, 54 },	/* 0000 0011 1000 */
        { 12, 0x27, 55 },	/* 0000 0010 0111 */
        { 12, 0x28, 56 },	/* 0000 0010 1000 */
        { 12, 0x58, 57 },	/* 0000 0101 1000 */
        { 12, 0x59, 58 },	/* 0000 0101 1001 */
        { 12, 0x2B, 59 },	/* 0000 0010 1011 */
        { 12, 0x2C, 60 },	/* 0000 0010 1100 */
        { 12, 0x5A, 61 },	/* 0000 0101 1010 */
        { 12, 0x66, 62 },	/* 0000 0110 0110 */
        { 12, 0x67, 63 },	/* 0000 0110 0111 */
        { 10, 0xF, 64 },	/* 0000 0011 11 */
        { 12, 0xC8, 128 },	/* 0000 1100 1000 */
        { 12, 0xC9, 192 },	/* 0000 1100 1001 */
        { 12, 0x5B, 256 },	/* 0000 0101 1011 */
        { 12, 0x33, 320 },	/* 0000 0011 0011 */
        { 12, 0x34, 384 },	/* 0000 0011 0100 */
        { 12, 0x35, 448 },	/* 0000 0011 0101 */
        { 13, 0x6C, 512 },	/* 0000 0011 0110 0 */
        { 13, 0x6D, 576 },	/* 0000 0011 0110 1 */
        { 13, 0x4A, 640 },	/* 0000 0010 0101 0 */
        { 13, 0x4B, 704 },	/* 0000 0010 0101 1 */
        { 13, 0x4C, 768 },	/* 0000 0010 0110 0 */
        { 13, 0x4D, 832 },	/* 0000 0010 0110 1 */
        { 13, 0x72, 896 },	/* 0000 0011 1001 0 */
        { 13, 0x73, 960 },	/* 0000 0011 1001 1 */
        { 13, 0x74, 1024 },	/* 0000 0011 1010 0 */
        { 13, 0x75, 1088 },	/* 0000 0011 1010 1 */
        { 13, 0x76, 1152 },	/* 0000 0011 1011 0 */
        { 13, 0x77, 1216 },	/* 0000 0011 1011 1 */
        { 13, 0x52, 1280 },	/* 0000 0010 1001 0 */
        { 13, 0x53, 1344 },	/* 0000 0010 1001 1 */
        { 13, 0x54, 1408 },	/* 0000 0010 1010 0 */
        { 13, 0x55, 1472 },	/* 0000 0010 1010 1 */
        { 13, 0x5A, 1536 },	/* 0000 0010 1101 0 */
        { 13, 0x5B, 1600 },	/* 0000 0010 1101 1 */
        { 13, 0x64, 1664 },	/* 0000 0011 0010 0 */
        { 13, 0x65, 1728 },	/* 0000 0011 0010 1 */
        { 11, 0x8, 1792 },	/* 0000 0001 000 */
        { 11, 0xC, 1856 },	/* 0000 0001 100 */
        { 11, 0xD, 1920 },	/* 0000 0001 101 */
        { 12, 0x12, 1984 },	/* 0000 0001 0010 */
        { 12, 0x13, 2048 },	/* 0000 0001 0011 */
        { 12, 0x14, 2112 },	/* 0000 0001 0100 */
        { 12, 0x15, 2176 },	/* 0000 0001 0101 */
        { 12, 0x16, 2240 },	/* 0000 0001 0110 */
        { 12, 0x17, 2304 },	/* 0000 0001 0111 */
        { 12, 0x1C, 2368 },	/* 0000 0001 1100 */
        { 12, 0x1D, 2432 },	/* 0000 0001 1101 */
        { 12, 0x1E, 2496 },	/* 0000 0001 1110 */
        { 12, 0x1F, 2560 },	/* 0000 0001 1111 */
        { 12, 0x1, G3CODE_EOL },	/* 0000 0000 0001 */
        { 9, 0x1, G3CODE_INVALID },	/* 0000 0000 1 */
        { 10, 0x1, G3CODE_INVALID },	/* 0000 0000 01 */
        { 11, 0x1, G3CODE_INVALID },	/* 0000 0000 001 */
        { 12, 0x0, G3CODE_INVALID }	/* 0000 0000 0000 */
    };
    
    private int[] horizcode =
        { 3, 0x1, 0 };		/* 001 */
    private int[] passcode =
        { 4, 0x1, 0 };		/* 0001 */
    private int[][] vcodes = {
        { 7, 0x03, 0 },	/* 0000 011 */
        { 6, 0x03, 0 },	/* 0000 11 */
        { 3, 0x03, 0 },	/* 011 */
        { 1, 0x1, 0 },		/* 1 */
        { 3, 0x2, 0 },		/* 010 */
        { 6, 0x02, 0 },	/* 0000 10 */
        { 7, 0x02, 0 }		/* 0000 010 */
    };
    private int[] msbmask =
    { 0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff };
}
