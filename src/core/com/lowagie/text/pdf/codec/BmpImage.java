/*
 * Copyright 2003-2008 by Paulo Soares.
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
 * This code was originally released in 2001 by SUN (see class
 * com.sun.media.imageioimpl.plugins.bmp.BMPImageReader.java)
 * using the BSD license in a specific wording. In a mail dating from
 * January 23, 2008, Brian Burkhalter (@sun.com) gave us permission
 * to use the code under the following version of the BSD license:
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility.
 */
package com.lowagie.text.pdf.codec;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.BadElementException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Image;
import com.lowagie.text.ImgRaw;
import com.lowagie.text.Utilities;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfString;

/** Reads a BMP image. All types of BMP can be read.
 * <p>
 * It is based in the JAI codec.
 *
 * @author  Paulo Soares (psoares@consiste.pt)
 */
public class BmpImage {
    
    // BMP variables
    private InputStream inputStream;
    private long bitmapFileSize;
    private long bitmapOffset;
    private long compression;
    private long imageSize;
    private byte palette[];
    private int imageType;
    private int numBands;
    private boolean isBottomUp;
    private int bitsPerPixel;
    private int redMask, greenMask, blueMask, alphaMask;
    public HashMap properties = new HashMap();    
    private long xPelsPerMeter;
    private long yPelsPerMeter;
    // BMP Image types
    private static final int VERSION_2_1_BIT = 0;
    private static final int VERSION_2_4_BIT = 1;
    private static final int VERSION_2_8_BIT = 2;
    private static final int VERSION_2_24_BIT = 3;
    
    private static final int VERSION_3_1_BIT = 4;
    private static final int VERSION_3_4_BIT = 5;
    private static final int VERSION_3_8_BIT = 6;
    private static final int VERSION_3_24_BIT = 7;
    
    private static final int VERSION_3_NT_16_BIT = 8;
    private static final int VERSION_3_NT_32_BIT = 9;
    
    private static final int VERSION_4_1_BIT = 10;
    private static final int VERSION_4_4_BIT = 11;
    private static final int VERSION_4_8_BIT = 12;
    private static final int VERSION_4_16_BIT = 13;
    private static final int VERSION_4_24_BIT = 14;
    private static final int VERSION_4_32_BIT = 15;
    
    // Color space types
    private static final int LCS_CALIBRATED_RGB = 0;
    private static final int LCS_sRGB = 1;
    private static final int LCS_CMYK = 2;
    
    // Compression Types
    private static final int BI_RGB = 0;
    private static final int BI_RLE8 = 1;
    private static final int BI_RLE4 = 2;
    private static final int BI_BITFIELDS = 3;
    
    int width;
    int height;
    
    BmpImage(InputStream is, boolean noHeader, int size) throws IOException {
        bitmapFileSize = size;
        bitmapOffset = 0;
        process(is, noHeader);
    }
    
