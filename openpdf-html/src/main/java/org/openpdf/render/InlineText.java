/*
 * {{{ header & license
 * Copyright (c) 2005 Joshua Marinacci
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
package org.openpdf.render;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Text;
import org.openpdf.extend.FSGlyphVector;
import org.openpdf.layout.FunctionData;
import org.openpdf.layout.LayoutContext;
import org.openpdf.layout.WhitespaceStripper;
import org.openpdf.util.Uu;

import java.awt.*;

/**
 * A lightweight object which contains a chunk of text from an inline element.
 * It will never extend across a line break nor will it extend across an element
 * nested within its inline element.
 */
public class InlineText {
    @Nullable
    private InlineLayoutBox _parent;

    private int _x;

    private String _masterText;
    private int _start;
    private int _end;

    private int _width;

    @Nullable
    private FunctionData _functionData;

    private boolean _containedLF = false;

    private short _selectionStart;
    private short _selectionEnd;

    private float @Nullable [] _glyphPositions;

    private boolean _trimmedLeadingSpace;
    private boolean _trimmedTrailingSpace;
    private final Text _textNode;

    public void trimTrailingSpace(LayoutContext c) {
        if (! isEmpty() && _masterText.charAt(_end-1) == ' ') {
            _end--;
            setWidth(c.getTextRenderer().getWidth(c.getFontContext(),
                    getParent().getStyle().getFSFont(c),
                    getSubstring()));
            setTrimmedTrailingSpace();
        }
    }

    public boolean isEmpty() {
        return _start == _end && ! _containedLF;
    }

    @CheckReturnValue
    public String getSubstring() {
        if (getMasterText() != null) {
            if (_start == -1 || _end == -1) {
                throw new RuntimeException("negative index in InlineBox (start: %s, end: %s) for element %s".formatted(_start, _end, this));
            }
            if (_end < _start) {
                throw new RuntimeException("end is less than start (%s < %s) for element %s".formatted(_end, _start, this));
            }
            return getMasterText().substring(_start, _end);
        } else {
            throw new RuntimeException("No master text set for element " + this);
        }
    }

    public InlineText(String masterText, Text textNode, int start, int end, int width) {
        _masterText = masterText;
        _textNode = textNode;
        _width = width;

        if (end < start) {
            Uu.p("setting substring to: " + start + " " + end);
            throw new RuntimeException("end is less than start (%s < %s) for element %s".formatted(end, start, this));
        } else if (end < 0 || start < 0) {
            throw new RuntimeException("Trying to set negative index to inline box (start: %s, end: %s)".formatted(start, end));
        }
        _start = start;
        _end = end;

        if (_end > 0 && _masterText.charAt(_end-1) == WhitespaceStripper.EOLC) {
            _containedLF = true;
            _end--;
        }
    }

    public String getMasterText() {
        return _masterText;
    }

    public int getX() {
        return _x;
    }

    public void setX(int x) {
        _x = x;
    }

    public int getWidth() {
        return _width;
    }

    public final void setWidth(int width) {
        _width = width;
    }

    public void paint(RenderingContext c) {
        c.getOutputDevice().drawText(c, this);
    }

    public void paintSelection(RenderingContext c) {
        c.getOutputDevice().drawSelection(c, this);
    }

    @Nullable
    public InlineLayoutBox getParent() {
        return _parent;
    }

    public void setParent(InlineLayoutBox parent) {
        _parent = parent;
    }

    public boolean isDynamicFunction() {
        return _functionData != null;
    }

    public FunctionData getFunctionData() {
        return _functionData;
    }

    public void setFunctionData(FunctionData functionData) {
        _functionData = functionData;
    }

    public void updateDynamicValue(RenderingContext c) {
        String value = _functionData.getContentFunction().calculate(
                c, _functionData.getFunction(), this);
        _start = 0;
        _end = value.length();
        _masterText = value;
        _width = c.getTextRenderer().getWidth(
                c.getFontContext(), getParent().getStyle().getFSFont(c),
                value);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("InlineText: ");
        if (_containedLF || isDynamicFunction()) {
            result.append("(");
            if (_containedLF) {
                result.append('L');
            }
            if (isDynamicFunction()) {
                result.append('F');
            }
            result.append(") ");
        }
        result.append('(');
        result.append(getSubstring());
        result.append(')');

        return result.toString();
    }

