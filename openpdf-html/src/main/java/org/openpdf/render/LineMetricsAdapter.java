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
package org.openpdf.render;

import java.awt.font.LineMetrics;


/**
 * A note on this class: What we really want is a FontMetrics2D object (i.e.
 * font metrics with float precision).  Unfortunately, it doesn't seem
 * the JDK provides this.  However, looking at the JDK code, it appears the
 * metrics contained in the LineMetrics are actually the metrics of the font, not
 * the metrics of the line (and empirically strings of "X" and "j" return the same
 * value for getAscent()).  So... for now we use LineMetrics for font metrics.
 */
public class LineMetricsAdapter implements FSFontMetrics {
    private final LineMetrics _lineMetrics;

    public LineMetricsAdapter(LineMetrics lineMetrics) {
        _lineMetrics = lineMetrics;
    }

    @Override
    public float getAscent() {
        return _lineMetrics.getAscent();
    }

    @Override
    public float getDescent() {
        return _lineMetrics.getDescent();
    }

    @Override
    public float getStrikethroughOffset() {
        return _lineMetrics.getStrikethroughOffset();
    }

    @Override
    public float getStrikethroughThickness() {
        return _lineMetrics.getStrikethroughThickness();
    }

    @Override
    public float getUnderlineOffset() {
        return _lineMetrics.getUnderlineOffset();
    }

    @Override
    public float getUnderlineThickness() {
        return _lineMetrics.getUnderlineThickness();
    }
}
