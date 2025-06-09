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
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.parser.CSSParseException;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.List;

import static java.util.Arrays.asList;

public class BorderSpacingPropertyBuilder extends AbstractPropertyBuilder {
    private static final CSSName[] ALL = {
        CSSName.FS_BORDER_SPACING_HORIZONTAL, CSSName.FS_BORDER_SPACING_VERTICAL };

    @Override
    public List<PropertyDeclaration> buildDeclarations(CSSName cssName, List<? extends CSSPrimitiveValue> values,
                                                       Origin origin, boolean important, boolean inheritAllowed) {
        List<PropertyDeclaration> result = checkInheritAll(ALL, values, origin, important, inheritAllowed);
        if (result != null) {
            return result;
        }

        assertFoundUpToValues(CSSName.BORDER_SPACING, values, 2);

        PropertyDeclaration horizontalSpacing;
        PropertyDeclaration verticalSpacing;

        if (values.size() == 1) {
            PropertyValue value = (PropertyValue)values.get(0);
            checkLengthType(cssName, value);
            if (value.getFloatValue() < 0.0f) {
                throw new CSSParseException("border-spacing may not be negative", -1);
            }
            horizontalSpacing = new PropertyDeclaration(
                    CSSName.FS_BORDER_SPACING_HORIZONTAL, value, important, origin);
            verticalSpacing = new PropertyDeclaration(
                    CSSName.FS_BORDER_SPACING_VERTICAL, value, important, origin);
        } else { /* values.size() == 2 */
            PropertyValue horizontal = (PropertyValue)values.get(0);
            checkLengthType(cssName, horizontal);
            if (horizontal.getFloatValue() < 0.0f) {
                throw new CSSParseException("border-spacing may not be negative", -1);
            }
            horizontalSpacing = new PropertyDeclaration(
                    CSSName.FS_BORDER_SPACING_HORIZONTAL, horizontal, important, origin);

            PropertyValue vertical = (PropertyValue)values.get(1);
            checkLengthType(cssName, vertical);
            if (vertical.getFloatValue() < 0.0f) {
                throw new CSSParseException("border-spacing may not be negative", -1);
            }
            verticalSpacing = new PropertyDeclaration(
                    CSSName.FS_BORDER_SPACING_VERTICAL, vertical, important, origin);
        }

        return asList(horizontalSpacing, verticalSpacing);
    }
}
