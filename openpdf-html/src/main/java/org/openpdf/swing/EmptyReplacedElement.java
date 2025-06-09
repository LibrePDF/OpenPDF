/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.swing;

import org.openpdf.extend.ReplacedElement;
import org.openpdf.layout.LayoutContext;

import java.awt.*;

public class EmptyReplacedElement implements ReplacedElement {
    private final int _width;
    private final int _height;

    private Point _location = new Point(0, 0);

    public EmptyReplacedElement(int width, int height) {
        _width = width;
        _height = height;
    }

    @Override
    public void detach(LayoutContext c) {
    }

    @Override
    public int getIntrinsicHeight() {
        return _height;
    }

    @Override
    public int getIntrinsicWidth() {
        return _width;
    }

    @Override
    public Point getLocation() {
        return _location;
    }

    @Override
    public boolean isRequiresInteractivePaint() {
        return false;
    }

    @Override
    public void setLocation(int x, int y) {
        _location = new Point(0, 0);
    }

    @Override
    public int getBaseline() {
        return 0;
    }

    @Override
    public boolean hasBaseline() {
        return false;
    }
}
