/*
 * {{{ header & license
 * Copyright (c) 2005 Patrick Wright
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

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.FSDerivedValue;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNullElseGet;
import static org.w3c.dom.css.CSSValue.CSS_INHERIT;

public class DerivedValueFactory {
    private static final Map<String, FSDerivedValue> CACHED_COLORS = new HashMap<>();

    @CheckReturnValue
    public static FSDerivedValue newDerivedValue(
            @Nullable CalculatedStyle style, CSSName cssName, PropertyValue value) {
        if (value.getCssValueType() == CSS_INHERIT) {
            return style.getParent().valueByName(cssName);
        }
        return switch (value.getPropertyValueType()) {
            case VALUE_TYPE_LENGTH -> new LengthValue(style, cssName, value);
            case VALUE_TYPE_IDENT -> getIdentValue(value);
            case VALUE_TYPE_STRING -> new StringValue(cssName, value);
            case VALUE_TYPE_NUMBER -> new NumberValue(cssName, value);
            case VALUE_TYPE_COLOR -> getColor(cssName, value, value.getCssText());
            case VALUE_TYPE_LIST -> new ListValue(cssName, value);
            case VALUE_TYPE_FUNCTION -> new FunctionValue(cssName, value);
        };
    }

    @CheckReturnValue
    private static IdentValue getIdentValue(PropertyValue value) {
        return requireNonNullElseGet(
                value.getIdentValue(),
                () -> IdentValue.getByIdentString(value.getStringValue())
        );
    }

    @CheckReturnValue
    private static FSDerivedValue getColor(CSSName cssName, PropertyValue value, String cssText) {
        FSDerivedValue color = CACHED_COLORS.get(cssText);
        if (color == null) {
            color = new ColorValue(cssName, value);
            CACHED_COLORS.put(cssText, color);
        }
        return color;
    }
}
