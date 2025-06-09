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
package org.openpdf.css.style.derived;

import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.style.DerivedValue;

import java.util.List;

public class ListValue extends DerivedValue {
    private static final String[] NO_VALUES = new String[0];

    @Nullable
    private final List<Object> _values;

    public ListValue(CSSName name, PropertyValue value) {
        super(name, value.getPrimitiveType(), value.getCssText(), value.getCssText());
        _values = value.getValues();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> List<T> getValues() {
        return (List<T>) _values;
    }

    @Override
    public String[] asStringArray() {
        if (_values == null || _values.isEmpty()) {
            return NO_VALUES;
        }

        String[] arr = new String[_values.size()];
        int i = 0;

        for (Object value : _values) {
            arr[i++] = value.toString();
        }

        return arr;
    }
}
