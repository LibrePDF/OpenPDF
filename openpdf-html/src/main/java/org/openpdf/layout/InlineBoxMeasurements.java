/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
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
package org.openpdf.layout;

/**
 * A bean which tracks various characteristics of an inline box.  It is used
 * when calculating the vertical position of boxes in a line.
 */
public class InlineBoxMeasurements {
    private final int _textTop;
    private final int _textBottom;
    private final int _baseline;
    private final int _inlineTop;
    private final int _inlineBottom;

    private final int _paintingTop;
    private final int _paintingBottom;

    public InlineBoxMeasurements(int baseline,
                                 int textTop, int textBottom,
                                 int inlineTop, int inlineBottom,
                                 int paintingTop, int paintingBottom) {
        _textTop = textTop;
        _textBottom = textBottom;
        _baseline = baseline;
        _inlineTop = inlineTop;
        _inlineBottom = inlineBottom;
        _paintingTop = paintingTop;
        _paintingBottom = paintingBottom;
    }

    public int getBaseline() {
        return _baseline;
    }

    public int getInlineBottom() {
        return _inlineBottom;
    }

    public int getInlineTop() {
        return _inlineTop;
    }

    public int getTextBottom() {
        return _textBottom;
    }

    public int getTextTop() {
        return _textTop;
    }

    public int getPaintingBottom() {
        return _paintingBottom;
    }

    public int getPaintingTop() {
        return _paintingTop;
    }
}
