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
import org.openpdf.css.parser.FSFunction;
import org.openpdf.css.parser.FSRGBColor;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.parser.Token;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.w3c.dom.css.CSSValue.CSS_INHERIT;
import static org.openpdf.css.constants.IdentValue.ABSOLUTE;
import static org.openpdf.css.constants.IdentValue.ALWAYS;
import static org.openpdf.css.constants.IdentValue.ARMENIAN;
import static org.openpdf.css.constants.IdentValue.AUTO;
import static org.openpdf.css.constants.IdentValue.AVOID;
import static org.openpdf.css.constants.IdentValue.BASELINE;
import static org.openpdf.css.constants.IdentValue.BLOCK;
import static org.openpdf.css.constants.IdentValue.BOLD;
import static org.openpdf.css.constants.IdentValue.BOLDER;
import static org.openpdf.css.constants.IdentValue.BORDER_BOX;
import static org.openpdf.css.constants.IdentValue.BOTH;
import static org.openpdf.css.constants.IdentValue.BOTTOM;
import static org.openpdf.css.constants.IdentValue.BREAK_ALL;
import static org.openpdf.css.constants.IdentValue.BREAK_WORD;
import static org.openpdf.css.constants.IdentValue.CAPITALIZE;
import static org.openpdf.css.constants.IdentValue.CENTER;
import static org.openpdf.css.constants.IdentValue.CIRCLE;
import static org.openpdf.css.constants.IdentValue.COLLAPSE;
import static org.openpdf.css.constants.IdentValue.CONTAIN;
import static org.openpdf.css.constants.IdentValue.CONTENT_BOX;
import static org.openpdf.css.constants.IdentValue.COVER;
import static org.openpdf.css.constants.IdentValue.CREATE;
import static org.openpdf.css.constants.IdentValue.CROSSHAIR;
import static org.openpdf.css.constants.IdentValue.DASHED;
import static org.openpdf.css.constants.IdentValue.DECIMAL;
import static org.openpdf.css.constants.IdentValue.DECIMAL_LEADING_ZERO;
import static org.openpdf.css.constants.IdentValue.DEFAULT;
import static org.openpdf.css.constants.IdentValue.DISC;
import static org.openpdf.css.constants.IdentValue.DOTTED;
import static org.openpdf.css.constants.IdentValue.DOUBLE;
import static org.openpdf.css.constants.IdentValue.DYNAMIC;
import static org.openpdf.css.constants.IdentValue.EMBED;
import static org.openpdf.css.constants.IdentValue.E_RESIZE;
import static org.openpdf.css.constants.IdentValue.FIXED;
import static org.openpdf.css.constants.IdentValue.GEORGIAN;
import static org.openpdf.css.constants.IdentValue.GROOVE;
import static org.openpdf.css.constants.IdentValue.HELP;
import static org.openpdf.css.constants.IdentValue.HIDDEN;
import static org.openpdf.css.constants.IdentValue.HIDE;
import static org.openpdf.css.constants.IdentValue.INLINE;
import static org.openpdf.css.constants.IdentValue.INLINE_BLOCK;
import static org.openpdf.css.constants.IdentValue.INLINE_TABLE;
import static org.openpdf.css.constants.IdentValue.INSET;
import static org.openpdf.css.constants.IdentValue.INSIDE;
import static org.openpdf.css.constants.IdentValue.ITALIC;
import static org.openpdf.css.constants.IdentValue.JUSTIFY;
import static org.openpdf.css.constants.IdentValue.KEEP;
import static org.openpdf.css.constants.IdentValue.LANDSCAPE;
import static org.openpdf.css.constants.IdentValue.LARGE;
import static org.openpdf.css.constants.IdentValue.LARGER;
import static org.openpdf.css.constants.IdentValue.LEFT;
import static org.openpdf.css.constants.IdentValue.LIGHTER;
import static org.openpdf.css.constants.IdentValue.LINE;
import static org.openpdf.css.constants.IdentValue.LINEAR_GRADIENT;
import static org.openpdf.css.constants.IdentValue.LINE_THROUGH;
import static org.openpdf.css.constants.IdentValue.LIST_ITEM;
import static org.openpdf.css.constants.IdentValue.LOWERCASE;
import static org.openpdf.css.constants.IdentValue.LOWER_ALPHA;
import static org.openpdf.css.constants.IdentValue.LOWER_GREEK;
import static org.openpdf.css.constants.IdentValue.LOWER_LATIN;
import static org.openpdf.css.constants.IdentValue.LOWER_ROMAN;
import static org.openpdf.css.constants.IdentValue.MANUAL;
import static org.openpdf.css.constants.IdentValue.MEDIUM;
import static org.openpdf.css.constants.IdentValue.MIDDLE;
import static org.openpdf.css.constants.IdentValue.MOVE;
import static org.openpdf.css.constants.IdentValue.NE_RESIZE;
import static org.openpdf.css.constants.IdentValue.NONE;
import static org.openpdf.css.constants.IdentValue.NORMAL;
import static org.openpdf.css.constants.IdentValue.NOWRAP;
import static org.openpdf.css.constants.IdentValue.NO_REPEAT;
import static org.openpdf.css.constants.IdentValue.NW_RESIZE;
import static org.openpdf.css.constants.IdentValue.N_RESIZE;
import static org.openpdf.css.constants.IdentValue.OBLIQUE;
import static org.openpdf.css.constants.IdentValue.OUTSET;
import static org.openpdf.css.constants.IdentValue.OUTSIDE;
import static org.openpdf.css.constants.IdentValue.OVERLINE;
import static org.openpdf.css.constants.IdentValue.PAGINATE;
import static org.openpdf.css.constants.IdentValue.POINTER;
import static org.openpdf.css.constants.IdentValue.PORTRAIT;
import static org.openpdf.css.constants.IdentValue.PRE;
import static org.openpdf.css.constants.IdentValue.PRE_LINE;
import static org.openpdf.css.constants.IdentValue.PRE_WRAP;
import static org.openpdf.css.constants.IdentValue.PROGRESS;
import static org.openpdf.css.constants.IdentValue.RELATIVE;
import static org.openpdf.css.constants.IdentValue.REPEAT;
import static org.openpdf.css.constants.IdentValue.REPEAT_X;
import static org.openpdf.css.constants.IdentValue.REPEAT_Y;
import static org.openpdf.css.constants.IdentValue.RIDGE;
import static org.openpdf.css.constants.IdentValue.RIGHT;
import static org.openpdf.css.constants.IdentValue.SCROLL;
import static org.openpdf.css.constants.IdentValue.SEPARATE;
import static org.openpdf.css.constants.IdentValue.SE_RESIZE;
import static org.openpdf.css.constants.IdentValue.SHOW;
import static org.openpdf.css.constants.IdentValue.SMALL;
import static org.openpdf.css.constants.IdentValue.SMALLER;
import static org.openpdf.css.constants.IdentValue.SMALL_CAPS;
import static org.openpdf.css.constants.IdentValue.SOLID;
import static org.openpdf.css.constants.IdentValue.SQUARE;
import static org.openpdf.css.constants.IdentValue.START;
import static org.openpdf.css.constants.IdentValue.STATIC;
import static org.openpdf.css.constants.IdentValue.SUB;
import static org.openpdf.css.constants.IdentValue.SUPER;
import static org.openpdf.css.constants.IdentValue.SW_RESIZE;
import static org.openpdf.css.constants.IdentValue.S_RESIZE;
import static org.openpdf.css.constants.IdentValue.TABLE;
import static org.openpdf.css.constants.IdentValue.TABLE_CAPTION;
import static org.openpdf.css.constants.IdentValue.TABLE_CELL;
import static org.openpdf.css.constants.IdentValue.TABLE_COLUMN;
import static org.openpdf.css.constants.IdentValue.TABLE_COLUMN_GROUP;
import static org.openpdf.css.constants.IdentValue.TABLE_FOOTER_GROUP;
import static org.openpdf.css.constants.IdentValue.TABLE_HEADER_GROUP;
import static org.openpdf.css.constants.IdentValue.TABLE_ROW;
import static org.openpdf.css.constants.IdentValue.TABLE_ROW_GROUP;
import static org.openpdf.css.constants.IdentValue.TEXT;
import static org.openpdf.css.constants.IdentValue.TEXT_BOTTOM;
import static org.openpdf.css.constants.IdentValue.TEXT_TOP;
import static org.openpdf.css.constants.IdentValue.THICK;
import static org.openpdf.css.constants.IdentValue.THIN;
import static org.openpdf.css.constants.IdentValue.TOP;
import static org.openpdf.css.constants.IdentValue.TRANSPARENT;
import static org.openpdf.css.constants.IdentValue.UNDERLINE;
import static org.openpdf.css.constants.IdentValue.UPPERCASE;
import static org.openpdf.css.constants.IdentValue.UPPER_ALPHA;
import static org.openpdf.css.constants.IdentValue.UPPER_LATIN;
import static org.openpdf.css.constants.IdentValue.UPPER_ROMAN;
import static org.openpdf.css.constants.IdentValue.VISIBLE;
import static org.openpdf.css.constants.IdentValue.WAIT;
import static org.openpdf.css.constants.IdentValue.W_RESIZE;
import static org.openpdf.css.constants.IdentValue.XX_LARGE;
import static org.openpdf.css.constants.IdentValue.XX_SMALL;
import static org.openpdf.css.constants.IdentValue.X_LARGE;
import static org.openpdf.css.constants.IdentValue.X_SMALL;
import static org.openpdf.css.parser.PropertyValue.Type.VALUE_TYPE_FUNCTION;

