/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.extend;

import org.openpdf.layout.LayoutContext;

import java.awt.*;

/**
 * A replaced element is an XML element in the document being rendered whose visual output is delegated. For example,
 * an {@code <img>} element in HTML may be rendered using some form of {@link java.awt.Image}. The idea is that
 * there are some XML elements which Flying Saucer knows how to position and size (that's in the CSS) but has no
 * idea how to render on screen. Replaced elements serve that purpose.
 */
public interface ReplacedElement {
    int getIntrinsicWidth();

    int getIntrinsicHeight();

    /**
     * Returns the current location where the element will be rendered on the canvas
     */
    Point getLocation();

    /**
     * Assigns the new locations where the element will be rendered.
     * @param x new horizontal position
     * @param y new vertical position
     */
    void setLocation(int x, int y);

    void detach(LayoutContext c);

    boolean isRequiresInteractivePaint();

    boolean hasBaseline();

    int getBaseline();
}
