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

import org.w3c.dom.Text;

/**
 * A bean which serves as a way for the layout code to pass information to the
 * line breaking code and for the line breaking code to pass instructions back
 * to the layout code.
 */
public class LineBreakContext {
    private String _master;
    private int _start;
    private int _end;
    private int _savedEnd;
    private boolean _unbreakable;
    private boolean _needsNewLine;
    private int _width;
    private boolean _endsOnNL;
    private final Text _textNode;

    public LineBreakContext(String master, Text textNode) {
        this._master = master;
        this._textNode = textNode;
    }

    public int getLast() {
        return _master.length();
    }

    public void reset() {
        _width = 0;
        _unbreakable = false;
        _needsNewLine = false;
    }

    public int getEnd() {
        return _end;
    }

    public void setEnd(int end) {
        _end = end;
    }

    public String getMaster() {
        return _master;
    }

    public void setMaster(String master) {
        _master = master;
    }

    public int getStart() {
        return _start;
    }

    public void setStart(int start) {
        _start = start;
    }

    public String getStartSubstring() {
        return _master.substring(_start);
    }

    public String getCalculatedSubstring() {
        // mimic the calculation in InlineText.setSubstring to strip newlines for our width calculations
        // the original text width calculation in InlineBox.calcMaxWidthFromLineLength() excludes the newline character
        // so if we include them here we get spurious newlines
        // apparently newlines do take up some width in most fonts
        if (_end > 0 && _master.charAt(_end-1) == WhitespaceStripper.EOLC) {
            return _master.substring(_start, _end-1);
        }
        return _master.substring(_start, _end);
    }

    public boolean isUnbreakable() {
        return _unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        _unbreakable = unbreakable;
    }

    public boolean isNeedsNewLine() {
        return _needsNewLine;
    }

    public void setNeedsNewLine(boolean needsLineBreak) {
        _needsNewLine = needsLineBreak;
    }

    public int getWidth() {
        return _width;
    }

    public void setWidth(int width) {
        _width = width;
    }

    public boolean isFinished() {
        return _end == getMaster().length();
    }

    public void resetEnd() {
        _end = _savedEnd;
    }

    public void saveEnd() {
        _savedEnd = _end;
    }

    public boolean isEndsOnNL() {
        return _endsOnNL;
    }

    public void setEndsOnNL(boolean b) {
        _endsOnNL = b;
    }

    public Text getTextNode() {
        return this._textNode;
    }
}
