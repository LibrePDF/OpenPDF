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
package com.sun.pdfview;

import java.awt.geom.Rectangle2D;
import java.awt.Color;

public class ImageInfo {

    int width;
    int height;
    Rectangle2D clip;
    Color bgColor;

    public ImageInfo(int width, int height, Rectangle2D clip) {
        this(width, height, clip, Color.WHITE);
    }

    public ImageInfo(int width, int height, Rectangle2D clip, Color bgColor) {
        this.width = width;
        this.height = height;
        this.clip = clip;
        this.bgColor = bgColor;
    }

    // a hashcode that uses width, height and clip to generate its number
    @Override
    public int hashCode() {
        int code = (this.width ^ this.height << 16);

        if (this.clip != null) {
            code ^= ((int) this.clip.getWidth() | (int) this.clip.getHeight()) << 8;
            code ^= ((int) this.clip.getMinX() | (int) this.clip.getMinY());
        }

        return code;
    }

    // an equals method that compares values
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ImageInfo)) {
            return false;
        }

        ImageInfo ii = (ImageInfo) o;

        if (this.width != ii.width || this.height != ii.height) {
            return false;
        } else if (this.clip != null && ii.clip != null) {
            return this.clip.equals(ii.clip);
        } else if (this.clip == null && ii.clip == null) {
            return true;
        } else {
            return false;
        }
    }
}