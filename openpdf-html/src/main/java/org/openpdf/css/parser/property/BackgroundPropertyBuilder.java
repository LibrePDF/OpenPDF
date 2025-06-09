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
import org.openpdf.css.parser.FSRGBColor;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class BackgroundPropertyBuilder extends AbstractPropertyBuilder {
    // [<'background-color'> || <'background-image'> || <'background-repeat'> ||
    // <'background-attachment'> || <'background-position'>] | inherit
    private static final CSSName[] ALL = {
        CSSName.BACKGROUND_COLOR, CSSName.BACKGROUND_IMAGE, CSSName.BACKGROUND_REPEAT,
        CSSName.BACKGROUND_ATTACHMENT, CSSName.BACKGROUND_POSITION };

    private boolean isAppliesToBackgroundPosition(PropertyValue value) {
        short type = value.getPrimitiveType();

        if (isLength(value) || type == CSSPrimitiveValue.CSS_PERCENTAGE) {
            return true;
        } else if (type != CSSPrimitiveValue.CSS_IDENT) {
            return false;
        } else {
            IdentValue ident = IdentValue.valueOf(value.getStringValue());
            return ident != null &&
                PrimitivePropertyBuilders.BACKGROUND_POSITIONS.get(ident.FS_ID);
        }
    }

    @Override
    public List<PropertyDeclaration> buildDeclarations(
            CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
        final List<PropertyDeclaration> result = checkInheritAll(ALL, values, origin, important, inheritAllowed);
        if (result != null) {
            return result;
        }

        PropertyDeclaration backgroundColor = null;
        PropertyDeclaration backgroundImage = null;
        PropertyDeclaration backgroundRepeat = null;
        PropertyDeclaration backgroundAttachment =  null;
        PropertyDeclaration backgroundPosition = null;

        for (int i = 0; i < values.size(); i++) {
            PropertyValue value = (PropertyValue)values.get(i);
            checkInheritAllowed(value, false);

            boolean processingBackgroundPosition = false;
            short type = value.getPrimitiveType();
            if (type == CSSPrimitiveValue.CSS_IDENT) {
                FSRGBColor color = Conversions.getColor(value.getStringValue());
                if (color != null) {
                    if (backgroundColor != null) {
                        throw new CSSParseException("A background-color value cannot be set twice", -1);
                    }

                    backgroundColor = new PropertyDeclaration(
                            CSSName.BACKGROUND_COLOR,
                            new PropertyValue(color),
                            important, origin);
                    continue;
                }

                IdentValue ident = checkIdent(value);

                if (PrimitivePropertyBuilders.BACKGROUND_REPEATS.get(ident.FS_ID)) {
                    if (backgroundRepeat != null) {
                        throw new CSSParseException("A background-repeat value cannot be set twice", -1);
                    }

                    backgroundRepeat = new PropertyDeclaration(
                            CSSName.BACKGROUND_REPEAT, value, important, origin);
                }

                if (PrimitivePropertyBuilders.BACKGROUND_ATTACHMENTS.get(ident.FS_ID)) {
                    if (backgroundAttachment != null) {
                        throw new CSSParseException("A background-attachment value cannot be set twice", -1);
                    }

                    backgroundAttachment = new PropertyDeclaration(
                            CSSName.BACKGROUND_ATTACHMENT, value, important, origin);
                }

                if (ident == IdentValue.TRANSPARENT) {
                    if (backgroundColor != null) {
                        throw new CSSParseException("A background-color value cannot be set twice", -1);
                    }

                    backgroundColor = new PropertyDeclaration(
                            CSSName.BACKGROUND_COLOR, value, important, origin);
                }

                if (ident == IdentValue.NONE) {
                    if (backgroundImage != null) {
                        throw new CSSParseException("A background-image value cannot be set twice", -1);
                    }

                    backgroundImage = new PropertyDeclaration(
                            CSSName.BACKGROUND_IMAGE, value, important, origin);
                }

                if (PrimitivePropertyBuilders.BACKGROUND_POSITIONS.get(ident.FS_ID)) {
                    processingBackgroundPosition = true;
                }
            } else if (type == CSSPrimitiveValue.CSS_RGBCOLOR) {
                if (backgroundColor != null) {
                    throw new CSSParseException("A background-color value cannot be set twice", -1);
                }

                backgroundColor = new PropertyDeclaration(
                        CSSName.BACKGROUND_COLOR, value, important, origin);
            } else if (type == CSSPrimitiveValue.CSS_URI || value.toString().startsWith(IdentValue.LINEAR_GRADIENT.asString())) {
                if (backgroundImage != null) {
                    throw new CSSParseException("A background-image value cannot be set twice", -1);
                }

                backgroundImage = new PropertyDeclaration(
                        CSSName.BACKGROUND_IMAGE, value, important, origin);
            }

            if (processingBackgroundPosition || isLength(value) || type == CSSPrimitiveValue.CSS_PERCENTAGE) {
                if (backgroundPosition != null) {
                    throw new CSSParseException("A background-position value cannot be set twice", -1);
                }

                List<PropertyValue> v = new ArrayList<>(2);
                v.add(value);
                if (i < values.size() - 1) {
                    PropertyValue next = (PropertyValue)values.get(i+1);
                    if (isAppliesToBackgroundPosition(next)) {
                        v.add(next);
                        i++;
                    }
                }

                PropertyBuilder builder = CSSName.getPropertyBuilder(CSSName.BACKGROUND_POSITION);
                backgroundPosition = builder.buildDeclarations(
                        CSSName.BACKGROUND_POSITION, v, origin, important).get(0);
            }
        }

        if (backgroundColor == null) {
            backgroundColor = new PropertyDeclaration(
                    CSSName.BACKGROUND_COLOR, new PropertyValue(IdentValue.TRANSPARENT), important, origin);
        }

        if (backgroundImage == null) {
            backgroundImage = new PropertyDeclaration(
                    CSSName.BACKGROUND_IMAGE, new PropertyValue(IdentValue.NONE), important, origin);
        }

        if (backgroundRepeat == null) {
            backgroundRepeat = new PropertyDeclaration(
                    CSSName.BACKGROUND_REPEAT, new PropertyValue(IdentValue.REPEAT), important, origin);
        }

        if (backgroundAttachment == null) {
            backgroundAttachment = new PropertyDeclaration(
                    CSSName.BACKGROUND_ATTACHMENT, new PropertyValue(IdentValue.SCROLL), important, origin);

        }

        if (backgroundPosition == null) {
            List<PropertyValue> v = new ArrayList<>(2);
            v.add(new PropertyValue(CSSPrimitiveValue.CSS_PERCENTAGE, 0.0f, "0%"));
            v.add(new PropertyValue(CSSPrimitiveValue.CSS_PERCENTAGE, 0.0f, "0%"));
            backgroundPosition = new PropertyDeclaration(
                    CSSName.BACKGROUND_POSITION, new PropertyValue(v), important, origin);
        }

        return asList(backgroundColor, backgroundImage, backgroundRepeat, backgroundAttachment, backgroundPosition);
    }
}
