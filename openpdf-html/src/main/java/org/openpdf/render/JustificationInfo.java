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
package org.openpdf.render;

import java.util.Objects;

public final class JustificationInfo {
    private final float nonSpaceAdjust;
    private final float spaceAdjust;

    public JustificationInfo(float nonSpaceAdjust, float spaceAdjust) {
        this.nonSpaceAdjust = nonSpaceAdjust;
        this.spaceAdjust = spaceAdjust;
    }

    public float nonSpaceAdjust() { return nonSpaceAdjust; }
    public float spaceAdjust() { return spaceAdjust; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JustificationInfo that)) return false;
        return Float.compare(nonSpaceAdjust, that.nonSpaceAdjust) == 0
                && Float.compare(spaceAdjust, that.spaceAdjust) == 0;
    }

    @Override
    public int hashCode() { return Objects.hash(nonSpaceAdjust, spaceAdjust); }

    @Override
    public String toString() {
        return "JustificationInfo[nonSpaceAdjust=" + nonSpaceAdjust + ", spaceAdjust=" + spaceAdjust + "]";
    }
}
