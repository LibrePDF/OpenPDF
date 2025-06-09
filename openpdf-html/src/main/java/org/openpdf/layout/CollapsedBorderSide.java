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
package org.openpdf.layout;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.newtable.CollapsedBorderValue;
import org.openpdf.newtable.TableCellBox;
import org.openpdf.render.BorderPainter;

import static org.openpdf.newtable.TableCellBox.compareBorders;

/**
 * A class that contains a single border side of a collapsed cell.  Collapsed
 * border sides are painted in order of priority (so for example, wider borders
 * always paint over narrower borders regardless of the relative tree order of
 * the cells in question).
 */
public class CollapsedBorderSide implements Comparable<CollapsedBorderSide> {
    private final TableCellBox _cell;
    private final int _side;

    public CollapsedBorderSide(TableCellBox cell, int side) {
        _side = side;
        _cell = cell;
    }

    public TableCellBox getCell() {
        return _cell;
    }

    public int getSide() {
        return _side;
    }

    @Override
    public int compareTo(CollapsedBorderSide that) {
        CollapsedBorderValue v1 = getCollapsedBorder(this);
        CollapsedBorderValue v2 = getCollapsedBorder(that);
        CollapsedBorderValue result = compareBorders(v1, v2, true);

        if (result == null) {
            return 0;
        } else {
            return result == v1 ? 1 : -1;
        }
    }

    @Nullable
    @CheckReturnValue
    private static CollapsedBorderValue getCollapsedBorder(CollapsedBorderSide c1) {
        return switch (c1._side) {
            case BorderPainter.TOP -> c1._cell.getCollapsedBorderTop();
            case BorderPainter.RIGHT -> c1._cell.getCollapsedBorderRight();
            case BorderPainter.BOTTOM -> c1._cell.getCollapsedBorderBottom();
            case BorderPainter.LEFT -> c1._cell.getCollapsedBorderLeft();
            default -> null;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollapsedBorderSide that)) return false;

        return _side == that._side && _cell.equals(that._cell);
    }

    @Override
    public int hashCode() {
        int result = _cell.hashCode();
        result = 31 * result + _side;
        return result;
    }
}
