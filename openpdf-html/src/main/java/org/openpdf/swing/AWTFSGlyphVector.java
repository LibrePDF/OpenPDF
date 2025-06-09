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

import java.awt.font.GlyphVector;

import org.openpdf.extend.FSGlyphVector;

public class AWTFSGlyphVector implements FSGlyphVector {
    private final GlyphVector _glyphVector;

    public AWTFSGlyphVector(GlyphVector vector) {
        _glyphVector = vector;
    }

    public GlyphVector getGlyphVector() {
        return _glyphVector;
    }
}
