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
package org.openpdf.css.parser;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.util.ArrayUtil;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.openpdf.css.parser.PropertyValue.Type.VALUE_TYPE_COLOR;
import static org.openpdf.css.parser.PropertyValue.Type.VALUE_TYPE_FUNCTION;
import static org.openpdf.css.parser.PropertyValue.Type.VALUE_TYPE_IDENT;
import static org.openpdf.css.parser.PropertyValue.Type.VALUE_TYPE_LENGTH;
import static org.openpdf.css.parser.PropertyValue.Type.VALUE_TYPE_LIST;
import static org.openpdf.css.parser.PropertyValue.Type.VALUE_TYPE_NUMBER;
import static org.openpdf.css.parser.PropertyValue.Type.VALUE_TYPE_STRING;

public class PropertyValue implements CSSPrimitiveValue {
    public enum Type {
        VALUE_TYPE_NUMBER,
        VALUE_TYPE_LENGTH,
        VALUE_TYPE_COLOR,
        VALUE_TYPE_IDENT,
        VALUE_TYPE_STRING,
        VALUE_TYPE_LIST,
        VALUE_TYPE_FUNCTION
    }

    private final short _type;
    private final short _cssValueType;

    @Nullable
    private final String _stringValue;
    private float _floatValue;
    private final String @Nullable [] _stringArrayValue;

    private final String _cssText;

    @Nullable
    private final FSColor _FSColor;

    @Nullable
    private IdentValue _identValue;

    private final Type _propertyValueType;

    @Nullable
    private final Token _operator;

    private final List<?> _values;
    @Nullable
    private final FSFunction _function;

    public PropertyValue(short type, float floatValue, String cssText) {
        this(type, floatValue, cssText, null);
    }

    public PropertyValue(short type, float floatValue, String cssText, @Nullable Token operatorToken) {
        _type = type;
        _floatValue = floatValue;
        _cssValueType = CSSValue.CSS_PRIMITIVE_VALUE;
        _cssText = cssText;

        _propertyValueType = (type == CSS_NUMBER && floatValue != 0.0f) ? VALUE_TYPE_NUMBER : VALUE_TYPE_LENGTH;
        _stringValue = null;
        _stringArrayValue = null;
        _values = emptyList();
        _function = null;
        _FSColor = null;
        _operator = operatorToken;
    }

    public PropertyValue(FSColor color) {
        this(color, null);
    }

    public PropertyValue(FSColor color, @Nullable Token operatorToken) {
        _type = CSSPrimitiveValue.CSS_RGBCOLOR;
        _cssValueType = CSSValue.CSS_PRIMITIVE_VALUE;
        _cssText = color.toString();
        _FSColor = color;

        _propertyValueType = VALUE_TYPE_COLOR;
        _stringValue = null;
        _stringArrayValue = null;
        _values = emptyList();
        _function = null;
        _operator = operatorToken;
    }

    public PropertyValue(short type, String stringValue, String cssText) {
        this(type, stringValue, cssText, null);
    }

    public PropertyValue(short type, String stringValue, String cssText, @Nullable Token operatorToken) {
        this(type, stringValue, cssText, null, operatorToken);
    }

    public PropertyValue(short type, String stringValue, String cssText, String @Nullable [] stringArrayValue, @Nullable Token operatorToken) {
        _type = type;
        _stringValue = stringValue;
        // Must be a case-insensitive compare since ident values aren't normalized
        // for font and font-family
        _cssValueType = _stringValue.equalsIgnoreCase("inherit") ? CSSValue.CSS_INHERIT : CSSValue.CSS_PRIMITIVE_VALUE;
        _cssText = cssText;

        _propertyValueType = (type == CSS_IDENT) ? VALUE_TYPE_IDENT : VALUE_TYPE_STRING;
        _stringArrayValue = ArrayUtil.cloneOrEmpty(stringArrayValue);
        _values = emptyList();
        _function = null;
        _FSColor = null;
        _operator = operatorToken;
    }

