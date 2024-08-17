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

/**
 * The abstract superclass of all drawing commands for a PDFPage.
 * @author Mike Wessler
 */
public abstract class PDFCmd {

    /**
     * mark the page or change the graphics state
     * @param state the current graphics state;  may be modified during
     * execution.
     * @return the region of the page made dirty by executing this command
     *         or null if no region was touched.  Note this value should be
     *         in the coordinates of the image touched, not the page.
     */
    public abstract Rectangle2D execute(PDFRenderer state);

    /**
     * a human readable representation of this command
     */
    @Override
    public String toString() {
        String name = getClass().getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            return name.substring(lastDot + 1);
        } else {
            return name;
        }
    }

    /**
     * the details of this command
     */
    public String getDetails() {
        return super.toString();
    }
}