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

import org.openpdf.css.newmatch.CascadedStyle;
import org.openpdf.css.style.CalculatedStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * A managed list of {@link CalculatedStyle} objects.  It is used when keeping
 * track of the styles which apply to a :first-line or :first-letter pseudo-element.
 */
public class StyleTracker {
    private final List<CascadedStyle> _styles = new ArrayList<>();

    public void addStyle(CascadedStyle style) {
        _styles.add(style);
    }

    public void removeLast() {
        if (!_styles.isEmpty()) {
            _styles.remove(_styles.size()-1);
        }
    }

    public boolean hasStyles() {
        return !_styles.isEmpty();
    }

    public void clearStyles() {
        _styles.clear();
    }

    public CalculatedStyle deriveAll(CalculatedStyle start) {
        CalculatedStyle result = start;
        for (CascadedStyle o : getStyles()) {
            result = result.deriveStyle(o);
        }
        return result;
    }

    public List<CascadedStyle> getStyles() {
        return _styles;
    }

    public StyleTracker copyOf() {
        StyleTracker result = new StyleTracker();
        result._styles.addAll(this._styles);
        return result;
    }
}
