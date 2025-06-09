/*
 * CalculatedStyle.java
 * Copyright (c) 2004, 2005 Patrick Wright, Torbjoern Gannholm
 * Copyright (c) 2006 Wisconsin Court System
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
 *
 */
package org.openpdf.css.style;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.newmatch.CascadedStyle;
import org.openpdf.css.parser.CounterData;
import org.openpdf.css.parser.FSColor;
import org.openpdf.css.parser.FSFunction;
import org.openpdf.css.parser.FSRGBColor;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.parser.property.PrimitivePropertyBuilders;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.style.derived.BorderPropertySet;
import org.openpdf.css.style.derived.DerivedValueFactory;
import org.openpdf.css.style.derived.FSLinearGradient;
import org.openpdf.css.style.derived.FunctionValue;
import org.openpdf.css.style.derived.LengthValue;
import org.openpdf.css.style.derived.ListValue;
import org.openpdf.css.style.derived.NumberValue;
import org.openpdf.css.style.derived.RectPropertySet;
import org.openpdf.css.value.FontSpecification;
import org.openpdf.render.FSFont;
import org.openpdf.render.FSFontMetrics;
import org.openpdf.util.GeneralUtil;
import org.openpdf.util.XRLog;
import org.openpdf.util.XRRuntimeException;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static org.openpdf.css.constants.CSSName.PADDING_SIDE_PROPERTIES;
import static org.openpdf.css.style.CssKnowledge.BLOCK_EQUIVALENTS;
import static org.openpdf.css.style.CssKnowledge.BORDERS_NOT_ALLOWED;
import static org.openpdf.css.style.CssKnowledge.LAID_OUT_IN_INLINE_CONTEXT;
import static org.openpdf.css.style.CssKnowledge.MARGINS_NOT_ALLOWED;
import static org.openpdf.css.style.CssKnowledge.MAY_HAVE_FIRST_LETTER;
import static org.openpdf.css.style.CssKnowledge.MAY_HAVE_FIRST_LINE;
import static org.openpdf.css.style.CssKnowledge.OVERFLOW_APPLICABLE;
import static org.openpdf.css.style.CssKnowledge.TABLE_SECTIONS;
import static org.openpdf.css.style.CssKnowledge.UNDER_TABLE_LAYOUT;
import static org.openpdf.css.style.Length.LengthType.FIXED;
import static org.openpdf.css.style.Length.LengthType.PERCENT;

/**
 * A set of properties that apply to a single Element, derived from all matched
 * properties following the rules for CSS cascade, inheritance, importance,
 * specificity and sequence. A derived style is just like a style but
 * (presumably) has additional information that allows relative properties to be
 * assigned values, e.g. font attributes. Property values are fully resolved
 * when this style is created. A property retrieved by name should always have
 * only one value in this class (e.g. one-one map). Any methods to retrieve
 * property values from an instance of this class require a valid {@link
 * CssContext} be given to it, for some cases of property
 * resolution. Generally, a programmer will not use this class directly, but
 * will retrieve properties using a {@link org.openpdf.context.StyleReference}
 * implementation.
 *
 * @author Torbjoern Gannholm
 * @author Patrick Wright
 */
public class CalculatedStyle {
    /**
     * The parent-style we inherit from
     */
    @Nullable
    private final CalculatedStyle _parent;

    @Nullable
    private BorderPropertySet _border;
    @Nullable
    private RectPropertySet _margin;
    @Nullable
    private RectPropertySet _padding;

    private float _lineHeight;
    private boolean _lineHeightResolved;

    @Nullable
    private FSFont _FSFont;
    @Nullable
    private FSFontMetrics _FSFontMetrics;

    private boolean _marginsAllowed = true;
    private boolean _paddingAllowed = true;
    private boolean _bordersAllowed = true;

    @Nullable
    private BackgroundSize _backgroundSize;

    /**
     * Cache child styles of this style that have the same cascaded properties
     */
    private final Map<String, CalculatedStyle> _childCache = new ConcurrentHashMap<>();

    /**
     * Our main array of property values defined in this style, keyed
     * by the CSSName assigned ID.
     */
    @Nullable
    private final FSDerivedValue[] _derivedValuesById;

    /**
     * The derived Font for this style
     */
    @Nullable
    private FontSpecification _font;

    private CalculatedStyle(@Nullable CalculatedStyle parent) {
        _derivedValuesById = new FSDerivedValue[CSSName.countCSSNames()];
        _parent = parent;
    }

    /**
     * Default constructor; as the instance is immutable after use, don't use
     * this for class instantiation externally.
     */
    protected CalculatedStyle() {
        this(null);
    }

    /**
     * Constructor for the CalculatedStyle object. To get a derived style, use
     * the Styler objects getDerivedStyle which will cache styles
     */
    private CalculatedStyle(CalculatedStyle parent, CascadedStyle matched) {
        this(parent);

        derive(matched);

        IdentValue display = getDisplay();
        _paddingAllowed = checkPaddingAllowed(display);
        _marginsAllowed = checkMarginsAllowed(display);
        _bordersAllowed = checkBordersAllowed(display);
    }

