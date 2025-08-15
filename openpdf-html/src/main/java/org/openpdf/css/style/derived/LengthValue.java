/*
 * Copyright (c) 2005 Patrick Wright
 * Copyright (c) 2007 Wisconsin Court System
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
package org.openpdf.css.style.derived;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.ValueConstants;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.CssContext;
import org.openpdf.css.style.DerivedValue;
import org.openpdf.css.value.FontSpecification;
import org.openpdf.util.XRLog;

import java.util.logging.Level;

public class LengthValue extends DerivedValue {
    private final static int MM__PER__CM = 10;
    private final static float CM__PER__IN = 2.54F;
    private final static float PT__PER__IN = 1f / 72f;
    private final static float PC__PER__PT = 12;

    /**
     * The specified length value, as a float; pulled from the CSS text
     */
    private final float _lengthAsFloat;

    private final CalculatedStyle _style;

    /**
     * The specified primitive SAC data type given for this length, from the CSS text
     */
    private final short _lengthPrimitiveType;

    public LengthValue(CalculatedStyle style, CSSName name, PropertyValue value) {
        super(name, value.getPrimitiveType(), value.getCssText(), value.getCssText());

        _style = style;
        _lengthAsFloat = value.getFloatValue();
        _lengthPrimitiveType = value.getPrimitiveType();
    }

    @Override
    public float asFloat() {
        return _lengthAsFloat;
    }

    /**
     * Computes a relative unit (e.g. percentage) as an absolute value, using
     * the input value. Used for such properties whose parent value cannot be
     * known before layout/render
     *
     * @param cssName   Name of the property
     * @return the absolute value or computed absolute value
     */
    @Override
    public float getFloatProportionalTo(CSSName cssName,
                                        float baseValue,
                                        CssContext ctx) {
        return calcFloatProportionalValue(getStyle(),
                cssName,
                getStringValue(),
                _lengthAsFloat,
                _lengthPrimitiveType,
                baseValue,
                ctx);
    }

    @Override
    public boolean hasAbsoluteUnit() {
        return ValueConstants.isAbsoluteUnit(getCssSacUnitType());
    }

    @Override
    public boolean isDependentOnFontSize() {
        return _lengthPrimitiveType == CSSPrimitiveValue.CSS_EXS ||
                    _lengthPrimitiveType == CSSPrimitiveValue.CSS_EMS;
    }

    public static float calcFloatProportionalValue(CalculatedStyle style,
                                                      CSSName cssName,
                                                      String stringValue,
                                                      float relVal,
                                                      short primitiveType,
                                                      float baseValue,
                                                      CssContext ctx) {

        float absVal = Float.MIN_VALUE;

        // NOTE: we used to cache absolute values, but have removed that to see if it
        // really makes a difference, since the calculations are so simple. In any case, for DPI-relative
        // values we shouldn't be caching, unless we also check if the DPI is changed, which
        // would seem to obviate the advantage of caching anyway.
        switch (primitiveType) {
            case CSSPrimitiveValue.CSS_PX:
                absVal = relVal * ctx.getDotsPerPixel();
                break;
            case CSSPrimitiveValue.CSS_IN:
                absVal = (((relVal * CM__PER__IN) * MM__PER__CM) / ctx.getMmPerDot());
                break;
            case CSSPrimitiveValue.CSS_CM:
                absVal = ((relVal * MM__PER__CM) / ctx.getMmPerDot());
                break;
            case CSSPrimitiveValue.CSS_MM:
                absVal = relVal / ctx.getMmPerDot();
                break;
            case CSSPrimitiveValue.CSS_PT:
                absVal = (((relVal * PT__PER__IN) * CM__PER__IN) * MM__PER__CM) / ctx.getMmPerDot();
                break;
            case CSSPrimitiveValue.CSS_PC:
                absVal = ((((relVal * PC__PER__PT) * PT__PER__IN) * CM__PER__IN) * MM__PER__CM) / ctx.getMmPerDot();
                break;
            case CSSPrimitiveValue.CSS_EMS:
                // EM is equal to font-size of element on which it is used
                // The exception is when ?em? occurs in the value of
                // the ?font-size? property itself, in which case it refers
                // to the calculated font size of the parent element
                // http://www.w3.org/TR/CSS21/fonts.html#font-size-props
                if (cssName == CSSName.FONT_SIZE) {
                    FontSpecification parentFont = style.getParent().getFont(ctx);
                    //font size and FontSize2D should be identical
                    absVal = relVal * parentFont.size;//ctx.getFontSize2D(parentFont);
                } else {
                    absVal = relVal * style.getFont(ctx).size;//ctx.getFontSize2D(style.getFont(ctx));
                }

                break;
            case CSSPrimitiveValue.CSS_EXS:
                // To convert EMS to pixels, we need the height of the lowercase 'Xx' character in the current
                // element...
                // to the font size of the parent element (spec: 4.3.2)
                float xHeight;
                if (cssName == CSSName.FONT_SIZE) {
                    FontSpecification parentFont = style.getParent().getFont(ctx);
                    xHeight = ctx.getXHeight(parentFont);
                } else {
                    FontSpecification font = style.getFont(ctx);
                    xHeight = ctx.getXHeight(font);
                }
                absVal = relVal * xHeight;

                break;
            case CSSPrimitiveValue.CSS_PERCENTAGE:
                // percentage depends on the property this value belongs to
                if (cssName == CSSName.VERTICAL_ALIGN) {
                    baseValue = style.getParent().getLineHeight(ctx);
                } else if (cssName == CSSName.FONT_SIZE) {
                    // same as with EM
                    FontSpecification parentFont = style.getParent().getFont(ctx);
                    baseValue = ctx.getFontSize2D(parentFont);
                } else if (cssName == CSSName.LINE_HEIGHT) {
                    FontSpecification font = style.getFont(ctx);
                    baseValue = ctx.getFontSize2D(font);
                }
                absVal = (relVal / 100F) * baseValue;

                break;
            case CSSPrimitiveValue.CSS_NUMBER:
                absVal = relVal;
                break;
            default:
                // nothing to do, we only convert those listed above
                XRLog.cascade(Level.SEVERE,
                        "Asked to convert " + cssName + " from relative to absolute, " +
                        " don't recognize the datatype " +
                        "'" + ValueConstants.stringForSACPrimitiveType(primitiveType) + "' "
                        + primitiveType + "(" + stringValue + ")");
        }

        if (XRLog.isLoggingEnabled()) {
            if (cssName == CSSName.FONT_SIZE) {
                XRLog.cascade(Level.FINEST, cssName + ", relative= " +
                        relVal + " (" + stringValue + "), absolute= "
                        + absVal);
            } else {
                XRLog.cascade(Level.FINEST, cssName + ", relative= " +
                        relVal + " (" + stringValue + "), absolute= "
                        + absVal + " using base=" + baseValue);
            }
        }

        double d = Math.round((double) absVal);
        absVal = (float) d;
        return absVal;
    }

    private CalculatedStyle getStyle() {
        return _style;
    }
}
