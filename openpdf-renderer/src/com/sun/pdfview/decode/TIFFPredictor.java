/*
 * $Id: TIFFPredictor.java,v 1.1 2010-05-23 22:07:04 lujke Exp $
 * 
 * Copyright 2010 Pirion Systems Pty Ltd, 139 Warry St,
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

package com.sun.pdfview.decode;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Undo prediction based on the TIFF Predictor 2 algorithm
 */
public class TIFFPredictor extends Predictor {

    public TIFFPredictor() {
        super (TIFF);
    }

    /**
     * Undo data based on the png algorithm
     */
    public ByteBuffer unpredict(ByteBuffer imageData)
        throws IOException
    {
        ByteBuffer out = ByteBuffer.allocate(imageData.limit());

        final int numComponents = getColors();
        final int pixelBits = numComponents * getBitsPerComponent();

        int bytePerRow = (getColumns() * pixelBits + 7) / 8;

        final byte[] row = new byte[bytePerRow];

        while(imageData.remaining() > 0) {
            imageData.get(row);
            if (getBitsPerComponent() == 8) {
                for (int i = numComponents; i < row.length; i += numComponents) {
                    for (int c = 0; c < numComponents; ++c) {
                        final int pos = i + c;
                        row[pos] += row[pos - numComponents];
                    }
                }
            } else if (getBitsPerComponent() == 16) {
                final short[] prev = new short[numComponents];
                for (int c = 0; c < numComponents; c += 1) {
                    final int pos = c * 2;
                    prev[c] = (short) ((row[pos] << 8 | (row[pos + 1]) & 0xFFFF));
                }
                for (int i = numComponents * 2; i < row.length; i += numComponents * 2) {
                    for (int c = 0; c < numComponents; c += 1) {
                        final int pos = i + c * 2;
                        short cur = (short) ((row[pos] << 8 | (row[pos + 1]) & 0xFFFF));
                        cur += prev[c];
                        row[pos] = (byte) (cur >>> 8 & 0xFF);
                        row[pos + 1] = (byte) (cur & 0xFF);
                        prev[c] = cur;
                    }
                }
            } else {
                assert getBitsPerComponent() == 1 || getBitsPerComponent() == 2 || getBitsPerComponent() == 4 : "we don't want to grab components across pixel boundaries";
                int bitsOnRow = pixelBits * getColumns(); // may be less than bytesOnRow * 8
                byte prev[] = new byte[numComponents];
                final int shiftWhenAligned = 8 - getBitsPerComponent();
                final int mask = (1 << getBitsPerComponent()) - 1;
                for (int c = 0; c < numComponents; ++c) {
                    prev[c] = getbits(row, c * getBitsPerComponent(), shiftWhenAligned, mask);
                }
                for (int i = pixelBits; i < bitsOnRow; i += pixelBits) {
                    for (int c = 0; c < numComponents; ++c) {
                        byte cur = getbits(row, i + c * getBitsPerComponent(), shiftWhenAligned, mask);
                        cur += prev[c];
                        prev[c] = cur;
                        setbits(row, i + c * getBitsPerComponent(), shiftWhenAligned, mask, cur);
                    }
                }
            }
            out.put(row);
        }


        // reset start pointer
        out.flip();

        // return
        return out;

    }

    private static byte getbits(byte[] data, int bitIndex, int shiftWhenByteAligned, int mask)
    {
        final int b = data[(bitIndex >> 3)];
        final int bitIndexInB = bitIndex & 7;
        final int shift =  shiftWhenByteAligned - bitIndexInB;
        return (byte) ((b >>> shift) & mask);
    }

    private static void setbits(byte[] data, int bitIndex, int shiftWhenByteAligned, int mask, byte bits)
    {
        final int b = data[(bitIndex >> 3)];
        final int bitIndexInB = bitIndex & 7;
        final int shift =  shiftWhenByteAligned - bitIndexInB;
        data[bitIndex >> 3] = (byte) ((b & ~(mask << shift)) | (bits << shift));
    }


}