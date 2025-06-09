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
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.CSSParseException;
import org.openpdf.css.parser.FSFunction;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.openpdf.css.parser.PropertyValue.Type.VALUE_TYPE_FUNCTION;

public class ContentPropertyBuilder extends AbstractPropertyBuilder {

    @Override
    public List<PropertyDeclaration> buildDeclarations(
            CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
        if (values.size() == 1) {
            PropertyValue value = (PropertyValue)values.get(0);
            if (value.getCssValueType() == CSSValue.CSS_INHERIT) {
                return emptyList();
            } else if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                IdentValue ident = checkIdent(value);
                if (ident == IdentValue.NONE || ident == IdentValue.NORMAL) {
                    return singletonList(
                            new PropertyDeclaration(CSSName.CONTENT, value, important, origin));
                }
            }
        }

        List<PropertyValue> resultValues = new ArrayList<>();
        for (CSSPrimitiveValue cssPrimitiveValue : values) {
            PropertyValue value = (PropertyValue) cssPrimitiveValue;

            if (value.getOperator() != null) {
                throw new CSSParseException(
                        "Found unexpected operator, " + value.getOperator().getExternalName(), -1);
            }

            short type = value.getPrimitiveType();
            if (type == CSSPrimitiveValue.CSS_URI) {
                continue;
            } else if (type == CSSPrimitiveValue.CSS_STRING) {
                resultValues.add(value);
            } else if (value.getPropertyValueType() == VALUE_TYPE_FUNCTION) {
                if (!isFunctionAllowed(value.getFunction())) {
                    throw new CSSParseException(
                            "Function " + value.getFunction().getName() + " is not allowed here", -1);
                }
                resultValues.add(value);
            } else if (type == CSSPrimitiveValue.CSS_IDENT) {
                IdentValue ident = checkIdent(value);
                if (ident == IdentValue.OPEN_QUOTE || ident == IdentValue.CLOSE_QUOTE ||
                        ident == IdentValue.NO_CLOSE_QUOTE || ident == IdentValue.NO_OPEN_QUOTE) {
                    resultValues.add(value);
                } else {
                    throw new CSSParseException(
                            "Identifier " + ident + " is not a valid value for the content property", -1);
                }
            } else {
                throw new CSSParseException(
                        value.getCssText() + " is not a value value for the content property", -1);
            }
        }

        if (!resultValues.isEmpty()) {
            return singletonList(
                    new PropertyDeclaration(CSSName.CONTENT, new PropertyValue(resultValues), important, origin));
        } else {
            return emptyList();
        }
    }

    private boolean isFunctionAllowed(FSFunction function) {
        return function.is("attr") || function.is("counter") || function.is("counters") ||
                function.is("element") || function.getName().startsWith("-fs") || function.is("target-counter") ||
                function.is("leader");
    }
}