public class PrimitivePropertyBuilders {
    // none | hidden | dotted | dashed | solid | double | groove | ridge | inset | outset
    public static final BitSet BORDER_STYLES = setFor(
        NONE, HIDDEN, DOTTED, DASHED, SOLID, DOUBLE, GROOVE, RIDGE, INSET, OUTSET);

    // thin | medium | thick
    public static final BitSet BORDER_WIDTHS = setFor(THIN, MEDIUM, THICK);

    // normal | small-caps | inherit
    public static final BitSet FONT_VARIANTS = setFor(NORMAL, SMALL_CAPS);

    // normal | italic | oblique | inherit
    public static final BitSet FONT_STYLES = setFor(NORMAL, ITALIC, OBLIQUE);

    public static final BitSet FONT_WEIGHTS = setFor(NORMAL, BOLD, BOLDER, LIGHTER);

    public static final BitSet PAGE_ORIENTATIONS = setFor(AUTO, PORTRAIT, LANDSCAPE);

    // inside | outside | inherit
    public static final BitSet LIST_STYLE_POSITIONS = setFor(INSIDE, OUTSIDE);

    // disc | circle | square | decimal
    // | decimal-leading-zero | lower-roman | upper-roman
    // | lower-greek | lower-latin | upper-latin | armenian
    // | georgian | lower-alpha | upper-alpha | none | inherit
    public static final BitSet LIST_STYLE_TYPES = setFor(DISC, CIRCLE, SQUARE,
        DECIMAL, DECIMAL_LEADING_ZERO, LOWER_ROMAN, UPPER_ROMAN, LOWER_GREEK,
        LOWER_LATIN, UPPER_LATIN, ARMENIAN, GEORGIAN, LOWER_ALPHA, UPPER_ALPHA, NONE);

    // repeat | repeat-x | repeat-y | no-repeat | inherit
    public static final BitSet BACKGROUND_REPEATS = setFor(REPEAT, REPEAT_X, REPEAT_Y, NO_REPEAT);

    // scroll | fixed | inherit
    public static final BitSet BACKGROUND_ATTACHMENTS = setFor(SCROLL, FIXED);

    // left | right | top | bottom | center
    public static final BitSet BACKGROUND_POSITIONS = setFor(LEFT, RIGHT, TOP, BOTTOM, CENTER);

