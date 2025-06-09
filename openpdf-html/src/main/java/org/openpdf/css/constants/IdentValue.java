/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Patrick Wright
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
package org.openpdf.css.constants;

import org.jspecify.annotations.Nullable;
import org.openpdf.css.parser.FSColor;
import org.openpdf.css.style.CssContext;
import org.openpdf.css.style.FSDerivedValue;
import org.openpdf.util.XRRuntimeException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * An IdentValue represents a string that you can assign to a CSS property,
 * where the string is one of several enumerated values. For example,
 * "whitespace" can take the values "nowrap", "pre" and "normal". There is a
 * static instance for all idents in the CSS 2 spec, which you can retrieve
 * using the {@link #getByIdentString(String)} method. The instance doesn't have
 * any behavior: it's just a marker so that you can retrieve an ident from a
 * DerivedValue or CalculatedStyle, then compare to the instance here. For
 * example: <pre>
 * CalculatedStyle style = ...getstyle from somewhere
 * IdentValue whitespace = style.getIdent(CSSName.WHITESPACE);
 * if ( whitespace == IdentValue.NORMAL ) {
 *      // perform normal spacing
 * } else if ( whitespace == IdentValue.NOWRAP ) {
 *      // space with no wrapping
 * } else if ( whitespace == IdentValue.PRE ) {
 *      // preserve spacing
 * }
 * </pre> All static instances are instantiated automatically, and are
 * Singletons, so you can compare using a simple Object comparison using {@code ==}
 * .
 *
 * @author Patrick Wright
 */
public class IdentValue implements FSDerivedValue {
    private static final Map<String, IdentValue> ALL_IDENT_VALUES = new ConcurrentHashMap<>();
    private static int maxAssigned;

    private final String ident;
    public final int FS_ID;

    public static final IdentValue ABSOLUTE = addValue("absolute");
    public static final IdentValue ALWAYS = addValue("always");
    public static final IdentValue ARMENIAN = addValue("armenian");
    public static final IdentValue AUTO = addValue("auto");
    public static final IdentValue AVOID = addValue("avoid");
    public static final IdentValue BASELINE = addValue("baseline");
    public static final IdentValue BLINK = addValue("blink");
    public static final IdentValue BLOCK = addValue("block");
    public static final IdentValue BOLD = addValue("bold");
    public static final IdentValue BOLDER = addValue("bolder");
    public static final IdentValue BORDER_BOX = addValue("border-box");
    public static final IdentValue BOTH = addValue("both");
    public static final IdentValue BOTTOM = addValue("bottom");
    public static final IdentValue BREAK_ALL = addValue("break-all");
    public static final IdentValue CAPITALIZE = addValue("capitalize");
    public static final IdentValue CENTER = addValue("center");
    public static final IdentValue CIRCLE = addValue("circle");
    public static final IdentValue CJK_IDEOGRAPHIC = addValue("cjk-ideographic");
    public static final IdentValue CLOSE_QUOTE = addValue("close-quote");
    public static final IdentValue COLLAPSE = addValue("collapse");
    public static final IdentValue COMPACT = addValue("compact");
    public static final IdentValue CONTAIN = addValue("contain");
    public static final IdentValue CONTENT_BOX = addValue("content-box");
    public static final IdentValue COVER = addValue("cover");
    public static final IdentValue CREATE = addValue("create");
    public static final IdentValue DASHED = addValue("dashed");
    public static final IdentValue DECIMAL = addValue("decimal");
    public static final IdentValue DECIMAL_LEADING_ZERO = addValue("decimal-leading-zero");
    public static final IdentValue DISC = addValue("disc");
    public static final IdentValue DOTTED = addValue("dotted");
    public static final IdentValue DOUBLE = addValue("double");
    public static final IdentValue DYNAMIC = addValue("dynamic");
    public static final IdentValue FIXED = addValue("fixed");
    public static final IdentValue FONT_WEIGHT_100 = addValue("100");
    public static final IdentValue FONT_WEIGHT_200 = addValue("200");
    public static final IdentValue FONT_WEIGHT_300 = addValue("300");
    public static final IdentValue FONT_WEIGHT_400 = addValue("400");
    public static final IdentValue FONT_WEIGHT_500 = addValue("500");
    public static final IdentValue FONT_WEIGHT_600 = addValue("600");
    public static final IdentValue FONT_WEIGHT_700 = addValue("700");
    public static final IdentValue FONT_WEIGHT_800 = addValue("800");
    public static final IdentValue FONT_WEIGHT_900 = addValue("900");
    public static final IdentValue FS_CONTENT_PLACEHOLDER = addValue("-fs-content-placeholder");
    public static final IdentValue FS_INITIAL_VALUE = addValue("-fs-initial-value");
    public static final IdentValue GEORGIAN = addValue("georgian");
    public static final IdentValue GROOVE = addValue("groove");
    public static final IdentValue HEBREW = addValue("hebrew");
    public static final IdentValue HIDDEN = addValue("hidden");
    public static final IdentValue HIDE = addValue("hide");
    public static final IdentValue HIRAGANA = addValue("hiragana");
    public static final IdentValue HIRAGANA_IROHA = addValue("hiragana-iroha");
    public static final IdentValue INHERIT = addValue("inherit");
    public static final IdentValue INLINE = addValue("inline");
    public static final IdentValue INLINE_BLOCK = addValue("inline-block");
    public static final IdentValue INLINE_TABLE = addValue("inline-table");
    public static final IdentValue INSET = addValue("inset");
    public static final IdentValue INSIDE = addValue("inside");
    public static final IdentValue ITALIC = addValue("italic");
    public static final IdentValue JUSTIFY = addValue("justify");
    public static final IdentValue KATAKANA = addValue("katakana");
    public static final IdentValue KATAKANA_IROHA = addValue("katakana-iroha");
    public static final IdentValue KEEP = addValue("keep");
    public static final IdentValue LANDSCAPE = addValue("landscape");
    public static final IdentValue LEFT = addValue("left");
    public static final IdentValue LIGHTER = addValue("lighter");
    public static final IdentValue LINE = addValue("line");
    public static final IdentValue LINEAR_GRADIENT = addValue("linear-gradient");
    public final static IdentValue LINE_THROUGH = addValue("line-through");
    public static final IdentValue LIST_ITEM = addValue("list-item");
    public static final IdentValue LOWER_ALPHA = addValue("lower-alpha");
    public static final IdentValue LOWER_GREEK = addValue("lower-greek");
    public static final IdentValue LOWER_LATIN = addValue("lower-latin");
    public static final IdentValue LOWER_ROMAN = addValue("lower-roman");
    public static final IdentValue LOWERCASE = addValue("lowercase");
    public static final IdentValue LTR = addValue("ltr");
    public static final IdentValue MARKER = addValue("marker");
    public static final IdentValue MIDDLE = addValue("middle");
    public static final IdentValue NO_CLOSE_QUOTE = addValue("no-close-quote");
    public static final IdentValue NO_OPEN_QUOTE = addValue("no-open-quote");
    public static final IdentValue NO_REPEAT = addValue("no-repeat");
    public static final IdentValue NONE = addValue("none");
    public static final IdentValue NORMAL = addValue("normal");
    public static final IdentValue NOWRAP = addValue("nowrap");
    public static final IdentValue BREAK_WORD = addValue("break-word");
    public static final IdentValue OBLIQUE = addValue("oblique");
    public static final IdentValue OPEN_QUOTE = addValue("open-quote");
    public static final IdentValue OUTSET = addValue("outset");
    public static final IdentValue OUTSIDE = addValue("outside");
    public static final IdentValue OVERLINE = addValue("overline");
    public static final IdentValue PAGINATE = addValue("paginate");
    public static final IdentValue POINTER = addValue("pointer");
    public static final IdentValue PORTRAIT = addValue("portrait");
    public static final IdentValue PRE = addValue("pre");
    public static final IdentValue PRE_LINE = addValue("pre-line");
    public static final IdentValue PRE_WRAP = addValue("pre-wrap");
    public static final IdentValue RELATIVE = addValue("relative");
    public static final IdentValue REPEAT = addValue("repeat");
    public static final IdentValue REPEAT_X = addValue("repeat-x");
    public static final IdentValue REPEAT_Y = addValue("repeat-y");
    public static final IdentValue RIDGE = addValue("ridge");
    public static final IdentValue RIGHT = addValue("right");
    public static final IdentValue RUN_IN = addValue("run-in");
    public static final IdentValue SCROLL = addValue("scroll");
    public static final IdentValue SEPARATE = addValue("separate");
    public static final IdentValue SHOW = addValue("show");
    public static final IdentValue SMALL_CAPS = addValue("small-caps");
    public static final IdentValue SOLID = addValue("solid");
    public static final IdentValue SQUARE = addValue("square");
    public static final IdentValue STATIC = addValue("static");
    public static final IdentValue SUB = addValue("sub");
    public static final IdentValue SUPER = addValue("super");
    public static final IdentValue TABLE = addValue("table");
    public static final IdentValue TABLE_CAPTION = addValue("table-caption");
    public static final IdentValue TABLE_CELL = addValue("table-cell");
    public static final IdentValue TABLE_COLUMN = addValue("table-column");
    public static final IdentValue TABLE_COLUMN_GROUP = addValue("table-column-group");
    public static final IdentValue TABLE_FOOTER_GROUP = addValue("table-footer-group");
    public static final IdentValue TABLE_HEADER_GROUP = addValue("table-header-group");
    public static final IdentValue TABLE_ROW = addValue("table-row");
    public static final IdentValue TABLE_ROW_GROUP = addValue("table-row-group");
    public static final IdentValue TEXT_BOTTOM = addValue("text-bottom");
    public static final IdentValue TEXT_TOP = addValue("text-top");
    public static final IdentValue THICK = addValue("thick");
    public static final IdentValue THIN = addValue("thin");
    public static final IdentValue TOP = addValue("top");
    public static final IdentValue TRANSPARENT = addValue("transparent");
    public static final IdentValue UNDERLINE = addValue("underline");
    public static final IdentValue UPPER_ALPHA = addValue("upper-alpha");
    public static final IdentValue UPPER_LATIN = addValue("upper-latin");
    public static final IdentValue UPPER_ROMAN = addValue("upper-roman");
    public static final IdentValue UPPERCASE = addValue("uppercase");
    public static final IdentValue VISIBLE = addValue("visible");
    public static final IdentValue CROSSHAIR = addValue("crosshair");
    public static final IdentValue DEFAULT = addValue("default");
    public static final IdentValue EMBED = addValue("embed");
    public static final IdentValue E_RESIZE = addValue("e-resize");
    public static final IdentValue HELP = addValue("help");
    public static final IdentValue LARGE = addValue("large");
    public static final IdentValue LARGER = addValue("larger");
    public static final IdentValue MEDIUM = addValue("medium");
    public static final IdentValue MOVE = addValue("move");
    public static final IdentValue N_RESIZE = addValue("n-resize");
    public static final IdentValue NE_RESIZE = addValue("ne-resize");
    public static final IdentValue NW_RESIZE = addValue("nw-resize");
    public static final IdentValue PROGRESS = addValue("progress");
    public static final IdentValue S_RESIZE = addValue("s-resize");
    public static final IdentValue SE_RESIZE = addValue("se-resize");
    public static final IdentValue SMALL = addValue("small");
    public static final IdentValue SMALLER = addValue("smaller");
    public static final IdentValue START = addValue("start");
    public static final IdentValue SW_RESIZE = addValue("sw-resize");
    public static final IdentValue TEXT = addValue("text");
    public static final IdentValue W_RESIZE = addValue("w-resize");
    public static final IdentValue WAIT = addValue("wait");
    public static final IdentValue X_LARGE = addValue("x-large");
    public static final IdentValue X_SMALL = addValue("x-small");
    public static final IdentValue XX_LARGE = addValue("xx-large");
    public static final IdentValue XX_SMALL = addValue("xx-small");
    public static final IdentValue MANUAL = addValue("manual");

    private IdentValue(String ident) {
        this.ident = ident;
        this.FS_ID = maxAssigned++;
    }

    /**
     * Returns a string representation of the object, in this case, the ident as
     * a string (as it appears in the CSS spec).
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return ident;
    }

    /**
     * Returns the Singleton IdentValue that corresponds to the given string,
     * e.g. for "normal" will return IdentValue.NORMAL. Use this when you have
     * the string but need to look up the Singleton. If the string doesn't match
     * an ident in the CSS spec, a runtime exception is thrown.
     *
     * @param ident The identifier to retrieve the Singleton IdentValue for.
     */
    public static IdentValue getByIdentString(String ident) {
        IdentValue val = ALL_IDENT_VALUES.get(ident);
        if (val == null) {
            throw new XRRuntimeException("Ident named " + ident + " has no IdentValue instance assigned to it.");
        }
        return val;
    }

    public static boolean looksLikeIdent(String ident) {
        return ALL_IDENT_VALUES.get(ident) != null;
    }

    @Nullable
    public static IdentValue valueOf(String ident) {
        return ALL_IDENT_VALUES.get(ident);
    }

    public static int getIdentCount() {
        return ALL_IDENT_VALUES.size();
    }

    /**
     * Adds a feature to the Value attribute of the IdentValue class
     *
     * @param ident The feature to be added to the Value attribute
     */
    private static IdentValue addValue(String ident) {
        IdentValue val = new IdentValue(ident);
        ALL_IDENT_VALUES.put(ident, val);
        return val;
    }

    /*
     * METHODS USED TO SUPPORT IdentValue as an FSDerivedValue, used in CalculatedStyle.
     * Most of these throw exceptions--makes use of the interface easier in CS (avoids casting)
     */

    @Override
    public boolean isDeclaredInherit() {
        return this == INHERIT;
    }

    public FSDerivedValue computedValue() {
        return this;
    }

    @Override
    public float asFloat() {
        throw new XRRuntimeException("Ident value is never a float; wrong class used for derived value.");
    }

    @Override
    public FSColor asColor() {
        throw new XRRuntimeException("Ident value is never a color; wrong class used for derived value.");
    }

    @Override
    public float getFloatProportionalTo(CSSName cssName,
                                        float baseValue,
                                        CssContext ctx) {
        throw new XRRuntimeException("Ident value (" + this + ") is never a length; wrong class used for derived value.");
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public String[] asStringArray() {
        throw new XRRuntimeException("Ident value is never a string array; wrong class used for derived value.");
    }

    @Override
    public IdentValue asIdentValue() {
        return this;
    }

    @Override
    public boolean hasAbsoluteUnit() {
        // log and return false
        throw new XRRuntimeException("Ident value is never an absolute unit; wrong class used for derived value; this " +
                "ident value is a " + this.asString());
    }

    @Override
    public boolean isIdent() {
        return true;
    }

    @Override
    public boolean isDependentOnFontSize() {
        return false;
    }
}
