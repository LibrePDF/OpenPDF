package org.openpdf.css.style;

import org.openpdf.css.constants.CSSName;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.style.derived.LengthValue;
import org.openpdf.css.style.derived.ListValue;

import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PERCENTAGE;
import static org.openpdf.css.constants.CSSName.BORDER_BOTTOM_LEFT_RADIUS;
import static org.openpdf.css.constants.CSSName.BORDER_BOTTOM_RIGHT_RADIUS;
import static org.openpdf.css.constants.CSSName.BORDER_TOP_LEFT_RADIUS;
import static org.openpdf.css.constants.CSSName.BORDER_TOP_RIGHT_RADIUS;

public class BorderRadiusCorner {

    public static final BorderRadiusCorner UNDEFINED = new BorderRadiusCorner(0, 0);

    private record Length(float value, boolean percent) {}

    private final Length _left;
    private final Length _right;

    // TODO: FIXME the way values are passed from the CSS to the border corners really sucks, improve it

    public BorderRadiusCorner(float left, float right) {
        this._left = new Length(left, false);
        this._right = new Length(right, false);
    }

    public BorderRadiusCorner(CSSName fromVal, CalculatedStyle style, CssContext ctx) {
        FSDerivedValue value = style.valueByName(fromVal);
        if (value instanceof ListValue lValues) {
            PropertyValue first = (PropertyValue) lValues.getValues().get(0);
            PropertyValue second = lValues.getValues().size() > 1 ? (PropertyValue) lValues.getValues().get(1) : first;

            if (fromVal.equals(BORDER_TOP_LEFT_RADIUS) || fromVal.equals(BORDER_BOTTOM_RIGHT_RADIUS)) {
                _right = calculate(fromVal, style, first, ctx);
                _left = calculate(fromVal, style, second, ctx);
            } else if (fromVal.equals(BORDER_TOP_RIGHT_RADIUS) || fromVal.equals(BORDER_BOTTOM_LEFT_RADIUS)) {
                _left = calculate(fromVal, style, first, ctx);
                _right = calculate(fromVal, style, second, ctx);
            } else {
                throw new IllegalArgumentException("Unknown border radius type: " + fromVal);
            }
        } else if (value instanceof LengthValue lv) {

            if (lv.getStringValue().contains("%")) {
                _left = _right = new Length(value.asFloat() / 100.0f, true);
            } else {
                _left = _right = new Length((int) lv.getFloatProportionalTo(fromVal, 0, ctx), false);
            }
        } else {
            throw new IllegalArgumentException("Unknown length value: " + value);
        }
    }

    private Length calculate(CSSName fromVal, CalculatedStyle style, PropertyValue value, CssContext ctx) {
        return switch (value.getPrimitiveType()) {
            case CSS_PERCENTAGE -> new Length(value.getFloatValue() / 100.0f, true);
            default -> new Length(LengthValue.calcFloatProportionalValue(
                style,
                fromVal,
                value.getCssText(),
                value.getFloatValue(),
                value.getPrimitiveType(),
                0,
                ctx),
                false
            );
        };
    }

    public boolean hasRadius() {
        return _left.value() > 0 || _right.value() > 0;
    }

    public float getMaxLeft(float max) {
        if (_left.percent())
            return max * _left.value();
        return Math.min(_left.value(), max);
    }

    public float getMaxRight(float max) {
        if (_right.percent())
            return max * _right.value();
        return Math.min(_right.value(), max);
    }


    public float left() {
        return _left.value();
    }

    public float right() {
        return _right.value();
    }
}