    private boolean checkPaddingAllowed(IdentValue display) {
        return display != IdentValue.TABLE_HEADER_GROUP && display != IdentValue.TABLE_ROW_GROUP &&
                display != IdentValue.TABLE_FOOTER_GROUP && display != IdentValue.TABLE_ROW &&
                (!isTable(display) || !isCollapseBorders());
    }

    private static boolean isTable(IdentValue display) {
        return display == IdentValue.TABLE || display == IdentValue.INLINE_TABLE;
    }

    private static boolean checkMarginsAllowed(IdentValue display) {
        return !MARGINS_NOT_ALLOWED.contains(display);
    }

    private static boolean checkBordersAllowed(IdentValue display) {
        return !BORDERS_NOT_ALLOWED.contains(display);
    }

    /**
     * derives a child style from this style.
     * <p>
     * depends on the ability to return the identical CascadedStyle each time a child style is needed
     *
     * @param matched the CascadedStyle to apply
     * @return The derived child style
     */
    @NonNull
    @CheckReturnValue
    public CalculatedStyle deriveStyle(CascadedStyle matched) {
        String fingerprint = matched.getFingerprint();
        return _childCache.computeIfAbsent(fingerprint, (key) -> new CalculatedStyle(this, matched));
    }

    @Nullable
    public CalculatedStyle getParent() {
        return _parent;
    }

    @Override
    public String toString() {
        return genStyleKey();
    }

    public FSColor asColor(CSSName cssName) {
        FSDerivedValue prop = valueByName(cssName);
        return prop == IdentValue.TRANSPARENT ? FSRGBColor.TRANSPARENT : prop.asColor();
    }

    public float asFloat(CSSName cssName) {
        return valueByName(cssName).asFloat();
    }

    public String asString(CSSName cssName) {
        return valueByName(cssName).asString();
    }

    public String[] asStringArray(CSSName cssName) {
        return valueByName(cssName).asStringArray();
    }

    public void setDefaultValue(CSSName cssName, FSDerivedValue fsDerivedValue) {
        if (_derivedValuesById[cssName.FS_ID] == null) {
            _derivedValuesById[cssName.FS_ID] = fsDerivedValue;
        }
    }

    // TODO: doc
    public boolean hasAbsoluteUnit(CSSName cssName) {
        boolean isAbs;
        try {
            isAbs = valueByName(cssName).hasAbsoluteUnit();
        } catch (Exception e) {
            XRLog.layout(Level.WARNING, "Property " + cssName + " has an assignment we don't understand, " +
                    "and can't tell if it's an absolute unit or not. Assuming it is not. Exception was: " +
                    e.getMessage());
            isAbs = false;
        }
        return isAbs;
    }

    /**
     * Gets the ident attribute of the CalculatedStyle object
     */
    public boolean isIdent(CSSName cssName, IdentValue val) {
        return valueByName(cssName) == val;
    }

    /**
     * Gets the ident attribute of the CalculatedStyle object
     */
    public IdentValue getIdent(CSSName cssName) {
        return valueByName(cssName).asIdentValue();
    }

    /**
     * Convenience property accessor; returns a Opacity
     * Uses the actual value (computed actual value) for this
     * element.
     *
     * @return The opacity value
     */
    public float getOpacity() {
    	float opacity = asFloat(CSSName.OPACITY);

        for (CalculatedStyle parentStyle = getParent(); parentStyle != null; parentStyle = parentStyle.getParent()) {
    		opacity = opacity * parentStyle.asFloat(CSSName.OPACITY);
    	}

    	return opacity;
    }

    public IdentValue getDisplay() {
        return getIdent(CSSName.DISPLAY);
    }

    /**
     * Convenience property accessor; returns a Color initialized with the
     * foreground color Uses the actual value (computed actual value) for this
     * element.
     *
     * @return The color value
     */
    public FSColor getColor() {
        return asColor(CSSName.COLOR);
    }

    /**
     * Convenience property accessor; returns a Color initialized with the
     * background color value; Uses the actual value (computed actual value) for
     * this element.
     *
     * @return The backgroundColor value
     */
    @Nullable
    public FSColor getBackgroundColor() {
        FSDerivedValue prop = valueByName(CSSName.BACKGROUND_COLOR);
        if (prop == IdentValue.TRANSPARENT) {
            return null;
        } else {
            return asColor(CSSName.BACKGROUND_COLOR);
        }
    }

    public BackgroundSize getBackgroundSize() {
        if (_backgroundSize == null) {
            _backgroundSize = createBackgroundSize();
        }

        return _backgroundSize;
    }