    /** Reads a BMP from an url.
     * @param url the url
     * @throws IOException on error
     * @return the image
     */    
    public static Image getImage(URL url) throws IOException {
        InputStream is = null;
        try {
            is = url.openStream();
            Image img = getImage(is);
            img.setUrl(url);
            return img;
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
    }
    
    /** Reads a BMP from a stream. The stream is not closed.
     * @param is the stream
     * @throws IOException on error
     * @return the image
     */    
    public static Image getImage(InputStream is) throws IOException {
        return getImage(is, false, 0);
    }
    
    /** Reads a BMP from a stream. The stream is not closed.
     * The BMP may not have a header and be considered as a plain DIB.
     * @param is the stream
     * @param noHeader true to process a plain DIB
     * @param size the size of the DIB. Not used for a BMP
     * @throws IOException on error
     * @return the image
     */    
    public static Image getImage(InputStream is, boolean noHeader, int size) throws IOException {
        BmpImage bmp = new BmpImage(is, noHeader, size);
        try {
            Image img = bmp.getImage();
            img.setDpi((int)(bmp.xPelsPerMeter * 0.0254d + 0.5d), (int)(bmp.yPelsPerMeter * 0.0254d + 0.5d));
            img.setOriginalType(Image.ORIGINAL_BMP);
            return img;
        }
        catch (BadElementException be) {
            throw new ExceptionConverter(be);
        }
    }
    
    /** Reads a BMP from a file.
     * @param file the file
     * @throws IOException on error
     * @return the image
     */    
    public static Image getImage(String file) throws IOException {
        return getImage(Utilities.toURL(file));
    }
    
    /** Reads a BMP from a byte array.
     * @param data the byte array
     * @throws IOException on error
     * @return the image
     */    
    public static Image getImage(byte data[]) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        Image img = getImage(is);
        img.setOriginalData(data);
        return img;
    }

    
    protected void process(InputStream stream, boolean noHeader) throws IOException {
        if (noHeader || stream instanceof BufferedInputStream) {
            inputStream = stream;
        } else {
            inputStream = new BufferedInputStream(stream);
        }
        if (!noHeader) {
            // Start File Header
            if (!(readUnsignedByte(inputStream) == 'B' &&
            readUnsignedByte(inputStream) == 'M')) {
                throw new RuntimeException(MessageLocalization.getComposedMessage("invalid.magic.value.for.bmp.file"));
            }

            // Read file size
            bitmapFileSize = readDWord(inputStream);

            // Read the two reserved fields
            readWord(inputStream);
            readWord(inputStream);

            // Offset to the bitmap from the beginning
            bitmapOffset = readDWord(inputStream);

            // End File Header
        }
        // Start BitmapCoreHeader
        long size = readDWord(inputStream);
        
        if (size == 12) {
            width = readWord(inputStream);
            height = readWord(inputStream);
        } else {
            width = readLong(inputStream);
            height = readLong(inputStream);
        }
        
        int planes = readWord(inputStream);
        bitsPerPixel = readWord(inputStream);
        
        properties.put("color_planes", new Integer(planes));
        properties.put("bits_per_pixel", new Integer(bitsPerPixel));
        
        // As BMP always has 3 rgb bands, except for Version 5,
        // which is bgra
        numBands = 3;
        if (bitmapOffset == 0)
            bitmapOffset = size;
        if (size == 12) {
            // Windows 2.x and OS/2 1.x
            properties.put("bmp_version", "BMP v. 2.x");
            
            // Classify the image type
            if (bitsPerPixel == 1) {
                imageType = VERSION_2_1_BIT;
            } else if (bitsPerPixel == 4) {
                imageType = VERSION_2_4_BIT;
            } else if (bitsPerPixel == 8) {
                imageType = VERSION_2_8_BIT;
            } else if (bitsPerPixel == 24) {
                imageType = VERSION_2_24_BIT;
            }
            
            // Read in the palette
            int numberOfEntries = (int)((bitmapOffset-14-size) / 3);
            int sizeOfPalette = numberOfEntries*3;
            if (bitmapOffset == size) {
                switch (imageType) {
                    case VERSION_2_1_BIT:
                        sizeOfPalette = 2 * 3;
                        break;
                    case VERSION_2_4_BIT:
                        sizeOfPalette = 16 * 3;
                        break;
                    case VERSION_2_8_BIT:
                        sizeOfPalette = 256 * 3;
                        break;
                    case VERSION_2_24_BIT:
                        sizeOfPalette = 0;
                        break;
                }
                bitmapOffset = size + sizeOfPalette;
            }
            readPalette(sizeOfPalette);
        } else {
            
            compression = readDWord(inputStream);
            imageSize = readDWord(inputStream);
            xPelsPerMeter = readLong(inputStream);
            yPelsPerMeter = readLong(inputStream);
            long colorsUsed = readDWord(inputStream);
            long colorsImportant = readDWord(inputStream);
            
            switch((int)compression) {
                case BI_RGB:
                    properties.put("compression", "BI_RGB");
                    break;
                    
                case BI_RLE8:
                    properties.put("compression", "BI_RLE8");
                    break;
                    
                case BI_RLE4:
                    properties.put("compression", "BI_RLE4");
                    break;
                    
                case BI_BITFIELDS:
                    properties.put("compression", "BI_BITFIELDS");
                    break;
            }
            
            properties.put("x_pixels_per_meter", new Long(xPelsPerMeter));
            properties.put("y_pixels_per_meter", new Long(yPelsPerMeter));
            properties.put("colors_used", new Long(colorsUsed));
            properties.put("colors_important", new Long(colorsImportant));
            
            if (size == 40) {
                // Windows 3.x and Windows NT
                switch((int)compression) {
                    
                    case BI_RGB:  // No compression
                    case BI_RLE8:  // 8-bit RLE compression
                    case BI_RLE4:  // 4-bit RLE compression
                        
                        if (bitsPerPixel == 1) {
                            imageType = VERSION_3_1_BIT;
                        } else if (bitsPerPixel == 4) {
                            imageType = VERSION_3_4_BIT;
                        } else if (bitsPerPixel == 8) {
                            imageType = VERSION_3_8_BIT;
                        } else if (bitsPerPixel == 24) {
                            imageType = VERSION_3_24_BIT;
                        } else if (bitsPerPixel == 16) {
                            imageType = VERSION_3_NT_16_BIT;
                            redMask = 0x7C00;
                            greenMask = 0x3E0;
                            blueMask = 0x1F;
                            properties.put("red_mask", new Integer(redMask));
                            properties.put("green_mask", new Integer(greenMask));
                            properties.put("blue_mask", new Integer(blueMask));
                        } else if (bitsPerPixel == 32) {
                            imageType = VERSION_3_NT_32_BIT;
                            redMask   = 0x00FF0000;
                            greenMask = 0x0000FF00;
                            blueMask  = 0x000000FF;
                            properties.put("red_mask", new Integer(redMask));
                            properties.put("green_mask", new Integer(greenMask));
                            properties.put("blue_mask", new Integer(blueMask));
                        }

                        // Read in the palette
                        int numberOfEntries = (int)((bitmapOffset-14-size) / 4);
                        int sizeOfPalette = numberOfEntries*4;
                        if (bitmapOffset == size) {
                            switch (imageType) {
                                case VERSION_3_1_BIT:
                                    sizeOfPalette = (int)(colorsUsed == 0 ? 2 : colorsUsed) * 4;
                                    break;
                                case VERSION_3_4_BIT:
                                    sizeOfPalette = (int)(colorsUsed == 0 ? 16 : colorsUsed) * 4;
                                    break;
                                case VERSION_3_8_BIT:
                                    sizeOfPalette = (int)(colorsUsed == 0 ? 256 : colorsUsed) * 4;
                                    break;
                                default:
                                    sizeOfPalette = 0;
                                    break;
                            }
                            bitmapOffset = size + sizeOfPalette;
                        }
                        readPalette(sizeOfPalette);
                                                
                        properties.put("bmp_version", "BMP v. 3.x");
                        break;
                        
                    case BI_BITFIELDS:
                        
                        if (bitsPerPixel == 16) {
                            imageType = VERSION_3_NT_16_BIT;
                        } else if (bitsPerPixel == 32) {
                            imageType = VERSION_3_NT_32_BIT;
                        }
                        
                        // BitsField encoding
                        redMask = (int)readDWord(inputStream);
                        greenMask = (int)readDWord(inputStream);
                        blueMask = (int)readDWord(inputStream);
                        
                        properties.put("red_mask", new Integer(redMask));
                        properties.put("green_mask", new Integer(greenMask));
                        properties.put("blue_mask", new Integer(blueMask));
                        
                        if (colorsUsed != 0) {
                            // there is a palette
                            sizeOfPalette = (int)colorsUsed*4;
                            readPalette(sizeOfPalette);
                        }
                        
                        properties.put("bmp_version", "BMP v. 3.x NT");
                        break;
                        
                    default:
                        throw new
                        RuntimeException("Invalid compression specified in BMP file.");
                }
            } else if (size == 108) {
                // Windows 4.x BMP
                
                properties.put("bmp_version", "BMP v. 4.x");
                
                // rgb masks, valid only if comp is BI_BITFIELDS
                redMask = (int)readDWord(inputStream);
                greenMask = (int)readDWord(inputStream);
                blueMask = (int)readDWord(inputStream);
                // Only supported for 32bpp BI_RGB argb
                alphaMask = (int)readDWord(inputStream);
                long csType = readDWord(inputStream);
                int redX = readLong(inputStream);
                int redY = readLong(inputStream);
                int redZ = readLong(inputStream);
                int greenX = readLong(inputStream);
                int greenY = readLong(inputStream);
                int greenZ = readLong(inputStream);
                int blueX = readLong(inputStream);
                int blueY = readLong(inputStream);
                int blueZ = readLong(inputStream);
                long gammaRed = readDWord(inputStream);
                long gammaGreen = readDWord(inputStream);
                long gammaBlue = readDWord(inputStream);
                
                if (bitsPerPixel == 1) {
                    imageType = VERSION_4_1_BIT;
                } else if (bitsPerPixel == 4) {
                    imageType = VERSION_4_4_BIT;
                } else if (bitsPerPixel == 8) {
                    imageType = VERSION_4_8_BIT;
                } else if (bitsPerPixel == 16) {
                    imageType = VERSION_4_16_BIT;
                    if ((int)compression == BI_RGB) {
                        redMask = 0x7C00;
                        greenMask = 0x3E0;
                        blueMask = 0x1F;
                    }
                } else if (bitsPerPixel == 24) {
                    imageType = VERSION_4_24_BIT;
                } else if (bitsPerPixel == 32) {
                    imageType = VERSION_4_32_BIT;
                    if ((int)compression == BI_RGB) {
                        redMask   = 0x00FF0000;
                        greenMask = 0x0000FF00;
                        blueMask  = 0x000000FF;
                    }
                }
                
                properties.put("red_mask", new Integer(redMask));
                properties.put("green_mask", new Integer(greenMask));
                properties.put("blue_mask", new Integer(blueMask));
                properties.put("alpha_mask", new Integer(alphaMask));

                // Read in the palette
                int numberOfEntries = (int)((bitmapOffset-14-size) / 4);
                int sizeOfPalette = numberOfEntries*4;
                if (bitmapOffset == size) {
                    switch (imageType) {
                        case VERSION_4_1_BIT:
                            sizeOfPalette = (int)(colorsUsed == 0 ? 2 : colorsUsed) * 4;
                            break;
                        case VERSION_4_4_BIT:
                            sizeOfPalette = (int)(colorsUsed == 0 ? 16 : colorsUsed) * 4;
                            break;
                        case VERSION_4_8_BIT:
                            sizeOfPalette = (int)(colorsUsed == 0 ? 256 : colorsUsed) * 4;
                            break;
                        default:
                            sizeOfPalette = 0;
                            break;
                    }
                    bitmapOffset = size + sizeOfPalette;
                }
                readPalette(sizeOfPalette);
                
                switch((int)csType) {
                    case LCS_CALIBRATED_RGB:
                        // All the new fields are valid only for this case
                        properties.put("color_space", "LCS_CALIBRATED_RGB");
                        properties.put("redX", new Integer(redX));
                        properties.put("redY", new Integer(redY));
                        properties.put("redZ", new Integer(redZ));
                        properties.put("greenX", new Integer(greenX));
                        properties.put("greenY", new Integer(greenY));
                        properties.put("greenZ", new Integer(greenZ));
                        properties.put("blueX", new Integer(blueX));
                        properties.put("blueY", new Integer(blueY));
                        properties.put("blueZ", new Integer(blueZ));
                        properties.put("gamma_red", new Long(gammaRed));
                        properties.put("gamma_green", new Long(gammaGreen));
                        properties.put("gamma_blue", new Long(gammaBlue));
                        
                        // break;
                        throw new
                        RuntimeException("Not implemented yet.");
                        
                    case LCS_sRGB:
                        // Default Windows color space
                        properties.put("color_space", "LCS_sRGB");
                        break;
                        
                    case LCS_CMYK:
                        properties.put("color_space", "LCS_CMYK");
                        //		    break;
                        throw new
                        RuntimeException("Not implemented yet.");
                }
                
            } else {
                properties.put("bmp_version", "BMP v. 5.x");
                throw new
                RuntimeException("BMP version 5 not implemented yet.");
            }
        }
        
        if (height > 0) {
            // bottom up image
            isBottomUp = true;
        } else {
            // top down image
            isBottomUp = false;
            height = Math.abs(height);
        }
        // When number of bitsPerPixel is <= 8, we use IndexColorModel.
        if (bitsPerPixel == 1 || bitsPerPixel == 4 || bitsPerPixel == 8) {
            
            numBands = 1;
            
            
            // Create IndexColorModel from the palette.
            byte r[], g[], b[];
            int sizep;
            if (imageType == VERSION_2_1_BIT ||
            imageType == VERSION_2_4_BIT ||
            imageType == VERSION_2_8_BIT) {
                
                sizep = palette.length/3;
                
                if (sizep > 256) {
                    sizep = 256;
                }
                
                int off;
                r = new byte[sizep];
                g = new byte[sizep];
                b = new byte[sizep];
                for (int i=0; i<sizep; i++) {
                    off = 3 * i;
                    b[i] = palette[off];
                    g[i] = palette[off+1];
                    r[i] = palette[off+2];
                }
            } else {
                sizep = palette.length/4;
                
                if (sizep > 256) {
                    sizep = 256;
                }
                
                int off;
                r = new byte[sizep];
                g = new byte[sizep];
                b = new byte[sizep];
                for (int i=0; i<sizep; i++) {
                    off = 4 * i;
                    b[i] = palette[off];
                    g[i] = palette[off+1];
                    r[i] = palette[off+2];
                }
            }
            
        } else if (bitsPerPixel == 16) {
            numBands = 3;
        } else if (bitsPerPixel == 32) {
            numBands = alphaMask == 0 ? 3 : 4;
            
            // The number of bands in the SampleModel is determined by
            // the length of the mask array passed in.
        } else {
            numBands = 3;
        }
    }
    
