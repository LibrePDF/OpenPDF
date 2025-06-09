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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.pdf;

import org.openpdf.render.FSFontMetrics;

public class ITextFSFontMetrics implements FSFontMetrics {
    private final float _ascent;
    private final float _descent;
    private final float _strikethroughOffset;
    private final float _strikethroughThickness;
    private final float _underlineOffset;
    private final float _underlineThickness;

    public ITextFSFontMetrics(float ascent, float descent, float strikethroughOffset, float strikethroughThickness,
                              float underlineOffset, float underlineThickness) {
        _ascent = ascent;
        _descent = descent;
        _strikethroughOffset = strikethroughOffset;
        _strikethroughThickness = strikethroughThickness;
        _underlineOffset = underlineOffset;
        _underlineThickness = underlineThickness;
    }

    @Override
    public float getAscent() {
        return _ascent;
    }

    @Override
    public float getDescent() {
        return _descent;
    }

    @Override
    public float getStrikethroughOffset() {
        return _strikethroughOffset;
    }

    @Override
    public float getStrikethroughThickness() {
        return _strikethroughThickness;
    }

    @Override
    public float getUnderlineOffset() {
        return _underlineOffset;
    }

    @Override
    public float getUnderlineThickness() {
        return _underlineThickness;
    }
}
