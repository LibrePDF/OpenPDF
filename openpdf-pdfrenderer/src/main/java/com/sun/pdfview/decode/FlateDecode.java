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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;

/**
 * decode a deFlated byte array
 * @author Mike Wessler
 */
public class FlateDecode {

    /**
     * decode a byte buffer in Flate format.
     * <p>
     * Flate is a built-in Java algorithm.  It's part of the java.util.zip
     * package.
     *
     * @param buf the deflated input buffer
     * @param params parameters to the decoder (unused)
     * @return the decoded (inflated) bytes
     */
    public static ByteBuffer decode(PDFObject dict, ByteBuffer buf,
            PDFObject params) throws IOException {
        Inflater inf = new Inflater(false);

        int bufSize = buf.remaining();

        // copy the data, since the array() method is not supported
        // on raf-based ByteBuffers
        byte[] data = new byte[bufSize];
        buf.get(data);

        // set the input to the inflater
        inf.setInput(data);

        // output to a byte-array output stream, since we don't
        // know how big the output will be
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] decomp = new byte[bufSize];
        int read = 0;

        try {        	
            while (!inf.finished()) {
                read = inf.inflate(decomp);
                if (read <= 0) {
                    if (inf.needsDictionary()) {                    	
                        throw new PDFParseException("Don't know how to ask for a dictionary in FlateDecode");
                    } else {
                       	// just return the data which is already read
                       	break;
                    }
                }
                baos.write(decomp, 0, read);
            }
        } catch (DataFormatException dfe) {
            throw new PDFParseException("Data format exception:" + dfe.getMessage());
        }

        // return the output as a byte buffer
        ByteBuffer outBytes = ByteBuffer.wrap(baos.toByteArray());

        // undo a predictor algorithm, if any was used
        if (params != null && params.getDictionary().containsKey("Predictor")) {
            Predictor predictor = Predictor.getPredictor(params);
            if (predictor != null) {
                outBytes = predictor.unpredict(outBytes);
            }
        }

        return outBytes;
    }
}