    private byte[] getPalette(int group) {
        if (palette == null)
            return null;
        byte np[] = new byte[palette.length / group * 3];
        int e = palette.length / group;
        for (int k = 0; k < e; ++k) {
            int src = k * group;
            int dest = k * 3;
            np[dest + 2] = palette[src++];
            np[dest + 1] = palette[src++];
            np[dest] = palette[src];
        }
        return np;
    }
    
    private Image getImage() throws IOException, BadElementException {
        byte bdata[] = null; // buffer for byte data
        
        //	if (sampleModel.getDataType() == DataBuffer.TYPE_BYTE)
        //	    bdata = (byte[])((DataBufferByte)tile.getDataBuffer()).getData();
        //	else if (sampleModel.getDataType() == DataBuffer.TYPE_USHORT)
        //	    sdata = (short[])((DataBufferUShort)tile.getDataBuffer()).getData();
        //	else if (sampleModel.getDataType() == DataBuffer.TYPE_INT)
        //	    idata = (int[])((DataBufferInt)tile.getDataBuffer()).getData();
        
        // There should only be one tile.
        switch(imageType) {
            
            case VERSION_2_1_BIT:
                // no compression
                return read1Bit(3);
                
            case VERSION_2_4_BIT:
                // no compression
                return read4Bit(3);
                
            case VERSION_2_8_BIT:
                // no compression
                return read8Bit(3);
                
            case VERSION_2_24_BIT:
                // no compression
                bdata = new byte[width * height * 3];
                read24Bit(bdata);
                return new ImgRaw(width, height, 3, 8, bdata);
                
            case VERSION_3_1_BIT:
                // 1-bit images cannot be compressed.
                return read1Bit(4);
                
            case VERSION_3_4_BIT:
                switch((int)compression) {
                    case BI_RGB:
                        return read4Bit(4);
                        
                    case BI_RLE4:
                        return readRLE4();
                        
                    default:
                        throw new
                        RuntimeException("Invalid compression specified for BMP file.");
                }
                
            case VERSION_3_8_BIT:
                switch((int)compression) {
                    case BI_RGB:
                        return read8Bit(4);
                        
                    case BI_RLE8:
                        return readRLE8();
                        
                    default:
                        throw new
                        RuntimeException("Invalid compression specified for BMP file.");
                }
                
            case VERSION_3_24_BIT:
                // 24-bit images are not compressed
                bdata = new byte[width * height * 3];
                read24Bit(bdata);
                return new ImgRaw(width, height, 3, 8, bdata);
                
            case VERSION_3_NT_16_BIT:
                return read1632Bit(false);
                
            case VERSION_3_NT_32_BIT:
                return read1632Bit(true);
                
            case VERSION_4_1_BIT:
                return read1Bit(4);
                
            case VERSION_4_4_BIT:
                switch((int)compression) {
                    
                    case BI_RGB:
                        return read4Bit(4);
                        
                    case BI_RLE4:
                        return readRLE4();
                        
                    default:
                        throw new
                        RuntimeException("Invalid compression specified for BMP file.");
                }
                
            case VERSION_4_8_BIT:
                switch((int)compression) {
                    
                    case BI_RGB:
                        return read8Bit(4);
                        
                    case BI_RLE8:
                        return readRLE8();
                        
                    default:
                        throw new
                        RuntimeException("Invalid compression specified for BMP file.");
                }
                
            case VERSION_4_16_BIT:
                return read1632Bit(false);
                
            case VERSION_4_24_BIT:
                bdata = new byte[width * height * 3];
                read24Bit(bdata);
                return new ImgRaw(width, height, 3, 8, bdata);
                
            case VERSION_4_32_BIT:
                return read1632Bit(true);
        }
        return null;
    }
    
