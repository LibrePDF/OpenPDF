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
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.CSSParseException;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.List;

public class ListStylePropertyBuilder extends AbstractPropertyBuilder {
    private static final CSSName[] ALL = {
        CSSName.LIST_STYLE_TYPE, CSSName.LIST_STYLE_POSITION, CSSName.LIST_STYLE_IMAGE };

    @Override
    public List<PropertyDeclaration> buildDeclarations(CSSName cssName, List<? extends CSSPrimitiveValue> values,
                                                       Origin origin, boolean important, boolean inheritAllowed) {
        final List<PropertyDeclaration> inherited = checkInheritAll(ALL, values, origin, important, inheritAllowed);
        if (inherited != null) {
            return inherited;
        }

        PropertyDeclaration listStyleType = null;
        PropertyDeclaration listStylePosition = null;
        PropertyDeclaration listStyleImage = null;

        for (CSSPrimitiveValue cssPrimitiveValue : values) {
            PropertyValue value = (PropertyValue) cssPrimitiveValue;
            checkInheritAllowed(value, false);
            short type = value.getPrimitiveType();
            if (type == CSSPrimitiveValue.CSS_IDENT) {
                IdentValue ident = checkIdent(value);

                if (ident == IdentValue.NONE) {
                    if (listStyleType == null) {
                        listStyleType = new PropertyDeclaration(
                                CSSName.LIST_STYLE_TYPE, value, important, origin);
                    }

                    if (listStyleImage == null) {
                        listStyleImage = new PropertyDeclaration(
                                CSSName.LIST_STYLE_IMAGE, value, important, origin);
                    }
                } else if (PrimitivePropertyBuilders.LIST_STYLE_POSITIONS.get(ident.FS_ID)) {
                    if (listStylePosition != null) {
                        throw new CSSParseException("A list-style-position value cannot be set twice", -1);
                    }

                    listStylePosition = new PropertyDeclaration(
                            CSSName.LIST_STYLE_POSITION, value, important, origin);
                } else if (PrimitivePropertyBuilders.LIST_STYLE_TYPES.get(ident.FS_ID)) {
                    if (listStyleType != null) {
                        throw new CSSParseException("A list-style-type value cannot be set twice", -1);
                    }

                    listStyleType = new PropertyDeclaration(
                            CSSName.LIST_STYLE_TYPE, value, important, origin);
                }
            } else if (type == CSSPrimitiveValue.CSS_URI) {
                if (listStyleImage != null) {
                    throw new CSSParseException("A list-style-image value cannot be set twice", -1);
                }

                listStyleImage = new PropertyDeclaration(
                        CSSName.LIST_STYLE_IMAGE, value, important, origin);
            }
        }

        List<PropertyDeclaration> result = new ArrayList<>(3);
        if (listStyleType != null) {
            result.add(listStyleType);
        }
        if (listStylePosition != null) {
            result.add(listStylePosition);
        }
        if (listStyleImage != null) {
            result.add(listStyleImage);
        }

        return result;
    }
}
