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

import java.text.BreakIterator;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public interface BreakPointsProvider {

    /**
     * return next breaking point if available.
     * If there are no more breaking points, return BreakPoint with position == {@link BreakIterator#DONE} (-1)
     */
    BreakPoint next();

}
