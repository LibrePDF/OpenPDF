package org.openpdf.css.style.derived;

import com.google.errorprone.annotations.CheckReturnValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.CssContext;

/**
 * Represents a set of CSS properties that together define
 * some rectangular area, and per-side thickness.
 */
public class RectPropertySet {
    public static final RectPropertySet ALL_ZEROS = new RectPropertySet(0, 0, 0, 0);

    private float _top;
    private float _right;
    private float _bottom;
    private float _left;

    public RectPropertySet(
            float top,
            float right,
            float bottom,
            float left
    ) {
        this._top = top;
        this._right = right;
        this._bottom = bottom;
        this._left = left;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static RectPropertySet newInstance(
            CalculatedStyle style,
            CSSName.CSSSideProperties sideProperties,
            float cbWidth,
            CssContext ctx
    ) {
        // HACK isLengthValue is part of margin auto hack
        return new RectPropertySet(
                ! style.isLengthOrNumber(sideProperties.top()) ? 0 : style.getFloatPropertyProportionalHeight(sideProperties.top(), cbWidth, ctx),
                ! style.isLengthOrNumber(sideProperties.right()) ? 0 : style.getFloatPropertyProportionalWidth(sideProperties.right(), cbWidth, ctx),
                ! style.isLengthOrNumber(sideProperties.bottom()) ? 0 : style.getFloatPropertyProportionalHeight(sideProperties.bottom(), cbWidth, ctx),
                ! style.isLengthOrNumber(sideProperties.left()) ? 0 : style.getFloatPropertyProportionalWidth(sideProperties.left(), cbWidth, ctx)
        );
    }

    @Override
    public String toString() {
        return "RectPropertySet[top=%s,right=%s,bottom=%s,left=%s]".formatted(_top, _right, _bottom, _left);
    }

    public float top() {
        return _top;
    }

    public float right() {
        return _right;
    }

    public float bottom() {
        return _bottom;
    }

    public float left() {
        return _left;
    }

    public float getLeftRightDiff() {
        return _left - _right;
    }

    public float height() {
        return _top + _bottom;
    }

    public float width() {
        return _left + _right;
    }

    public void setTop(float _top) {
        this._top = _top;
    }

    public void setRight(float _right) {
        this._right = _right;
    }

    public void setBottom(float _bottom) {
        this._bottom = _bottom;
    }

    public void setLeft(float _left) {
        this._left = _left;
    }

    public RectPropertySet copyOf() {
        return new RectPropertySet(_top, _right, _bottom, _left);
    }

    public boolean isAllZeros() {
        return _top == 0.0f && _right == 0.0f && _bottom == 0.0f && _left == 0.0f;
    }

    public boolean hasNegativeValues() {
        return _top < 0 || _right < 0 || _bottom < 0 || _left < 0;
    }

    @CheckReturnValue
    public RectPropertySet resetNegativeValues() {
        return new RectPropertySet(Math.max(0, top()), Math.max(0, right()), Math.max(0, bottom()), Math.max(0, left()));
    }
}
