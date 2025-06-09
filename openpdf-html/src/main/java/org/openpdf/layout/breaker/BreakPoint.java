/*
 * Copyright (C) 2017 Lukas Zaruba, lukas.zaruba@gmail.com
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
 *
 */
package org.openpdf.layout.breaker;

import org.jspecify.annotations.Nullable;

import java.text.BreakIterator;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public class BreakPoint implements Comparable<BreakPoint> {

    private int position;
    @Nullable
    private String hyphen;

    public BreakPoint(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "BreakPoint [position=" + position + "]";
    }

    @Override
    public int compareTo(BreakPoint o) {
        return Integer.compare(position, o.position);
    }

    public void setHyphen(String hyphen) {
        this.hyphen = hyphen;
    }

    public String getHyphen() {
        if (hyphen == null) return "";
        return hyphen;
    }

    public static BreakPoint getDonePoint() {
        return new BreakPoint(BreakIterator.DONE);
    }

}
