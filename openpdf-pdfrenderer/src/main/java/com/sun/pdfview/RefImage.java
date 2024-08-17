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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A BufferedImage subclass that holds a strong reference to its graphics 
 * object.  This means that the graphics will never go away as long as 
 * someone holds a reference to this image, and createGraphics() and
 * getGraphics() can be called multiple times safely, and will always return
 * the same graphics object.
 */
public class RefImage extends BufferedImage {

    /** a strong reference to the graphics object */
    private Graphics2D g;

    /** Creates a new instance of RefImage */
    public RefImage(int width, int height, int type) {
        super(width, height, type);
    }

    /** 
     * Create a graphics object only if it is currently null, otherwise
     * return the existing graphics object.
     */
    @Override
	public Graphics2D createGraphics() {
        if (this.g == null) {
            this.g = super.createGraphics();
        }

        return this.g;
    }
}