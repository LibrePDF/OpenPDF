/* Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;

/**
 * decode a JPX encoded imagestream into a byte array.  This class uses Java's
 * image_io JPEG2000 reader to do the decoding.
 *
 * @author Bernd Rosstauscher
 */

public class JPXDecode {
	
    /*************************************************************************
     * @param dict
     * @param buf
     * @param params
     * @return
     * @throws PDFParseException
     ************************************************************************/
	
    protected static ByteBuffer decode(PDFObject dict, ByteBuffer buf, PDFObject params) throws PDFParseException {
        BufferedImage bimg = loadImageData(buf);
        byte[] output = ImageDataDecoder.decodeImageData(bimg);
		return ByteBuffer.wrap(output);
    }

	/*************************************************************************
	 * @param buf
	 * @return
	 * @throws PDFParseException
	 * @throws IOException
	 ************************************************************************/
    
	private static BufferedImage loadImageData(ByteBuffer buf) throws PDFParseException {
        ImageReader reader = null;
		try {
			byte[] input = new byte[buf.remaining()];
			buf.get(input);
			Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType("image/jpeg2000");
			if (readers.hasNext() == false) {
				throw new PDFParseException("JPXDecode failed. No reader available");
			}
			reader = readers.next();
			reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(input)));
			BufferedImage bimg = reader.read(0);
			return bimg;
		} catch (IOException e) {
            throw new PDFParseException("JPXDecode failed", e);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
		}

	}
}