    private Image indexedModel(byte bdata[], int bpc, int paletteEntries) throws BadElementException {
        Image img = new ImgRaw(width, height, 1, bpc, bdata);
        PdfArray colorspace = new PdfArray();
        colorspace.add(PdfName.INDEXED);
        colorspace.add(PdfName.DEVICERGB);
        byte np[] = getPalette(paletteEntries);
        int len = np.length;
        colorspace.add(new PdfNumber(len / 3 - 1));
        colorspace.add(new PdfString(np));
        PdfDictionary ad = new PdfDictionary();
        ad.put(PdfName.COLORSPACE, colorspace);
        img.setAdditional(ad);
        return img;
    }
    
    private void readPalette(int sizeOfPalette) throws IOException {
        if (sizeOfPalette == 0) {
            return;
        }

        palette = new byte[sizeOfPalette];
        int bytesRead = 0;
        while (bytesRead < sizeOfPalette) {
            int r = inputStream.read(palette, bytesRead, sizeOfPalette - bytesRead);
            if (r < 0) {
                throw new RuntimeException(MessageLocalization.getComposedMessage("incomplete.palette"));
            }
            bytesRead += r;
        }
        properties.put("palette", palette);
    }
    
    // Deal with 1 Bit images using IndexColorModels
    private Image read1Bit(int paletteEntries) throws IOException, BadElementException {
        byte bdata[] = new byte[((width + 7) / 8) * height];
        int padding = 0;
        int bytesPerScanline = (int)Math.ceil(width/8.0d);
        
        int remainder = bytesPerScanline % 4;
        if (remainder != 0) {
            padding = 4 - remainder;
        }
        
        int imSize = (bytesPerScanline + padding) * height;
        
        // Read till we have the whole image
        byte values[] = new byte[imSize];
        int bytesRead = 0;
        while (bytesRead < imSize) {
            bytesRead += inputStream.read(values, bytesRead,
            imSize - bytesRead);
        }
        
        if (isBottomUp) {
            
            // Convert the bottom up image to a top down format by copying
            // one scanline from the bottom to the top at a time.
            
            for (int i=0; i<height; i++) {
                System.arraycopy(values,
                imSize - (i+1)*(bytesPerScanline + padding),
                bdata,
                i*bytesPerScanline, bytesPerScanline);
            }
        } else {
            
            for (int i=0; i<height; i++) {
                System.arraycopy(values,
                i * (bytesPerScanline + padding),
                bdata,
                i * bytesPerScanline,
                bytesPerScanline);
            }
        }
        return indexedModel(bdata, 1, paletteEntries);
    }
    