    public static final BitSet ABSOLUTE_FONT_SIZES = setFor(XX_SMALL, X_SMALL, SMALL, MEDIUM, LARGE, X_LARGE, XX_LARGE);

    public static final BitSet RELATIVE_FONT_SIZES = setFor(SMALLER, LARGER);

    public static final PropertyBuilder COLOR = new GenericColor();
    public static final PropertyBuilder BORDER_STYLE = new GenericBorderStyle();
    public static final PropertyBuilder BORDER_WIDTH = new GenericBorderWidth();
    public static final PropertyBuilder BORDER_RADIUS = new NonNegativeLengthLike();
    public static final PropertyBuilder MARGIN = new LengthLikeWithAuto();
    public static final PropertyBuilder PADDING = new NonNegativeLengthLike();

    private static BitSet setFor(IdentValue... values) {
        BitSet result = new BitSet(IdentValue.getIdentCount());
        for (IdentValue ident : values) {
            result.set(ident.FS_ID);
        }
        return result;
    }

    private static void assertFoundSingleValue(CSSName cssName, List<? extends CSSPrimitiveValue> values) {
        if (values.size() != 1) {
            throw new CSSParseException("Found " + values.size() + " value(s) for " +
                    cssName + " when " + 1 + " value(s) were expected", -1);
        }
    }

    private abstract static class SingleIdent extends AbstractPropertyBuilder {
        protected abstract BitSet getAllowed();

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            CSSPrimitiveValue value = values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkIdentType(cssName, value);
                IdentValue ident = checkIdent(value);

