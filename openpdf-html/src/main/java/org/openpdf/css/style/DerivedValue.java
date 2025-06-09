/*
 * {{{ header & license
 * Copyright (c) 2004-2009 Josh Marinacci, Tobjorn Gannholm, Patrick Wright, Wisconsin Court System
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
package org.openpdf.css.style;

import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.constants.ValueConstants;
import org.openpdf.css.parser.FSColor;
import org.openpdf.util.XRRuntimeException;

import static java.util.Objects.requireNonNullElse;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_ATTR;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_IDENT;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_STRING;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_URI;


public abstract class DerivedValue implements FSDerivedValue {
    private final String _asString;
    private final short _cssSacUnitType;

    protected DerivedValue(
            CSSName name,
            short cssSACUnitType,
            String cssText,
            @Nullable String cssStringValue) {
        this._cssSacUnitType = cssSACUnitType;

        if ( cssText == null ) {
            throw new XRRuntimeException(
                    "CSSValue for '" + name + "' is null after " +
                    "resolving CSS identifier for value '" + cssStringValue + "'");
        }
        this._asString = deriveStringValue(cssText, cssStringValue);
    }

    private String deriveStringValue(String cssText, @Nullable String cssStringValue) {
        return switch (_cssSacUnitType) {
            case CSS_IDENT, CSS_STRING, CSS_URI, CSS_ATTR -> requireNonNullElse(cssStringValue, cssText);
            default -> cssText;
        };
    }

    /** The getCssText() or getStringValue(), depending. */
    public String getStringValue() {
        return _asString;
    }

    /**
     * If value is declared INHERIT should always be the {@link IdentValue#INHERIT},
     * not a DerivedValue.
     */
    @Override
    public boolean isDeclaredInherit() {
        return false;
    }

    public short getCssSacUnitType() {
        return _cssSacUnitType;
    }

    public boolean isAbsoluteUnit() {
        return ValueConstants.isAbsoluteUnit(_cssSacUnitType);
    }

    @Override
    public float asFloat() {
        throw new XRRuntimeException("asFloat() needs to be overridden in subclass.");
    }

    @Override
    public FSColor asColor() {
        throw new XRRuntimeException("asColor() needs to be overridden in subclass.");
    }

    @Override
    public float getFloatProportionalTo(
            CSSName cssName,
            float baseValue,
            CssContext ctx
    ) {
        throw new XRRuntimeException("getFloatProportionalTo() needs to be overridden in subclass.");
    }

    @Override
    public String asString() {
        return getStringValue();
    }

    @Override
    public String[] asStringArray() {
        throw new XRRuntimeException("asStringArray() needs to be overridden in subclass.");
    }

    @Override
    public IdentValue asIdentValue() {
        throw new XRRuntimeException("asIdentValue() needs to be overridden in subclass.");
    }

    @Override
    public boolean hasAbsoluteUnit() {
        throw new XRRuntimeException("hasAbsoluteUnit() needs to be overridden in subclass.");
    }

    @Override
    public boolean isIdent() {
        return false;
    }

    @Override
    public boolean isDependentOnFontSize() {
        return false;
    }
}