    // Method to read a 4 bit BMP image data
    private Image read4Bit(int paletteEntries) throws IOException, BadElementException {
        byte bdata[] = new byte[((width + 1) / 2) * height];
        
        // Padding bytes at the end of each scanline
        int padding = 0;
        
        int bytesPerScanline = (int)Math.ceil(width/2.0d);
        int remainder = bytesPerScanline % 4;
        if (remainder != 0) {
            padding = 4 - remainder;
        }
        
        int imSize = (bytesPerScanline + padding) * height;
        
        // Read till we have the whole image
        byte values[] = new byte[imSize];
        int bytesRead = 0;
        while (bytesRead < imSize) {
            bytesRead += inputStream.read(values, bytesRead,
            imSize - bytesRead);
        }
        
        if (isBottomUp) {
            
            // Convert the bottom up image to a top down format by copying
            // one scanline from the bottom to the top at a time.
            for (int i=0; i<height; i++) {
                System.arraycopy(values,
                imSize - (i+1)*(bytesPerScanline + padding),
                bdata,
                i*bytesPerScanline,
                bytesPerScanline);
            }
        } else {
            for (int i=0; i<height; i++) {
                System.arraycopy(values,
                i * (bytesPerScanline + padding),
                bdata,
                i * bytesPerScanline,
                bytesPerScanline);
            }
        }
        return indexedModel(bdata, 4, paletteEntries);
    }
    
