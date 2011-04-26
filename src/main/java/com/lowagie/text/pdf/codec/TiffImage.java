/*
 * Copyright 2003-2005 by Paulo Soares.
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
 */
package com.lowagie.text.pdf.codec;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Image;
import com.lowagie.text.Jpeg;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.RandomAccessFileOrArray;

/** Reads TIFF images
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class TiffImage {
    
    /** Gets the number of pages the TIFF document has.
     * @param s the file source
     * @return the number of pages
     */    
    public static int getNumberOfPages(RandomAccessFileOrArray s) {
        try {
            return TIFFDirectory.getNumDirectories(s);
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    static int getDpi(TIFFField fd, int resolutionUnit) {
        if (fd == null)
            return 0;
        long res[] = fd.getAsRational(0);
        float frac = (float)res[0] / (float)res[1];
        int dpi = 0;
        switch (resolutionUnit) {
            case TIFFConstants.RESUNIT_INCH:
            case TIFFConstants.RESUNIT_NONE:
                dpi = (int)(frac + 0.5);
                break;
            case TIFFConstants.RESUNIT_CENTIMETER:
                dpi = (int)(frac * 2.54 + 0.5);
                break;
        }
        return dpi;
    }
    
    /** Reads a page from a TIFF image. Direct mode is not used.
     * @param s the file source
     * @param page the page to get. The first page is 1
     * @return the <CODE>Image</CODE>
     */    
    public static Image getTiffImage(RandomAccessFileOrArray s, int page) {
        return getTiffImage(s, page, false);
    }
    
    /** Reads a page from a TIFF image.
     * @param s the file source
     * @param page the page to get. The first page is 1
     * @param direct for single strip, CCITT images, generate the image
     * by direct byte copying. It's faster but may not work
     * every time
     * @return the <CODE>Image</CODE>
     */    
    public static Image getTiffImage(RandomAccessFileOrArray s, int page, boolean direct) {
        if (page < 1)
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("the.page.number.must.be.gt.eq.1"));
        try {
            TIFFDirectory dir = new TIFFDirectory(s, page - 1);
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_TILEWIDTH))
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("tiles.are.not.supported"));
            int compression = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_COMPRESSION);
            switch (compression) {
                case TIFFConstants.COMPRESSION_CCITTRLEW:
                case TIFFConstants.COMPRESSION_CCITTRLE:
                case TIFFConstants.COMPRESSION_CCITTFAX3:
                case TIFFConstants.COMPRESSION_CCITTFAX4:
                    break;
                default:
                    return getTiffImageColor(dir, s);
            }
            float rotation = 0;
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_ORIENTATION)) {
                int rot = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_ORIENTATION);
                if (rot == TIFFConstants.ORIENTATION_BOTRIGHT || rot == TIFFConstants.ORIENTATION_BOTLEFT)
                    rotation = (float)Math.PI;
                else if (rot == TIFFConstants.ORIENTATION_LEFTTOP || rot == TIFFConstants.ORIENTATION_LEFTBOT)
                    rotation = (float)(Math.PI / 2.0);
                else if (rot == TIFFConstants.ORIENTATION_RIGHTTOP || rot == TIFFConstants.ORIENTATION_RIGHTBOT)
                    rotation = -(float)(Math.PI / 2.0);
            }

            Image img = null;
            long tiffT4Options = 0;
            long tiffT6Options = 0;
            int fillOrder = 1;
            int h = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_IMAGELENGTH);
            int w = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_IMAGEWIDTH);
            int dpiX = 0;
            int dpiY = 0;
            float XYRatio = 0;
            int resolutionUnit = TIFFConstants.RESUNIT_INCH;
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_RESOLUTIONUNIT))
                resolutionUnit = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_RESOLUTIONUNIT);
            dpiX = getDpi(dir.getField(TIFFConstants.TIFFTAG_XRESOLUTION), resolutionUnit);
            dpiY = getDpi(dir.getField(TIFFConstants.TIFFTAG_YRESOLUTION), resolutionUnit);
            if (resolutionUnit == TIFFConstants.RESUNIT_NONE) {
                if (dpiY != 0)
                    XYRatio = (float)dpiX / (float)dpiY;
                dpiX = 0;
                dpiY = 0;
            }
            int rowsStrip = h;
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_ROWSPERSTRIP))
                rowsStrip = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_ROWSPERSTRIP);
            if (rowsStrip <= 0 || rowsStrip > h)
                rowsStrip = h;
            long offset[] = getArrayLongShort(dir, TIFFConstants.TIFFTAG_STRIPOFFSETS);
            long size[] = getArrayLongShort(dir, TIFFConstants.TIFFTAG_STRIPBYTECOUNTS);
            if ((size == null || (size.length == 1 && (size[0] == 0 || size[0] + offset[0] > s.length()))) && h == rowsStrip) { // some TIFF producers are really lousy, so...
                size = new long[]{s.length() - (int)offset[0]};
            }
            boolean reverse = false;
            TIFFField fillOrderField =  dir.getField(TIFFConstants.TIFFTAG_FILLORDER);
            if (fillOrderField != null)
                fillOrder = fillOrderField.getAsInt(0);
            reverse = (fillOrder == TIFFConstants.FILLORDER_LSB2MSB);
            int params = 0;
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_PHOTOMETRIC)) {
                long photo = dir.getFieldAsLong(TIFFConstants.TIFFTAG_PHOTOMETRIC);
                if (photo == TIFFConstants.PHOTOMETRIC_MINISBLACK)
                    params |= Image.CCITT_BLACKIS1;
            }
            int imagecomp = 0;
            switch (compression) {
                case TIFFConstants.COMPRESSION_CCITTRLEW:
                case TIFFConstants.COMPRESSION_CCITTRLE:
                    imagecomp = Image.CCITTG3_1D;
                    params |= Image.CCITT_ENCODEDBYTEALIGN | Image.CCITT_ENDOFBLOCK;
                    break;
                case TIFFConstants.COMPRESSION_CCITTFAX3:
                    imagecomp = Image.CCITTG3_1D;
                    params |= Image.CCITT_ENDOFLINE | Image.CCITT_ENDOFBLOCK;
                    TIFFField t4OptionsField = dir.getField(TIFFConstants.TIFFTAG_GROUP3OPTIONS);
                    if (t4OptionsField != null) {
                        tiffT4Options = t4OptionsField.getAsLong(0);
                    if ((tiffT4Options & TIFFConstants.GROUP3OPT_2DENCODING) != 0)
                        imagecomp = Image.CCITTG3_2D;
                    if ((tiffT4Options & TIFFConstants.GROUP3OPT_FILLBITS) != 0)
                        params |= Image.CCITT_ENCODEDBYTEALIGN;
                    }
                    break;
                case TIFFConstants.COMPRESSION_CCITTFAX4:
                    imagecomp = Image.CCITTG4;
                    TIFFField t6OptionsField = dir.getField(TIFFConstants.TIFFTAG_GROUP4OPTIONS);
                    if (t6OptionsField != null)
                        tiffT6Options = t6OptionsField.getAsLong(0);
                    break;
            }
            if (direct && rowsStrip == h) { //single strip, direct
                byte im[] = new byte[(int)size[0]];
                s.seek(offset[0]);
                s.readFully(im);
                img = Image.getInstance(w, h, false, imagecomp, params, im);
                img.setInverted(true);
            }
            else {
                int rowsLeft = h;
                CCITTG4Encoder g4 = new CCITTG4Encoder(w);
                for (int k = 0; k < offset.length; ++k) {
                    byte im[] = new byte[(int)size[k]];
                    s.seek(offset[k]);
                    s.readFully(im);
                    int height = Math.min(rowsStrip, rowsLeft);
                    TIFFFaxDecoder decoder = new TIFFFaxDecoder(fillOrder, w, height);
                    byte outBuf[] = new byte[(w + 7) / 8 * height];
                    switch (compression) {
                        case TIFFConstants.COMPRESSION_CCITTRLEW:
                        case TIFFConstants.COMPRESSION_CCITTRLE:
                            decoder.decode1D(outBuf, im, 0, height);
                            g4.fax4Encode(outBuf,height);
                            break;
                        case TIFFConstants.COMPRESSION_CCITTFAX3:
                            try {
                                decoder.decode2D(outBuf, im, 0, height, tiffT4Options);
                            }
                            catch (RuntimeException e) {
                                // let's flip the fill bits and try again...
                                tiffT4Options ^= TIFFConstants.GROUP3OPT_FILLBITS;
                                try {
                                    decoder.decode2D(outBuf, im, 0, height, tiffT4Options);
                                }
                                catch (RuntimeException e2) {
                                    throw e;
                                }
                            }
                            g4.fax4Encode(outBuf, height);
                            break;
                        case TIFFConstants.COMPRESSION_CCITTFAX4:
                            decoder.decodeT6(outBuf, im, 0, height, tiffT6Options);
                            g4.fax4Encode(outBuf, height);
                            break;
                    }
                    rowsLeft -= rowsStrip;
                }
                byte g4pic[] = g4.close();
                img = Image.getInstance(w, h, false, Image.CCITTG4, params & Image.CCITT_BLACKIS1, g4pic);
            }
            img.setDpi(dpiX, dpiY);
            img.setXYRatio(XYRatio);
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_ICCPROFILE)) {
                try {
                    TIFFField fd = dir.getField(TIFFConstants.TIFFTAG_ICCPROFILE);
                    ICC_Profile icc_prof = ICC_Profile.getInstance(fd.getAsBytes());
                    if (icc_prof.getNumComponents() == 1)
                        img.tagICC(icc_prof);
                }
                catch (RuntimeException e) {
                    //empty
                }
            }
            img.setOriginalType(Image.ORIGINAL_TIFF);
            if (rotation != 0)
                img.setInitialRotation(rotation);
            return img;
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
    
    protected static Image getTiffImageColor(TIFFDirectory dir, RandomAccessFileOrArray s) {
        try {
            int compression = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_COMPRESSION);
            int predictor = 1;
            TIFFLZWDecoder lzwDecoder = null;
            switch (compression) {
                case TIFFConstants.COMPRESSION_NONE:
                case TIFFConstants.COMPRESSION_LZW:
                case TIFFConstants.COMPRESSION_PACKBITS:
                case TIFFConstants.COMPRESSION_DEFLATE:
                case TIFFConstants.COMPRESSION_ADOBE_DEFLATE:
                case TIFFConstants.COMPRESSION_OJPEG:
                case TIFFConstants.COMPRESSION_JPEG:
                    break;
                default:
                    throw new IllegalArgumentException(MessageLocalization.getComposedMessage("the.compression.1.is.not.supported", compression));
            }
            int photometric = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_PHOTOMETRIC);
            switch (photometric) {
                case TIFFConstants.PHOTOMETRIC_MINISWHITE:
                case TIFFConstants.PHOTOMETRIC_MINISBLACK:
                case TIFFConstants.PHOTOMETRIC_RGB:
                case TIFFConstants.PHOTOMETRIC_SEPARATED:
                case TIFFConstants.PHOTOMETRIC_PALETTE:
                    break;
                default:
                    if (compression != TIFFConstants.COMPRESSION_OJPEG && compression != TIFFConstants.COMPRESSION_JPEG)
                        throw new IllegalArgumentException(MessageLocalization.getComposedMessage("the.photometric.1.is.not.supported", photometric));
            }
            float rotation = 0;
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_ORIENTATION)) {
                int rot = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_ORIENTATION);
                if (rot == TIFFConstants.ORIENTATION_BOTRIGHT || rot == TIFFConstants.ORIENTATION_BOTLEFT)
                    rotation = (float)Math.PI;
                else if (rot == TIFFConstants.ORIENTATION_LEFTTOP || rot == TIFFConstants.ORIENTATION_LEFTBOT)
                    rotation = (float)(Math.PI / 2.0);
                else if (rot == TIFFConstants.ORIENTATION_RIGHTTOP || rot == TIFFConstants.ORIENTATION_RIGHTBOT)
                    rotation = -(float)(Math.PI / 2.0);
            }
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_PLANARCONFIG)
                && dir.getFieldAsLong(TIFFConstants.TIFFTAG_PLANARCONFIG) == TIFFConstants.PLANARCONFIG_SEPARATE)
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("planar.images.are.not.supported"));
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_EXTRASAMPLES))
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("extra.samples.are.not.supported"));
            int samplePerPixel = 1;
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_SAMPLESPERPIXEL)) // 1,3,4
                samplePerPixel = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_SAMPLESPERPIXEL);
            int bitsPerSample = 1;
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_BITSPERSAMPLE))
                bitsPerSample = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_BITSPERSAMPLE);
            switch (bitsPerSample) {
                case 1:
                case 2:
                case 4:
                case 8:
                    break;
                default:
                    throw new IllegalArgumentException(MessageLocalization.getComposedMessage("bits.per.sample.1.is.not.supported", bitsPerSample));
            }
            Image img = null;

            int h = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_IMAGELENGTH);
            int w = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_IMAGEWIDTH);
            int dpiX = 0;
            int dpiY = 0;
            int resolutionUnit = TIFFConstants.RESUNIT_INCH;
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_RESOLUTIONUNIT))
                resolutionUnit = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_RESOLUTIONUNIT);
            dpiX = getDpi(dir.getField(TIFFConstants.TIFFTAG_XRESOLUTION), resolutionUnit);
            dpiY = getDpi(dir.getField(TIFFConstants.TIFFTAG_YRESOLUTION), resolutionUnit);
            int fillOrder = 1;
            boolean reverse = false;
            TIFFField fillOrderField =  dir.getField(TIFFConstants.TIFFTAG_FILLORDER);
            if (fillOrderField != null)
                fillOrder = fillOrderField.getAsInt(0);
            reverse = (fillOrder == TIFFConstants.FILLORDER_LSB2MSB);
            int rowsStrip = h;
            if (dir.isTagPresent(TIFFConstants.TIFFTAG_ROWSPERSTRIP)) //another hack for broken tiffs
                rowsStrip = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_ROWSPERSTRIP);
            if (rowsStrip <= 0 || rowsStrip > h)
                rowsStrip = h;
            long offset[] = getArrayLongShort(dir, TIFFConstants.TIFFTAG_STRIPOFFSETS);
            long size[] = getArrayLongShort(dir, TIFFConstants.TIFFTAG_STRIPBYTECOUNTS);
            if ((size == null || (size.length == 1 && (size[0] == 0 || size[0] + offset[0] > s.length()))) && h == rowsStrip) { // some TIFF producers are really lousy, so...
                size = new long[]{s.length() - (int)offset[0]};
            }
            if (compression == TIFFConstants.COMPRESSION_LZW) {
                TIFFField predictorField = dir.getField(TIFFConstants.TIFFTAG_PREDICTOR);
                if (predictorField != null) {
                    predictor = predictorField.getAsInt(0);
                    if (predictor != 1 && predictor != 2) {
                        throw new RuntimeException(MessageLocalization.getComposedMessage("illegal.value.for.predictor.in.tiff.file"));
                    }
                    if (predictor == 2 && bitsPerSample != 8) {
                        throw new RuntimeException(MessageLocalization.getComposedMessage("1.bit.samples.are.not.supported.for.horizontal.differencing.predictor", bitsPerSample));
                    }
                }
                lzwDecoder = new TIFFLZWDecoder(w, predictor, 
                                                samplePerPixel); 
            }
            int rowsLeft = h;
            ByteArrayOutputStream stream = null;
            DeflaterOutputStream zip = null;
            CCITTG4Encoder g4 = null;
            if (bitsPerSample == 1 && samplePerPixel == 1) {
                g4 = new CCITTG4Encoder(w);
            }
            else {
                stream = new ByteArrayOutputStream();
                if (compression != TIFFConstants.COMPRESSION_OJPEG && compression != TIFFConstants.COMPRESSION_JPEG)
                    zip = new DeflaterOutputStream(stream);
            }
            if (compression == TIFFConstants.COMPRESSION_OJPEG) {
                
                // Assume that the TIFFTAG_JPEGIFBYTECOUNT tag is optional, since it's obsolete and 
                // is often missing

                if ((!dir.isTagPresent(TIFFConstants.TIFFTAG_JPEGIFOFFSET))) {
                    throw new IOException(MessageLocalization.getComposedMessage("missing.tag.s.for.ojpeg.compression"));
                }
                int jpegOffset = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_JPEGIFOFFSET);
                int jpegLength = s.length() - jpegOffset;

                if (dir.isTagPresent(TIFFConstants.TIFFTAG_JPEGIFBYTECOUNT)) {
                    jpegLength = (int)dir.getFieldAsLong(TIFFConstants.TIFFTAG_JPEGIFBYTECOUNT) +
                        (int)size[0];
                }
                
                byte[] jpeg = new byte[Math.min(jpegLength, s.length() - jpegOffset)];

                int posFilePointer = s.getFilePointer();
                posFilePointer += jpegOffset;
                s.seek(posFilePointer);
                s.readFully(jpeg);
                img = new Jpeg(jpeg);
            } 
            else if (compression == TIFFConstants.COMPRESSION_JPEG) {
                if (size.length > 1)
                    throw new IOException(MessageLocalization.getComposedMessage("compression.jpeg.is.only.supported.with.a.single.strip.this.image.has.1.strips", size.length));
                byte[] jpeg = new byte[(int)size[0]];
                s.seek(offset[0]);
                s.readFully(jpeg);
                img = new Jpeg(jpeg);
            } 
            else {
                for (int k = 0; k < offset.length; ++k) {
                    byte im[] = new byte[(int)size[k]];
                    s.seek(offset[k]);
                    s.readFully(im);
                    int height = Math.min(rowsStrip, rowsLeft);
                    byte outBuf[] = null;
                    if (compression != TIFFConstants.COMPRESSION_NONE)
                        outBuf = new byte[(w * bitsPerSample * samplePerPixel + 7) / 8 * height];
                    if (reverse)
                        TIFFFaxDecoder.reverseBits(im);
                    switch (compression) {
                        case TIFFConstants.COMPRESSION_DEFLATE:
                        case TIFFConstants.COMPRESSION_ADOBE_DEFLATE:
                            inflate(im, outBuf);
                            break;
                        case TIFFConstants.COMPRESSION_NONE:
                            outBuf = im;
                            break;
                        case TIFFConstants.COMPRESSION_PACKBITS:
                            decodePackbits(im,  outBuf);
                            break;
                        case TIFFConstants.COMPRESSION_LZW:
                            lzwDecoder.decode(im, outBuf, height);
                            break;
                    }
                    if (bitsPerSample == 1 && samplePerPixel == 1) {
                        g4.fax4Encode(outBuf, height);
                    }
                    else {
                        zip.write(outBuf);
                    }
                    rowsLeft -= rowsStrip;
                }
                if (bitsPerSample == 1 && samplePerPixel == 1) {
                    img = Image.getInstance(w, h, false, Image.CCITTG4, 
                        photometric == TIFFConstants.PHOTOMETRIC_MINISBLACK ? Image.CCITT_BLACKIS1 : 0, g4.close());
                }
                else {
                    zip.close();
                    img = Image.getInstance(w, h, samplePerPixel, bitsPerSample, stream.toByteArray());
                    img.setDeflated(true);
                }
            }
            img.setDpi(dpiX, dpiY);
            if (compression != TIFFConstants.COMPRESSION_OJPEG && compression != TIFFConstants.COMPRESSION_JPEG) {
                if (dir.isTagPresent(TIFFConstants.TIFFTAG_ICCPROFILE)) {
                    try {
                        TIFFField fd = dir.getField(TIFFConstants.TIFFTAG_ICCPROFILE);
                        ICC_Profile icc_prof = ICC_Profile.getInstance(fd.getAsBytes());
                        if (samplePerPixel == icc_prof.getNumComponents())
                            img.tagICC(icc_prof);
                    }
                    catch (RuntimeException e) {
                        //empty
                    }
                }
                if (dir.isTagPresent(TIFFConstants.TIFFTAG_COLORMAP)) {
                    TIFFField fd = dir.getField(TIFFConstants.TIFFTAG_COLORMAP);
                    char rgb[] = fd.getAsChars();
                    byte palette[] = new byte[rgb.length];
                    int gColor = rgb.length / 3;
                    int bColor = gColor * 2;
                    for (int k = 0; k < gColor; ++k) {
                        palette[k * 3] = (byte)(rgb[k] >>> 8);
                        palette[k * 3 + 1] = (byte)(rgb[k + gColor] >>> 8);
                        palette[k * 3 + 2] = (byte)(rgb[k + bColor] >>> 8);
                    }
                    PdfArray indexed = new PdfArray();
                    indexed.add(PdfName.INDEXED);
                    indexed.add(PdfName.DEVICERGB);
                    indexed.add(new PdfNumber(gColor - 1));
                    indexed.add(new PdfString(palette));
                    PdfDictionary additional = new PdfDictionary();
                    additional.put(PdfName.COLORSPACE, indexed);
                    img.setAdditional(additional);
                }
                img.setOriginalType(Image.ORIGINAL_TIFF);
            }
            if (photometric == TIFFConstants.PHOTOMETRIC_MINISWHITE)
                img.setInverted(true);
            if (rotation != 0)
                img.setInitialRotation(rotation);
            return img;
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
    
    static long[] getArrayLongShort(TIFFDirectory dir, int tag) {
        TIFFField field = dir.getField(tag);
        if (field == null)
            return null;
        long offset[];
        if (field.getType() == TIFFField.TIFF_LONG)
            offset = field.getAsLongs();
        else { // must be short
            char temp[] = field.getAsChars();
            offset = new long[temp.length];
            for (int k = 0; k < temp.length; ++k)
                offset[k] = temp[k];
        }
        return offset;
    }
    
    // Uncompress packbits compressed image data.
    public static void decodePackbits(byte data[], byte[] dst) {
        int srcCount = 0, dstCount = 0;
        byte repeat, b;
        
        try {
            while (dstCount < dst.length) {
                b = data[srcCount++];
                if (b >= 0 && b <= 127) {
                    // literal run packet
                    for (int i=0; i<(b + 1); i++) {
                        dst[dstCount++] = data[srcCount++];
                    }

                } else if (b <= -1 && b >= -127) {
                    // 2 byte encoded run packet
                    repeat = data[srcCount++];
                    for (int i=0; i<(-b + 1); i++) {
                        dst[dstCount++] = repeat;
                    }
                } else {
                    // no-op packet. Do nothing
                    srcCount++;
                }
            }
        }
        catch (Exception e) {
            // do nothing
        }
    }

    public static void inflate(byte[] deflated, byte[] inflated) {
        Inflater inflater = new Inflater();
        inflater.setInput(deflated);
        try {
            inflater.inflate(inflated);
        }
        catch(DataFormatException dfe) {
            throw new ExceptionConverter(dfe);
        }
    }

}
