/*
 * {{{ header & license
 * Copyright (c) 2011 Wisconsin Court System
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

import com.google.errorprone.annotations.CheckReturnValue;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.CSSParseException;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.openpdf.css.constants.CSSName.QUOTES;
import static org.openpdf.css.parser.PropertyValue.Type.VALUE_TYPE_FUNCTION;

public class QuotesPropertyBuilder extends AbstractPropertyBuilder {

    @Override
    public List<PropertyDeclaration> buildDeclarations(CSSName cssName, List<? extends CSSPrimitiveValue> values,
                                                       Origin origin, boolean important, boolean inheritAllowed) {
        if (values.size() == 1) {
            PropertyValue value = (PropertyValue) values.get(0);
            if (value.getCssValueType() == CSSValue.CSS_INHERIT) {
                return emptyList();
            } else if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                IdentValue ident = checkIdent(value);
                if (ident == IdentValue.NONE) {
                    return singletonList(
                            new PropertyDeclaration(QUOTES, value, important, origin));
                }
            }
        }

        if (values.size() % 2 == 1) {
            throw new CSSParseException(
                    "Mismatched quotes " + values, -1);
        }

        List<String> resultValues = getStringValues(values);

        if (!resultValues.isEmpty()) {
            return singletonList(
                    new PropertyDeclaration(QUOTES, new PropertyValue(resultValues), important, origin));
        } else {
            return emptyList();
        }
    }

    @CheckReturnValue
    private List<String> getStringValues(List<? extends CSSPrimitiveValue> values) {
        return values.stream()
                .map(cssPrimitiveValue -> (PropertyValue) cssPrimitiveValue)
                .peek(this::assertNoOperator)
                .peek(this::assertValueIsString)
                .map(value -> value.getStringValue())
                .collect(toList());
    }

    private void assertNoOperator(PropertyValue cssPrimitiveValue) {
        if (cssPrimitiveValue.getOperator() != null) {
            throw new CSSParseException(
                    "Found unexpected operator, " + cssPrimitiveValue.getOperator().getExternalName(), -1);
        }
    }

    private void assertValueIsString(PropertyValue value) {
        short type = value.getPrimitiveType();
        if (type == CSSPrimitiveValue.CSS_URI) {
            throw new CSSParseException("URI is not allowed here", -1);
        } else if (value.getPropertyValueType() == VALUE_TYPE_FUNCTION) {
            throw new CSSParseException("Function " + value.getFunction().getName() + " is not allowed here", -1);
        } else if (type == CSSPrimitiveValue.CSS_IDENT) {
            throw new CSSParseException("Identifier is not a valid value for the quotes property", -1);
        } else if (type != CSSPrimitiveValue.CSS_STRING) {
            throw new CSSParseException(value.getCssText() + " is not a value value for the quotes property", -1);
        }
    }
}