    // Method to read 8 bit BMP image data
    private Image read8Bit(int paletteEntries) throws IOException, BadElementException {
        byte bdata[] = new byte[width * height];
        // Padding bytes at the end of each scanline
        int padding = 0;
        
        // width * bitsPerPixel should be divisible by 32
        int bitsPerScanline = width * 8;
        if ( bitsPerScanline%32 != 0) {
            padding = (bitsPerScanline/32 + 1)*32 - bitsPerScanline;
            padding = (int)Math.ceil(padding/8.0);
        }
        
        int imSize = (width + padding) * height;
        
        // Read till we have the whole image
        byte values[] = new byte[imSize];
        int bytesRead = 0;
        while (bytesRead < imSize) {
            bytesRead += inputStream.read(values, bytesRead, imSize - bytesRead);
        }
        
        if (isBottomUp) {
            
            // Convert the bottom up image to a top down format by copying
            // one scanline from the bottom to the top at a time.
            for (int i=0; i<height; i++) {
                System.arraycopy(values,
                imSize - (i+1) * (width + padding),
                bdata,
                i * width,
                width);
            }
        } else {
            for (int i=0; i<height; i++) {
                System.arraycopy(values,
                i * (width + padding),
                bdata,
                i * width,
                width);
            }
        }
        return indexedModel(bdata, 8, paletteEntries);
    }
    
    // Method to read 24 bit BMP image data
    private void read24Bit(byte[] bdata) {
        // Padding bytes at the end of each scanline
        int padding = 0;
        
        // width * bitsPerPixel should be divisible by 32
        int bitsPerScanline = width * 24;
        if ( bitsPerScanline%32 != 0) {
            padding = (bitsPerScanline/32 + 1)*32 - bitsPerScanline;
            padding = (int)Math.ceil(padding/8.0);
        }
        
        
        int imSize = ((width * 3 + 3) / 4 * 4) * height;
        // Read till we have the whole image
        byte values[] = new byte[imSize];
        try {
            int bytesRead = 0;
            while (bytesRead < imSize) {
                int r = inputStream.read(values, bytesRead,
                imSize - bytesRead);
                if (r < 0)
                    break;
                bytesRead += r;
            }
        } catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
        
        int l=0, count;
        
        if (isBottomUp) {
            int max = width*height*3-1;
            
            count = -padding;
            for (int i=0; i<height; i++) {
                l = max - (i+1)*width*3 + 1;
                count += padding;
                for (int j=0; j<width; j++) {
                    bdata[l + 2] = values[count++];
                    bdata[l + 1] = values[count++];
                    bdata[l] = values[count++];
                    l += 3;
                }
            }
        } else {
            count = -padding;
            for (int i=0; i<height; i++) {
                count += padding;
                for (int j=0; j<width; j++) {
                    bdata[l + 2] = values[count++];
                    bdata[l + 1] = values[count++];
                    bdata[l] = values[count++];
                    l += 3;
                }
            }
        }
    }
    
