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

import org.openpdf.css.style.CalculatedStyle;

import java.text.BreakIterator;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public class DefaultLineBreakingStrategy implements LineBreakingStrategy {

    @Override
    public BreakPointsProvider getBreakPointsProvider(String text, String lang, CalculatedStyle style) {
        BreakIterator iterator = new UrlAwareLineBreakIterator(text);
        return new DefaultBreakPointsProvider(iterator);
    }

    private record DefaultBreakPointsProvider(BreakIterator iterator) implements BreakPointsProvider {
        @Override
            public BreakPoint next() {
                int next = iterator.next();
                if (next < 0) return BreakPoint.getDonePoint();
                return new BreakPoint(next);
            }
        }
}
