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
package org.openpdf.css.parser.property;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.parser.CSSParseException;
import org.openpdf.css.parser.CounterData;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CounterPropertyBuilder extends AbstractPropertyBuilder {
    // [ <identifier> <integer>? ]+ | none | inherit

    protected abstract int getDefaultValue();

    // XXX returns a PropertyValue of type VALUE_TYPE_LIST, but the List contains
    // CounterData objects and not PropertyValue objects
    @Override
    public List<PropertyDeclaration> buildDeclarations(CSSName cssName, List<? extends CSSPrimitiveValue> values,
                                                       Origin origin, boolean important, boolean inheritAllowed) {
        if (values.size() == 1) {
            PropertyValue value = (PropertyValue)values.get(0);

            checkInheritAllowed(value, inheritAllowed);

            if (value.getCssValueType() == CSSValue.CSS_INHERIT) {
                return Collections.singletonList(new PropertyDeclaration(cssName, value, important, origin));
            } else if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                if (value.getCssText().equals("none")) {
                    return Collections.singletonList(new PropertyDeclaration(cssName, value, important, origin));
                } else {
                    CounterData data = new CounterData(
                            value.getStringValue(),
                            getDefaultValue());

                    return Collections.singletonList(
                            new PropertyDeclaration(cssName, new PropertyValue(
                                    Collections.singletonList(data)), important, origin));
                }
            }

            throw new CSSParseException("The syntax of the " + cssName + " property is invalid", -1);
        } else {
            List<CounterData> result = new ArrayList<>();
            for (int i = 0; i < values.size(); i++) {
                PropertyValue value = (PropertyValue)values.get(i);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    String name = value.getStringValue();
                    int cValue = getDefaultValue();

                    if (i < values.size() - 1) {
                        PropertyValue next = (PropertyValue)values.get(i+1);
                        if (next.getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER) {
                            checkNumberIsInteger(cssName, next);

                            cValue = (int)next.getFloatValue();
                        }

                        i++;
                    }
                    result.add(new CounterData(name, cValue));
                } else {
                    throw new CSSParseException("The syntax of the " + cssName + " property is invalid", -1);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, new PropertyValue(result), important, origin));
        }
    }

    private void checkNumberIsInteger(CSSName cssName, CSSPrimitiveValue value) {
        if ((int)value.getFloatValue(CSSPrimitiveValue.CSS_NUMBER) !=
                    Math.round(value.getFloatValue(CSSPrimitiveValue.CSS_NUMBER))) {
            throw new CSSParseException("The value " + value.getFloatValue(CSSPrimitiveValue.CSS_NUMBER) + " in " +
                    cssName + " must be an integer", -1);
        }
    }

    public static class CounterReset extends CounterPropertyBuilder {
        @Override
        protected int getDefaultValue() {
            return 0;
        }
    }

    public static class CounterIncrement extends CounterPropertyBuilder {
        @Override
        protected int getDefaultValue() {
            return 1;
        }
    }
}