    private BackgroundSize createBackgroundSize() {
        FSDerivedValue value = valueByName(CSSName.BACKGROUND_SIZE);
        if (value instanceof IdentValue ident) {
            if (ident == IdentValue.COVER) {
                return new BackgroundSize(false, true, false);
            } else if (ident == IdentValue.CONTAIN) {
                return new BackgroundSize(true, false, false);
            }
        } else {
            ListValue valueList = (ListValue)value;
            List<PropertyValue> values = valueList.getValues();
            boolean firstAuto = values.get(0).getIdentValue() == IdentValue.AUTO;
            boolean secondAuto = values.get(1).getIdentValue() == IdentValue.AUTO;

            if (firstAuto && secondAuto) {
                return new BackgroundSize(false, false, true);
            } else {
                return new BackgroundSize(values.get(0), values.get(1));
            }
        }

        throw new RuntimeException("Cannot created background size for " + value);
    }

    public BackgroundPosition getBackgroundPosition() {
        ListValue result = (ListValue) valueByName(CSSName.BACKGROUND_POSITION);
        List<PropertyValue> values = result.getValues();

        return new BackgroundPosition(values.get(0), values.get(1));
    }

    @Nullable
    public List<CounterData> getCounterReset() {
        FSDerivedValue value = valueByName(CSSName.COUNTER_RESET);

        if (value == IdentValue.NONE) {
            return null;
        } else {
            return ((ListValue) value).getValues();
        }
    }

    @Nullable
    public List<CounterData> getCounterIncrement() {
        FSDerivedValue value = valueByName(CSSName.COUNTER_INCREMENT);

        if (value == IdentValue.NONE) {
            return null;
        } else {
            return ((ListValue) value).getValues();
        }
    }

    public BorderPropertySet getBorder(@Nullable CssContext ctx) {
        if (! _bordersAllowed) {
            return BorderPropertySet.EMPTY_BORDER;
        } else {
            return getBorderProperty(this, ctx);
        }
    }

    public FontSpecification getFont(CssContext ctx) {
        if (_font == null) {
            _font = new FontSpecification();

            _font.families = valueByName(CSSName.FONT_FAMILY).asStringArray();

            FSDerivedValue fontSize = valueByName(CSSName.FONT_SIZE);
            if (fontSize instanceof IdentValue) {
                PropertyValue replacement;
                IdentValue resolved = resolveAbsoluteFontSize();
                if (resolved != null) {
                    replacement = FontSizeHelper.resolveAbsoluteFontSize(resolved, _font.families);
                } else {
                    replacement = FontSizeHelper.getDefaultRelativeFontSize((IdentValue) fontSize);
                }
                _font.size = LengthValue.calcFloatProportionalValue(
                        this, CSSName.FONT_SIZE, replacement.getCssText(),
                        replacement.getFloatValue(), replacement.getPrimitiveType(), 0, ctx);
            } else {
                _font.size = getFloatPropertyProportionalTo(CSSName.FONT_SIZE, 0, ctx);
            }

            _font.fontWeight = getIdent(CSSName.FONT_WEIGHT);

            _font.fontStyle = getIdent(CSSName.FONT_STYLE);
            _font.variant = getIdent(CSSName.FONT_VARIANT);
        }
        return _font;
    }

    @Nullable
    public FontSpecification getFontSpecification() {
    return _font;
    }

    @Nullable
    private IdentValue resolveAbsoluteFontSize() {
        FSDerivedValue fontSize = valueByName(CSSName.FONT_SIZE);
        if (!(fontSize instanceof IdentValue fontSizeIdent)) {
            return null;
        }
        if (PrimitivePropertyBuilders.ABSOLUTE_FONT_SIZES.get(fontSizeIdent.FS_ID)) {
            return fontSizeIdent;
        }

        IdentValue parent = getParent().resolveAbsoluteFontSize();
        if (parent != null) {
            if (fontSizeIdent == IdentValue.SMALLER) {
                return FontSizeHelper.getNextSmaller(parent);
            } else if (fontSize == IdentValue.LARGER) {
                return FontSizeHelper.getNextLarger(parent);
            }
        }

        return null;
    }

    public int getIntPropertyProportionalTo(CSSName cssName, float baseValue, CssContext ctx) {
        return (int) getFloatPropertyProportionalTo(cssName, baseValue, ctx);
    }

    public float getFloatPropertyProportionalTo(CSSName cssName, float baseValue, CssContext ctx) {
        return valueByName(cssName).getFloatProportionalTo(cssName, baseValue, ctx);
    }

    public float getFloatPropertyProportionalWidth(CSSName cssName, float parentWidth, CssContext ctx) {
        return valueByName(cssName).getFloatProportionalTo(cssName, parentWidth, ctx);
    }

    public float getFloatPropertyProportionalHeight(CSSName cssName, float parentHeight, CssContext ctx) {
        return valueByName(cssName).getFloatProportionalTo(cssName, parentHeight, ctx);
    }

