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

package com.sun.pdfview.colorspace;

import java.awt.color.ColorSpace;

/**
 * A ColorSpace for the YCCK color space.  This color space converts to CMYK and then
 * uses an existing CMYK color space to convert from CMYK to RGB.  This allows embedded
 * CMYK color profiles to be used with YCCK images.  If no CMYK color space is
 * provided then by default it uses a CMYKColorSpace. Only toRGB is supported.
 *
 * @author Ben Day
 */
public class YCCKColorSpace extends ColorSpace
{

    private final ColorSpace cmykColorSpace;

    /**
     * create a new YCCK color space:  a ColorSpace with 4 components
     */
    public YCCKColorSpace(ColorSpace existingCmykColorSpace)
    {
        super(TYPE_4CLR, 4);
        cmykColorSpace = existingCmykColorSpace;
    }

    public YCCKColorSpace()
    {
        this(new CMYKColorSpace());
    }

    /**
     * Convert from CIEXYZ to RGB.  NOT IMPLEMENTED
     */
    @Override
    public float[] fromCIEXYZ(float[] colorvalue)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Convert from RGB to YCCK.  NOT IMPLEMENTED
     *
     * @param rgbvalue the red, green, and blue values (0-1)
     * @return the YCCK values (0-1)
     */
    @Override
    public float[] fromRGB(float[] rgbvalue)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * the number of components
     */
    @Override
    public int getNumComponents()
    {
        return 4;
    }

    /**
     * the name of this color space
     */
    @Override
    public String getName(int idx)
    {
        return "YCCK";
    }

    /**
     * the type of this color space (TYPE_4CLR)
     */
    @Override
    public int getType()
    {
        return TYPE_4CLR;
    }

    /**
     * Convert from YCCK to CIEXYZ.  NOT IMPLEMENTED
     */
    @Override
    public float[] toCIEXYZ(float[] colorvalue)
    {
        return cmykColorSpace.toCIEXYZ(toCmyk(colorvalue));
    }

    /**
     * Convert from YCCK to RGB.
     *
     * @param colorvalue the YCCK values (0-1)
     * @return the RGB values (0-1)
     */
    @Override
    public float[] toRGB(float[] colorvalue)
    {
        return cmykColorSpace.toRGB(toCmyk(colorvalue));
    }

    private float[] toCmyk(float[] colorvalue) {
        float y = colorvalue[0], cb = colorvalue[1], cr = colorvalue[2], k = colorvalue[3];
        float[] cmyk = new float[4];
        float v;
        v = (float) (1.0 - (y + 1.402 * (cr - 0.5)));
        cmyk[0] = v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
        v = (float) (1.0 - (y - 0.34414 * (cb - 0.5) - 0.71414 * (cr - 0.5)));
        cmyk[1] = v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
        v = (float) (1.0 - (y + 1.772 * (cb - 0.5)));
        cmyk[2] = v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
        cmyk[3] = k;
        return cmyk;
    }
}
