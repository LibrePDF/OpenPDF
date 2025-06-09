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
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.List;

public abstract class OneToFourPropertyBuilders {
    private abstract static class OneToFourPropertyBuilder extends AbstractPropertyBuilder {
        protected abstract CSSName[] getProperties();
        protected abstract PropertyBuilder getPropertyBuilder();

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            List<PropertyDeclaration> result = new ArrayList<>(4);
            assertFoundUpToValues(cssName, values, 4);

            PropertyBuilder builder = getPropertyBuilder();

            CSSName[] props = getProperties();

            PropertyDeclaration decl1;
            PropertyDeclaration decl2;
            PropertyDeclaration decl3;
            PropertyDeclaration decl4;
            switch (values.size()) {
                case 1:
                    decl1 = builder.buildDeclarations(
                            cssName, values, origin, important).get(0);

                    result.add(copyOf(decl1, props[0]));
                    result.add(copyOf(decl1, props[1]));
                    result.add(copyOf(decl1, props[2]));
                    result.add(copyOf(decl1, props[3]));
                    break;

                case 2:
                    decl1 = builder.buildDeclarations(
                                    cssName, values.subList(0, 1), origin, important, false).get(0);
                    decl2 = builder.buildDeclarations(
                                    cssName, values.subList(1, 2), origin, important, false).get(0);

                    result.add(copyOf(decl1, props[0]));
                    result.add(copyOf(decl2, props[1]));
                    result.add(copyOf(decl1, props[2]));
                    result.add(copyOf(decl2, props[3]));
                    break;

                case 3:
                    decl1 = builder.buildDeclarations(
                                    cssName, values.subList(0, 1), origin, important, false).get(0);
                    decl2 = builder.buildDeclarations(
                                    cssName, values.subList(1, 2), origin, important, false).get(0);
                    decl3 = builder.buildDeclarations(
                                    cssName, values.subList(2, 3), origin, important, false).get(0);

                    result.add(copyOf(decl1, props[0]));
                    result.add(copyOf(decl2, props[1]));
                    result.add(copyOf(decl3, props[2]));
                    result.add(copyOf(decl2, props[3]));
                    break;

                case 4:
                    decl1 = builder.buildDeclarations(
                                    cssName, values.subList(0, 1), origin, important, false).get(0);
                    decl2 = builder.buildDeclarations(
                                    cssName, values.subList(1, 2), origin, important, false).get(0);
                    decl3 = builder.buildDeclarations(
                                    cssName, values.subList(2, 3), origin, important, false).get(0);
                    decl4 = builder.buildDeclarations(
                                    cssName, values.subList(3, 4), origin, important, false).get(0);

                    result.add(copyOf(decl1, props[0]));
                    result.add(copyOf(decl2, props[1]));
                    result.add(copyOf(decl3, props[2]));
                    result.add(copyOf(decl4, props[3]));
                    break;
            }

            return result;
        }
    }

    public static class BorderColor extends OneToFourPropertyBuilder {
        @Override
        protected CSSName[] getProperties() {
            return new CSSName[] {
                    CSSName.BORDER_TOP_COLOR,
                    CSSName.BORDER_RIGHT_COLOR,
                    CSSName.BORDER_BOTTOM_COLOR,
                    CSSName.BORDER_LEFT_COLOR };
        }

        @Override
        protected PropertyBuilder getPropertyBuilder() {
            return PrimitivePropertyBuilders.COLOR;
        }
    }

    public static class BorderStyle extends OneToFourPropertyBuilder {
        @Override
        protected CSSName[] getProperties() {
            return new CSSName[] {
                    CSSName.BORDER_TOP_STYLE,
                    CSSName.BORDER_RIGHT_STYLE,
                    CSSName.BORDER_BOTTOM_STYLE,
                    CSSName.BORDER_LEFT_STYLE };
        }

        @Override
        protected PropertyBuilder getPropertyBuilder() {
            return PrimitivePropertyBuilders.BORDER_STYLE;
        }
    }

    public static class BorderWidth extends OneToFourPropertyBuilder {
        @Override
        protected CSSName[] getProperties() {
            return new CSSName[] {
                    CSSName.BORDER_TOP_WIDTH,
                    CSSName.BORDER_RIGHT_WIDTH,
                    CSSName.BORDER_BOTTOM_WIDTH,
                    CSSName.BORDER_LEFT_WIDTH };
        }

        @Override
        protected PropertyBuilder getPropertyBuilder() {
            return PrimitivePropertyBuilders.BORDER_WIDTH;
        }
    }


    public static class BorderRadius extends OneToFourPropertyBuilder {
        @Override
        protected CSSName[] getProperties() {
            return new CSSName[] {
                    CSSName.BORDER_TOP_LEFT_RADIUS,
                    CSSName.BORDER_TOP_RIGHT_RADIUS,
                    CSSName.BORDER_BOTTOM_RIGHT_RADIUS,
                    CSSName.BORDER_BOTTOM_LEFT_RADIUS };
        }

        @Override
        protected PropertyBuilder getPropertyBuilder() {
            return PrimitivePropertyBuilders.BORDER_RADIUS;
        }
    }

    public static class Margin extends OneToFourPropertyBuilder {
        @Override
        protected CSSName[] getProperties() {
            return new CSSName[] {
                    CSSName.MARGIN_TOP,
                    CSSName.MARGIN_RIGHT,
                    CSSName.MARGIN_BOTTOM,
                    CSSName.MARGIN_LEFT };
        }

        @Override
        protected PropertyBuilder getPropertyBuilder() {
            return PrimitivePropertyBuilders.MARGIN;
        }
    }

    public static class Padding extends OneToFourPropertyBuilder {
        @Override
        protected CSSName[] getProperties() {
            return new CSSName[] {
                    CSSName.PADDING_TOP,
                    CSSName.PADDING_RIGHT,
                    CSSName.PADDING_BOTTOM,
                    CSSName.PADDING_LEFT };
        }

        @Override
        protected PropertyBuilder getPropertyBuilder() {
            return PrimitivePropertyBuilders.PADDING;
        }
    }
}