    public float getLineHeight(CssContext ctx) {
        if (! _lineHeightResolved) {
            if (isIdent(CSSName.LINE_HEIGHT, IdentValue.NORMAL)) {
                float lineHeight1 = getFont(ctx).size * 1.1f;
                // Make sure rasterized characters will (probably) fit inside
                // the line box
                FSFontMetrics metrics = getFSFontMetrics(ctx);
                float lineHeight2 = (float)Math.ceil(metrics.getDescent() + metrics.getAscent());
                _lineHeight = Math.max(lineHeight1, lineHeight2);
            } else if (isLength(CSSName.LINE_HEIGHT)) {
                //could be more elegant, I suppose
                _lineHeight = getFloatPropertyProportionalHeight(CSSName.LINE_HEIGHT, 0, ctx);
            } else {
                //must be a number
                _lineHeight = getFont(ctx).size * valueByName(CSSName.LINE_HEIGHT).asFloat();
            }
            _lineHeightResolved = true;
        }
        return _lineHeight;
    }

    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided margin width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @return The marginWidth value
     */
    public RectPropertySet getMarginRect(float cbWidth, CssContext ctx) {
        return getMarginRect(cbWidth, ctx, true);
    }

    public RectPropertySet getMarginRect(float cbWidth, CssContext ctx, boolean useCache) {
        if (! _marginsAllowed) {
            return RectPropertySet.ALL_ZEROS;
        } else {
            return getMarginProperty(
                    this, cbWidth, ctx, useCache);
        }
    }

    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided padding width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @return The paddingWidth value
     */
    public RectPropertySet getPaddingRect(float cbWidth, CssContext ctx) {
        if (! _paddingAllowed) {
            return RectPropertySet.ALL_ZEROS;
        } else {
            return getPaddingProperty(this, cbWidth, ctx);
        }
    }

    public String getStringProperty(CSSName cssName) {
        return valueByName(cssName).asString();
    }

    public boolean isLength(CSSName cssName) {
        FSDerivedValue val = valueByName(cssName);
        return val instanceof LengthValue;
    }

    public boolean isLengthOrNumber(CSSName cssName) {
        FSDerivedValue val = valueByName(cssName);
        return val instanceof NumberValue || val instanceof LengthValue;
    }

    /**
     * Returns a {@link FSDerivedValue} by name. Because we are a derived
     * style, the property will already be resolved at this point.
     *
     * @param cssName The CSS property name, e.g. "font-family"
     */
    public FSDerivedValue valueByName(CSSName cssName) {
        FSDerivedValue val = _derivedValuesById[cssName.FS_ID];

        boolean needInitialValue = val == IdentValue.FS_INITIAL_VALUE;

        // but the property may not be defined for this Element
        if (val == null || needInitialValue) {
            // if it is inheritable (like color) and we are not root, ask our parent
            // for the value
            if (!needInitialValue && CSSName.propertyInherits(cssName)
                    && _parent != null
                    //
                    && (val = _parent.valueByName(cssName)) != null) {
                // Do nothing, val is already set
            } else {
                // otherwise, use the initial value (defined by the CSS2 Spec)
                String initialValue = CSSName.initialValue(cssName);
                if (initialValue == null) {
                    throw new XRRuntimeException("Property '" + cssName + "' has no initial values assigned. " +
                            "Check CSSName declarations.");
                }
                if (initialValue.charAt(0) == '=') {
                    CSSName ref = CSSName.getByPropertyName(initialValue.substring(1));
                    val = valueByName(ref);
                } else {
                    val = cssName.initialDerivedValue();
                }
            }
            _derivedValuesById[cssName.FS_ID] = val;
        }
        return val;
    }

    /**
     * <p>
     * Implements cascade/inherit/important logic. This should result in the
     * element for this style having a value for *each and every* (visual)
     * property in the CSS2 spec. The implementation is based on the notion that
     * the matched styles are given to us in a perfectly sorted order, such that
     * properties appearing later in the rule-set always override properties
     * appearing earlier. It also assumes that all properties in the CSS2 spec
     * are defined somewhere across all the matched styles; for example, that
     * the full-property set is given in the user-agent CSS that is always
     * loaded with styles. The current implementation makes no attempt to check
     * either of these assumptions. When this method exits, the derived property
     * list for this class will be populated with the properties defined for
     * this element, properly cascaded.</p>
     */
    private void derive(CascadedStyle matched) {
        Iterator<PropertyDeclaration> mProps = matched.getCascadedPropertyDeclarations();
        while (mProps.hasNext()) {
            PropertyDeclaration pd = mProps.next();
            FSDerivedValue val = deriveValue(pd.getCSSName(), pd.getValue());
            _derivedValuesById[pd.getCSSName().FS_ID] = val;
        }
    }

    private FSDerivedValue deriveValue(CSSName cssName, org.w3c.dom.css.CSSPrimitiveValue value) {
        return DerivedValueFactory.newDerivedValue(this, cssName, (PropertyValue) value);
    }