    public PropertyValue(IdentValue ident) {
        _type = CSS_IDENT;
        _stringValue = ident.toString();
        _cssValueType = _stringValue.equals("inherit") ? CSSValue.CSS_INHERIT : CSSValue.CSS_PRIMITIVE_VALUE;
        _cssText = ident.toString();

        _propertyValueType = VALUE_TYPE_IDENT;
        _identValue = ident;
        _stringArrayValue = null;
        _values = emptyList();
        _function = null;
        _FSColor = null;
        _operator = null;
    }

    public PropertyValue(List<?> values) {
        _type = CSSPrimitiveValue.CSS_UNKNOWN; // HACK
        _cssValueType = CSSValue.CSS_CUSTOM;
        _cssText = values.toString(); // HACK

        _values = values;
        _propertyValueType = VALUE_TYPE_LIST;
        _stringValue = null;
        _stringArrayValue = null;
        _function = null;
        _FSColor = null;
        _operator = null;
    }

    public PropertyValue(FSFunction function) {
        this(function, null);
    }

    public PropertyValue(FSFunction function, @Nullable Token operatorToken) {
        _type = CSSPrimitiveValue.CSS_UNKNOWN;
        _cssValueType = CSSValue.CSS_CUSTOM;
        _cssText = function.toString();

        _function = function;
        _propertyValueType = VALUE_TYPE_FUNCTION;
        _values = emptyList();
        _stringValue = null;
        _stringArrayValue = null;
        _FSColor = null;
        _operator = operatorToken;
    }

    @Override
    public Counter getCounterValue() throws DOMException {
        throw new UnsupportedOperationException("Unsupported operation: getCounterValue");
    }

    @Override
    public float getFloatValue(short unitType) throws DOMException {
        return _floatValue;
    }

    public float getFloatValue() {
        return _floatValue;
    }

    @Override
    public short getPrimitiveType() {
        return _type;
    }

    @Override
    public RGBColor getRGBColorValue() throws DOMException {
        throw new UnsupportedOperationException("Unsupported operation: getRGBColorValue");
    }

    @Override
    public Rect getRectValue() throws DOMException {
        throw new UnsupportedOperationException("Unsupported operation: getRectValue");
    }

    @Nullable
    @Override
    public String getStringValue() throws DOMException {
        return _stringValue;
    }

    @Override
    public void setFloatValue(short unitType, float floatValue) throws DOMException {
        throw new UnsupportedOperationException("Unsupported operation: setFloatValue");
    }

    @Override
    public void setStringValue(short stringType, String stringValue) throws DOMException {
        throw new UnsupportedOperationException("Unsupported operation: setStringValue");
    }

    @Override
    public String getCssText() {
        return _cssText;
    }

    @Override
    public short getCssValueType() {
        return _cssValueType;
    }

    @Override
    public void setCssText(String cssText) throws DOMException {
        throw new UnsupportedOperationException("Unsupported operation: setCssText");
    }

    @Nullable
    public FSColor getFSColor() {
        return _FSColor;
    }

    @Nullable
    public IdentValue getIdentValue() {
        return _identValue;
    }

    public void setIdentValue(IdentValue identValue) {
        _identValue = identValue;
    }

    public Type getPropertyValueType() {
        return _propertyValueType;
    }

    @Nullable
    public Token getOperator() {
        return _operator;
    }

    public String[] getStringArrayValue() {
        return ArrayUtil.cloneOrEmpty(_stringArrayValue);
    }

    @Override
    public String toString() {
        return _cssText;
    }

    public <T> List<T> getValues() {
        //noinspection unchecked
        return unmodifiableList((List<T>) _values);
    }

    @Nullable
    public FSFunction getFunction() {
        return _function;
    }

    @CheckReturnValue
    public String getFingerprint() {
        return switch (getPropertyValueType()) {
            case VALUE_TYPE_IDENT -> {
                if (_identValue == null) {
                    _identValue = IdentValue.getByIdentString(getStringValue());
                }
                yield "I" + _identValue.FS_ID;
            }
            default -> getCssText();
        };
    }
}
