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
import org.openpdf.css.parser.FSRGBColor;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.List;

import static org.w3c.dom.css.CSSValue.CSS_INHERIT;

public class BorderPropertyBuilders {
    private abstract static class BorderSidePropertyBuilder extends AbstractPropertyBuilder {
        protected abstract CSSName[][] getProperties();

        private void addAll(List<PropertyDeclaration> result, CSSName[] properties, CSSPrimitiveValue value,
                            Origin origin, boolean important) {
            for (CSSName property : properties) {
                result.add(new PropertyDeclaration(
                        property, value, important, origin));
            }
        }

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            CSSName[][] props = getProperties();

            List<PropertyDeclaration> result = new ArrayList<>(3);

            if (values.size() == 1 && values.get(0).getCssValueType() == CSS_INHERIT) {
                CSSPrimitiveValue value = values.get(0);
                addAll(result, props[0], value, origin, important);
                addAll(result, props[1], value, origin, important);
                addAll(result, props[2], value, origin, important);

            } else {
                assertFoundUpToValues(cssName, values, 3);
                boolean haveBorderStyle = false;
                boolean haveBorderColor = false;
                boolean haveBorderWidth = false;

                for (CSSPrimitiveValue value : values) {
                    checkInheritAllowed(value, false);
                    boolean matched = false;
                    CSSPrimitiveValue borderWidth = convertToBorderWidth(value);
                    if (borderWidth != null) {
                        if (haveBorderWidth) {
                            throw new CSSParseException("A border width cannot be set twice", -1);
                        }
                        haveBorderWidth = true;
                        matched = true;
                        addAll(result, props[0], borderWidth, origin, important);
                    }

                    if (isBorderStyle(value)) {
                        if (haveBorderStyle) {
                            throw new CSSParseException("A border style cannot be set twice", -1);
                        }
                        haveBorderStyle = true;
                        matched = true;
                        addAll(result, props[1], value, origin, important);
                    }

                    CSSPrimitiveValue borderColor = convertToBorderColor(value);
                    if (borderColor != null) {
                        if (haveBorderColor) {
                            throw new CSSParseException("A border color cannot be set twice", -1);
                        }
                        haveBorderColor = true;
                        matched = true;
                        addAll(result, props[2], borderColor, origin, important);
                    }

                    if (!matched) {
                        throw new CSSParseException(value.getCssText() + " is not a border width, style, or color", -1);
                    }
                }

                if (! haveBorderWidth) {
                    addAll(result, props[0], new PropertyValue(IdentValue.FS_INITIAL_VALUE), origin, important);
                }

                if (! haveBorderStyle) {
                    addAll(result, props[1], new PropertyValue(IdentValue.FS_INITIAL_VALUE), origin, important);
                }

                if (! haveBorderColor) {
                    addAll(result, props[2], new PropertyValue(IdentValue.FS_INITIAL_VALUE), origin, important);
                }

            }
            return result;
        }

        private boolean isBorderStyle(CSSPrimitiveValue value) {
            if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
                return false;
            }

            IdentValue ident = IdentValue.valueOf(value.getCssText());
            if (ident == null) {
                return false;
            }

            return PrimitivePropertyBuilders.BORDER_STYLES.get(ident.FS_ID);
        }

        @Nullable
        private CSSPrimitiveValue convertToBorderWidth(CSSPrimitiveValue value) {
            int type = value.getPrimitiveType();
            if (type != CSSPrimitiveValue.CSS_IDENT && ! isLength(value)) {
                return null;
            }

            if (isLength(value)) {
                return value;
            } else {
                IdentValue ident = IdentValue.valueOf(value.getStringValue());
                if (ident == null) {
                    return null;
                }

                if (PrimitivePropertyBuilders.BORDER_WIDTHS.get(ident.FS_ID)) {
                    return Conversions.getBorderWidth(ident.toString());
                } else {
                    return null;
                }
            }
        }

        @Nullable
        private CSSPrimitiveValue convertToBorderColor(CSSPrimitiveValue value) {
            int type = value.getPrimitiveType();
            if (type != CSSPrimitiveValue.CSS_IDENT && type != CSSPrimitiveValue.CSS_RGBCOLOR) {
                return null;
            }

            if (type != CSSPrimitiveValue.CSS_RGBCOLOR) {
                FSRGBColor color = Conversions.getColor(value.getStringValue());
                if (color != null) {
                    return new PropertyValue(color);
                }

                IdentValue ident = IdentValue.valueOf(value.getCssText());
                if (ident == null || ident != IdentValue.TRANSPARENT) {
                    return null;
                }
            }
            return value;
        }
    }

    public static class BorderTop extends BorderSidePropertyBuilder {
        @Override
        protected CSSName[][] getProperties() {
            return new CSSName[][] {
                    new CSSName[] { CSSName.BORDER_TOP_WIDTH },
                    new CSSName[] { CSSName.BORDER_TOP_STYLE },
                    new CSSName[] { CSSName.BORDER_TOP_COLOR } };
        }
    }

    public static class BorderRight extends BorderSidePropertyBuilder {
        @Override
        protected CSSName[][] getProperties() {
            return new CSSName[][] {
                    new CSSName[] { CSSName.BORDER_RIGHT_WIDTH },
                    new CSSName[] { CSSName.BORDER_RIGHT_STYLE },
                    new CSSName[] { CSSName.BORDER_RIGHT_COLOR } };
        }
    }

    public static class BorderBottom extends BorderSidePropertyBuilder {
        @Override
        protected CSSName[][] getProperties() {
            return new CSSName[][] {
                    new CSSName[] { CSSName.BORDER_BOTTOM_WIDTH },
                    new CSSName[] { CSSName.BORDER_BOTTOM_STYLE },
                    new CSSName[] { CSSName.BORDER_BOTTOM_COLOR } };
        }
    }

    public static class BorderLeft extends BorderSidePropertyBuilder {
        @Override
        protected CSSName[][] getProperties() {
            return new CSSName[][] {
                    new CSSName[] { CSSName.BORDER_LEFT_WIDTH },
                    new CSSName[] { CSSName.BORDER_LEFT_STYLE },
                    new CSSName[] { CSSName.BORDER_LEFT_COLOR } };
        }
    }

    public static class Border extends BorderSidePropertyBuilder {
        @Override
        protected CSSName[][] getProperties() {
            return new CSSName[][] {
                    new CSSName[] {
                            CSSName.BORDER_TOP_WIDTH, CSSName.BORDER_RIGHT_WIDTH,
                            CSSName.BORDER_BOTTOM_WIDTH, CSSName.BORDER_LEFT_WIDTH },
                    new CSSName[] {
                            CSSName.BORDER_TOP_STYLE, CSSName.BORDER_RIGHT_STYLE,
                            CSSName.BORDER_BOTTOM_STYLE, CSSName.BORDER_LEFT_STYLE },
                    new CSSName[] {
                            CSSName.BORDER_TOP_COLOR, CSSName.BORDER_RIGHT_COLOR,
                            CSSName.BORDER_BOTTOM_COLOR, CSSName.BORDER_LEFT_COLOR } };
        }
    }
}