    private String genStyleKey() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < _derivedValuesById.length; i++) {
            CSSName name = CSSName.getByID(i);
            FSDerivedValue val = _derivedValuesById[i];
            if (val != null) {
                sb.append(name);
            } else {
                sb.append("(no prop assigned in this pos)");
            }
            sb.append("|\n");
        }
        return sb.toString();

    }

    public RectPropertySet getCachedPadding() {
        if (_padding == null) {
            throw new XRRuntimeException("No padding property cached yet; should have called getPropertyRect() at least once before.");
        } else {
            return _padding;
        }
    }

    private static RectPropertySet getPaddingProperty(CalculatedStyle style,
                                                      float cbWidth,
                                                      CssContext ctx) {
        if (style._padding == null) {
            style._padding = newRectInstance(style, PADDING_SIDE_PROPERTIES, cbWidth, ctx)
                    .resetNegativeValues();
        }

        return style._padding;
    }

    private static RectPropertySet getMarginProperty(CalculatedStyle style,
                                                     float cbWidth,
                                                     CssContext ctx,
                                                     boolean useCache) {
        if (! useCache) {
            return newRectInstance(style, CSSName.MARGIN_SIDE_PROPERTIES, cbWidth, ctx);
        } else {
            if (style._margin == null) {
                style._margin = newRectInstance(style, CSSName.MARGIN_SIDE_PROPERTIES, cbWidth, ctx);
            }

            return style._margin;
        }
    }

    private static RectPropertySet newRectInstance(CalculatedStyle style,
                                                   CSSName.CSSSideProperties sides,
                                                   float cbWidth,
                                                   CssContext ctx) {
        RectPropertySet rect = RectPropertySet.newInstance(style, sides, cbWidth, ctx);

        if (rect.isAllZeros()) {
            rect = RectPropertySet.ALL_ZEROS;
        }
        return rect;
    }

    private static BorderPropertySet getBorderProperty(CalculatedStyle style,
                                                       @Nullable CssContext ctx) {
        if (style._border == null) {
            style._border = BorderPropertySet.newInstance(style, ctx).resetNegativeValues();
        }
        return style._border;
    }

    public enum Edge {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
    }

    public int getMarginBorderPadding(CssContext cssCtx, int cbWidth, Edge edge) {
        BorderPropertySet border = getBorder(cssCtx);
        RectPropertySet margin = getMarginRect(cbWidth, cssCtx);
        RectPropertySet padding = getPaddingRect(cbWidth, cssCtx);

        return switch (edge) {
            case LEFT -> (int) (margin.left() + border.left() + padding.left());
            case RIGHT -> (int) (margin.right() + border.right() + padding.right());
            case TOP -> (int) (margin.top() + border.top() + padding.top());
            case BOTTOM -> (int) (margin.bottom() + border.bottom() + padding.bottom());
        };
    }

    public IdentValue getWhitespace() {
        return getIdent(CSSName.WHITE_SPACE);
    }

    public FSFont getFSFont(CssContext cssContext) {
        if (_FSFont == null) {
            _FSFont = cssContext.getFont(getFont(cssContext));
        }
        return _FSFont;
    }

    public FSFontMetrics getFSFontMetrics(CssContext c) {
        if (_FSFontMetrics == null) {
            _FSFontMetrics = c.getFSFontMetrics(getFSFont(c));
        }
        return _FSFontMetrics;
    }

    public IdentValue getWordWrap() {
        return getIdent(CSSName.WORD_WRAP);
    }
