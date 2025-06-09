/*
 * PropertyDeclaration.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm, Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.openpdf.css.sheet;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.StylesheetInfo.Origin;


/**
 * Represents a single property declared in a CSS rule set. A
 * PropertyDeclaration is created from an CSSValue and is immutable. The
 * declaration knows its origin, importance and specificity, and thus is
 * prepared to be sorted out among properties of the same name, within a matched
 * group, for the CSS cascade, into a {@link
 * org.openpdf.css.newmatch.CascadedStyle}.
 *
 * @author Torbjoern Gannholm
 * @author Patrick Wright
 */
public class PropertyDeclaration {
    private final String propName;
    private final CSSName cssName;
    private final CSSPrimitiveValue cssPrimitiveValue;

    /**
     * Whether the property was declared as important! by the user.
     */
    private final boolean important;

    private final Origin origin;
    private IdentValue _identVal;
    private boolean identIsSet;
    private String _fingerprint;

    /**
     * ImportanceAndOrigin of stylesheet - how many different
     */
    public static final int IMPORTANCE_AND_ORIGIN_COUNT = 6;

    /**
     * ImportanceAndOrigin of stylesheet - user agent
     */
    private static final int USER_AGENT = 1;

    /**
     * ImportanceAndOrigin of stylesheet - user normal
     */
    private static final int USER_NORMAL = 2;

    /**
     * ImportanceAndOrigin of stylesheet - author normal
     */
    private static final int AUTHOR_NORMAL = 3;

    /**
     * ImportanceAndOrigin of stylesheet - author important
     */
    private static final int AUTHOR_IMPORTANT = 4;

    /**
     * ImportanceAndOrigin of stylesheet - user important
     */
    private static final int USER_IMPORTANT = 5;

    /**
     * Creates a new instance of PropertyDeclaration from an {@link
     * CSSPrimitiveValue} instance.
     *
     * @param cssName the name of CSS property
     * @param value   The CSSValue to wrap
     * @param imp     True if property was declared important! and false if
     *                not.
     * @param orig    origin of the property declaration, that is, the origin of the style sheet
     *                where it was declared.
     */
    public PropertyDeclaration(CSSName cssName,
                               CSSPrimitiveValue value,
                               boolean imp,
                               Origin orig) {
        this.propName = cssName.toString();
        this.cssName = cssName;
        this.cssPrimitiveValue = value;
        this.important = imp;
        this.origin = orig;
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return "%s: %s".formatted(getPropertyName(), getValue().toString());
    }

    public IdentValue asIdentValue() {
        if (!identIsSet) {
            _identVal = IdentValue.getByIdentString(cssPrimitiveValue.getCssText());
            identIsSet = true;
        }
        return _identVal;
    }

    public String getDeclarationStandardText() {
        return cssName + ": " + cssPrimitiveValue.getCssText() + ";";
    }

    public String getFingerprint() {
        if (_fingerprint == null) {
            _fingerprint = 'P' + cssName.FS_ID + ':' + ((PropertyValue)cssPrimitiveValue).getFingerprint() + ';';
        }
        return _fingerprint;
    }

    /**
     * Returns an int representing the combined origin and importance of the
     * property as declared. The int is assigned such that default origin and
     * importance is 0, and highest an important! property defined by the user
     * (origin is {@link StylesheetInfo.Origin#USER}). The combined value would allow this property
     * to be sequenced in the CSS cascade along with other properties matched to
     * the same element with the same property name. In that sort, the highest
     * sequence number returned from this method would take priority in the
     * cascade, so that a user important! property would override a user
     * non-important! property, and so on. The actual integer value returned by
     * this method is unimportant, but has the lowest value of 0 and increments
     * sequentially by 1 for each increase in origin/importance.
     */
    public int getImportanceAndOrigin() {
        return switch (origin) {
            case USER_AGENT -> USER_AGENT;
            case USER -> important ? USER_IMPORTANT : USER_NORMAL;
            case AUTHOR -> important ? AUTHOR_IMPORTANT : AUTHOR_NORMAL;
        };
    }

    /**
     * Returns the CSS name of this property, e.g. "font-family".
     */
    public String getPropertyName() {
        return propName;
    }

    /**
     * Gets the cSSName attribute of the PropertyDeclaration object
     *
     * @return The cSSName value
     */
    public CSSName getCSSName() {
        return cssName;
    }

    /**
     * Returns the specified {@link org.w3c.dom.css.CSSValue} for this property.
     * Specified means the value as entered by the user. Modifying the CSSValue
     * returned here will result in indeterminate behavior--consider it
     * immutable.
     */
    public CSSPrimitiveValue getValue() {
        return cssPrimitiveValue;
    }

    public boolean isImportant() {
        return important;
    }

    public Origin getOrigin() {
        return origin;
    }
}
