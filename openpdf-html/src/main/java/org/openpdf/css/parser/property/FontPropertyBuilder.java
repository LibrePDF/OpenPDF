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
import org.openpdf.css.parser.Token;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static java.util.Locale.ROOT;

public class FontPropertyBuilder extends AbstractPropertyBuilder {
    // [ [ <'font-style'> || <'font-variant'> || <'font-weight'> ]? <'font-size'> [ / <'line-height'> ]? <'font-family'> ]
    private static final CSSName[] ALL = {
        CSSName.FONT_STYLE, CSSName.FONT_VARIANT, CSSName.FONT_WEIGHT,
        CSSName.FONT_SIZE, CSSName.LINE_HEIGHT, CSSName.FONT_FAMILY };

    @Override
    public List<PropertyDeclaration> buildDeclarations(
            CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
        List<PropertyDeclaration> result = checkInheritAll(ALL, values, origin, important, inheritAllowed);
        if (result != null) {
            return result;
        }

        PropertyDeclaration fontStyle = null;
        PropertyDeclaration fontVariant = null;
        PropertyDeclaration fontWeight = null;
        PropertyDeclaration fontSize = null;
        PropertyDeclaration lineHeight = null;
        PropertyDeclaration fontFamily = null;

        boolean keepGoing = false;

        ListIterator<? extends CSSPrimitiveValue> i = values.listIterator();
        while (i.hasNext()) {
            PropertyValue value = (PropertyValue)i.next();
            int type = value.getPrimitiveType();
            if (type == CSSPrimitiveValue.CSS_IDENT) {
                // The parser will have given us ident values as they appear
                // (case-wise) in the CSS text since we might be creating
                // a font-family list out of them.  Here we want the normalized
                // (lowercase) version though.
                String lowerCase = value.getStringValue().toLowerCase(ROOT);
                value = new PropertyValue(CSSPrimitiveValue.CSS_IDENT, lowerCase, lowerCase);
                IdentValue ident = checkIdent(value);
                if (ident == IdentValue.NORMAL) { // skip to avoid double set false positives
                    continue;
                }
                if (PrimitivePropertyBuilders.FONT_STYLES.get(ident.FS_ID)) {
                    if (fontStyle != null) {
                        throw new CSSParseException("font-style cannot be set twice", -1);
                    }
                    fontStyle = new PropertyDeclaration(CSSName.FONT_STYLE, value, important, origin);
                } else if (PrimitivePropertyBuilders.FONT_VARIANTS.get(ident.FS_ID)) {
                    if (fontVariant != null) {
                        throw new CSSParseException("font-variant cannot be set twice", -1);
                    }
                    fontVariant = new PropertyDeclaration(CSSName.FONT_VARIANT, value, important, origin);
                } else if (PrimitivePropertyBuilders.FONT_WEIGHTS.get(ident.FS_ID)) {
                    if (fontWeight != null) {
                        throw new CSSParseException("font-weight cannot be set twice", -1);
                    }
                    fontWeight = new PropertyDeclaration(CSSName.FONT_WEIGHT, value, important, origin);
                } else {
                    keepGoing = true;
                    break;
                }
            } else if (type == CSSPrimitiveValue.CSS_NUMBER && value.getFloatValue() > 0) {
                if (fontWeight != null) {
                    throw new CSSParseException("font-weight cannot be set twice", -1);
                }

                IdentValue weight = Conversions.getNumericFontWeight(value.getFloatValue());
                if (weight == null) {
                    throw new CSSParseException(value + " is not a valid font weight", -1);
                }

                PropertyValue replacement = new PropertyValue(
                        CSSPrimitiveValue.CSS_IDENT, weight.toString(), weight.toString());
                replacement.setIdentValue(weight);

                fontWeight = new PropertyDeclaration(CSSName.FONT_WEIGHT, replacement, important, origin);
            } else {
                keepGoing = true;
                break;
            }
        }

        if (keepGoing) {
            i.previous();
            PropertyValue value = (PropertyValue)i.next();

            if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                String lowerCase = value.getStringValue().toLowerCase(ROOT);
                value = new PropertyValue(CSSPrimitiveValue.CSS_IDENT, lowerCase, lowerCase);
            }

            PropertyBuilder fontSizeBuilder = CSSName.getPropertyBuilder(CSSName.FONT_SIZE);
            List<PropertyDeclaration> l = fontSizeBuilder.buildDeclarations(
                    CSSName.FONT_SIZE, Collections.singletonList(value), origin, important);

            fontSize = l.get(0);

            if (i.hasNext()) {
                value = (PropertyValue)i.next();
                if (value.getOperator() == Token.TK_VIRGULE) {
                    PropertyBuilder lineHeightBuilder = CSSName.getPropertyBuilder(CSSName.LINE_HEIGHT);
                    l = lineHeightBuilder.buildDeclarations(
                            CSSName.LINE_HEIGHT, Collections.singletonList(value), origin, important);
                    lineHeight = l.get(0);
                } else {
                    i.previous();
                }
            }

            if (i.hasNext()) {
                List<CSSPrimitiveValue> families = new ArrayList<>();
                while (i.hasNext()) {
                    families.add(i.next());
                }
                PropertyBuilder fontFamilyBuilder = CSSName.getPropertyBuilder(CSSName.FONT_FAMILY);
                l = fontFamilyBuilder.buildDeclarations(
                        CSSName.FONT_FAMILY, families, origin, important);
                fontFamily = l.get(0);
            }
        }

        if (fontStyle == null) {
            fontStyle = new PropertyDeclaration(
                    CSSName.FONT_STYLE, new PropertyValue(IdentValue.NORMAL), important, origin);
        }

        if (fontVariant == null) {
            fontVariant = new PropertyDeclaration(
                    CSSName.FONT_VARIANT, new PropertyValue(IdentValue.NORMAL), important, origin);
        }

        if (fontWeight == null) {
            fontWeight = new PropertyDeclaration(
                    CSSName.FONT_WEIGHT, new PropertyValue(IdentValue.NORMAL), important, origin);
        }

        if (fontSize == null) {
            throw new CSSParseException("A font-size value is required", -1);
        }

        if (lineHeight == null) {
            lineHeight = new PropertyDeclaration(
                    CSSName.LINE_HEIGHT, new PropertyValue(IdentValue.NORMAL), important, origin);
        }

        // XXX font-family should be reset too (although, does this really make sense?)

        result = new ArrayList<>(ALL.length);
        result.add(fontStyle);
        result.add(fontVariant);
        result.add(fontWeight);
        result.add(fontSize);
        result.add(lineHeight);
        if (fontFamily != null) {
            result.add(fontFamily);
        }

        return result;
    }
}
