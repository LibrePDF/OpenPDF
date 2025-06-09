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
package org.openpdf.render;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.layout.FloatManager;
import org.openpdf.layout.Layer;

/**
 * A bean containing additional information used by floated boxes.  The
 * {@code marginFromSibling} property contains the margin from our
 * previous inflow block level sibling (if it exists).  It is necessary to
 * correctly position the box when collapsing vertical margins.
 */
public class FloatedBoxData {
    @Nullable
    private Layer _drawingLayer;
    @Nullable
    private FloatManager _manager;
    private int _marginFromSibling;

    @Nullable
    @CheckReturnValue
    public Layer getDrawingLayer() {
        return _drawingLayer;
    }

    public void setDrawingLayer(Layer drawingLayer) {
        _drawingLayer = drawingLayer;
    }

    @Nullable
    @CheckReturnValue
    public FloatManager getManager() {
        return _manager;
    }

    public void setManager(@Nullable FloatManager manager) {
        _manager = manager;
    }

    public int getMarginFromSibling() {
        return _marginFromSibling;
    }

    public void setMarginFromSibling(int marginFromSibling) {
        _marginFromSibling = marginFromSibling;
    }
}