    private int findMask(int mask) {
        int k = 0;
        for (; k < 32; ++k) {
            if ((mask & 1) == 1)
                break;
            mask >>>= 1;
        }
        return mask;
    }
    
    private int findShift(int mask) {
        int k = 0;
        for (; k < 32; ++k) {
            if ((mask & 1) == 1)
                break;
            mask >>>= 1;
        }
        return k;
    }
    
    private Image read1632Bit(boolean is32) throws IOException, BadElementException {
        
        int red_mask = findMask(redMask);
        int red_shift = findShift(redMask);
        int red_factor = red_mask + 1;
        int green_mask = findMask(greenMask);
        int green_shift = findShift(greenMask);
        int green_factor = green_mask + 1;
        int blue_mask = findMask(blueMask);
        int blue_shift = findShift(blueMask);
        int blue_factor = blue_mask + 1;
        byte bdata[] = new byte[width * height * 3];
        // Padding bytes at the end of each scanline
        int padding = 0;
        
        if (!is32) {
        // width * bitsPerPixel should be divisible by 32
            int bitsPerScanline = width * 16;
            if ( bitsPerScanline%32 != 0) {
                padding = (bitsPerScanline/32 + 1)*32 - bitsPerScanline;
                padding = (int)Math.ceil(padding/8.0);
            }
        }
        
        int imSize = (int)imageSize;
        if (imSize == 0) {
            imSize = (int)(bitmapFileSize - bitmapOffset);
        }
        
        int l=0;
        int v;
        if (isBottomUp) {
            for (int i=height - 1; i >= 0; --i) {
                l = width * 3 * i;
                for (int j=0; j<width; j++) {
                    if (is32)
                        v = (int)readDWord(inputStream);
                    else
                        v = readWord(inputStream);
                    bdata[l++] = (byte)(((v >>> red_shift) & red_mask) * 256 / red_factor);
                    bdata[l++] = (byte)(((v >>> green_shift) & green_mask) * 256 / green_factor);
                    bdata[l++] = (byte)(((v >>> blue_shift) & blue_mask) * 256 / blue_factor);
                }
                for (int m=0; m<padding; m++) {
                    inputStream.read();
                }
            }
        } else {
            for (int i=0; i<height; i++) {
                for (int j=0; j<width; j++) {
                    if (is32)
                        v = (int)readDWord(inputStream);
                    else
                        v = readWord(inputStream);
                    bdata[l++] = (byte)(((v >>> red_shift) & red_mask) * 256 / red_factor);
                    bdata[l++] = (byte)(((v >>> green_shift) & green_mask) * 256 / green_factor);
                    bdata[l++] = (byte)(((v >>> blue_shift) & blue_mask) * 256 / blue_factor);
                }
                for (int m=0; m<padding; m++) {
                    inputStream.read();
                }
            }
        }
        return new ImgRaw(width, height, 3, 8, bdata);
    }
    
    private Image readRLE8() throws IOException, BadElementException {
        
        // If imageSize field is not provided, calculate it.
        int imSize = (int)imageSize;
        if (imSize == 0) {
            imSize = (int)(bitmapFileSize - bitmapOffset);
        }
        
        // Read till we have the whole image
        byte values[] = new byte[imSize];
        int bytesRead = 0;
        while (bytesRead < imSize) {
            bytesRead += inputStream.read(values, bytesRead,
            imSize - bytesRead);
        }
        
        // Since data is compressed, decompress it
        byte val[] = decodeRLE(true, values);
        
        // Uncompressed data does not have any padding
        imSize = width * height;
        
        if (isBottomUp) {
            
            // Convert the bottom up image to a top down format by copying
            // one scanline from the bottom to the top at a time.
            // int bytesPerScanline = (int)Math.ceil((double)width/8.0);
            byte temp[] = new byte[val.length];
            int bytesPerScanline = width;
            for (int i=0; i<height; i++) {
                System.arraycopy(val,
                imSize - (i+1)*(bytesPerScanline),
                temp,
                i*bytesPerScanline, bytesPerScanline);
            }
            val = temp;
        }
        return indexedModel(val, 8, 4);
    }
    
