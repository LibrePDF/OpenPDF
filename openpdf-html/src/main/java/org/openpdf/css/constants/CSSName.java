/*
 * {{{ header & license
 * CSSName.java
 * Copyright (c) 2004, 2005 Patrick Wright
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
 * }}}
 */
package org.openpdf.css.constants;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.css.parser.CSSParser;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.parser.property.BackgroundPropertyBuilder;
import org.openpdf.css.parser.property.BorderPropertyBuilders;
import org.openpdf.css.parser.property.BorderSpacingPropertyBuilder;
import org.openpdf.css.parser.property.ContentPropertyBuilder;
import org.openpdf.css.parser.property.CounterPropertyBuilder;
import org.openpdf.css.parser.property.FontPropertyBuilder;
import org.openpdf.css.parser.property.ListStylePropertyBuilder;
import org.openpdf.css.parser.property.OneToFourPropertyBuilders;
import org.openpdf.css.parser.property.PrimitivePropertyBuilders;
import org.openpdf.css.parser.property.PropertyBuilder;
import org.openpdf.css.parser.property.QuotesPropertyBuilder;
import org.openpdf.css.parser.property.SizePropertyBuilder;
import org.openpdf.css.sheet.StylesheetInfo;
import org.openpdf.css.style.FSDerivedValue;
import org.openpdf.css.style.derived.DerivedValueFactory;
import org.openpdf.util.XRLog;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A CSSName is a Singleton representing a single CSS property name, like
 * border-width. The class declares a Singleton static instance for each CSS
 * Level 2 property. A CSSName instance has the property name available from the
 * {@link #toString()} method, as well as a unique (among all CSSName instances)
 * integer id ranging from 0...n instances, incremented by 1, available using
 * the final public int FS_ID (e.g. CSSName.COLOR.FS_ID).
 *
 * @author Patrick Wright
 */
public final class CSSName implements Comparable<CSSName> {
    /**
     * marker var, used for initialization
     */
    private static final Integer PRIMITIVE = 0;

    /**
     * marker var, used for initialization
     */
    private static final Integer SHORTHAND = 1;

    /**
     * marker var, used for initialization
     */
    private static final Integer INHERITS = 2;

    /**
     * marker var, used for initialization
     */
    private static final Integer NOT_INHERITED = 3;

    /**
     * Used to assign unique int id values to new CSSNames created in this class
     */
    private static final AtomicInteger maxAssigned = new AtomicInteger(0);

    /**
     * The CSS 2 property name, e.g. "border"
     */
    private final String propName;

    /**
     * A (String) initial value from the CSS 2.1 specification
     */
    private final String initialValue;

    /**
     * True if the property inherits by default, false if not inherited
     */
    private final boolean propertyInherits;

    private FSDerivedValue initialDerivedValue;

    private final boolean implemented;

    @Nullable
    private final PropertyBuilder builder;

    /**
     * Unique integer id for a CSSName.
     */
    public final int FS_ID;

    /**
     * Map of all CSS properties
     */
    private static final CSSName[] ALL_PROPERTIES;

    /**
     * Map of all CSS properties
     */
    private static final Map<String, CSSName> ALL_PROPERTY_NAMES = new TreeMap<>();

    /**
     * Map of all non-shorthand CSS properties
     */
    private static final Map<String, CSSName> ALL_PRIMITIVE_PROPERTY_NAMES = new TreeMap<>();

    /**
     * Unique CSSName instance for CSS2 property.
     * TODO: UA dependent
     */
    public static final CSSName COLOR =
            addProperty(
                    "color",
                    PRIMITIVE,
                    "black",
                    INHERITS,
                    new PrimitivePropertyBuilders.Color()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BACKGROUND_COLOR =
            addProperty(
                    "background-color",
                    PRIMITIVE,
                    "transparent",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BackgroundColor()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BACKGROUND_IMAGE =
            addProperty(
                    "background-image",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BackgroundImage()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BACKGROUND_REPEAT =
            addProperty(
                    "background-repeat",
                    PRIMITIVE,
                    "repeat",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BackgroundRepeat()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BACKGROUND_ATTACHMENT =
            addProperty(
                    "background-attachment",
                    PRIMITIVE,
                    "scroll",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BackgroundAttachment()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BACKGROUND_POSITION =
            addProperty(
                    "background-position",
                    PRIMITIVE,
                    "0% 0%",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BackgroundPosition()
            );

    public static final CSSName BACKGROUND_SIZE =
        addProperty(
                "background-size",
                PRIMITIVE,
                "auto auto",
                NOT_INHERITED,
                new PrimitivePropertyBuilders.BackgroundSize()
        );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_COLLAPSE =
            addProperty(
                    "border-collapse",
                    PRIMITIVE,
                    "separate",
                    INHERITS,
                    new PrimitivePropertyBuilders.BorderCollapse()
            );

    /**
     * Unique CSSName instance for fictitious property.
     */
    public static final CSSName FS_BORDER_SPACING_HORIZONTAL =
            addProperty(
                    "-fs-border-spacing-horizontal",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSBorderSpacingHorizontal()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_BORDER_SPACING_VERTICAL =
            addProperty(
                    "-fs-border-spacing-vertical",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSBorderSpacingVertical()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_DYNAMIC_AUTO_WIDTH =
            addProperty(
                    "-fs-dynamic-auto-width",
                    PRIMITIVE,
                    "static",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSDynamicAutoWidth()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_FONT_METRIC_SRC =
            addProperty(
                    "-fs-font-metric-src",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSFontMetricSrc()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_KEEP_WITH_INLINE =
            addProperty(
                    "-fs-keep-with-inline",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSKeepWithInline()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_PAGE_WIDTH =
            addProperty(
                    "-fs-page-width",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSPageWidth()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_PAGE_HEIGHT =
            addProperty(
                    "-fs-page-height",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSPageHeight()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_PAGE_SEQUENCE =
            addProperty(
                    "-fs-page-sequence",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSPageSequence()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_PDF_FONT_EMBED =
            addProperty(
                    "-fs-pdf-font-embed",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSPDFFontEmbed()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_PDF_FONT_ENCODING =
            addProperty(
                    "-fs-pdf-font-encoding",
                    PRIMITIVE,
                    "Cp1252",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSPDFFontEncoding()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_PAGE_ORIENTATION =
            addProperty(
                    "-fs-page-orientation",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSPageOrientation()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_TABLE_PAGINATE =
            addProperty(
                    "-fs-table-paginate",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSTablePaginate()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_TEXT_DECORATION_EXTENT =
            addProperty(
                    "-fs-text-decoration-extent",
                    PRIMITIVE,
                    "line",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSTextDecorationExtent()
            );

    /**
     * Used for forcing images to scale to a certain width
     */
    public static final CSSName FS_FIT_IMAGES_TO_WIDTH =
        addProperty(
                "-fs-fit-images-to-width",
                PRIMITIVE,
                "auto",
                NOT_INHERITED,
                new PrimitivePropertyBuilders.FSFitImagesToWidth()
        );

    /**
     * Used to control creation of named destinations for boxes having the id attribute set.
     */
    public static final CSSName FS_NAMED_DESTINATION =
            addProperty(
                    "-fs-named-destination",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSNamedDestination()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BOTTOM =
            addProperty(
                    "bottom",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Bottom()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName CAPTION_SIDE =
            addProperty(
                    "caption-side",
                    PRIMITIVE,
                    "top",
                    INHERITS,
                    new PrimitivePropertyBuilders.CaptionSide()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName CLEAR =
            addProperty(
                    "clear",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Clear()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName CLIP =
            addProperty(
                    "clip",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName CONTENT =
            addProperty(
                    "content",
                    PRIMITIVE,
                    "normal",
                    NOT_INHERITED,
                    new ContentPropertyBuilder()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName COUNTER_INCREMENT =
            addProperty(
                    "counter-increment",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    true,
                    new CounterPropertyBuilder.CounterIncrement()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName COUNTER_RESET =
            addProperty(
                    "counter-reset",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    true,
                    new CounterPropertyBuilder.CounterReset()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName CURSOR =
            addProperty(
                    "cursor",
                    PRIMITIVE,
                    "auto",
                    INHERITS,
                    true,
                    new PrimitivePropertyBuilders.Cursor()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName DIRECTION =
            addProperty(
                    "direction",
                    PRIMITIVE,
                    "ltr",
                    INHERITS,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName DISPLAY =
            addProperty(
                    "display",
                    PRIMITIVE,
                    "inline",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Display()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName EMPTY_CELLS =
            addProperty(
                    "empty-cells",
                    PRIMITIVE,
                    "show",
                    INHERITS,
                    new PrimitivePropertyBuilders.EmptyCells()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FLOAT =
            addProperty(
                    "float",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Float()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FONT_STYLE =
            addProperty(
                    "font-style",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.FontStyle()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FONT_VARIANT =
            addProperty(
                    "font-variant",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.FontVariant()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FONT_WEIGHT =
            addProperty(
                    "font-weight",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.FontWeight()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FONT_SIZE =
            addProperty(
                    "font-size",
                    PRIMITIVE,
                    "medium",
                    INHERITS,
                    new PrimitivePropertyBuilders.FontSize()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName LINE_HEIGHT =
            addProperty(
                    "line-height",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.LineHeight()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * TODO: UA dependent
     */
    public static final CSSName FONT_FAMILY =
            addProperty(
                    "font-family",
                    PRIMITIVE,
                    "serif",
                    INHERITS,
                    new PrimitivePropertyBuilders.FontFamily()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_COLSPAN =
            addProperty(
                    "-fs-table-cell-colspan",
                    PRIMITIVE,
                    "1",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSTableCellColspan()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FS_ROWSPAN =
            addProperty(
                    "-fs-table-cell-rowspan",
                    PRIMITIVE,
                    "1",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSTableCellRowspan()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName HEIGHT =
            addProperty(
                    "height",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Height()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName LEFT =
            addProperty(
                    "left",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Left()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName LETTER_SPACING =
            addProperty(
                    "letter-spacing",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    true,
                    new PrimitivePropertyBuilders.LetterSpacing()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName LIST_STYLE_TYPE =
            addProperty(
                    "list-style-type",
                    PRIMITIVE,
                    "disc",
                    INHERITS,
                    new PrimitivePropertyBuilders.ListStyleType()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName LIST_STYLE_POSITION =
            addProperty(
                    "list-style-position",
                    PRIMITIVE,
                    "outside",
                    INHERITS,
                    new PrimitivePropertyBuilders.ListStylePosition()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName LIST_STYLE_IMAGE =
            addProperty(
                    "list-style-image",
                    PRIMITIVE,
                    "none",
                    INHERITS,
                    new PrimitivePropertyBuilders.ListStyleImage()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName MAX_HEIGHT =
            addProperty(
                    "max-height",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MaxHeight()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName MAX_WIDTH =
            addProperty(
                    "max-width",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MaxWidth()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName MIN_HEIGHT =
            addProperty(
                    "min-height",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MinHeight()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * TODO: UA dependent
     */
    public static final CSSName MIN_WIDTH =
            addProperty(
                    "min-width",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MinWidth()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName ORPHANS =
            addProperty(
                    "orphans",
                    PRIMITIVE,
                    "2",
                    INHERITS,
                    true,
                    new PrimitivePropertyBuilders.Orphans()
            );
    
    public final static CSSName OPACITY =
    		addProperty(
    				"opacity",
    				PRIMITIVE,
    				"1",
                    NOT_INHERITED, // PR22 - INHERITS
    				true,
    				new PrimitivePropertyBuilders.Opacity()
    		);

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName OUTLINE_COLOR =
            addProperty(
                    "outline-color",
                    PRIMITIVE,
                    /* "invert", */ "black",  // XXX Wrong (but doesn't matter for now)
                    NOT_INHERITED,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName OUTLINE_STYLE =
            addProperty(
                    "outline-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName OUTLINE_WIDTH =
            addProperty(
                    "outline-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName OVERFLOW =
            addProperty(
                    "overflow",
                    PRIMITIVE,
                    "visible",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Overflow()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName PAGE =
            addProperty(
                    "page",
                    PRIMITIVE,
                    "auto",
                    INHERITS,
                    new PrimitivePropertyBuilders.Page()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName PAGE_BREAK_AFTER =
            addProperty(
                    "page-break-after",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.PageBreakAfter()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName PAGE_BREAK_BEFORE =
            addProperty(
                    "page-break-before",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.PageBreakBefore()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName PAGE_BREAK_INSIDE =
            addProperty(
                    "page-break-inside",
                    PRIMITIVE,
                    "auto",
                    INHERITS,
                    new PrimitivePropertyBuilders.PageBreakInside()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName POSITION =
            addProperty(
                    "position",
                    PRIMITIVE,
                    "static",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Position()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * TODO: UA dependent
     */
    public static final CSSName QUOTES =
            addProperty(
                    "quotes",
                    PRIMITIVE,
                    "none",
                    INHERITS,
                    new QuotesPropertyBuilder()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName RIGHT =
            addProperty(
                    "right",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Right()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName SRC =
            addProperty(
                    "src",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Src()
            );

    /**
     * Used for controlling tab size in pre tags. See <a href="http://dev.w3.org/csswg/css3-text/#tab-size">...</a>
     */
    public static final CSSName TAB_SIZE =
            addProperty(
                    "tab-size",
                    PRIMITIVE,
                    "8",
                    INHERITS,
                    new PrimitivePropertyBuilders.TabSize()
                    );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName TABLE_LAYOUT =
            addProperty(
                    "table-layout",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.TableLayout()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * TODO: UA dependent
     */
    public static final CSSName TEXT_ALIGN =
            addProperty(
                    "text-align",
                    PRIMITIVE,
                    "left",
                    INHERITS,
                    new PrimitivePropertyBuilders.TextAlign()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName TEXT_DECORATION =
            addProperty(
                    "text-decoration",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.TextDecoration()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName TEXT_INDENT =
            addProperty(
                    "text-indent",
                    PRIMITIVE,
                    "0",
                    INHERITS,
                    new PrimitivePropertyBuilders.TextIndent()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName TEXT_TRANSFORM =
            addProperty(
                    "text-transform",
                    PRIMITIVE,
                    "none",
                    INHERITS,
                    new PrimitivePropertyBuilders.TextTransform()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName TOP =
            addProperty(
                    "top",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Top()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName UNICODE_BIDI =
            addProperty(
                    "unicode-bidi",
                    PRIMITIVE,
                    "normal",
                    NOT_INHERITED,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName VERTICAL_ALIGN =
            addProperty(
                    "vertical-align",
                    PRIMITIVE,
                    "baseline",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.VerticalAlign()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName VISIBILITY =
            addProperty(
                    "visibility",
                    PRIMITIVE,
                    "visible",
                    INHERITS,
                    new PrimitivePropertyBuilders.Visibility()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName WHITE_SPACE =
            addProperty(
                    "white-space",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.WhiteSpace()
            );

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public static final CSSName WORD_BREAK =
            addProperty(
                    "word-break",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.WordBreak()
            );

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public static final CSSName WORD_WRAP =
            addProperty(
                    "word-wrap",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.WordWrap()
            );

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public static final CSSName HYPHENS =
            addProperty(
                    "hyphens",
                    PRIMITIVE,
                    "none",
                    INHERITS,
                    new PrimitivePropertyBuilders.Hyphens()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName WIDOWS =
            addProperty(
                    "widows",
                    PRIMITIVE,
                    "2",
                    INHERITS,
                    true,
                    new PrimitivePropertyBuilders.Widows()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName WIDTH =
            addProperty(
                    "width",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Width()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName WORD_SPACING =
            addProperty(
                    "word-spacing",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    true,
                    new PrimitivePropertyBuilders.WordSpacing()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName Z_INDEX =
            addProperty(
                    "z-index",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.ZIndex()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_TOP_COLOR =
            addProperty(
                    "border-top-color",
                    PRIMITIVE,
                    "=color",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderTopColor()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_RIGHT_COLOR =
            addProperty(
                    "border-right-color",
                    PRIMITIVE,
                    "=color",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderLeftColor()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_BOTTOM_COLOR =
            addProperty(
                    "border-bottom-color",
                    PRIMITIVE,
                    "=color",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderBottomColor()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_LEFT_COLOR =
            addProperty(
                    "border-left-color",
                    PRIMITIVE,
                    "=color",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderLeftColor()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_TOP_STYLE =
            addProperty(
                    "border-top-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderTopStyle()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_RIGHT_STYLE =
            addProperty(
                    "border-right-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderRightStyle()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_BOTTOM_STYLE =
            addProperty(
                    "border-bottom-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderBottomStyle()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_LEFT_STYLE =
            addProperty(
                    "border-left-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderLeftStyle()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_TOP_WIDTH =
            addProperty(
                    "border-top-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderTopWidth()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_RIGHT_WIDTH =
            addProperty(
                    "border-right-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderRightWidth()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_BOTTOM_WIDTH =
            addProperty(
                    "border-bottom-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderBottomWidth()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_LEFT_WIDTH =
            addProperty(
                    "border-left-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderLeftWidth()
            );

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public static final CSSName BORDER_TOP_LEFT_RADIUS =
            addProperty(
                    "border-top-left-radius",
                    PRIMITIVE,
                    "0 0",
                    NOT_INHERITED,
                    true,
                    new PrimitivePropertyBuilders.BorderTopLeftRadius()
            );

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public static final CSSName BORDER_TOP_RIGHT_RADIUS =
            addProperty(
                    "border-top-right-radius",
                    PRIMITIVE,
                    "0 0",
                    NOT_INHERITED,
                    true,
                    new PrimitivePropertyBuilders.BorderTopRightRadius()
            );

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public static final CSSName BORDER_BOTTOM_RIGHT_RADIUS =
            addProperty(
                    "border-bottom-right-radius",
                    PRIMITIVE,
                    "0 0",
                    NOT_INHERITED,
                    true,
                    new PrimitivePropertyBuilders.BorderBottomRightRadius()
            );

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public static final CSSName BORDER_BOTTOM_LEFT_RADIUS =
            addProperty(
                    "border-bottom-left-radius",
                    PRIMITIVE,
                    "0 0",
                    NOT_INHERITED,
                    true,
                    new PrimitivePropertyBuilders.BorderBottomLeftRadius()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName MARGIN_TOP =
            addProperty(
                    "margin-top",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MarginTop()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName MARGIN_RIGHT =
            addProperty(
                    "margin-right",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MarginRight()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName MARGIN_BOTTOM =
            addProperty(
                    "margin-bottom",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MarginBottom()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName MARGIN_LEFT =
            addProperty(
                    "margin-left",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MarginLeft()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName PADDING_TOP =
            addProperty(
                    "padding-top",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.PaddingTop()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName PADDING_RIGHT =
            addProperty(
                    "padding-right",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.PaddingRight()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName PADDING_BOTTOM =
            addProperty(
                    "padding-bottom",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.PaddingBottom()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName PADDING_LEFT =
            addProperty(
                    "padding-left",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.PaddingLeft()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BACKGROUND_SHORTHAND =
            addProperty(
                    "background",
                    SHORTHAND,
                    "transparent none repeat scroll 0% 0%",
                    NOT_INHERITED,
                    new BackgroundPropertyBuilder()
            );


    /**
     * Unique CSSName instance for CSS3 property.
     */
    public static final CSSName BORDER_RADIUS_SHORTHAND =
            addProperty(
                    "border-radius",
                    SHORTHAND,
                    "0px",
                    NOT_INHERITED,
                    true,
                    new OneToFourPropertyBuilders.BorderRadius()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_WIDTH_SHORTHAND =
            addProperty(
                    "border-width",
                    SHORTHAND,
                    "medium",
                    NOT_INHERITED,
                    new OneToFourPropertyBuilders.BorderWidth()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_STYLE_SHORTHAND =
            addProperty(
                    "border-style",
                    SHORTHAND,
                    "none",
                    NOT_INHERITED,
                    new OneToFourPropertyBuilders.BorderStyle()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_SHORTHAND =
            addProperty(
                    "border",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED,
                    new BorderPropertyBuilders.Border()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_TOP_SHORTHAND =
            addProperty(
                    "border-top",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED,
                    new BorderPropertyBuilders.BorderTop()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_RIGHT_SHORTHAND =
            addProperty(
                    "border-right",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED,
                    new BorderPropertyBuilders.BorderRight()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_BOTTOM_SHORTHAND =
            addProperty(
                    "border-bottom",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED,
                    new BorderPropertyBuilders.BorderBottom()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_LEFT_SHORTHAND =
            addProperty(
                    "border-left",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED,
                    new BorderPropertyBuilders.BorderLeft()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_COLOR_SHORTHAND =
            addProperty(
                    "border-color",
                    SHORTHAND,
                    "black",
                    NOT_INHERITED,
                    new OneToFourPropertyBuilders.BorderColor()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BORDER_SPACING =
            addProperty(
                    "border-spacing",
                    SHORTHAND,
                    "0",
                    INHERITS,
                    new BorderSpacingPropertyBuilder()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName FONT_SHORTHAND =
            addProperty(
                    "font",
                    SHORTHAND,
                    "",
                    INHERITS,
                    new FontPropertyBuilder()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName LIST_STYLE_SHORTHAND =
            addProperty(
                    "list-style",
                    SHORTHAND,
                    "disc outside none",
                    INHERITS,
                    new ListStylePropertyBuilder()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName MARGIN_SHORTHAND =
            addProperty(
                    "margin",
                    SHORTHAND,
                    "0",
                    NOT_INHERITED,
                    new OneToFourPropertyBuilders.Margin()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName OUTLINE_SHORTHAND =
            addProperty(
                    "outline",
                    SHORTHAND,
                    "invert none medium",
                    NOT_INHERITED,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName PADDING_SHORTHAND =
            addProperty(
                    "padding",
                    SHORTHAND,
                    "0",
                    NOT_INHERITED,
                    new OneToFourPropertyBuilders.Padding()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName SIZE_SHORTHAND =
            addProperty(
                    "size",
                    SHORTHAND,
                    "auto",
                    NOT_INHERITED,
                    new SizePropertyBuilder()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public static final CSSName BOX_SIZING =
            addProperty(
                    "box-sizing",
                    PRIMITIVE,
                    "content-box",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BoxSizing()
            );

    public static final CSSSideProperties MARGIN_SIDE_PROPERTIES =
            new CSSSideProperties(
                    CSSName.MARGIN_TOP,
                    CSSName.MARGIN_RIGHT,
                    CSSName.MARGIN_BOTTOM,
                    CSSName.MARGIN_LEFT);

    public static final CSSSideProperties PADDING_SIDE_PROPERTIES =
            new CSSSideProperties(
                    CSSName.PADDING_TOP,
                    CSSName.PADDING_RIGHT,
                    CSSName.PADDING_BOTTOM,
                    CSSName.PADDING_LEFT);

    public static final CSSSideProperties BORDER_SIDE_PROPERTIES =
            new CSSSideProperties(
                    CSSName.BORDER_TOP_WIDTH,
                    CSSName.BORDER_RIGHT_WIDTH,
                    CSSName.BORDER_BOTTOM_WIDTH,
                    CSSName.BORDER_LEFT_WIDTH);

    public static final CSSSideProperties BORDER_STYLE_PROPERTIES =
            new CSSSideProperties(
                    CSSName.BORDER_TOP_STYLE,
                    CSSName.BORDER_RIGHT_STYLE,
                    CSSName.BORDER_BOTTOM_STYLE,
                    CSSName.BORDER_LEFT_STYLE);

    public static final CSSSideProperties BORDER_COLOR_PROPERTIES =
            new CSSSideProperties(
                    CSSName.BORDER_TOP_COLOR,
                    CSSName.BORDER_RIGHT_COLOR,
                    CSSName.BORDER_BOTTOM_COLOR,
                    CSSName.BORDER_LEFT_COLOR);


    private CSSName(
            String propName, String initialValue, boolean inherits,
            boolean implemented, @Nullable PropertyBuilder builder) {
        this.propName = propName;
        this.FS_ID = maxAssigned.getAndIncrement();
        this.initialValue = initialValue;
        this.propertyInherits = inherits;
        this.implemented = implemented;
        this.builder = builder;
    }

    /**
     * Returns a string representation of the object, in this case, always the
     * full CSS property name in lowercase.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return this.propName;
    }

    /**
     * Returns a count of all CSS properties known to this class, shorthand and primitive.
     */
    public static int countCSSNames() {
        return maxAssigned.get();
    }

    /**
     * Returns a count of all CSS primitive (non-shorthand) properties known to this class.
     */
    public static int countCSSPrimitiveNames() {
        return ALL_PRIMITIVE_PROPERTY_NAMES.size();
    }

    /**
     * Iterator of ALL CSS 2 visual property names.
     */
    public static Iterator<String> allCSS2PropertyNames() {
        return ALL_PROPERTY_NAMES.keySet().iterator();
    }

    /**
     * Iterator of ALL primitive (non-shorthand) CSS 2 visual property names.
     */
    public static Iterator<String> allCSS2PrimitivePropertyNames() {
        return ALL_PRIMITIVE_PROPERTY_NAMES.keySet().iterator();
    }

    /**
     * Returns true if the named property inherits by default, according to the
     * CSS2 spec.
     */
    // CLEAN: method is now unnecessary
    public static boolean propertyInherits(CSSName cssName) {
        return cssName.propertyInherits;
    }

    /**
     * Returns the initial value of the named property, according to the CSS2
     * spec, as a String. Casting must be taken care of by the caller, as there
     * is too much variation in value-types.
     */
    // CLEAN: method is now unnecessary
    public static String initialValue(CSSName cssName) {
        return cssName.initialValue;
    }

    public FSDerivedValue initialDerivedValue() {
        return initialDerivedValue;
    }

    public static boolean isImplemented(CSSName cssName) {
        return cssName.implemented;
    }

    @Nullable
    public static PropertyBuilder getPropertyBuilder(CSSName cssName) {
        return cssName.builder;
    }

    /**
     * Gets the byPropertyName attribute of the CSSName class
     */
    @Nullable
    @CheckReturnValue
    public static CSSName getByPropertyName(String propName) {
        return ALL_PROPERTY_NAMES.get(propName);
    }

    public static CSSName getByID(int id) {
        return ALL_PROPERTIES[id];
    }

    private static synchronized CSSName addProperty(
            String propName,
            Object type,
            String initialValue,
            Object inherit,
            PropertyBuilder builder
    ) {
        return addProperty(propName, type, initialValue, inherit, true, builder);
    }

    /**
     * Adds a feature to the Property attribute of the CSSName class
     *
     * @param propName     The feature to be added to the Property attribute
     */
    private static synchronized CSSName addProperty(
            String propName,
            Object type,
            String initialValue,
            Object inherit,
            boolean implemented,
            @Nullable PropertyBuilder builder
    ) {
        CSSName cssName = new CSSName(
                propName, initialValue, (inherit == INHERITS), implemented, builder);

        ALL_PROPERTY_NAMES.put(propName, cssName);

        if (type == PRIMITIVE) {
            ALL_PRIMITIVE_PROPERTY_NAMES.put(propName, cssName);
        }

        return cssName;
    }

    static {
        ALL_PROPERTIES = new CSSName[ALL_PROPERTY_NAMES.size()];
        for (CSSName name : ALL_PROPERTY_NAMES.values()) {
            ALL_PROPERTIES[name.FS_ID] = name;
        }
    }

    static {
        CSSParser parser = new CSSParser((uri, message) -> XRLog.cssParse("(" + uri + ") " + message));
        for (CSSName cssName : ALL_PRIMITIVE_PROPERTY_NAMES.values()) {
            if (cssName.initialValue.charAt(0) != '=' && cssName.implemented) {
                PropertyValue value = parser.parsePropertyValue(
                        cssName, StylesheetInfo.Origin.USER_AGENT, cssName.initialValue);

                if (value == null) {
                    XRLog.exception("Unable to derive initial value for " + cssName);
                } else {
                    cssName.initialDerivedValue = DerivedValueFactory.newDerivedValue(
                            null,
                            cssName,
                            value);
                }
            }
        }
    }

    //Assumed to be consistent with equals because CSSName is in essence an enum
    @Override
    public int compareTo(CSSName object) {
        if (object == null) throw new NullPointerException("Cannot compare " + this + " to null");
        return FS_ID - object.FS_ID;//will throw ClassCastException according to Comparable if not a CSSName
    }

    // FIXME equals, hashcode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CSSName cssName)) return false;

        return FS_ID == cssName.FS_ID;
    }

    @Override
    public int hashCode() {
        return FS_ID;
    }

    public record CSSSideProperties(CSSName top, CSSName right, CSSName bottom, CSSName left) {
    }
}