    public boolean updateSelection(RenderingContext c, Rectangle selection) {
        ensureGlyphPositions(c);
        float[] positions = _glyphPositions;
        int y = getParent().getAbsY();
        int offset = getParent().getAbsX() + getX();

        int prevSelectionStart = _selectionStart;
        int prevSelectionEnd = _selectionEnd;

        boolean found = false;
        _selectionStart = 0;
        _selectionEnd = 0;
        for (int i = 0; i < positions.length - 2; i += 2) {
            Rectangle target = new Rectangle(
                    (int)(offset + (positions[i] + positions[i+2]) / 2),
                    y,
                    1,
                    getParent().getHeight());
            if (selection.intersects(target)) {
                if (! found) {
                    found = true;
                    _selectionStart = (short)(i / 2);
                    _selectionEnd = (short)(i / 2 + 1);
                } else {
                    _selectionEnd++;
                }
            }
        }

        return prevSelectionStart != _selectionStart || prevSelectionEnd != _selectionEnd;
    }

    private void ensureGlyphPositions(RenderingContext c) {
        if (_glyphPositions == null) {
            FSGlyphVector glyphVector = c.getTextRenderer().getGlyphVector(
                    c.getOutputDevice(),
                    getParent().getStyle().getFSFont(c),
                    getSubstring());
            _glyphPositions = c.getTextRenderer().getGlyphPositions(
                    c.getOutputDevice(),
                    getParent().getStyle().getFSFont(c),
                    glyphVector);
        }
    }

    public boolean clearSelection() {
        boolean result = _selectionStart != 0 || _selectionEnd != 0;

        _selectionStart = 0;
        _selectionEnd = 0;

        return result;
    }

    public boolean isSelected() {
        return _selectionStart != _selectionEnd;
    }

    public short getSelectionEnd() {
        return _selectionEnd;
    }

    public short getSelectionStart() {
        return _selectionStart;
    }

    public String getSelection() {
        return getSubstring().substring(_selectionStart, _selectionEnd);
    }

    public void selectAll() {
        _selectionStart = 0;
        _selectionEnd = (short)getSubstring().length();
    }

    public String getTextExportText() {
        char[] ch = getSubstring().toCharArray();
        StringBuilder result = new StringBuilder();
        if (isTrimmedLeadingSpace()) {
            result.append(' ');
        }
        for (char c : ch) {
            if (c != '\n') {
                result.append(c);
            }
        }
        if (isTrimmedTrailingSpace()) {
            result.append(' ');
        }
        return result.toString();
    }

    public boolean isTrimmedLeadingSpace() {
        return _trimmedLeadingSpace;
    }

    public void setTrimmedLeadingSpace(boolean trimmedLeadingSpace) {
        _trimmedLeadingSpace = trimmedLeadingSpace;
    }

    private void setTrimmedTrailingSpace() {
        _trimmedTrailingSpace = true;
    }

    private boolean isTrimmedTrailingSpace() {
        return _trimmedTrailingSpace;
    }

    public void countJustifiableChars(CharCounts counts) {
        String s = getSubstring();
        int len = s.length();
        int spaces = 0;
        int other = 0;

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == ' ' || c == '\u00a0' || c == '\u3000') {
                spaces++;
            } else {
                other++;
            }
        }

        counts.setSpaceCount(counts.getSpaceCount() + spaces);
        counts.setNonSpaceCount(counts.getNonSpaceCount() + other);
    }

    public float calcTotalAdjustment(JustificationInfo info) {
        String s = getSubstring();
        int len = s.length();

        float result = 0.0f;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == ' ' || c == '\u00a0' || c == '\u3000') {
                result += info.spaceAdjust();
            } else {
                result += info.nonSpaceAdjust();
            }
        }

        return result;
    }
    public int getStart(){
        return _start;
    }
    public int getEnd(){
        return _end;
    }
    public void setSelectionStart(short s){
        _selectionStart = s;
    }
    public void setSelectionEnd(short s){
        _selectionEnd = s;
    }

    public Text getTextNode() {
        return this._textNode;
    }
}

