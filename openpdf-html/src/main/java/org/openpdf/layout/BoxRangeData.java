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
import org.openpdf.render.BlockBox;

import java.awt.*;

public class BoxRangeData {
    private final BlockBox _box;
    private final BoxRange _range;

    @Nullable
    private Shape _clip;

    public BoxRangeData(BlockBox box, BoxRange range) {
        _box = box;
        _range = range;
    }

    public BlockBox getBox() {
        return _box;
    }

    public BoxRange getRange() {
        return _range;
    }

    @Nullable
    @CheckReturnValue
    public Shape getClip() {
        return _clip;
    }

    public void setClip(Shape clip) {
        _clip = clip;
    }

    @Override
    public String toString() {
        return "[range= %s, box=%s, clip=%s]".formatted(_range, _box, _clip);
    }
}
