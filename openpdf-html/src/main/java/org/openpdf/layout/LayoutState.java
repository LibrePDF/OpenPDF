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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.layout;

import org.jspecify.annotations.Nullable;
import org.openpdf.render.MarkerData;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

/**
 * A bean which captures all state necessary to lay out an arbitrary box.
 * Mutable objects must be copied when provided to this class.  It is far too
 * expensive to maintain a bean of this class for each box.
 * It is only created as needed.
 */
public class LayoutState {
    private final StyleTracker _firstLines;
    private final StyleTracker _firstLetters;

    @Nullable
    private final MarkerData _currentMarkerData;

    private final Deque<BlockFormattingContext> _BFCs;

    @Nullable
    private final String _pageName;
    private final int _extraSpaceTop;
    private final int _extraSpaceBottom;
    private final int _noPageBreak;

    public LayoutState(StyleTracker firstLines, StyleTracker firstLetters, @Nullable MarkerData currentMarkerData,
                       Collection<BlockFormattingContext> blockFormattingContexts,
                       @Nullable String pageName, int extraSpaceTop, int extraSpaceBottom, int noPageBreak) {
        this._firstLines = firstLines;
        this._firstLetters = firstLetters;
        this._currentMarkerData = currentMarkerData;
        this._BFCs = new ArrayDeque<>(blockFormattingContexts);
        this._pageName = pageName;
        this._extraSpaceTop = extraSpaceTop;
        this._extraSpaceBottom = extraSpaceBottom;
        this._noPageBreak = noPageBreak;
    }

    public LayoutState(StyleTracker firstLines, StyleTracker firstLetters, @Nullable MarkerData currentMarkerData,
                       Collection<BlockFormattingContext> blockFormattingContexts) {
        this(firstLines, firstLetters, currentMarkerData, blockFormattingContexts, null, 0, 0, 0);
    }

    public Deque<BlockFormattingContext> getBFCs() {
        return _BFCs;
    }

    @Nullable
    public MarkerData getCurrentMarkerData() {
        return _currentMarkerData;
    }

    public StyleTracker getFirstLetters() {
        return _firstLetters;
    }

    public StyleTracker getFirstLines() {
        return _firstLines;
    }

    @Nullable
    public String getPageName() {
        return _pageName;
    }

    public int getExtraSpaceTop() {
        return _extraSpaceTop;
    }

    public int getExtraSpaceBottom() {
        return _extraSpaceBottom;
    }

    public int getNoPageBreak() {
        return _noPageBreak;
    }
}
