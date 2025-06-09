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

import org.jspecify.annotations.Nullable;
import org.openpdf.extend.OutputDevice;
import org.openpdf.render.RenderingContext;
import org.openpdf.util.XRRuntimeException;

import java.util.LinkedList;
import java.util.List;

public class BoxRangeHelper {
    private final LinkedList<BoxRangeData> _clipRegionStack = new LinkedList<>();

    private final OutputDevice _outputDevice;
    private final List<BoxRangeData> _rangeList;

    private int _rangeIndex;
    @Nullable
    private BoxRangeData _current;

    public BoxRangeHelper(OutputDevice outputDevice, List<BoxRangeData> rangeList) {
        _outputDevice = outputDevice;
        _rangeList = rangeList;

        if (!rangeList.isEmpty()) {
            _current = rangeList.get(0);
        }
    }

    public void checkFinished() {
        if (!_clipRegionStack.isEmpty()) {
            throw new XRRuntimeException("internal error");
        }
    }

    public void pushClipRegion(RenderingContext c, int contentIndex) {
        while (_current != null && _current.getRange().getStart() == contentIndex) {
            _current.setClip(_outputDevice.getClip());
            _clipRegionStack.add(_current);

            _outputDevice.clip(_current.getBox().getChildrenClipEdge(c));

            if (_rangeIndex == _rangeList.size() - 1) {
                _current = null;
            } else {
                _current = _rangeList.get(++_rangeIndex);
            }
        }
    }

    public void popClipRegions(int contentIndex) {
        while (!_clipRegionStack.isEmpty()) {
            BoxRangeData data = _clipRegionStack.getLast();
            if (data.getRange().getEnd() == contentIndex) {
                _outputDevice.setClip(data.getClip());
                _clipRegionStack.removeLast();
            } else {
                break;
            }
        }
    }
}

