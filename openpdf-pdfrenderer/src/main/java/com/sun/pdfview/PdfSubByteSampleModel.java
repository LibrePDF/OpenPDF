/*
 * $Id: PdfSubByteSampleModel.java,v 1.1 2010-05-23 22:07:05 lujke Exp $
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
package com.sun.pdfview;

import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;

/**
 * Provides a read-only sample-model where components are less than a byte in
 * width while allowing for pixels to cross byte-boundaries. For example, this
 * allows 2 pixels made of 3 4-bit-bands (p[pixel,band])
 * to be stored in 3 bytes as p[0,1] p[0,2] | p[0,3] p[1,0] | p[1,1] p[1,2].
 * {@link java.awt.image.MultiPixelPackedSampleModel}, which allows for sub-byte
 * components, does not allow for such byte spanning, while the PDF
 * specification does permit it -- hence the existence of this class.
 *
 * @author Luke Kirby, Pirion Systems
 */
public class PdfSubByteSampleModel extends SampleModel
{
    private final int transferBytesPerPixel;
    private final int storageBitsPerPixel;
    private final int bitsPerLine;
    private final int bitsPerBand;
    private final int componentMask;
    private final int[] sampleSize;
    private final int ignoredBitsPerComponentPerByte;

    public PdfSubByteSampleModel(int w, int h, int numComponents, int bitsPerComponent)
    {
        super(DataBuffer.TYPE_BYTE, w, h, numComponents);
        assert bitsPerComponent < 8 : "This is designed just for use with per-component sizes of less than 8 bits; " +
                "you should probably use PixelInterleavedSampleModel";
        assert bitsPerComponent == 1 || bitsPerComponent == 2 || bitsPerComponent == 4 :
                "we don't want to grab components across byte boundaries";
        transferBytesPerPixel = (numComponents * bitsPerComponent + 7) / 8;
        storageBitsPerPixel = numComponents * bitsPerComponent;
        // account for possible bits of padding on the end
        bitsPerLine = 8 * ((storageBitsPerPixel * w + 7) / 8);
        this.bitsPerBand = bitsPerComponent;
        componentMask = (1 << this.bitsPerBand) - 1;

        sampleSize = new int[numComponents];
        for (int i = 0; i < sampleSize.length; ++i) {
            sampleSize[i] = bitsPerComponent;
        }
        ignoredBitsPerComponentPerByte = 8 - bitsPerBand;
    }

    @Override
    public int getNumDataElements()
    {
        return transferBytesPerPixel;
    }

    @Override
    public Object getDataElements(int x, int y, Object obj, DataBuffer data)
    {
        byte[] elements = obj != null ? (byte[])obj : new byte[numBands];
        int bitIndex = y * bitsPerLine + storageBitsPerPixel * x;
        for (int i = 0; i < elements.length; ++i) {
            elements[i] = (byte) getComponent(data, bitIndex);
            bitIndex += bitsPerBand;
        }
        return elements;
    }

    private int getComponent(DataBuffer data, int aBitIndex)
    {
        final int boffset = aBitIndex >> 3; // == aBitIndex / 8
        final int b = data.getElem(boffset);
        final int bitIndexInB = aBitIndex & 7;
        final int shift =  ignoredBitsPerComponentPerByte - bitIndexInB;
        return (b >>> shift) & componentMask;
    }

    @Override
    public void setDataElements(int x, int y, Object obj, DataBuffer data)
    {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public int getSample(int x, int y, int b, DataBuffer data)
    {
        return getComponent(data, y * bitsPerLine + storageBitsPerPixel * x + bitsPerBand * b);
    }

    @Override
    public void setSample(int x, int y, int b, int s, DataBuffer data)
    {
        throw new UnsupportedOperationException("read only");

    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h)
    {
        throw new UnsupportedOperationException("Not required");
    }

    @Override
    public SampleModel createSubsetSampleModel(int[] bands)
    {
        throw new UnsupportedOperationException("Not required");
    }

    @Override
    public DataBuffer createDataBuffer()
    {
        throw new UnsupportedOperationException("Not required");
    }

    @Override
    public int[] getSampleSize()
    {
        return sampleSize;
    }

    @Override
    public int getSampleSize(int band)
    {
        return bitsPerBand;
    }
}
