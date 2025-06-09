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

import org.jspecify.annotations.Nullable;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.CSSParseException;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static org.w3c.dom.css.CSSPrimitiveValue.CSS_NUMBER;
import static org.w3c.dom.css.CSSValue.CSS_INHERIT;

public abstract class AbstractPropertyBuilder implements PropertyBuilder {
    @Override
    public List<PropertyDeclaration> buildDeclarations(CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important) {
        return buildDeclarations(cssName, values, origin, important, true);
    }

    protected void assertFoundUpToValues(CSSName cssName, List<? extends CSSPrimitiveValue> values, int max) {
        int found = values.size();
        if (found < 1 || found > max) {
            throw new CSSParseException("Found %d values for %s when between %d and %d value(s) were expected"
                    .formatted(found, cssName, 1, max), -1);
        }
    }

    protected void checkIdentType(CSSName cssName, CSSPrimitiveValue value) {
        if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
            throw new CSSParseException("Value for " + cssName + " must be an identifier", -1);
        }
    }

    protected void checkIdentOrURIType(CSSName cssName, CSSPrimitiveValue value) {
        int type = value.getPrimitiveType();
        if (type != CSSPrimitiveValue.CSS_IDENT && type != CSSPrimitiveValue.CSS_URI) {
            throw new CSSParseException("Value for " + cssName + " must be an identifier or a URI", -1);
        }
    }

    protected void checkIdentOrColorType(CSSName cssName, CSSPrimitiveValue value) {
        int type = value.getPrimitiveType();
        if (type != CSSPrimitiveValue.CSS_IDENT && type != CSSPrimitiveValue.CSS_RGBCOLOR) {
            throw new CSSParseException("Value for " + cssName + " must be an identifier or a color", -1);
        }
    }

    protected void checkIdentOrIntegerType(CSSName cssName, CSSPrimitiveValue value) {
        int type = value.getPrimitiveType();
        if ((type != CSSPrimitiveValue.CSS_IDENT &&
                type != CSS_NUMBER) ||
            (type == CSS_NUMBER &&
                    (int)value.getFloatValue(CSS_NUMBER) !=
                        Math.round(value.getFloatValue(CSS_NUMBER)))) {
            throw new CSSParseException("Value for " + cssName + " must be an identifier or an integer", -1);
        }
    }

    protected void checkInteger(CSSName cssName, CSSPrimitiveValue value) {
        int type = value.getPrimitiveType();
        if (type != CSS_NUMBER ||
                (int) value.getFloatValue(CSS_NUMBER) != Math.round(value.getFloatValue(CSS_NUMBER))) {
            throw new CSSParseException("Value for " + cssName + " must be an integer", -1);
        }
    }

    protected void checkIdentOrLengthType(CSSName cssName, CSSPrimitiveValue value) {
        int type = value.getPrimitiveType();
        if (type != CSSPrimitiveValue.CSS_IDENT && ! isLength(value)) {
            throw new CSSParseException("Value for " + cssName + " must be an identifier or a length", -1);
        }
    }

    protected void checkIdentOrNumberType(CSSName cssName, CSSPrimitiveValue value) {
        int type = value.getPrimitiveType();
        if (type != CSSPrimitiveValue.CSS_IDENT && type != CSS_NUMBER) {
            throw new CSSParseException("Value for " + cssName + " must be an identifier or a length", -1);
        }
    }

    protected void checkIdentLengthOrPercentType(CSSName cssName, CSSPrimitiveValue value) {
        int type = value.getPrimitiveType();
        if (type != CSSPrimitiveValue.CSS_IDENT && ! isLength(value) && type != CSSPrimitiveValue.CSS_PERCENTAGE) {
            throw new CSSParseException("Value for " + cssName + " must be an identifier, length, or percentage", -1);
        }
    }

    protected void checkLengthOrPercentType(CSSName cssName, CSSPrimitiveValue value) {
        int type = value.getPrimitiveType();
        if (! isLength(value) && type != CSSPrimitiveValue.CSS_PERCENTAGE) {
            throw new CSSParseException("Value for " + cssName + " must be a length or percentage", -1);
        }
    }

    protected void checkLengthType(CSSName cssName, CSSPrimitiveValue value) {
        if (! isLength(value)) {
            throw new CSSParseException("Value for " + cssName + " must be a length", -1);
        }
    }

    protected void checkNumberType(CSSName cssName, CSSPrimitiveValue value) {
        if (value.getPrimitiveType() != CSS_NUMBER) {
            throw new CSSParseException("Value for " + cssName + " must be a number", -1);
        }
    }

    protected void checkIdentOrString(CSSName cssName, CSSPrimitiveValue value) {
        short type = value.getPrimitiveType();
        if (type != CSSPrimitiveValue.CSS_STRING && type != CSSPrimitiveValue.CSS_IDENT) {
            throw new CSSParseException("Value for " + cssName + " must be an identifier or string", -1);
        }
    }

    protected void checkIdentLengthNumberOrPercentType(CSSName cssName, CSSPrimitiveValue value) {
        int type = value.getPrimitiveType();
        if (type != CSSPrimitiveValue.CSS_IDENT &&
                ! isLength(value) &&
                type != CSSPrimitiveValue.CSS_PERCENTAGE &&
                type != CSS_NUMBER) {
            throw new CSSParseException("Value for " + cssName + " must be an identifier, length, or percentage", -1);
        }
    }

    protected static void checkValueBetween(CSSName cssName, float value, float min, float max) {
        if (value > max || value < min) {
            throw new CSSParseException("%s must be between %f and %f, but received: %f".formatted(cssName, min, max, value), -1);
        }
    }

    protected boolean isLength(CSSPrimitiveValue value) {
        int unit = value.getPrimitiveType();
        return unit == CSSPrimitiveValue.CSS_EMS || unit == CSSPrimitiveValue.CSS_EXS
                || unit == CSSPrimitiveValue.CSS_PX || unit == CSSPrimitiveValue.CSS_IN
                || unit == CSSPrimitiveValue.CSS_CM || unit == CSSPrimitiveValue.CSS_MM
                || unit == CSSPrimitiveValue.CSS_PT || unit == CSSPrimitiveValue.CSS_PC
                || (unit == CSS_NUMBER && value.getFloatValue(CSSPrimitiveValue.CSS_IN) == 0.0f);
    }

    protected void checkValidity(CSSName cssName, BitSet validValues, IdentValue value) {
        if (! validValues.get(value.FS_ID)) {
            throw new CSSParseException("Ident " + value + " is an invalid or unsupported value for " + cssName, -1);
        }
    }

    protected IdentValue checkIdent(CSSPrimitiveValue value) {
        IdentValue result = IdentValue.valueOf(value.getStringValue());
        if (result == null) {
            throw new CSSParseException("Value " + value.getStringValue() + " is not a recognized identifier", -1);
        }
        ((PropertyValue)value).setIdentValue(result);
        return result;
    }

    protected PropertyDeclaration copyOf(PropertyDeclaration decl, CSSName newName) {
        return new PropertyDeclaration(newName, decl.getValue(), decl.isImportant(), decl.getOrigin());
    }

    protected void checkInheritAllowed(CSSPrimitiveValue value, boolean inheritAllowed) {
        if (value.getCssValueType() == CSS_INHERIT && ! inheritAllowed) {
            throw new CSSParseException("Invalid use of inherit", -1);
        }
    }

    @Nullable
    protected List<PropertyDeclaration> checkInheritAll(CSSName[] all, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
        if (values.size() == 1) {
            CSSPrimitiveValue value = values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() == CSS_INHERIT) {
                List<PropertyDeclaration> result = new ArrayList<>(all.length);
                for (CSSName cssName : all) {
                    result.add(new PropertyDeclaration(cssName, value, important, origin));
                }
                return result;
            }
        }

        return null;
    }
}