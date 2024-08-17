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

package com.sun.pdfview.font;

/**
 * A floating-point Point, with public fields.  Also contains a flag
 * for "open" to indicate that the path this point is a member of has
 * or hasn't been closed.
 *
 * @author Mike Wessler
 */
public class FlPoint {
    /** x coordinate of the point */
    public float x= 0;

    /** y coordinate of the point */
    public float y= 0;

    /**
     * whether the path this point is a part of is open or closed.
     * used in Type1CFont.java.
     */
    public boolean open= false;
    
    /** reset the values to (0,0) and closed */
    public final void reset() {
	this.x= 0;
	this.y= 0;
	this.open= false;
    }
}