    private Image readRLE4() throws IOException, BadElementException {
        
        // If imageSize field is not specified, calculate it.
        int imSize = (int)imageSize;
        if (imSize == 0) {
            imSize = (int)(bitmapFileSize - bitmapOffset);
        }
        
        // Read till we have the whole image
        byte values[] = new byte[imSize];
        int bytesRead = 0;
        while (bytesRead < imSize) {
            bytesRead += inputStream.read(values, bytesRead,
            imSize - bytesRead);
        }
        
        // Decompress the RLE4 compressed data.
        byte val[] = decodeRLE(false, values);
        
        // Invert it as it is bottom up format.
        if (isBottomUp) {
            
            byte inverted[] = val;
            val = new byte[width * height];
            int l = 0, index, lineEnd;
            
            for (int i = height-1; i >= 0; i--) {
                index = i * width;
                lineEnd = l + width;
                while(l != lineEnd) {
                    val[l++] = inverted[index++];
                }
            }
        }
        int stride = ((width + 1) / 2);
        byte bdata[] = new byte[stride * height];
        int ptr = 0;
        int sh = 0;
        for (int h = 0; h < height; ++h) {
            for (int w = 0; w < width; ++w) {
                if ((w & 1) == 0)
                    bdata[sh + w / 2] = (byte)(val[ptr++] << 4);
                else
                    bdata[sh + w / 2] |= (byte)(val[ptr++] & 0x0f);
            }
            sh += stride;
        }
        return indexedModel(bdata, 4, 4);
    }
    
    private byte[] decodeRLE(boolean is8, byte values[]) {
        byte val[] = new byte[width * height];
        try {
            int ptr = 0;
            int x = 0;
            int q = 0;
            for (int y = 0; y < height && ptr < values.length;) {
                int count = values[ptr++] & 0xff;
                if (count != 0) {
                    // encoded mode
                    int bt = values[ptr++] & 0xff;
                    if (is8) {
                        for (int i = count; i != 0; --i) {
                            val[q++] = (byte)bt;
                        }
                    }
                    else {
                        for (int i = 0; i < count; ++i) {
                            val[q++] = (byte)((i & 1) == 1 ? (bt & 0x0f) : ((bt >>> 4) & 0x0f));
                        }
                    }
                    x += count;
                }
                else {
                    // escape mode
                    count = values[ptr++] & 0xff;
                    if (count == 1)
                        break;
                    switch (count) {
                        case 0:
                            x = 0;
                            ++y;
                            q = y * width;
                            break;
                        case 2:
                            // delta mode
                            x += values[ptr++] & 0xff;
                            y += values[ptr++] & 0xff;
                            q = y * width + x;
                            break;
                        default:
                            // absolute mode
                            if (is8) {
                                for (int i = count; i != 0; --i)
                                    val[q++] = (byte)(values[ptr++] & 0xff);
                            }
                            else {
                                int bt = 0;
                                for (int i = 0; i < count; ++i) {
                                    if ((i & 1) == 0)
                                        bt = values[ptr++] & 0xff;
                                    val[q++] = (byte)((i & 1) == 1 ? (bt & 0x0f) : ((bt >>> 4) & 0x0f));
                                }
                            }
                            x += count;
                            // read pad byte
                            if (is8) {
                                if ((count & 1) == 1)
                                    ++ptr;
                            }
                            else {
                                if ((count & 3) == 1 || (count & 3) == 2)
                                    ++ptr;
                            }
                            break;
                    }
                }
            }
        }
        catch (RuntimeException e) {
            //empty on purpose
        }
        
        return val;
    }
    
    // Windows defined data type reading methods - everything is little endian
    
    // Unsigned 8 bits
    private int readUnsignedByte(InputStream stream) throws IOException {
        return (stream.read() & 0xff);
    }
    
    // Unsigned 2 bytes
    private int readUnsignedShort(InputStream stream) throws IOException {
        int b1 = readUnsignedByte(stream);
        int b2 = readUnsignedByte(stream);
        return ((b2 << 8) | b1) & 0xffff;
    }
    
    // Signed 16 bits
    private int readShort(InputStream stream) throws IOException {
        int b1 = readUnsignedByte(stream);
        int b2 = readUnsignedByte(stream);
        return (b2 << 8) | b1;
    }
    
    // Unsigned 16 bits
    private int readWord(InputStream stream) throws IOException {
        return readUnsignedShort(stream);
    }
    
    // Unsigned 4 bytes
    private long readUnsignedInt(InputStream stream) throws IOException {
        int b1 = readUnsignedByte(stream);
        int b2 = readUnsignedByte(stream);
        int b3 = readUnsignedByte(stream);
        int b4 = readUnsignedByte(stream);
        long l = (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
        return l & 0xffffffff;
    }
    
    // Signed 4 bytes
    private int readInt(InputStream stream) throws IOException {
        int b1 = readUnsignedByte(stream);
        int b2 = readUnsignedByte(stream);
        int b3 = readUnsignedByte(stream);
        int b4 = readUnsignedByte(stream);
        return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }
    
    // Unsigned 4 bytes
    private long readDWord(InputStream stream) throws IOException {
        return readUnsignedInt(stream);
    }
    
    // 32 bit signed value
    private int readLong(InputStream stream) throws IOException {
        return readInt(stream);
    }
}
