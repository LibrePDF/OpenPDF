/*
 * Copyright 2009 by Kevin Day.
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
 * https://github.com/LibrePDF/OpenPDF
 */
package com.lowagie.text.pdf.parser;

/**
 * Represents a vector (i.e. a point in space).  This class is completely unrelated to the {@link java.util.Vector}
 * class in the standard JRE.
 * <br><br>
 * For many PDF related operations, the z coordinate is specified as 1 This is to support the coordinate transformation
 * calculations.  If it helps, just think of all PDF drawing operations as occurring in a single plane with z=1.
 */
@SuppressWarnings("WeakerAccess")
public class Vector {

    /**
     * index of the X coordinate
     */
    private static final int I1 = 0;
    /**
     * index of the Y coordinate
     */
    private static final int I2 = 1;
    /**
     * index of the Z coordinate
     */
    private static final int I3 = 2;

    /**
     * the values inside the vector
     */
    private final float[] values = new float[]{
            0, 0, 0
    };

    /**
     * Creates a new Vector
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    public Vector(float x, float y, float z) {
        values[I1] = x;
        values[I2] = y;
        values[I3] = z;
    }

    /**
     * Gets the value from a coordinate of the vector
     *
     * @param index the index of the value to get (I1, I2 or I3)
     * @return value from a coordinate of the vector
     */
    public float get(int index) {
        return values[index];
    }

    /**
     * Computes the cross product of this vector and the specified matrix
     *
     * @param by the matrix to cross this vector with
     * @return the result of the cross product
     */
    public Vector cross(Matrix by) {
        float x = values[I1] * by.get(Matrix.I11) + values[I2] * by.get(Matrix.I21) + values[I3] * by.get(Matrix.I31);
        float y = values[I1] * by.get(Matrix.I12) + values[I2] * by.get(Matrix.I22) + values[I3] * by.get(Matrix.I32);
        float z = values[I1] * by.get(Matrix.I13) + values[I2] * by.get(Matrix.I23) + values[I3] * by.get(Matrix.I33);

        return new Vector(x, y, z);
    }

    /**
     * Computes the difference between this vector and the specified vector
     *
     * @param v the vector to subtract from this one
     * @return the results of the subtraction
     */
    public Vector subtract(Vector v) {
        float x = values[I1] - v.values[I1];
        float y = values[I2] - v.values[I2];
        float z = values[I3] - v.values[I3];

        return new Vector(x, y, z);
    }

    /**
     * Computes the sum of this vector and the specified vector
     *
     * @param v the vector to subtract from this one
     * @return the results of the subtraction
     */
    public Vector add(Vector v) {
        float x = values[I1] + v.values[I1];
        float y = values[I2] + v.values[I2];
        float z = values[I3] + v.values[I3];

        return new Vector(x, y, z);
    }

    /**
     * Computes the cross product of this vector and the specified vector
     *
     * @param with the vector to cross this vector with
     * @return the cross product
     */
    public Vector cross(Vector with) {
        float x = values[I2] * with.values[I3] - values[I3] * with.values[I2];
        float y = values[I3] * with.values[I1] - values[I1] * with.values[I3];
        float z = values[I1] * with.values[I2] - values[I2] * with.values[I1];

        return new Vector(x, y, z);
    }

    /**
     * Computes the dot product of this vector with the specified vector
     *
     * @param with the vector to dot product this vector with
     * @return the dot product
     */
    public float dot(Vector with) {
        return values[I1] * with.values[I1] + values[I2] * with.values[I2] + values[I3] * with.values[I3];
    }

    /**
     * Computes the length of this vector
     *
     * <b>Note:</b> If you are working with raw vectors from PDF, be careful -
     * the Z axis will generally be set to 1.  If you want to compute the length of a vector, subtract it from the
     * origin first (this will set the Z axis to 0).
     * <p>
     * For example:
     * <code>aVector.subtract(originVector).length();</code>
     *
     * @return the length of this vector
     */
    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    /**
     * Computes the length squared of this vector.
     * <p>
     * The square of the length is less expensive to compute, and is often useful without taking the square root.
     * <br><br>
     * <b>Note:</b> See the important note under {@link Vector#length()}
     *
     * @return the square of the length of the vector
     */
    public float lengthSquared() {
        return values[I1] * values[I1] + values[I2] * values[I2] + values[I3] * values[I3];
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return values[I1] + "," + values[I2] + "," + values[I3];
    }
}
