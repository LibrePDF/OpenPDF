/*
 * Copyright 2008 by Kevin Day.
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
 * the Initial Developer are Copyright (C) 1999-2008 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2008 by Paulo Soares. All Rights Reserved.
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
package com.lowagie.text.pdf.parser;

import java.util.Arrays;

/**
 * Keeps all the values of a 3 by 3 matrix
 * and allows you to do some math with matrices.
 * @since	2.1.4
 */
public class Matrix {
    /** the row=1, col=1 position ('a') in the matrix. */
    public static final int I11 = 0; 
    /** the row=1, col=2 position ('b') in the matrix. */
    public static final int I12 = 1; 
    /** the row=1, col=3 position (always 0 for 2-D) in the matrix. */
    public static final int I13 = 2;
    /** the row=2, col=1 position ('c') in the matrix. */
    public static final int I21 = 3; 
    /** the row=2, col=2 position ('d') in the matrix. */
    public static final int I22 = 4;  
    /** the row=2, col=3 position (always 0 for 2-D) in the matrix. */
    public static final int I23 = 5;  
    /** the row=3, col=1 ('e', or X translation) position in the matrix. */
    public static final int I31 = 6;  
	/** the row=3, col=2 ('f', or Y translation) position in the matrix. */
    public static final int I32 = 7;  
	/** the row=3, col=3 position (always 1 for 2-D) in the matrix. */
    public static final int I33 = 8;   
    
    /** the values inside the matrix (the identity matrix by default). */
    private final float[] vals = new float[]{
            1,0,0,
            0,1,0,
            0,0,1
    };
    
    /**
     * constructs a new Matrix with identity.
     */
    public Matrix() {
    }

    /**
     * Constructs a matrix that represents translation
     * @param tx
     * @param ty
     */
    public Matrix(float tx, float ty){
        vals[I31] = tx;
        vals[I32] = ty;
    }
    
    /**
     * Creates a Matrix with 6 specified entries
     * @param a
     * @param b
     * @param c
     * @param d
     * @param e
     * @param f
     */
    public Matrix(float a, float b, float c, float d, float e, float f){
        vals[I11] = a;
        vals[I12] = b;
        vals[I13] = 0;
        vals[I21] = c;
        vals[I22] = d;
        vals[I23] = 0;
        vals[I31] = e;
        vals[I32] = f;
        vals[I33] = 1;
    }
    
    /**
     * Gets a specific value inside the matrix.
     * @param	index	an array index corresponding with a value inside the matrix
     * @return	the value at that specific position.
     */
    public float get(int index){
        return vals[index];
    }
    
    /**
     * multiplies this matrix by 'b' and returns the result
     * See http://en.wikipedia.org/wiki/Matrix_multiplication
     * @param by The matrix to multiply by
     * @return	the resulting matrix
     */
    public Matrix multiply(Matrix by){
        Matrix rslt = new Matrix();
        
        float[] a = vals;
        float[] b = by.vals;
        float[] c = rslt.vals;
        
        c[I11] = a[I11]*b[I11] + a[I12]*b[I21] + a[I13]*b[I31];  
        c[I12] = a[I11]*b[I12] + a[I12]*b[I22] + a[I13]*b[I32]; 
        c[I13] = a[I11]*b[I13] + a[I12]*b[I23] + a[I13]*b[I33]; 
        c[I21] = a[I21]*b[I11] + a[I22]*b[I21] + a[I23]*b[I31];  
        c[I22] = a[I21]*b[I12] + a[I22]*b[I22] + a[I23]*b[I32]; 
        c[I23] = a[I21]*b[I13] + a[I22]*b[I23] + a[I23]*b[I33]; 
        c[I31] = a[I31]*b[I11] + a[I32]*b[I21] + a[I33]*b[I31];  
        c[I32] = a[I31]*b[I12] + a[I32]*b[I22] + a[I33]*b[I32]; 
        c[I33] = a[I31]*b[I13] + a[I32]*b[I23] + a[I33]*b[I33]; 
        
        return rslt;
    }

    /**
     * Subtracts a matrix from this matrix and returns the results
     * @param arg the matrix to subtract from this matrix
     * @return
     */
    public Matrix subtract(Matrix arg){
        Matrix rslt = new Matrix();
        
        float[] a = vals;
        float[] b = arg.vals;
        float[] c = rslt.vals;
        
        c[I11] = a[I11]-b[I11];  
        c[I12] = a[I12]-b[I12]; 
        c[I13] = a[I13]-b[I13]; 
        c[I21] = a[I21]-b[I21];  
        c[I22] = a[I22]-b[I22]; 
        c[I23] = a[I23]-b[I23]; 
        c[I31] = a[I31]-b[I31];  
        c[I32] = a[I32]-b[I32]; 
        c[I33] = a[I33]-b[I33]; 

        return rslt;
    }
    
    /**
     * Checks equality of matrices.
     * @param obj	the other Matrix that needs to be compared with this matrix.
     * @return	true if both matrices are equal
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Matrix))
            return false;
        
        return Arrays.equals(vals, ((Matrix)obj).vals);
    }
    
    /**
     * Generates a hash code for this object.
     * @return	the hash code of this object
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        //return Arrays.hashCode(vals); // JDK 5 code, replaced with the following
        
        int result = 1;
        for (int i = 0; i < vals.length; i++)
            result = 31 * result + Float.floatToIntBits(vals[i]);

        return result;
    }
    
    /**
     * Generates a String representation of the matrix.
     * @return	the values, delimited with tabs and newlines.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return  vals[I11] + "\t" + vals[I12] + "\t" + vals[I13] + "\n" + 
                vals[I21] + "\t" + vals[I22] + "\t" + vals[I13] + "\n" +
                vals[I31] + "\t" + vals[I32] + "\t" + vals[I33];
    }
}