                checkValidity(cssName, getAllowed(), ident);
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }
    }

    private static class GenericColor extends AbstractPropertyBuilder {
        private static final BitSet ALLOWED = setFor(TRANSPARENT);

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            CSSPrimitiveValue value = values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkIdentOrColorType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    FSRGBColor color = Conversions.getColor(value.getStringValue());
                    if (color != null) {
                        return singletonList(
                                new PropertyDeclaration(
                                        cssName,
                                        new PropertyValue(color),
                                        important,
                                        origin));
                    }

                    IdentValue ident = checkIdent(value);
                    checkValidity(cssName, ALLOWED, ident);
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }

    private static class GenericBorderStyle extends SingleIdent {
        @Override
        protected BitSet getAllowed() {
            return BORDER_STYLES;
        }
    }

    private static class GenericBorderWidth extends AbstractPropertyBuilder {
        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkIdentOrLengthType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(value);
                    checkValidity(cssName, BORDER_WIDTHS, ident);

                    return singletonList(
                            new PropertyDeclaration(
                                    cssName, Conversions.getBorderWidth(ident.toString()), important, origin));
                } else {
                    if (value.getFloatValue() < 0.0f) {
                        throw new CSSParseException(cssName + " may not be negative", -1);
                    }
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }

    private static class GenericBorderCornerRadius extends AbstractPropertyBuilder  {
        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin,
                boolean important, boolean inheritAllowed) {
            assertFoundUpToValues(cssName, values, 2);

            PropertyValue first = (PropertyValue) values.get(0);
            PropertyValue second = null;
            if (values.size() == 2) {
                second = (PropertyValue) values.get(1);
            }

            checkInheritAllowed(first, inheritAllowed);

            if (second != null) {
                checkInheritAllowed(second, false);
            }

            checkLengthOrPercentType(cssName, first);
            if (second == null) {
                return createTwoValueResponse(cssName, first, first, origin, important);
            } else {
                checkLengthOrPercentType(cssName, second);
                return createTwoValueResponse(cssName, first, second, origin, important);
            }
        }
    }

    private abstract static class LengthWithIdent extends AbstractPropertyBuilder {
        protected abstract BitSet getAllowed();

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkIdentOrLengthType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(value);
                    checkValidity(cssName, getAllowed(), ident);
                } else if (! isNegativeValuesAllowed() && value.getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " may not be negative", -1);
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }

        protected boolean isNegativeValuesAllowed() {
            return true;
        }
    }

    private abstract static class LengthLikeWithIdent extends AbstractPropertyBuilder {
        protected abstract BitSet getAllowed();

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkIdentLengthOrPercentType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(value);
                    checkValidity(cssName, getAllowed(), ident);
                } else if (! isNegativeValuesAllowed() && value.getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " may not be negative", -1);
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }

        protected boolean isNegativeValuesAllowed() {
            return true;
        }
    }

    private static class LengthLike extends AbstractPropertyBuilder {
        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkLengthOrPercentType(cssName, value);

                if (! isNegativeValuesAllowed() && value.getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " may not be negative", -1);
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }

        protected boolean isNegativeValuesAllowed() {
            return true;
        }
    }

    private static class NonNegativeLengthLike extends LengthLike {
        @Override
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    private static class ColOrRowSpan extends AbstractPropertyBuilder {
        @Override
        public List<PropertyDeclaration> buildDeclarations(CSSName cssName, List<? extends CSSPrimitiveValue> values,
                                                           Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkNumberType(cssName, value);

                if (value.getFloatValue() < 1) {
                    throw new CSSParseException("colspan/rowspan must be greater than zero", -1);
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }

    private static class PlainInteger extends AbstractPropertyBuilder {
        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkInteger(cssName, value);

                if (! isNegativeValuesAllowed() && value.getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " may not be negative", -1);
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }

        protected boolean isNegativeValuesAllowed() {
            return true;
        }
    }

    private static class Length extends AbstractPropertyBuilder {
        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkLengthType(cssName, value);

                if (! isNegativeValuesAllowed() && value.getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " may not be negative", -1);
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }

        protected boolean isNegativeValuesAllowed() {
            return true;
        }
    }

    /*
    private static class SingleString extends AbstractPropertyBuilder {
        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            checkSingleValue(cssName, values)
            CSSPrimitiveValue value = (CSSPrimitiveValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkStringType(cssName, value);
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }
    }
    */

    /*
    private static abstract class SingleStringWithIdent extends AbstractPropertyBuilder {
        protected abstract BitSet getAllowed();

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            checkSingleValue(cssName, values);
            CSSPrimitiveValue value = (CSSPrimitiveValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentOrString(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(cssName, value);

                    checkValidity(cssName, getAllowed(), ident);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }
    }
    */

    /*
    private static class SingleStringWithNone extends SingleStringWithIdent {
        private static final BitSet ALLOWED = setFor( NONE );

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }
    */

    private static class LengthLikeWithAuto extends LengthLikeWithIdent {
        // <length> | <percentage> | auto | inherit
        private static final BitSet ALLOWED = setFor(
            AUTO);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    private static class LengthWithNormal extends LengthWithIdent {
        // <length> | normal | inherit
        private static final BitSet ALLOWED = setFor(
            NORMAL);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    private static class LengthLikeWithNone extends LengthLikeWithIdent {
        // <length> | <percentage> | none | inherit
        private static final BitSet ALLOWED = setFor(
            NONE);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    private static class GenericURIWithNone extends AbstractPropertyBuilder {
        // <uri> | none | inherit
        private static final BitSet ALLOWED = setFor(NONE);

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            CSSPrimitiveValue value = values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkIdentOrURIType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(value);
                    checkValidity(cssName, ALLOWED, ident);
                }
            }
            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }

    public static class BackgroundAttachment extends SingleIdent {
        @Override
        protected BitSet getAllowed() {
            return BACKGROUND_ATTACHMENTS;
        }
    }

    public static class BackgroundColor extends GenericColor {
    }

    public static class BackgroundImage extends GenericURIWithNone {
        @Override
        public List<PropertyDeclaration> buildDeclarations(
            CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {

            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue)values.get(0);

            if (!value.toString().startsWith(LINEAR_GRADIENT.asString())) {
                return super.buildDeclarations(cssName, values, origin, important, inheritAllowed);
            }

            BuilderUtil.checkFunctionsAllowed(value.getFunction(), "linear-gradient");
            return singletonList(new PropertyDeclaration(cssName, value, important, origin));
        }
    }

    public static class BackgroundSize extends AbstractPropertyBuilder {
        private static final BitSet ALL_ALLOWED = setFor(AUTO, CONTAIN, COVER);

        @Override
        public List<PropertyDeclaration> buildDeclarations(CSSName cssName, List<? extends CSSPrimitiveValue> values,
                                                           Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundUpToValues(cssName, values, 2);

            CSSPrimitiveValue first = values.get(0);
            CSSPrimitiveValue second = null;
            if (values.size() == 2) {
                second = values.get(1);
            }

            checkInheritAllowed(first, inheritAllowed);
            if (values.size() == 1 &&
                    first.getCssValueType() == CSS_INHERIT) {
                return singletonList(
                        new PropertyDeclaration(cssName, first, important, origin));
            }

            if (second != null) {
                checkInheritAllowed(second, false);
            }

            checkIdentLengthOrPercentType(cssName, first);
            if (second == null) {
                if (first.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue firstIdent = checkIdent(first);
                    checkValidity(cssName, ALL_ALLOWED, firstIdent);

                    if (firstIdent == CONTAIN || firstIdent == COVER) {
                        return singletonList(
                                new PropertyDeclaration(cssName, first, important, origin));
                    } else {
                        return createTwoValueResponse(CSSName.BACKGROUND_SIZE, first, first, origin, important);
                    }
                } else {
                    return createTwoValueResponse(CSSName.BACKGROUND_SIZE, first, new PropertyValue(AUTO), origin, important);
                }
            } else {
                checkIdentLengthOrPercentType(cssName, second);

                if (first.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue firstIdent = checkIdent(first);
                    if (firstIdent != AUTO) {
                        throw new CSSParseException("The only ident value allowed here is 'auto'", -1);
                    }
                } else if (((PropertyValue)first).getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " values cannot be negative", -1);
                }

                if (second.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue secondIdent = checkIdent(second);
                    if (secondIdent != AUTO) {
                        throw new CSSParseException("The only ident value allowed here is 'auto'", -1);
                    }
                } else if (((PropertyValue)second).getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " values cannot be negative", -1);
                }

                return createTwoValueResponse(CSSName.BACKGROUND_SIZE, first, second, origin, important);
            }
        }
    }

    public static class BackgroundPosition extends AbstractPropertyBuilder {
        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundUpToValues(cssName, values, 2);

            CSSPrimitiveValue first = values.get(0);
            CSSPrimitiveValue second = null;
            if (values.size() == 2) {
                second = values.get(1);
            }

            checkInheritAllowed(first, inheritAllowed);
            if (values.size() == 1 &&
                    first.getCssValueType() == CSS_INHERIT) {
                return singletonList(
                        new PropertyDeclaration(cssName, first, important, origin));
            }

            if (second != null) {
                checkInheritAllowed(second, false);
            }

            checkIdentLengthOrPercentType(cssName, first);
            if (second == null) {
                if (isLength(first) || first.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
                    List<CSSPrimitiveValue> responseValues = new ArrayList<>(2);
                    responseValues.add(first);
                    responseValues.add(new PropertyValue(
                            CSSPrimitiveValue.CSS_PERCENTAGE, 50.0f, "50%"));
                    return singletonList(new PropertyDeclaration(
                                CSSName.BACKGROUND_POSITION,
                                new PropertyValue(responseValues), important, origin));
                }
            } else {
                checkIdentLengthOrPercentType(cssName, second);
            }


            IdentValue firstIdent = null;
            if (first.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                firstIdent = checkIdent(first);
                checkValidity(cssName, getAllowed(), firstIdent);
            }

            IdentValue secondIdent = null;
            if (second == null) {
                secondIdent = CENTER;
            } else if (second.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                secondIdent = checkIdent(second);
                checkValidity(cssName, getAllowed(), secondIdent);
            }

            if (firstIdent == null && secondIdent == null) {
                return singletonList(new PropertyDeclaration(
                        CSSName.BACKGROUND_POSITION, new PropertyValue(values), important, origin));
            } else if (firstIdent != null && secondIdent != null) {
                if (firstIdent == TOP || firstIdent == BOTTOM ||
                        secondIdent == LEFT || secondIdent == RIGHT) {
                    IdentValue temp = firstIdent;
                    firstIdent = secondIdent;
                    secondIdent = temp;
                }

                checkIdentPosition(cssName, firstIdent, secondIdent);

                return createTwoPercentValueResponse(
                        getPercentForIdent(firstIdent),
                        getPercentForIdent(secondIdent),
                        important,
                        origin);
            } else {
                checkIdentPosition(cssName, firstIdent, secondIdent);

                List<CSSPrimitiveValue> responseValues = new ArrayList<>(2);

                if (firstIdent == null) {
                    responseValues.add(first);
                    responseValues.add(createValueForIdent(secondIdent));
                } else {
                    responseValues.add(createValueForIdent(firstIdent));
                    responseValues.add(second);
                }

                return singletonList(new PropertyDeclaration(
                        CSSName.BACKGROUND_POSITION,
                        new PropertyValue(responseValues), important, origin));
            }
        }

        private void checkIdentPosition(CSSName cssName, IdentValue firstIdent, IdentValue secondIdent) {
            if (firstIdent == TOP || firstIdent == BOTTOM ||
                    secondIdent == LEFT || secondIdent == RIGHT) {
                throw new CSSParseException("Invalid combination of keywords in " + cssName, -1);
            }
        }

        private float getPercentForIdent(IdentValue ident) {
            float percent = 0.0f;

            if (ident == CENTER) {
                percent = 50.0f;
            } else if (ident == BOTTOM || ident == RIGHT) {
                percent = 100.0f;
            }

            return percent;
        }

        private PropertyValue createValueForIdent(IdentValue ident) {
            float percent = getPercentForIdent(ident);
            return new PropertyValue(
                    CSSPrimitiveValue.CSS_PERCENTAGE, percent, percent + "%");
        }

        private List<PropertyDeclaration> createTwoPercentValueResponse(
                float percent1, float percent2, boolean important, Origin origin) {
            PropertyValue value1 = new PropertyValue(
                    CSSPrimitiveValue.CSS_PERCENTAGE, percent1, percent1 + "%");
            PropertyValue value2 = new PropertyValue(
                    CSSPrimitiveValue.CSS_PERCENTAGE, percent2, percent2 + "%");

            List<PropertyValue> values = new ArrayList<>(2);
            values.add(value1);
            values.add(value2);

            PropertyDeclaration result = new PropertyDeclaration(
                    CSSName.BACKGROUND_POSITION,
                    new PropertyValue(values), important, origin);

            return singletonList(result);
        }

        private BitSet getAllowed() {
            return BACKGROUND_POSITIONS;
        }
    }

    public static class BackgroundRepeat extends SingleIdent {
        @Override
        protected BitSet getAllowed() {
            return BACKGROUND_REPEATS;
        }
    }

    public static class BorderCollapse extends SingleIdent {
        // collapse | separate | inherit
        private static final BitSet ALLOWED = setFor(COLLAPSE, SEPARATE);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class BorderTopColor extends GenericColor {
    }

    public static class BorderRightColor extends GenericColor {
    }

    public static class BorderBottomColor extends GenericColor {
    }

    public static class BorderLeftColor extends GenericColor {
    }

    public static class BorderTopStyle extends GenericBorderStyle {
    }

    public static class BorderRightStyle extends GenericBorderStyle {
    }

    public static class BorderBottomStyle extends GenericBorderStyle {
    }

    public static class BorderLeftStyle extends GenericBorderStyle {
    }

    public static class BorderTopWidth extends GenericBorderWidth {
    }

    public static class BorderRightWidth extends GenericBorderWidth {
    }

    public static class BorderBottomWidth extends GenericBorderWidth {
    }

    public static class BorderLeftWidth extends GenericBorderWidth {
    }

    public static class BorderTopLeftRadius extends GenericBorderCornerRadius {
    }

    public static class BorderTopRightRadius extends GenericBorderCornerRadius {
    }

    public static class BorderBottomRightRadius extends GenericBorderCornerRadius {
    }

    public static class BorderBottomLeftRadius extends GenericBorderCornerRadius {
    }

    public static class Bottom extends LengthLikeWithAuto {
    }

    public static class CaptionSide extends SingleIdent {
        // top | bottom | inherit
        private static final BitSet ALLOWED = setFor(
            TOP, BOTTOM);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Clear extends SingleIdent {
        // none | left | right | both | inherit
        private static final BitSet ALLOWED = setFor(NONE, LEFT, RIGHT, BOTH);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Color extends GenericColor {
    }

    public static class Cursor extends SingleIdent {
        // [ [<uri> ,]* [ auto | crosshair | default | pointer | move | e-resize
        // | ne-resize | nw-resize | n-resize | se-resize | sw-resize | s-resize
        // | w-resize | text | wait | help | progress ] ] | inherit
        private static final BitSet ALLOWED = setFor(
            AUTO, CROSSHAIR,
            DEFAULT, POINTER,
            MOVE, E_RESIZE,
            NE_RESIZE, NW_RESIZE,
            N_RESIZE, SE_RESIZE,
            SW_RESIZE, S_RESIZE,
            W_RESIZE, TEXT,
            WAIT, HELP,
            PROGRESS);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Display extends SingleIdent {
        // inline | block | list-item | run-in | inline-block | table | inline-table
        // | table-row-group | table-header-group
        // | table-footer-group | table-row | table-column-group | table-column
        // | table-cell | table-caption | none | inherit
        private static final BitSet ALLOWED = setFor(
            INLINE, BLOCK,
            LIST_ITEM, /* RUN_IN, */
            INLINE_BLOCK, TABLE,
            INLINE_TABLE, TABLE_ROW_GROUP,
            TABLE_HEADER_GROUP, TABLE_FOOTER_GROUP,
            TABLE_ROW, TABLE_COLUMN_GROUP,
            TABLE_COLUMN, TABLE_CELL,
            TABLE_CAPTION, NONE);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class EmptyCells extends SingleIdent {
        // show | hide | inherit
        private static final BitSet ALLOWED = setFor(SHOW, HIDE);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Float extends SingleIdent {
        // left | right | none | inherit
        private static final BitSet ALLOWED = setFor(
            LEFT, RIGHT, NONE);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class FontFamily extends AbstractPropertyBuilder {
        // [[ <family-name> | <generic-family> ] [, <family-name>| <generic-family>]* ] | inherit

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            if (values.size() == 1) {
                CSSPrimitiveValue value = values.get(0);
                checkInheritAllowed(value, inheritAllowed);
                if (value.getCssValueType() == CSS_INHERIT) {
                    return singletonList(
                            new PropertyDeclaration(cssName, value, important, origin));
                }
            }

            // Both Opera and Firefox parse "Century Gothic" Arial sans-serif as
            // [Century Gothic], [Arial sans-serif] (i.e. the comma is assumed
            // after a string).  Seems wrong per the spec, but FF (at least)
            // does it in standards mode, so we do too.
            List<String> consecutiveIdents = new ArrayList<>();
            List<String> normalized = new ArrayList<>(values.size());
            for (CSSPrimitiveValue cssPrimitiveValue : values) {
                PropertyValue value = (PropertyValue) cssPrimitiveValue;

                Token operator = value.getOperator();
                if (operator != null && operator != Token.TK_COMMA) {
                    throw new CSSParseException("Invalid font-family definition", -1);
                }

                if (operator != null) {
                    if (!consecutiveIdents.isEmpty()) {
                        normalized.add(concat(consecutiveIdents, ' '));
                        consecutiveIdents.clear();
                    }
                }

                checkInheritAllowed(value, false);
                short type = value.getPrimitiveType();
                if (type == CSSPrimitiveValue.CSS_STRING) {
                    if (!consecutiveIdents.isEmpty()) {
                        normalized.add(concat(consecutiveIdents, ' '));
                        consecutiveIdents.clear();
                    }
                    normalized.add(value.getStringValue());
                } else if (type == CSSPrimitiveValue.CSS_IDENT) {
                    consecutiveIdents.add(value.getStringValue());
                } else {
                    throw new CSSParseException("Invalid font-family definition", -1);
                }
            }
            if (!consecutiveIdents.isEmpty()) {
                normalized.add(concat(consecutiveIdents, ' '));
            }

            String text = concat(normalized, ',');
            PropertyValue result = new PropertyValue(
                    CSSPrimitiveValue.CSS_STRING, text, text, normalized.toArray(new String[0]), null);  // HACK cssText can be wrong

            return singletonList(
                    new PropertyDeclaration(cssName, result, important, origin));
        }

        private String concat(List<String> strings, char separator) {
            StringBuilder buf = new StringBuilder(64);
            for (Iterator<String> i = strings.iterator(); i.hasNext(); ) {
                String s = i.next();
                buf.append(s);
                if (i.hasNext()) {
                    buf.append(separator);
                }
            }
            return buf.toString();
        }
    }

    public static class FontSize extends AbstractPropertyBuilder {
        // <absolute-size> | <relative-size> | <length> | <percentage> | inherit
        private static final BitSet ALLOWED;

        static {
            ALLOWED = new BitSet(IdentValue.getIdentCount());
            ALLOWED.or(ABSOLUTE_FONT_SIZES);
            ALLOWED.or(RELATIVE_FONT_SIZES);
        }

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkIdentLengthOrPercentType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(value);
                    checkValidity(cssName, ALLOWED, ident);
                } else if (value.getFloatValue() < 0.0f) {
                    throw new CSSParseException("font-size may not be negative", -1);
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }
    }

    public static class FontStyle extends SingleIdent {
        @Override
        protected BitSet getAllowed() {
            return FONT_STYLES;
        }
    }

    public static class FontVariant extends SingleIdent {
        @Override
        protected BitSet getAllowed() {
            return FONT_VARIANTS;
        }
    }

    public static class FontWeight extends AbstractPropertyBuilder {
        // normal | bold | bolder | lighter | 100 | 200 | 300 | 400 | 500 | 600 | 700 | 800 | 900 | inherit
        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkIdentOrNumberType(cssName, value);

                short type = value.getPrimitiveType();
                if (type == CSSPrimitiveValue.CSS_IDENT) {
                    checkIdentType(cssName, value);
                    IdentValue ident = checkIdent(value);

                    checkValidity(cssName, getAllowed(), ident);
                } else if (type == CSSPrimitiveValue.CSS_NUMBER) {
                    IdentValue weight = Conversions.getNumericFontWeight(value.getFloatValue());
                    if (weight == null) {
                        throw new CSSParseException(value + " is not a valid font weight", -1);
                    }

                    PropertyValue replacement = new PropertyValue(
                            CSSPrimitiveValue.CSS_IDENT, weight.toString(), weight.toString());
                    replacement.setIdentValue(weight);
                    return singletonList(
                            new PropertyDeclaration(cssName, replacement, important, origin));

                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }

        private BitSet getAllowed() {
            return FONT_WEIGHTS;
        }
    }

    public static class FSBorderSpacingHorizontal extends Length {
    }

    public static class FSBorderSpacingVertical extends Length {
    }

    public static class FSFontMetricSrc extends GenericURIWithNone {
    }

    public static class FSPageHeight extends LengthLikeWithAuto {
        @Override
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class FSPageWidth extends LengthLikeWithAuto {
        @Override
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class FSPageSequence extends SingleIdent {
        // start | auto
        private static final BitSet ALLOWED = setFor(START, AUTO);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class FSPageOrientation extends SingleIdent {
        @Override
        protected BitSet getAllowed() {
            return PAGE_ORIENTATIONS;
        }
    }

    public static class FSPDFFontEmbed extends SingleIdent {
        // auto | embed
        private static final BitSet ALLOWED = setFor(AUTO, EMBED);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class FSPDFFontEncoding extends AbstractPropertyBuilder {
        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            CSSPrimitiveValue value = values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkIdentOrString(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    // Convert to string
                    return singletonList(
                            new PropertyDeclaration(
                                    cssName,
                                    new PropertyValue(
                                            CSSPrimitiveValue.CSS_STRING,
                                            value.getStringValue(),
                                            value.getCssText()),
                                    important,
                                    origin));
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }

    public static class FSTableCellColspan extends ColOrRowSpan {
    }

    public static class FSTableCellRowspan extends ColOrRowSpan {
    }

    public static class FSTablePaginate extends SingleIdent {
        private static final BitSet ALLOWED = setFor(PAGINATE, AUTO);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
     }

    public static class FSTextDecorationExtent extends SingleIdent {
       private static final BitSet ALLOWED = setFor(LINE, BLOCK);

       @Override
       protected BitSet getAllowed() {
           return ALLOWED;
       }
    }

    public static class FSFitImagesToWidth extends LengthLikeWithAuto {
        @Override
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
     }

    public static class Height extends LengthLikeWithAuto {
        @Override
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class FSDynamicAutoWidth extends SingleIdent {
        private static final BitSet ALLOWED = setFor(DYNAMIC, STATIC);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class FSKeepWithInline extends SingleIdent {
        // auto | keep
        private static final BitSet ALLOWED = setFor(AUTO, KEEP);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class FSNamedDestination extends SingleIdent {
        // none | create
        private static final BitSet ALLOWED = setFor(NONE, CREATE);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Left extends LengthLikeWithAuto {
    }

    public static class LetterSpacing extends LengthWithNormal {
    }

    public static class LineHeight extends AbstractPropertyBuilder {
        // normal | <number> | <length> | <percentage> | inherit
        private static final BitSet ALLOWED = setFor(
            NORMAL);

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkIdentLengthNumberOrPercentType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(value);
                    checkValidity(cssName, ALLOWED, ident);
                } else if (value.getFloatValue() < 0.0) {
                    throw new CSSParseException("line-height may not be negative", -1);
                }
            }
            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }

    public static class ListStyleImage extends GenericURIWithNone {
    }

    public static class ListStylePosition extends SingleIdent {
        @Override
        protected BitSet getAllowed() {
            return LIST_STYLE_POSITIONS;
        }
    }

    public static class ListStyleType extends SingleIdent {
        @Override
        protected BitSet getAllowed() {
            return LIST_STYLE_TYPES;
        }
    }

    public static class MarginTop extends LengthLikeWithAuto {
    }

    public static class MarginRight extends LengthLikeWithAuto {
    }

    public static class MarginBottom extends LengthLikeWithAuto {
    }

    public static class MarginLeft extends LengthLikeWithAuto {
    }

    public static class MaxHeight extends LengthLikeWithNone {
        @Override
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class MaxWidth extends LengthLikeWithNone {
        @Override
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class MinHeight extends NonNegativeLengthLike {
    }

    public static class MinWidth extends NonNegativeLengthLike {
    }

    public static class Orphans extends PlainInteger {
        @Override
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class Opacity extends AbstractPropertyBuilder {
        @Override
        public List<PropertyDeclaration> buildDeclarations(CSSName cssName, List<? extends CSSPrimitiveValue> values,
                                                           Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue) values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            checkNumberType(cssName, value);
            checkValueBetween(cssName, value.getFloatValue(), 0, 1);

            return singletonList(new PropertyDeclaration(cssName, value, important, origin));
        }

    }

    public static class Overflow extends SingleIdent {
        // visible | hidden | scroll | auto | inherit
        private static final BitSet ALLOWED = setFor(VISIBLE, HIDDEN/* SCROLL, AUTO, */);

        // We only support visible or hidden for now

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class PaddingTop extends NonNegativeLengthLike {
    }

    public static class PaddingRight extends NonNegativeLengthLike {
    }

    public static class PaddingBottom extends NonNegativeLengthLike {
    }

    public static class PaddingLeft extends NonNegativeLengthLike {
    }

    public static class PageBreakBefore extends SingleIdent {
        // auto | always | avoid | left | right | inherit
        private static final BitSet ALLOWED = setFor(AUTO, ALWAYS, AVOID, LEFT, RIGHT);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Page extends AbstractPropertyBuilder {
        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            CSSPrimitiveValue value = values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkIdentType(cssName, value);

                if (! value.getStringValue().equals("auto")) {
                    // Treat as string since it won't be a proper IdentValue
                    value = new PropertyValue(
                            CSSPrimitiveValue.CSS_STRING, value.getStringValue(), value.getCssText());
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));


        }
    }

    public static class PageBreakAfter extends SingleIdent {
        // auto | always | avoid | left | right | inherit
        private static final BitSet ALLOWED = setFor(
            AUTO, ALWAYS,
            AVOID, LEFT,
            RIGHT);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class PageBreakInside extends SingleIdent {
        // avoid | auto | inherit
        private static final BitSet ALLOWED = setFor(
            AVOID, AUTO);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Position extends AbstractPropertyBuilder {
        // static | relative | absolute | fixed | inherit
        private static final BitSet ALLOWED = setFor(STATIC, RELATIVE, ABSOLUTE, FIXED);

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    checkIdentType(cssName, value);
                    IdentValue ident = checkIdent(value);

                    checkValidity(cssName, getAllowed(), ident);
                } else if (value.getPropertyValueType() == VALUE_TYPE_FUNCTION) {
                    FSFunction function = value.getFunction();
                    if (function.is("running")) {
                        List<PropertyValue> params = function.getParameters();
                        if (params.size() == 1) {
                            PropertyValue param = params.get(0);
                            if (param.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
                                throw new CSSParseException("The running function takes an identifier as a parameter", -1);
                            }
                        } else {
                            throw new CSSParseException("The running function takes one parameter", -1);
                        }
                    } else {
                        throw new CSSParseException("Only the running function is supported here", -1);
                    }
                } else {
                    throw new CSSParseException("Value for " + cssName + " must be an identifier or function", -1);
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }

        private BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Right extends LengthLikeWithAuto {
    }

    public static class Src extends AbstractPropertyBuilder {
        // <uri> | none | inherit
        // Also supports: url('font.woff') format('woff'), url('font.ttf') format('truetype')
        private static final BitSet ALLOWED = setFor(NONE);

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            // Handle inherit case
            if (values.size() == 1) {
                CSSPrimitiveValue value = values.get(0);
                checkInheritAllowed(value, inheritAllowed);
                if (value.getCssValueType() == CSS_INHERIT) {
                    return singletonList(new PropertyDeclaration(cssName, value, important, origin));
                }

                // Handle single value case (none or single URL)
                checkIdentOrURIType(cssName, value);
                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(value);
                    checkValidity(cssName, ALLOWED, ident);
                }
                return singletonList(new PropertyDeclaration(cssName, value, important, origin));
            }

            // Handle multiple values (e.g., url() format() pairs)
            // Wrap all values into a PropertyValue list
            PropertyValue listValue = new PropertyValue(new ArrayList<>(values));
            return singletonList(new PropertyDeclaration(cssName, listValue, important, origin));
        }
    }

    public static class TabSize extends PlainInteger {
        @Override
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class Top extends LengthLikeWithAuto {
    }

    public static class TableLayout extends SingleIdent {
        // auto | fixed | inherit
        private static final BitSet ALLOWED = setFor(
            AUTO, FIXED);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class TextAlign extends SingleIdent {
        // left | right | center | justify | inherit
        private static final BitSet ALLOWED = setFor(LEFT, RIGHT, CENTER, JUSTIFY);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class TextDecoration extends AbstractPropertyBuilder {
        // none | [ underline || overline || line-through || blink ] | inherit
        private static final BitSet ALLOWED = setFor(
            /* NONE, */ UNDERLINE,
            OVERLINE, LINE_THROUGH
            /* BLINK */);

        private BitSet getAllowed() {
            return ALLOWED;
        }

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            if (values.size() == 1) {
                CSSPrimitiveValue value = values.get(0);
                boolean goWithSingle = false;
                if (value.getCssValueType() == CSS_INHERIT) {
                    goWithSingle = true;
                } else {
                    checkIdentType(CSSName.TEXT_DECORATION, value);
                    IdentValue ident = checkIdent(value);
                    if (ident == NONE) {
                        goWithSingle = true;
                    }
                }

                if (goWithSingle) {
                    return singletonList(
                            new PropertyDeclaration(cssName, value, important, origin));
                }
            }

            for (CSSPrimitiveValue cssPrimitiveValue : values) {
                PropertyValue value = (PropertyValue) cssPrimitiveValue;
                checkInheritAllowed(value, false);
                checkIdentType(cssName, value);
                IdentValue ident = checkIdent(value);
                if (ident == NONE) {
                    throw new CSSParseException("Value none may not be used in this position", -1);
                }
                checkValidity(cssName, getAllowed(), ident);
            }

            return singletonList(
                    new PropertyDeclaration(cssName, new PropertyValue(values), important, origin));

        }
    }

    public static class TextIndent extends LengthLike {
    }

    public static class TextTransform extends SingleIdent {
        // capitalize | uppercase | lowercase | none | inherit
        private static final BitSet ALLOWED = setFor(CAPITALIZE, UPPERCASE, LOWERCASE, NONE);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class VerticalAlign extends LengthLikeWithIdent {
        // baseline | sub | super | top | text-top | middle
        // | bottom | text-bottom | <percentage> | <length> | inherit
        private static final BitSet ALLOWED = setFor(BASELINE, SUB, SUPER, TOP, TEXT_TOP, MIDDLE, BOTTOM, TEXT_BOTTOM);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Visibility extends SingleIdent {
        // visible | hidden | collapse | inherit
        private static final BitSet ALLOWED = setFor(
            VISIBLE, HIDDEN, COLLAPSE);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class WhiteSpace extends SingleIdent {
        // normal | pre | nowrap | pre-wrap | pre-line | inherit
        private static final BitSet ALLOWED = setFor(NORMAL, PRE, NOWRAP, PRE_WRAP, PRE_LINE);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class WordBreak extends SingleIdent {
        // normal | break-all
        private static final BitSet ALLOWED = setFor(NORMAL, BREAK_ALL);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class WordWrap extends SingleIdent {
        // normal | break-word
        private static final BitSet ALLOWED = setFor(NORMAL, BREAK_WORD);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Hyphens extends SingleIdent {
        // none | manual | auto
        private static final BitSet ALLOWED = setFor(NONE, MANUAL, AUTO);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class BoxSizing extends SingleIdent {
        // border-box | content-box
        private static final BitSet ALLOWED = setFor(BORDER_BOX, CONTENT_BOX);

        @Override
        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Widows extends PlainInteger {
        @Override
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class Width extends LengthLikeWithAuto {
        @Override
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class WordSpacing extends LengthWithNormal {
    }

    public static class ZIndex extends AbstractPropertyBuilder {
        // auto | <integer> | inherit
        private static final BitSet ALLOWED = setFor(
            AUTO);

        @Override
        public List<PropertyDeclaration> buildDeclarations(
                CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
            assertFoundSingleValue(cssName, values);
            CSSPrimitiveValue value = values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSS_INHERIT) {
                checkIdentOrIntegerType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(value);
                    checkValidity(cssName, ALLOWED, ident);
                }
            }

            return singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }


    private static List<PropertyDeclaration> createTwoValueResponse(
        CSSName cssName, CSSPrimitiveValue value1,
        CSSPrimitiveValue value2,
        Origin origin, boolean important) {

        return singletonList(
            new PropertyDeclaration(cssName,
                new PropertyValue(asList(value1, value2)), important, origin)
        );
    }
}
