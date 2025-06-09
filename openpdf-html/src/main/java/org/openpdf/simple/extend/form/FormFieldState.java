/*
 * {{{ header & license
 * Copyright (c) 2007 Sean Bright
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
package org.openpdf.simple.extend.form;

import org.jspecify.annotations.Nullable;
import org.openpdf.util.ArrayUtil;

import java.util.List;

public class FormFieldState {
    private final String _value;
    private final boolean _checked;
    private final int @Nullable[] _selected;

    private FormFieldState(String value, boolean checked, int @Nullable [] selected) {
        _value = value;
        _checked = checked;
        _selected = selected;
    }

    public String getValue() {
        return _value;
    }

    public boolean isChecked() {
        return _checked;
    }

    public int[] getSelectedIndices() {
        return ArrayUtil.cloneOrEmpty(_selected);
    }

    public static FormFieldState fromString(String s) {
        return new FormFieldState(s, false, null);
    }

    public static FormFieldState fromBoolean(boolean b) {
        return new FormFieldState("", b, null);
    }

    public static FormFieldState fromList(List<Integer> list) {
        int[] indices = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            indices[i] = list.get(i);
        }

        return new FormFieldState("", false, indices);
    }
}