public IdentValue getWordBreak() {
        return getIdent(CSSName.WORD_BREAK);
    }
    public IdentValue getHyphens() {
        return getIdent(CSSName.HYPHENS);
    }

    public boolean isClearLeft() {
        IdentValue clear = getIdent(CSSName.CLEAR);
        return clear == IdentValue.LEFT || clear == IdentValue.BOTH;
    }

    public boolean isClearRight() {
        IdentValue clear = getIdent(CSSName.CLEAR);
        return clear == IdentValue.RIGHT || clear == IdentValue.BOTH;
    }

    public boolean isCleared() {
        return ! isIdent(CSSName.CLEAR, IdentValue.NONE);
    }

    public IdentValue getBackgroundRepeat() {
        return getIdent(CSSName.BACKGROUND_REPEAT);
    }

    public IdentValue getBackgroundAttachment() {
        return getIdent(CSSName.BACKGROUND_ATTACHMENT);
    }

    public boolean isFixedBackground() {
        return getIdent(CSSName.BACKGROUND_ATTACHMENT) == IdentValue.FIXED;
    }

    public boolean isInline() {
        return isIdent(CSSName.DISPLAY, IdentValue.INLINE) &&
                ! (isFloated() || isAbsolute() || isFixed() || isRunning());
    }

    public boolean isInlineBlock() {
        return isIdent(CSSName.DISPLAY, IdentValue.INLINE_BLOCK);
    }

    public boolean isTable() {
        return isIdent(CSSName.DISPLAY, IdentValue.TABLE);
    }

    public boolean isInlineTable() {
        return isIdent(CSSName.DISPLAY, IdentValue.INLINE_TABLE);
    }

    public boolean isUnderTableLayout() {
        return UNDER_TABLE_LAYOUT.contains(getDisplay());
    }

    public boolean isTableCell() {
        return isIdent(CSSName.DISPLAY, IdentValue.TABLE_CELL);
    }

    public boolean isTableSection() {
        return TABLE_SECTIONS.contains(getDisplay());
    }

    public boolean isTableCaption() {
        return isIdent(CSSName.DISPLAY, IdentValue.TABLE_CAPTION);
    }

    public boolean isTableHeader() {
        return isIdent(CSSName.DISPLAY, IdentValue.TABLE_HEADER_GROUP);
    }

    public boolean isTableFooter() {
        return isIdent(CSSName.DISPLAY, IdentValue.TABLE_FOOTER_GROUP);
    }

    public boolean isTableRow() {
        return isIdent(CSSName.DISPLAY, IdentValue.TABLE_ROW);
    }

    public boolean isDisplayNone() {
        return isIdent(CSSName.DISPLAY, IdentValue.NONE);
    }

    public boolean isSpecifiedAsBlock() {
        return isIdent(CSSName.DISPLAY, IdentValue.BLOCK);
    }

    public boolean isBlockEquivalent() {
        if (isFloated() || isAbsolute() || isFixed()) {
            return true;
        } else {
            return BLOCK_EQUIVALENTS.contains(getDisplay());
        }
    }

    public boolean isLaidOutInInlineContext() {
        if (isFloated() || isAbsolute() || isFixed() || isRunning()) {
            return true;
        } else {
            return LAID_OUT_IN_INLINE_CONTEXT.contains(getDisplay());
        }
    }

    public boolean isNeedAutoMarginResolution() {
        return ! (isAbsolute() || isFixed() || isFloated() || isInlineBlock());
    }

    public boolean isAbsolute() {
        return isIdent(CSSName.POSITION, IdentValue.ABSOLUTE);
    }

    public boolean isFixed() {
        return isIdent(CSSName.POSITION, IdentValue.FIXED);
    }

    public boolean isFloated() {
        if (isUnderTableLayout()) {
            return false;
        }
        IdentValue floatVal = getIdent(CSSName.FLOAT);
        return floatVal == IdentValue.LEFT || floatVal == IdentValue.RIGHT;
    }

    public boolean isFloatedLeft() {
        return isIdent(CSSName.FLOAT, IdentValue.LEFT);
    }

    public boolean isFloatedRight() {
        return isIdent(CSSName.FLOAT, IdentValue.RIGHT);
    }

    public boolean isRelative() {
        return isIdent(CSSName.POSITION, IdentValue.RELATIVE);
    }

    public boolean isPositionedOrFloated() {
        return isAbsolute() || isFixed() || isFloated() || isRelative();
    }

    public boolean isPositioned() {
        return isAbsolute() || isFixed() || isRelative();
    }

    public boolean isAutoWidth() {
        return isIdent(CSSName.WIDTH, IdentValue.AUTO);
    }

    public boolean isAbsoluteWidth() {
        return valueByName(CSSName.WIDTH).hasAbsoluteUnit();
    }

    public boolean isAutoHeight() {
        return isIdent(CSSName.HEIGHT, IdentValue.AUTO);
    }

    public boolean isAutoLeftMargin() {
        return isIdent(CSSName.MARGIN_LEFT, IdentValue.AUTO);
    }

    public boolean isAutoRightMargin() {
        return isIdent(CSSName.MARGIN_RIGHT, IdentValue.AUTO);
    }

    public boolean isAutoZIndex() {
        return isIdent(CSSName.Z_INDEX, IdentValue.AUTO);
    }

    public boolean establishesBFC() {
        FSDerivedValue value = valueByName(CSSName.POSITION);

        if (value instanceof FunctionValue) {  // running(header)
            return false;
        } else {
            IdentValue display = getDisplay();
            IdentValue position = (IdentValue)value;

            return isFloated() ||
                    position == IdentValue.ABSOLUTE || position == IdentValue.FIXED ||
                    display == IdentValue.INLINE_BLOCK || display == IdentValue.TABLE_CELL ||
                    ! isIdent(CSSName.OVERFLOW, IdentValue.VISIBLE);
        }
    }

    public boolean requiresLayer() {
        FSDerivedValue value = valueByName(CSSName.POSITION);

        if (value instanceof FunctionValue) {  // running(header)
            return false;
        } else {
            IdentValue position = getIdent(CSSName.POSITION);

            if (position == IdentValue.ABSOLUTE ||
                    position == IdentValue.RELATIVE || position == IdentValue.FIXED) {
                return true;
            }

            IdentValue overflow = getIdent(CSSName.OVERFLOW);
            return (overflow == IdentValue.SCROLL || overflow == IdentValue.AUTO) &&
                    isOverflowApplies();
        }
    }

    public boolean isRunning() {
        FSDerivedValue value = valueByName(CSSName.POSITION);
        return value instanceof FunctionValue;
    }

    public boolean isLinearGradient() {
        FSDerivedValue value = valueByName(CSSName.BACKGROUND_IMAGE);
        return value instanceof FunctionValue function &&
        		GeneralUtil.ciEquals(function.getFunction().getName(), "linear-gradient");
    }

    public FSLinearGradient getLinearGradient(final CssContext cssContext, final int w, final int h)
    {
        assert(isLinearGradient());

        final FunctionValue value = (FunctionValue) valueByName(CSSName.BACKGROUND_IMAGE);
        return new FSLinearGradient(value.getFunction(), this, w, h, cssContext);
    }

    public String getRunningName() {
        FunctionValue value = (FunctionValue)valueByName(CSSName.POSITION);
        FSFunction function = value.getFunction();
        PropertyValue param = function.getParameters().get(0);
        return param.getStringValue();
    }

    public boolean isOverflowApplies() {
        return OVERFLOW_APPLICABLE.contains(getDisplay());
    }

    public boolean isOverflowVisible() {
        return valueByName(CSSName.OVERFLOW) == IdentValue.VISIBLE;
    }

    public boolean isHorizontalBackgroundRepeat() {
        IdentValue value = getIdent(CSSName.BACKGROUND_REPEAT);
        return value == IdentValue.REPEAT_X || value == IdentValue.REPEAT;
    }

    public boolean isVerticalBackgroundRepeat() {
        IdentValue value = getIdent(CSSName.BACKGROUND_REPEAT);
        return value == IdentValue.REPEAT_Y || value == IdentValue.REPEAT;
    }

    public boolean isTopAuto() {
        return isIdent(CSSName.TOP, IdentValue.AUTO);
    }

    public boolean isBottomAuto() {
        return isIdent(CSSName.BOTTOM, IdentValue.AUTO);
    }

    public boolean isListItem() {
        return isIdent(CSSName.DISPLAY, IdentValue.LIST_ITEM);
    }

    public boolean isVisible() {
        return isIdent(CSSName.VISIBILITY, IdentValue.VISIBLE);
    }

    public boolean isForcePageBreakBefore() {
        IdentValue val = getIdent(CSSName.PAGE_BREAK_BEFORE);
        return val == IdentValue.ALWAYS || val == IdentValue.LEFT
                || val == IdentValue.RIGHT;
    }

    public boolean isForcePageBreakAfter() {
        IdentValue val = getIdent(CSSName.PAGE_BREAK_AFTER);
        return val == IdentValue.ALWAYS || val == IdentValue.LEFT
                || val == IdentValue.RIGHT;
    }

    public boolean isAvoidPageBreakInside() {
        return isIdent(CSSName.PAGE_BREAK_INSIDE, IdentValue.AVOID);
    }

    public CalculatedStyle createAnonymousStyle(IdentValue display) {
        return deriveStyle(CascadedStyle.createAnonymousStyle(display));
    }

    public boolean mayHaveFirstLine() {
        return MAY_HAVE_FIRST_LINE.contains(getDisplay());
    }

    public boolean mayHaveFirstLetter() {
        return MAY_HAVE_FIRST_LETTER.contains(getDisplay());
    }

    public boolean isNonFlowContent() {
        return isFloated() || isAbsolute() || isFixed() || isRunning();
    }

    public boolean isMayCollapseMarginsWithChildren() {
        return isIdent(CSSName.OVERFLOW, IdentValue.VISIBLE) &&
                ! (isFloated() || isAbsolute() || isFixed() || isInlineBlock());
    }

    public boolean isAbsFixedOrInlineBlockEquiv() {
        return isAbsolute() || isFixed() || isInlineBlock() || isInlineTable();
    }

    public boolean isMaxWidthNone() {
        return isIdent(CSSName.MAX_WIDTH, IdentValue.NONE);
    }

    public boolean isMaxHeightNone() {
        return isIdent(CSSName.MAX_HEIGHT, IdentValue.NONE);
    }

    public boolean isBorderBox() {
        return isIdent(CSSName.BOX_SIZING, IdentValue.BORDER_BOX);
    }

    public int getMinWidth(CssContext c, int cbWidth) {
        return (int) getFloatPropertyProportionalTo(CSSName.MIN_WIDTH, cbWidth, c);
    }

    public int getMaxWidth(CssContext c, int cbWidth) {
        return (int) getFloatPropertyProportionalTo(CSSName.MAX_WIDTH, cbWidth, c);
    }

    public int getMinHeight(CssContext c, int cbHeight) {
        return (int) getFloatPropertyProportionalTo(CSSName.MIN_HEIGHT, cbHeight, c);
    }

    public int getMaxHeight(CssContext c, int cbHeight) {
        return (int) getFloatPropertyProportionalTo(CSSName.MAX_HEIGHT, cbHeight, c);
    }

    public boolean isCollapseBorders() {
        return isIdent(CSSName.BORDER_COLLAPSE, IdentValue.COLLAPSE);
    }

    public int getBorderHSpacing(CssContext c) {
        return isCollapseBorders() ? 0 : (int) getFloatPropertyProportionalTo(CSSName.FS_BORDER_SPACING_HORIZONTAL, 0, c);
    }

    public int getBorderVSpacing(CssContext c) {
        return isCollapseBorders() ? 0 : (int) getFloatPropertyProportionalTo(CSSName.FS_BORDER_SPACING_VERTICAL, 0, c);
    }

    public int getRowSpan() {
        int result = (int) asFloat(CSSName.FS_ROWSPAN);
        return result > 0 ? result : 1;
    }

    public int getColSpan() {
        int result = (int) asFloat(CSSName.FS_COLSPAN);
        return result > 0 ? result : 1;
    }

    public Length asLength(CssContext c, CSSName cssName) {
        FSDerivedValue value = valueByName(cssName);
        if (value instanceof LengthValue || value instanceof NumberValue) {
            if (value.hasAbsoluteUnit()) {
                return new Length((int) value.getFloatProportionalTo(cssName, 0, c), FIXED);
            } else {
                return new Length((int) value.asFloat(), PERCENT);
            }
        }

        return Length.ZERO;
    }

    public boolean isShowEmptyCells() {
        return isCollapseBorders() || isIdent(CSSName.EMPTY_CELLS, IdentValue.SHOW);
    }

    public boolean isHasBackground() {
        return ! (isIdent(CSSName.BACKGROUND_COLOR, IdentValue.TRANSPARENT) &&
                isIdent(CSSName.BACKGROUND_IMAGE, IdentValue.NONE));
    }

    @Nullable
    public List<FSDerivedValue> getTextDecorations() {
        FSDerivedValue value = valueByName(CSSName.TEXT_DECORATION);
        if (value == IdentValue.NONE) {
            return null;
        } else {
            List<PropertyValue> idents = ((ListValue) value).getValues();
            List<FSDerivedValue> result = new ArrayList<>(idents.size());
            for (PropertyValue ident : idents) {
                result.add(DerivedValueFactory.newDerivedValue(
                        this, CSSName.TEXT_DECORATION, ident));
            }
            return result;
        }
    }

    @Nullable
    public Cursor getCursor() {
        FSDerivedValue value = valueByName(CSSName.CURSOR);

        if (value == IdentValue.AUTO || value == IdentValue.DEFAULT) {
            return Cursor.getDefaultCursor();
        } else if (value == IdentValue.CROSSHAIR) {
            return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        } else if (value == IdentValue.POINTER) {
            return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        } else if (value == IdentValue.MOVE) {
            return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        } else if (value == IdentValue.E_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
        } else if (value == IdentValue.NE_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
        } else if (value == IdentValue.NW_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
        } else if (value == IdentValue.N_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
        } else if (value == IdentValue.SE_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
        } else if (value == IdentValue.SW_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
        } else if (value == IdentValue.S_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
        } else if (value == IdentValue.W_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
        } else if (value == IdentValue.TEXT) {
            return Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
        } else if (value == IdentValue.WAIT) {
            return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        } else if (value == IdentValue.HELP) {
            // We don't have a cursor for this by default, maybe we need
            // a custom one for this (but I don't like it).
            return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        } else if (value == IdentValue.PROGRESS) {
            // We don't have a cursor for this by default, maybe we need
            // a custom one for this (but I don't like it).
            return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        }

        return null;
    }

    public boolean isPaginateTable() {
        return isIdent(CSSName.FS_TABLE_PAGINATE, IdentValue.PAGINATE);
    }

    public boolean isTextJustify() {
        return isIdent(CSSName.TEXT_ALIGN, IdentValue.JUSTIFY) &&
                ! (isIdent(CSSName.WHITE_SPACE, IdentValue.PRE) ||
                        isIdent(CSSName.WHITE_SPACE, IdentValue.PRE_LINE));
    }

    public boolean isListMarkerInside() {
        return isIdent(CSSName.LIST_STYLE_POSITION, IdentValue.INSIDE);
    }

    public boolean isKeepWithInline() {
        return isIdent(CSSName.FS_KEEP_WITH_INLINE, IdentValue.KEEP);
    }

    public boolean isDynamicAutoWidth() {
        return isIdent(CSSName.FS_DYNAMIC_AUTO_WIDTH, IdentValue.DYNAMIC);
    }

    public boolean isDynamicAutoWidthApplicable() {
        return isDynamicAutoWidth() && isAutoWidth() && ! isCanBeShrunkToFit();
    }

    public boolean isCanBeShrunkToFit() {
        return isInlineBlock() || isFloated() || isAbsolute() || isFixed();
    }
}
