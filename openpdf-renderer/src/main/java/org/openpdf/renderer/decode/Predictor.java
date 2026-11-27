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

package org.openpdf.renderer.decode;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.openpdf.renderer.PDFObject;
import org.openpdf.renderer.PDFParseException;

/**
 * The abstract superclass of various predictor objects that undo well-known
 * prediction algorithms.
 */
public abstract class Predictor {
    /** well known algorithms */
    public static final int TIFF = 0;
    public static final int PNG = 1;
    
    /** the algorithm to use */
    private final int algorithm;
    
    /** the number of colors per sample */
    private int colors = 1;
    
    /** the number of bits per color component */
    private int bpc = 8;
    
    /** the number of columns per row */
    private int columns = 1;
    
    /** 
     * Create an instance of a predictor.  Use <code>getPredictor()</code>
     * instead of this.
     */
    protected Predictor(int algorithm) {
        this.algorithm = algorithm;
    }
    
    /**
     * Actually perform this algorithm on decoded image data.
     * Subclasses must implement this method
     */
    public abstract ByteBuffer unpredict(ByteBuffer imageData)
        throws IOException;
    
    /**
     * Get an instance of a predictor
     *
     * @param params the filter parameters
     */
    public static Predictor getPredictor(PDFObject params) throws IOException {
        // get the algorithm (required)
        PDFObject algorithmObj = params.getDictRef("Predictor");
        if (algorithmObj == null) {
            // no predictor
            return null;
        }
        int algorithm = algorithmObj.getIntValue();
    
        // create the predictor object
        Predictor predictor = null;
        switch (algorithm) {
            case 1:
                // no predictor
                return null;
            case 2:
                predictor = new TIFFPredictor();
            break;
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                predictor = new PNGPredictor();
                break;
            default:
                throw new PDFParseException("Unknown predictor: " + algorithm);
        }
        
        // read the colors (optional)
        PDFObject colorsObj = params.getDictRef("Colors");
        if (colorsObj != null) {
            predictor.setColors(colorsObj.getIntValue());
        }
        
        // read the bits per component (optional)
        PDFObject bpcObj = params.getDictRef("BitsPerComponent");
        if (bpcObj != null) {
            predictor.setBitsPerComponent(bpcObj.getIntValue());
        }
        
        // read the columns (optional)
        PDFObject columnsObj = params.getDictRef("Columns");
        if (columnsObj != null) {
            predictor.setColumns(columnsObj.getIntValue());
        }
        
        // make sure that the parameters describe a row of samples
        predictor.getBytesPerRow();

        // all set
        return predictor;
    }
    
    /**
     * Get the number of bytes that a row of samples takes, without the
     * byte that tells which PNG algorithm was used for the row.
     *
     * @return the number of bytes, which is at least one
     * @throws PDFParseException if there is no such row: the number of
     *  columns or colors is less than one, the number of bits per component
     *  is not 1, 2, 4, 8 or 16, or the row is too long to be read
     */
    protected int getBytesPerRow() throws PDFParseException {
        boolean validBits = this.bpc == 1 || this.bpc == 2 || this.bpc == 4 || this.bpc == 8 || this.bpc == 16;
        long samples = (long) this.columns * this.colors;
        if (!validBits || this.columns < 1 || this.colors < 1 || samples > Integer.MAX_VALUE) {
            throw new PDFParseException("Invalid predictor parameters: Columns " + this.columns
                    + ", Colors " + this.colors + ", BitsPerComponent " + this.bpc);
        }
        long bytes = (samples * this.bpc + 7) / 8;
        if (bytes >= Integer.MAX_VALUE) {
            throw new PDFParseException("Predictor row is too long: " + bytes);
        }
        return (int) bytes;
    }

    /**
     * Get the algorithm in use
     *
     * @return one of the known algorithm types
     */
    public int getAlgorithm() {
        return this.algorithm;
    }
    
    /**
     * Get the number of colors per sample
     */
    public int getColors() {
        return this.colors;
    }
    
    /**
     * Set the number of colors per sample
     */
    protected void setColors(int colors) {
        this.colors = colors;
    }
    
    /**
     * Get the number of bits per color component
     */
    public int getBitsPerComponent() {
        return this.bpc;
    }
    
    /**
     * Set the number of bits per color component
     */
    public void setBitsPerComponent(int bpc) {
        this.bpc = bpc;
    }
    
    /**
     * Get the number of columns
     */
    public int getColumns() {
        return this.columns;
    }
    
    /**
     * Set the number of columns
     */
    public void setColumns(int columns) {
        this.columns = columns;
    }
}